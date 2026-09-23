package com.epiac9.cobblemonnomanslands.client.mixin;

import com.epiac9.cobblemonnomanslands.client.ExplorationSelectionClientState;
import com.epiac9.cobblemonnomanslands.client.ExplorationTabState;
import com.cobblemonexpeditions.gui.Rect;
import com.cobblemonexpeditions.gui.BoardLayout;
import com.cobblemonexpeditions.gui.BoardFooterLayout;
import com.cobblemonexpeditions.gui.chrome.ButtonRenderer;
import com.cobblemonexpeditions.gui.chrome.IconSheet;
import com.cobblemonexpeditions.gui.chrome.Panel;
import com.cobblemonexpeditions.gui.chrome.Panel.CardState;
import com.cobblemonexpeditions.gui.chrome.SlotRenderer;
import com.cobblemonexpeditions.gui.chrome.TooltipRenderer;
import com.cobblemonexpeditions.gui.theme.BoardTheme;
import com.cobblemonexpeditions.network.s2c.SyncStatePayload;
import com.epiac9.cobblemonnomanslands.network.ExplorationSelectionPayload;
import com.epiac9.cobblemonnomanslands.network.ExplorationSelectionResultPayload;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;
import net.neoforged.neoforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Mixin(targets = "com.cobblemonexpeditions.gui.ExpeditionBoardScreen")
public abstract class ExpeditionBoardScreenMixin extends Screen {
    private static final int TAB_HEIGHT = 18;
    private static final int EXPLORATION_MAX_MEMBERS = 3;
    private static final int EXPLORATION_POWER_COLUMN_OFFSET = 120;

    @Shadow
    private BlockPos blockPos;
    @Shadow
    private int selectedExpeditionIndex;
    @Shadow
    private SyncStatePayload syncedState;
    @Shadow
    protected abstract List<SyncStatePayload.AvailableExpeditionInfo> getSortedExpeditions(SyncStatePayload state);
    @Shadow
    private Rect expContentRect;
    @Shadow
    private Rect pokeGridRect;
    @Shadow
    private Rect filterBarRect;
    @Shadow
    private Rect selectionBarRect;
    @Shadow
    private Rect activeExpRect;
    @Shadow
    private Rect rewardPanelRect;
    @Shadow
    private int cancelBtnW;
    @Shadow
    private BoardLayout layout;
    @Shadow
    private BoardTheme theme;
    @Shadow
    public abstract void showError(String message);

    private boolean explorationMode;
    private boolean explorationSelected;
    private boolean explorationConfirmed;
    private ResourceLocation activeExplorationId;
    private ExplorationSelectionResultPayload synchronizedResult;
    private int routedCurrentPower;
    private int routedRequiredPower;
    private int routedCurrentMembers;
    private int routedMaxMembers = EXPLORATION_MAX_MEMBERS;
    private final java.util.Map<UUID, ExplorationSelectionResultPayload.PlayerStats> routedPlayerStats = new HashMap<>();
    private String teammateSearch = "";
    private EditBox teammateSearchBox;
    private String explorationStatus = "Select an exploration and teammates";
    private final Set<UUID> selectedTeammates = new LinkedHashSet<>();
    protected ExpeditionBoardScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void cobblemonNoMansLands$initSearchBox(CallbackInfo callbackInfo) {
        explorationMode = ExplorationTabState.isExplorationMode();
        Rect filterBar = layout.getFilterBar();
        Rect searchRect = filterBar;
        teammateSearchBox = new EditBox(font, searchRect.getX() + 11, searchRect.getY() + 2,
            searchRect.getW() - 14, searchRect.getH() - 4,
            Component.translatable("gui.cobblemon_expeditions.search"));
        teammateSearchBox.setBordered(false);
        teammateSearchBox.setMaxLength(24);
        teammateSearchBox.setTextColor(0x00000000);
        teammateSearchBox.setHint(Component.empty());
        teammateSearchBox.setValue(teammateSearch);
        teammateSearchBox.setResponder(value -> teammateSearch = value);
        addWidget(teammateSearchBox);
    }

    @Inject(method = "renderCanvas", at = @At("TAIL"))
    private void cobblemonNoMansLands$renderTabs(GuiGraphics graphics, int mouseX, int mouseY,
                                                 CallbackInfo callbackInfo) {
        Rect panel = layout.getExpPanel();
        int tabWidth = panel.getW() / 2;
        int tabY = panel.getY();
        drawTab(graphics, panel.getX(), tabY, tabWidth, "Exploration", explorationMode,
            inside(mouseX, mouseY, panel.getX(), tabY, tabWidth, TAB_HEIGHT));
        drawTab(graphics, panel.getX() + tabWidth, tabY, tabWidth, "Expeditions", !explorationMode,
            inside(mouseX, mouseY, panel.getX() + tabWidth, tabY, tabWidth, TAB_HEIGHT));
    }

    @Inject(method = "drawPokemonGrid", at = @At("HEAD"), cancellable = true)
    private void cobblemonNoMansLands$drawTeammates(GuiGraphics graphics, SyncStatePayload state,
                                                    Rect rect, int mouseX, int mouseY,
                                                    CallbackInfo callbackInfo) {
        if (!explorationMode) {
            return;
        }
        callbackInfo.cancel();
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) {
            return;
        }
        List<net.minecraft.world.entity.player.Player> players = new ArrayList<>();
        players.add(minecraft.player);
        players.addAll(minecraft.level.players().stream()
                .filter(player -> !player.getUUID().equals(minecraft.player.getUUID()))
            .filter(player -> teammateSearch.isBlank()
                || player.getName().getString().toLowerCase(java.util.Locale.ROOT)
                .contains(teammateSearch.toLowerCase(java.util.Locale.ROOT)))
                .toList());
        for (int index = 0; index < Math.min(players.size(), 10); index++) {
            UUID playerId = players.get(index).getUUID();
            boolean ownerCell = index == 0;
            boolean selected = ownerCell || selectedTeammates.contains(playerId);
            Rect cell = layout.pokeCell(index % 5, index / 5);
                SlotRenderer.INSTANCE.drawWell(graphics, theme, cell.getX(), cell.getY(), selected);
                ExplorationSelectionResultPayload.PlayerStats playerStats = routedPlayerStats.get(playerId);
                int rank = playerStats == null && syncedState != null ? syncedState.getExpeditionRank()
                    : playerStats == null ? 0 : playerStats.rank();
                String rankText = "Rank: " + rank;
                drawCellText(graphics, rankText,
                    cell.getX() + (cell.getW() - (int) (font.width(rankText) * 0.65F)) / 2,
                    cell.getY() + 4, 0xFFFF55, 0.65F);
            if (players.get(index) instanceof Player player) {
                renderPlayerHead(graphics, player, cell);
                int power = playerStats == null ? getPlayerPower(player) : playerStats.power();
                graphics.drawString(font, Component.literal(String.valueOf(power)),
                        cell.getX() + 5, cell.getBottom() - 10, theme.getTextOnPanel(), false);
            }
                SlotRenderer.INSTANCE.decorate(graphics, cell.getX(), cell.getY(), selected, false, false);
        }
    }

    private void renderPlayerHead(GuiGraphics graphics, Player player, Rect cell) {
        ItemStack head = new ItemStack(Items.PLAYER_HEAD);
        head.set(DataComponents.PROFILE, new ResolvableProfile(player.getGameProfile()));
        PoseStack pose = graphics.pose();
        pose.pushPose();

        pose.translate(cell.getX() + cell.getW() / 2.0f, cell.getY() + cell.getH() / 2.0f + 2.0F, 0);
        pose.scale(1.25F, 1.25F, 1.25F);

        graphics.renderItem(head, -8, -8);

        pose.popPose();
    }

    private int getPlayerPower(Player player) {
        return 0;
    }

    private List<Player> getVisiblePlayers() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) {
            return List.of();
        }
        List<Player> players = new ArrayList<>();
        players.add(minecraft.player);
        players.addAll(minecraft.level.players().stream()
                .filter(player -> !player.getUUID().equals(minecraft.player.getUUID()))
                .filter(player -> teammateSearch.isBlank()
                        || player.getName().getString().toLowerCase(java.util.Locale.ROOT)
                        .contains(teammateSearch.toLowerCase(java.util.Locale.ROOT)))
                .toList());
        return players;
    }

    private void drawCellText(GuiGraphics graphics, String text, int x, int y, int color, float scale) {
        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(x, y, 0.0F);
        pose.scale(scale, scale, scale);
        graphics.drawString(font, Component.literal(text), 0, 0, color, false);
        pose.popPose();
    }

    @Inject(method = "drawPanelLabel", at = @At("HEAD"), cancellable = true)
    private void cobblemonNoMansLands$hideOriginalPanelLabels(GuiGraphics graphics, Rect rect, String label,
                                                              CallbackInfo callbackInfo) {
        String normalizedLabel = label == null ? "" : label.trim().toLowerCase(java.util.Locale.ROOT);
        if (normalizedLabel.contains("expedition")) {
            callbackInfo.cancel();
            return;
        }
        if (explorationMode && (normalizedLabel.contains("pokemon") || normalizedLabel.contains("pokémon"))) {
            callbackInfo.cancel();
            drawPanelHeader(graphics, rect, "Teammates");
            return;
        }
        if (explorationMode && normalizedLabel.equals("active")) {
            callbackInfo.cancel();
            drawPanelHeader(graphics, rect, "Active");
            return;
        }
    }

    @Inject(method = "drawFilterBar", at = @At("HEAD"), cancellable = true)
    private void cobblemonNoMansLands$drawTeammateHeader(GuiGraphics graphics, Rect rect, int mouseX, int mouseY,
                                                         CallbackInfo callbackInfo) {
        if (!explorationMode) {
            return;
        }
        callbackInfo.cancel();
        Rect searchRect = rect;
        graphics.fill(searchRect.getX(), searchRect.getY(), searchRect.getRight(), searchRect.getBottom(),
            theme.getRecess());
        graphics.renderOutline(searchRect.getX(), searchRect.getY(), searchRect.getW(), searchRect.getH(),
            teammateSearchBox != null && teammateSearchBox.isFocused()
                ? theme.getAccent().getLit()
                : theme.getMaterial().getLit());
        IconSheet.INSTANCE.drawSized(graphics, searchRect.getX() + 2, searchRect.getY() + 2, 8,
            IconSheet.Glyph.SEARCH, IconSheet.Tone.MUTED);
        if (teammateSearchBox != null) {
            String searchText = teammateSearchBox.getValue();
            if (searchText.isEmpty()) {
                searchText = Component.translatable("gui.cobblemon_expeditions.search").getString();
            }
            int searchTextColor = teammateSearchBox.getValue().isEmpty()
                ? 0xFFAAAAAA
                : theme.getTextOnPanel();
            graphics.drawString(font, searchText, searchRect.getX() + 11, searchRect.getY() + 2,
                searchTextColor, false);
            teammateSearchBox.render(graphics, mouseX, mouseY, 0f);
        }
    }

    @Inject(method = "drawExpeditionHeaderToggles", at = @At("HEAD"), cancellable = true)
    private void cobblemonNoMansLands$hideExpeditionToggles(GuiGraphics graphics, int mouseX, int mouseY,
                                                            CallbackInfo callbackInfo) {
        if (explorationMode) {
            callbackInfo.cancel();
        }
    }

    @Inject(method = "drawHeaderToggleTooltips", at = @At("HEAD"), cancellable = true)
    private void cobblemonNoMansLands$hideExpeditionToggleTooltips(GuiGraphics graphics, int mouseX, int mouseY,
                                                                    CallbackInfo callbackInfo) {
        if (explorationMode) {
            callbackInfo.cancel();
        }
    }

    @Inject(method = "drawPokemonTooltip", at = @At("HEAD"), cancellable = true)
    private void cobblemonNoMansLands$drawExplorationPlayerTooltip(GuiGraphics graphics, SyncStatePayload state,
                                                                    int mouseX, int mouseY,
                                                                    CallbackInfo callbackInfo) {
        if (explorationMode) {
            callbackInfo.cancel();
            List<Player> players = getVisiblePlayers();
            for (int index = 0; index < Math.min(players.size(), 10); index++) {
                Rect cell = layout.pokeCell(index % 5, index / 5);
                if (!cell.contains(mouseX, mouseY)) {
                    continue;
                }
                Player player = players.get(index);
                var stats = routedPlayerStats.get(player.getUUID());
                int rank = stats == null && syncedState != null ? syncedState.getExpeditionRank()
                    : stats == null ? 0 : stats.rank();
                List<Component> tooltip = List.of(
                        Component.literal(player.getName().getString()),
                        Component.literal("Rank: " + rank),
                    Component.literal("Power: " + (stats == null ? getPlayerPower(player) : stats.power()))
                );
                new TooltipRenderer().draw(graphics, font, theme, tooltip, mouseX, mouseY, width, height);
                break;
            }
        }
    }

    @Inject(method = "drawSelectionBar", at = @At("HEAD"), cancellable = true)
    private void cobblemonNoMansLands$drawExplorationSelection(GuiGraphics graphics, SyncStatePayload state,
                                                               Rect rect, int mouseX, int mouseY,
                                                               CallbackInfo callbackInfo) {
        if (!explorationMode) {
            return;
        }
        callbackInfo.cancel();
        syncExplorationResult();
        List<SyncStatePayload.AvailableExpeditionInfo> expeditions = getAvailableExpeditions();
        int requiredPower = routedRequiredPower;
        if (selectedExpeditionIndex >= 0 && selectedExpeditionIndex < expeditions.size()) {
            requiredPower = routedRequiredPower > 0 ? routedRequiredPower
                : rankRequirement(expeditions.get(selectedExpeditionIndex).getExpeditionId());
        }
        int currentMembers = routedCurrentMembers > 0 ? routedCurrentMembers : selectedTeammates.size() + 1;
        int currentPower = routedCurrentPower;
        int textX = rect.getX() + 4;
        int textY = rect.getY() + 5;
        if (explorationSelected) {
            String powerText = "Pwr: " + currentPower + "/" + requiredPower;
            String membersText = "Members: " + currentMembers + "/" + routedMaxMembers;
            int powerColor = currentPower >= requiredPower ? 0xFF55FF55 : 0xFFFF5555;
            graphics.drawString(font, Component.literal(membersText), textX, textY, theme.getTextOnPanel(), false);
            graphics.drawString(font, Component.literal(powerText), rect.getX() + EXPLORATION_POWER_COLUMN_OFFSET,
                textY, powerColor, false);
        } else {
            graphics.drawString(font, Component.literal("Select an exploration from the left panel"), textX, textY,
                0xFF666666, false);
        }
        if (explorationSelected) {
            int buttonHeight = ButtonRenderer.INSTANCE.getHEIGHT();
            int confirmWidth = ButtonRenderer.INSTANCE.widthFor("Confirm");
            int buttonY = rect.getBottom() - buttonHeight - 3;
            int confirmX = rect.getRight() - confirmWidth - 3;
            boolean canConfirm = canConfirmExploration();
            drawActionButton(graphics, confirmX, buttonY, confirmWidth, "Confirm",
                canConfirm && inside(mouseX, mouseY, confirmX, buttonY, confirmWidth, buttonHeight),
                canConfirm ? 0xFF55AA55 : 0xFF555555);
        }
    }

    @Inject(method = "drawActiveExpeditions", at = @At("HEAD"), cancellable = true)
    private void cobblemonNoMansLands$drawActiveExploration(GuiGraphics graphics, SyncStatePayload state,
                                                            Rect rect, int mouseX, int mouseY,
                                                            CallbackInfo callbackInfo) {
        if (!explorationMode) {
            return;
        }
        callbackInfo.cancel();
        syncExplorationResult();
        List<SyncStatePayload.AvailableExpeditionInfo> expeditions = getAvailableExpeditions();
        if (explorationConfirmed && activeExplorationId != null) {
            BoardFooterLayout footer = new BoardFooterLayout(rect);
            Rect row = footer.row(0);
            Rect closeAction = footer.action(0, cancelBtnW);
            SyncStatePayload.AvailableExpeditionInfo activeExpedition = expeditions.stream()
                .filter(expedition -> activeExplorationId != null
                    && activeExplorationId.equals(expedition.getExpeditionId()))
                .findFirst()
                .orElse(null);
            if (activeExpedition == null) {
            graphics.drawString(font, Component.literal("None"), rect.getX() + 2, rect.getY() + 4,
                0xFF666666, false);
            return;
            }
            graphics.drawString(font, displayName(activeExpedition.getName()),
                    row.getX(), row.getY() + 2, theme.getTextOnPanel());
            boolean actionHovered = closeAction.contains(mouseX, mouseY);
            IconSheet.INSTANCE.drawSized(graphics, closeAction.getX() + 1, closeAction.getY() + 1, 10,
                    IconSheet.Glyph.CROSS, actionHovered ? IconSheet.Tone.NEGATIVE : IconSheet.Tone.MUTED);
        } else {
            graphics.drawString(font, Component.literal("None"), rect.getX() + 2, rect.getY() + 4,
                    0xFF666666, false);
        }
    }

    @Inject(method = "drawRewardInbox", at = @At("HEAD"), cancellable = true)
    private void cobblemonNoMansLands$drawPortalStatus(GuiGraphics graphics, SyncStatePayload state,
                                                       Rect rect, int mouseX, int mouseY,
                                                       CallbackInfo callbackInfo) {
        if (!explorationMode) {
            return;
        }
        callbackInfo.cancel();
        graphics.drawString(font, Component.literal("None"), rect.getX() + 2, rect.getY() + 4,
            0xFF666666, false);
    }

    private void drawPanelHeader(GuiGraphics graphics, Rect rect, String label) {
        graphics.drawString(font, Component.literal(label), rect.getX() + 4, rect.getY() + 3,
                theme.getFixture().getSpecular(), true);
    }

    private void syncExplorationResult() {
        ExplorationSelectionResultPayload result = ExplorationSelectionClientState.peek(blockPos);
        if (result != null && result != synchronizedResult) {
            synchronizedResult = result;
            explorationConfirmed = result.accepted();
            activeExplorationId = result.accepted() ? result.explorationId() : null;
            explorationStatus = result.reason();
            routedCurrentPower = result.currentPower();
            routedRequiredPower = result.requiredPower();
            routedCurrentMembers = result.currentMembers();
            routedMaxMembers = result.maxMembers();
            routedPlayerStats.clear();
            for (var stats : result.playerStats()) {
                routedPlayerStats.put(stats.playerId(), stats);
            }
            if (!result.accepted() && result.reason().toLowerCase(java.util.Locale.ROOT)
                    .contains("no available portal")) {
                showError("Portal is busy!");
            }
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void cobblemonNoMansLands$handleExplorationClick(double mouseX, double mouseY, int button,
                                                             CallbackInfoReturnable<Boolean> callbackInfo) {
        if (button != 0) {
            return;
        }

        Rect panel = layout.getExpPanel();
        int tabWidth = panel.getW() / 2;
        int tabY = panel.getY();
        if (inside(mouseX, mouseY, panel.getX(), tabY, tabWidth, TAB_HEIGHT)) {
            explorationMode = true;
            ExplorationTabState.setExplorationMode(true);
            if (teammateSearchBox != null) {
                teammateSearchBox.setFocused(false);
            }
            callbackInfo.setReturnValue(true);
            return;
        }
        if (inside(mouseX, mouseY, panel.getX() + tabWidth, tabY, tabWidth, TAB_HEIGHT)) {
            explorationMode = false;
            ExplorationTabState.setExplorationMode(false);
            if (teammateSearchBox != null) {
                teammateSearchBox.setFocused(false);
            }
            callbackInfo.setReturnValue(true);
            return;
        }
        if (!explorationMode) {
            return;
        }

        if (filterBarRect.contains(mouseX, mouseY)) {
            if (teammateSearchBox != null) {
                teammateSearchBox.setFocused(true);
                teammateSearchBox.mouseClicked(mouseX, mouseY, button);
            }
            callbackInfo.setReturnValue(true);
            callbackInfo.cancel();
            return;
        }

        if (pokeGridRect.contains(mouseX, mouseY)) {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.level != null && minecraft.player != null) {
                List<net.minecraft.world.entity.player.Player> filteredPlayers = new ArrayList<>();
                filteredPlayers.add(minecraft.player);
                filteredPlayers.addAll(minecraft.level.players().stream()
                    .filter(player -> !player.getUUID().equals(minecraft.player.getUUID()))
                    .filter(player -> teammateSearch.isBlank() || player.getName().getString().toLowerCase(java.util.Locale.ROOT)
                        .contains(teammateSearch.toLowerCase(java.util.Locale.ROOT)))
                    .toList());
                int teammateIndex = (int) ((mouseY - pokeGridRect.getY()) / 50) * 5
                    + (int) ((mouseX - pokeGridRect.getX()) / 50);
                if (teammateIndex > 0 && teammateIndex < filteredPlayers.size() && teammateIndex < 10) {
                    UUID teammateId = filteredPlayers.get(teammateIndex).getUUID();
                    if (!selectedTeammates.remove(teammateId) && selectedTeammates.size() < 3) {
                        selectedTeammates.add(teammateId);
                    }
                }
            }
            callbackInfo.setReturnValue(true);
            return;
        }

        if (explorationConfirmed && activeExpRect.contains(mouseX, mouseY)) {
            BoardFooterLayout footer = new BoardFooterLayout(activeExpRect);
            Rect closeAction = footer.action(0, cancelBtnW);
            if (closeAction.contains(mouseX, mouseY)) {
                cancelExploration();
                callbackInfo.setReturnValue(true);
                return;
            }
        }

        if (selectionBarRect.contains(mouseX, mouseY) && explorationSelected) {
            int buttonHeight = ButtonRenderer.INSTANCE.getHEIGHT();
            int confirmWidth = ButtonRenderer.INSTANCE.widthFor("Confirm");
            int buttonY = selectionBarRect.getBottom() - buttonHeight - 3;
            int confirmX = selectionBarRect.getRight() - confirmWidth - 3;
            if (canConfirmExploration()
                    && inside(mouseX, mouseY, confirmX, buttonY, confirmWidth, buttonHeight)) {
                confirmExploration();
            } else if (!canConfirmExploration()
                    && inside(mouseX, mouseY, confirmX, buttonY, confirmWidth, buttonHeight)) {
                showError("Power Insufficient!");
            }
            callbackInfo.setReturnValue(true);
        }
    }

    @Inject(method = "mouseClicked", at = @At("RETURN"))
    private void cobblemonNoMansLands$rememberExplorationCard(double mouseX, double mouseY, int button,
                                                             CallbackInfoReturnable<Boolean> callbackInfo) {
        if (button == 0 && explorationMode && expContentRect.contains(mouseX, mouseY)) {
            explorationSelected = true;
            List<SyncStatePayload.AvailableExpeditionInfo> expeditions = getAvailableExpeditions();
            if (selectedExpeditionIndex >= 0 && selectedExpeditionIndex < expeditions.size()) {
                explorationStatus = displayName(expeditions.get(selectedExpeditionIndex).getName()).getString() + " selected";
            }
        }
    }

    @Inject(method = "charTyped", at = @At("HEAD"), cancellable = true)
    private void cobblemonNoMansLands$searchChar(char codePoint, int modifiers,
                                                  CallbackInfoReturnable<Boolean> callbackInfo) {
        if (explorationMode && teammateSearchBox != null && teammateSearchBox.isFocused()) {
            boolean consumed = teammateSearchBox.charTyped(codePoint, modifiers);
            callbackInfo.setReturnValue(consumed);
            callbackInfo.cancel();
        }
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void cobblemonNoMansLands$searchKey(int keyCode, int scanCode, int modifiers,
                                                CallbackInfoReturnable<Boolean> callbackInfo) {
        if (explorationMode && teammateSearchBox != null && teammateSearchBox.isFocused()) {
            boolean consumed = teammateSearchBox.keyPressed(keyCode, scanCode, modifiers);
            callbackInfo.setReturnValue(consumed);
            callbackInfo.cancel();
        }
    }

    private void drawTab(GuiGraphics graphics, int x, int y, int width, String label,
                         boolean selected, boolean hovered) {
        ButtonRenderer.INSTANCE.draw(graphics, theme, x, y, width, label, selected, hovered,
            selected ? theme.getTextOnCard() : theme.getTextOnPanel());
    }

    private void drawActionButton(GuiGraphics graphics, int x, int y, int width, String label,
                                  boolean hovered, int accentColor) {
        ButtonRenderer.INSTANCE.draw(graphics, theme, x, y, width, label, hovered, hovered, accentColor);
    }

    private void confirmExploration() {
        List<SyncStatePayload.AvailableExpeditionInfo> expeditions = getAvailableExpeditions();
        if (!explorationSelected || selectedExpeditionIndex < 0 || selectedExpeditionIndex >= expeditions.size()) {
            return;
        }
        String expeditionName = expeditions.get(selectedExpeditionIndex).getName();
        PacketDistributor.sendToServer(new ExplorationSelectionPayload(
                blockPos,
                ResourceLocation.fromNamespaceAndPath("cobblemon_expeditions", toIdPath(expeditionName)),
                new ArrayList<>(selectedTeammates)
        ));
        explorationStatus = "Activating connected portal...";
        explorationConfirmed = false;
        activeExplorationId = null;
    }

    private boolean canConfirmExploration() {
        List<SyncStatePayload.AvailableExpeditionInfo> expeditions = getAvailableExpeditions();
        if (!explorationSelected || selectedExpeditionIndex < 0 || selectedExpeditionIndex >= expeditions.size()) {
            return false;
        }
        int rank = syncedState == null ? 0 : syncedState.getExpeditionRank();
        return 0 >= rankRequirement(expeditions.get(selectedExpeditionIndex).getExpeditionId(), rank);
    }

    private int rankRequirement(ResourceLocation explorationId) {
        return rankRequirement(explorationId, syncedState == null ? 0 : syncedState.getExpeditionRank());
    }

    private int rankRequirement(ResourceLocation explorationId, int rank) {
        int basePower = switch (explorationId.getPath()) {
            case "forest_forage" -> 45;
            case "shoreline_survey" -> 60;
            case "berry_grove_harvest" -> 90;
            case "cave_delve" -> 120;
            case "deep_sea_dive" -> 180;
            case "volcanic_survey" -> 225;
            case "frozen_ruins" -> 338;
            case "distortion_rift" -> 450;
            default -> 0;
        };
        double multiplier = switch (Math.max(0, Math.min(10, rank))) {
            case 0 -> 1.0D;
            case 1 -> 1.2D;
            case 2 -> 1.44D;
            case 3 -> 1.72D;
            case 4 -> 2.04D;
            case 5 -> 2.40D;
            case 6 -> 2.76D;
            case 7 -> 3.12D;
            case 8 -> 3.44D;
            case 9 -> 3.72D;
            default -> 4.0D;
        };
        return (int) Math.round(basePower * multiplier);
    }

    private void cancelExploration() {
        selectedTeammates.clear();
        explorationSelected = false;
        explorationConfirmed = false;
        teammateSearch = "";
        if (teammateSearchBox != null) {
            teammateSearchBox.setValue("");
            teammateSearchBox.setFocused(false);
        }
        explorationStatus = "Select an exploration and teammates";
    }

    private List<SyncStatePayload.AvailableExpeditionInfo> getAvailableExpeditions() {
        return syncedState == null ? List.of() : getSortedExpeditions(syncedState);
    }

    private static String toIdPath(String name) {
        int separator = name.lastIndexOf('.');
        String path = separator >= 0 ? name.substring(separator + 1) : name;
        return path.toLowerCase(java.util.Locale.ROOT).replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
    }

    private static Component displayName(String name) {
        return name != null && name.startsWith("expedition.")
                ? Component.translatable(name)
                : Component.literal(name == null ? "" : name);
    }

    private static boolean inside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

}

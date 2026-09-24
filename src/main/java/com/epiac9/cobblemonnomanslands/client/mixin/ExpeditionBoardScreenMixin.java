package com.epiac9.cobblemonnomanslands.client.mixin;

import com.epiac9.cobblemonnomanslands.client.ExplorationSelectionClientState;
import com.epiac9.cobblemonnomanslands.expedition.dimension.ExpeditionDimensionMapping;
import com.epiac9.cobblemonnomanslands.expedition.manager.route.InitialExpeditionMappings;
import com.epiac9.cobblemonnomanslands.client.ExplorationTabState;
import com.epiac9.cobblemonnomanslands.client.ExplorationPartyClientState;
import com.cobblemonexpeditions.gui.Rect;
import com.cobblemonexpeditions.gui.BoardLayout;
import com.cobblemonexpeditions.gui.BoardFooterLayout;
import com.cobblemonexpeditions.gui.chrome.ButtonRenderer;
import com.cobblemonexpeditions.gui.chrome.IconSheet;
import com.cobblemonexpeditions.gui.chrome.Panel;
import com.cobblemonexpeditions.gui.chrome.Panel.CardState;
import com.cobblemonexpeditions.gui.chrome.SlotRenderer;
import com.cobblemonexpeditions.gui.theme.BoardTheme;
import com.cobblemonexpeditions.network.s2c.SyncStatePayload;
import com.epiac9.cobblemonnomanslands.network.ExplorationSelectionPayload;
import com.epiac9.cobblemonnomanslands.network.ExplorationSelectionResultPayload;
import com.epiac9.cobblemonnomanslands.network.ExplorationCancelPayload;
import com.epiac9.cobblemonnomanslands.network.ExplorationPartyStatusRequestPayload;
import com.epiac9.cobblemonnomanslands.network.ExplorationPartyStatusPayload;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(targets = "com.cobblemonexpeditions.gui.ExpeditionBoardScreen")
public abstract class ExpeditionBoardScreenMixin extends Screen {
    private static final ExpeditionDimensionMapping EXPLORATION_PROFILES = InitialExpeditionMappings.create();
    private static final int TAB_HEIGHT = 18;
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
    private Rect selectionBarRect;
    @Shadow
    private Rect activeExpRect;
    @Shadow
    private Rect rewardPanelRect;
    @Shadow
    private int cancelBtnW;
    @Shadow
    private int expLineH;
    @Shadow
    private int expeditionScrollOffset;
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
    private int currentPokemon;
    private int maximumPokemon = 1;
    private boolean portalAvailable;
    private boolean boardAccessible;
    private boolean statsAvailable;
    private boolean ownerHasPending;
    private boolean boardActionPending;
    private int currentRank;
    private int statusPollTicks;
    private ResourceLocation boardDimensionId;
    private ExplorationPartyStatusPayload synchronizedStatus;
    protected ExpeditionBoardScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void cobblemonNoMansLands$initExploration(CallbackInfo callbackInfo) {
        explorationMode = ExplorationTabState.isExplorationMode();
        if (blockPos != null && Minecraft.getInstance().level != null) {
            boardDimensionId = Minecraft.getInstance().level.dimension().location();
            ExplorationPartyClientState.begin(boardDimensionId, blockPos);
            ExplorationSelectionClientState.begin(boardDimensionId, blockPos);
            synchronizedStatus = null;
            synchronizedResult = null;
            explorationConfirmed = false;
            activeExplorationId = null;
            boardAccessible = false;
            statsAvailable = false;
            portalAvailable = false;
            ownerHasPending = false;
            boardActionPending = false;
            statusPollTicks = 0;
            requestExplorationStatus();
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void cobblemonNoMansLands$refreshExplorationStatus(CallbackInfo callbackInfo) {
        if (explorationMode && ++statusPollTicks >= 20) {
            statusPollTicks = 0;
            requestExplorationStatus();
        }
    }

    @Inject(method = "onClose", at = @At("TAIL"))
    private void cobblemonNoMansLands$clearExplorationView(CallbackInfo callbackInfo) {
        ExplorationPartyClientState.clear();
        ExplorationSelectionClientState.clear();
    }

    private void requestExplorationStatus() {
        if (blockPos != null && Minecraft.getInstance().level != null
                && Minecraft.getInstance().level.dimension().location().equals(boardDimensionId)) {
            PacketDistributor.sendToServer(new ExplorationPartyStatusRequestPayload(blockPos));
        }
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void cobblemonNoMansLands$syncBeforeRender(GuiGraphics graphics, int mouseX, int mouseY,
                                                      float partialTick, CallbackInfo callbackInfo) {
        if (explorationMode) {
            syncExplorationResult();
        }
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
    private void cobblemonNoMansLands$drawBlankExplorationCells(GuiGraphics graphics, SyncStatePayload state,
                                                                Rect rect, int mouseX, int mouseY,
                                                                CallbackInfo callbackInfo) {
        if (!explorationMode) {
            return;
        }
        callbackInfo.cancel();
        for (int index = 0; index < 15; index++) {
            Rect cell = layout.pokeCell(index % 5, index / 5);
            SlotRenderer.INSTANCE.drawWell(graphics, theme, cell.getX(), cell.getY(),
                cell.contains(mouseX, mouseY));
        }
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
            return;
        }
        if (explorationMode && normalizedLabel.equals("active")) {
            callbackInfo.cancel();
            drawPanelHeader(graphics, rect, "Active");
            return;
        }
    }

    @Inject(method = "drawFilterBar", at = @At("HEAD"), cancellable = true)
    private void cobblemonNoMansLands$hideExplorationFilterBar(GuiGraphics graphics, Rect rect, int mouseX,
                                                               int mouseY, CallbackInfo callbackInfo) {
        if (explorationMode) {
            callbackInfo.cancel();
        }
    }

    @Inject(method = "drawPokemonTooltip", at = @At("HEAD"), cancellable = true)
    private void cobblemonNoMansLands$hideExplorationPokemonTooltip(GuiGraphics graphics, SyncStatePayload state,
                                                                    int mouseX, int mouseY,
                                                                    CallbackInfo callbackInfo) {
        if (explorationMode) {
            callbackInfo.cancel();
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

    @Redirect(method = "drawAvailableExpeditions", at = @At(value = "INVOKE",
            target = "Lcom/cobblemonexpeditions/network/s2c/SyncStatePayload$AvailableExpeditionInfo;getRequiredPower()I"))
    private int cobblemonNoMansLands$redirectExplorationRequiredPower(
            SyncStatePayload.AvailableExpeditionInfo expedition) {
        if (!explorationMode) {
            return expedition.getRequiredPower();
        }
        var profile = EXPLORATION_PROFILES.getProfile(expedition.getExpeditionId());
        return profile == null ? expedition.getRequiredPower() : profile.requiredPowerForRank(currentRank);
    }

    @Redirect(method = "drawExpeditionTooltip", at = @At(value = "INVOKE",
            target = "Lcom/cobblemonexpeditions/network/s2c/SyncStatePayload$AvailableExpeditionInfo;getRequiredPower()I"))
    private int cobblemonNoMansLands$redirectExplorationTooltipPower(
            SyncStatePayload.AvailableExpeditionInfo expedition) {
        if (!explorationMode) {
            return expedition.getRequiredPower();
        }
        var profile = EXPLORATION_PROFILES.getProfile(expedition.getExpeditionId());
        return profile == null ? expedition.getRequiredPower() : profile.requiredPowerForRank(currentRank);
    }

    @Redirect(method = "drawAvailableExpeditions", at = @At(value = "INVOKE",
            target = "Lcom/cobblemonexpeditions/gui/BoardPreviewMath;effectiveDurationSeconds(Lcom/cobblemonexpeditions/network/s2c/SyncStatePayload;Lcom/cobblemonexpeditions/network/s2c/SyncStatePayload$AvailableExpeditionInfo;)I"))
    private int cobblemonNoMansLands$redirectExplorationDuration(
            com.cobblemonexpeditions.gui.BoardPreviewMath math, SyncStatePayload state,
            SyncStatePayload.AvailableExpeditionInfo expedition) {
        if (!explorationMode) {
            return math.effectiveDurationSeconds(state, expedition);
        }
        var profile = EXPLORATION_PROFILES.getProfile(expedition.getExpeditionId());
        return profile == null ? math.effectiveDurationSeconds(state, expedition)
            : profile.durationMinutesForRank(currentRank) * 60;
    }

    @Redirect(method = "drawExpeditionTooltip", at = @At(value = "INVOKE",
            target = "Lcom/cobblemonexpeditions/gui/BoardPreviewMath;effectiveDurationSeconds(Lcom/cobblemonexpeditions/network/s2c/SyncStatePayload;Lcom/cobblemonexpeditions/network/s2c/SyncStatePayload$AvailableExpeditionInfo;)I"))
    private int cobblemonNoMansLands$redirectExplorationTooltipDuration(
            com.cobblemonexpeditions.gui.BoardPreviewMath math, SyncStatePayload state,
            SyncStatePayload.AvailableExpeditionInfo expedition) {
        if (!explorationMode) {
            return math.effectiveDurationSeconds(state, expedition);
        }
        var profile = EXPLORATION_PROFILES.getProfile(expedition.getExpeditionId());
        return profile == null ? math.effectiveDurationSeconds(state, expedition)
            : profile.durationMinutesForRank(currentRank) * 60;
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
        int requiredPower = 0;
        boolean registeredExploration = false;
        if (selectedExpeditionIndex >= 0 && selectedExpeditionIndex < expeditions.size()) {
            ResourceLocation selectedId = expeditions.get(selectedExpeditionIndex).getExpeditionId();
            registeredExploration = EXPLORATION_PROFILES.contains(selectedId);
            requiredPower = rankRequirement(selectedId);
        }
        int currentPower = routedCurrentPower;
        int textX = rect.getX() + 4;
        int textY = rect.getY() + 5;
        if (explorationSelected) {
            String teamText = !registeredExploration ? "Exploration unavailable"
                : statsAvailable ? "Team: " + currentPokemon + "/" + maximumPokemon : "Party stats unavailable";
            String powerText = "Pwr: " + currentPower + "/" + requiredPower;
            int powerColor = currentPower >= requiredPower ? 0xFF55FF55 : 0xFFFF5555;
            int teamColor = !registeredExploration || !statsAvailable || currentPokemon == 0 || currentPokemon > maximumPokemon ? 0xFFFF5555 : theme.getTextOnPanel();
            graphics.drawString(font, Component.literal(teamText), textX, textY, teamColor, false);
            if (statsAvailable && registeredExploration) {
                graphics.drawString(font, Component.literal(powerText), rect.getX() + EXPLORATION_POWER_COLUMN_OFFSET,
                    textY, powerColor, false);
            }
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
            boolean confirmHovered = inside(mouseX, mouseY, confirmX, buttonY, confirmWidth, buttonHeight);
            drawActionButton(graphics, confirmX, buttonY, confirmWidth, "Confirm",
                confirmHovered,
                canConfirm,
                canConfirm ? 0xFF55FF55 : 0xFF666666);
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
        if (Minecraft.getInstance().level == null
                || !Minecraft.getInstance().level.dimension().location().equals(boardDimensionId)) {
            boardAccessible = false;
            explorationConfirmed = false;
            activeExplorationId = null;
            return;
        }
        var status = ExplorationPartyClientState.peek(boardDimensionId, blockPos);
        if (status != null && status != synchronizedStatus) {
            synchronizedStatus = status;
            currentPokemon = status.currentPokemon();
            maximumPokemon = status.maximumPokemon();
            currentRank = status.currentRank();
            routedCurrentPower = status.currentPower();
            boardAccessible = status.boardAccessible();
            statsAvailable = status.statsAvailable();
            portalAvailable = status.portalAvailable();
            ownerHasPending = status.ownerHasPending();
            activeExplorationId = status.activeExplorationId();
            explorationConfirmed = activeExplorationId != null;
            boardActionPending = false;
        }
        ExplorationSelectionResultPayload result = ExplorationSelectionClientState.peek(boardDimensionId, blockPos);
        if (result != null && result != synchronizedResult) {
            synchronizedResult = result;
            if (!result.accepted()) {
                showError(result.reason().toLowerCase(java.util.Locale.ROOT).contains("no available portal")
                    ? "Portal is busy!" : result.reason());
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
            requestExplorationStatus();
            callbackInfo.setReturnValue(true);
            return;
        }
        if (inside(mouseX, mouseY, panel.getX() + tabWidth, tabY, tabWidth, TAB_HEIGHT)) {
            explorationMode = false;
            ExplorationTabState.setExplorationMode(false);
            callbackInfo.setReturnValue(true);
            return;
        }
        if (!explorationMode) {
            return;
        }

        syncExplorationResult();

        if (explorationConfirmed && activeExpRect.contains(mouseX, mouseY)) {
            BoardFooterLayout footer = new BoardFooterLayout(activeExpRect);
            Rect closeAction = footer.action(0, cancelBtnW);
            if (closeAction.contains(mouseX, mouseY)) {
                if (!boardActionPending && boardAccessible) {
                    cancelExploration();
                }
                // Do not let a disabled Exploration control reach the native Expedition handler.
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
                if (selectedExpeditionIndex < 0 || selectedExpeditionIndex >= getAvailableExpeditions().size()
                        || !EXPLORATION_PROFILES.contains(getAvailableExpeditions().get(selectedExpeditionIndex).getExpeditionId())) {
                    showError("Exploration is not registered");
                } else if (boardActionPending) {
                    showError("Waiting for server...");
                } else if (!boardAccessible) {
                    showError("Board is unavailable or too far away");
                } else if (ownerHasPending) {
                    showError("You already have a pending portal.");
                } else if (!statsAvailable) {
                    showError("Party stats are temporarily unavailable. Please try again.");
                } else if (currentPokemon == 0) {
                    showError("Add pokemon to your party!");
                } else if (currentPokemon > maximumPokemon) {
                    showError("Max " + maximumPokemon + " pokemon!");
                } else if (!portalAvailable) {
                    showError("Portal is busy!");
                } else {
                    showError("Power Insufficient!");
                }
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
            }
        }
    }

    private void drawTab(GuiGraphics graphics, int x, int y, int width, String label,
                         boolean selected, boolean hovered) {
        ButtonRenderer.INSTANCE.draw(graphics, theme, x, y, width, label, selected, hovered,
            selected ? theme.getTextOnCard() : theme.getTextOnPanel());
    }

    private void drawActionButton(GuiGraphics graphics, int x, int y, int width, String label,
                                  boolean mouseOver, boolean enabled, int accentColor) {
        ButtonRenderer.INSTANCE.draw(graphics, theme, x, y, width, label, mouseOver, enabled, accentColor);
    }

    private void confirmExploration() {
        List<SyncStatePayload.AvailableExpeditionInfo> expeditions = getAvailableExpeditions();
        if (!explorationSelected || selectedExpeditionIndex < 0 || selectedExpeditionIndex >= expeditions.size()) {
            return;
        }
        PacketDistributor.sendToServer(new ExplorationSelectionPayload(
                blockPos,
                expeditions.get(selectedExpeditionIndex).getExpeditionId()
        ));
        boardActionPending = true;
    }

    private boolean canConfirmExploration() {
        List<SyncStatePayload.AvailableExpeditionInfo> expeditions = getAvailableExpeditions();
        if (!explorationSelected || selectedExpeditionIndex < 0 || selectedExpeditionIndex >= expeditions.size()) {
            return false;
        }
        ResourceLocation selectedId = expeditions.get(selectedExpeditionIndex).getExpeditionId();
        return EXPLORATION_PROFILES.contains(selectedId) && boardAccessible && statsAvailable && !boardActionPending && !ownerHasPending
            && currentPokemon > 0 && currentPokemon <= maximumPokemon
            && portalAvailable
            && routedCurrentPower >= rankRequirement(selectedId);
    }

    private int rankRequirement(ResourceLocation explorationId) {
        var profile = EXPLORATION_PROFILES.getProfile(explorationId);
        return profile == null ? 0 : profile.requiredPowerForRank(currentRank);
    }

    private void cancelExploration() {
        if (blockPos != null) {
            PacketDistributor.sendToServer(new ExplorationCancelPayload(blockPos));
            boardActionPending = true;
        }
    }

    private List<SyncStatePayload.AvailableExpeditionInfo> getAvailableExpeditions() {
        return syncedState == null ? List.of() : getSortedExpeditions(syncedState);
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

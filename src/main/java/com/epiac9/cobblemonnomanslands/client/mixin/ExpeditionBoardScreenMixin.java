package com.epiac9.cobblemonnomanslands.client.mixin;

import com.epiac9.cobblemonnomanslands.client.ExplorationSelectionClientState;
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
    private int routedRequiredPower;
    private int routedDurationMinutes = 12;
    private long routedStartedAt;
    private long routedExpiresAt;
    private int currentPokemon;
    private int maximumPokemon = 1;
    protected ExpeditionBoardScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void cobblemonNoMansLands$initExploration(CallbackInfo callbackInfo) {
        explorationMode = ExplorationTabState.isExplorationMode();
        if (blockPos != null) {
            PacketDistributor.sendToServer(new ExplorationPartyStatusRequestPayload(blockPos));
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
        int rank = syncedState == null ? 0 : syncedState.getExpeditionRank();
        return rankRequirement(expedition.getExpeditionId(), rank);
    }

    @Redirect(method = "drawExpeditionTooltip", at = @At(value = "INVOKE",
            target = "Lcom/cobblemonexpeditions/network/s2c/SyncStatePayload$AvailableExpeditionInfo;getRequiredPower()I"))
    private int cobblemonNoMansLands$redirectExplorationTooltipPower(
            SyncStatePayload.AvailableExpeditionInfo expedition) {
        if (!explorationMode) {
            return expedition.getRequiredPower();
        }
        int rank = syncedState == null ? 0 : syncedState.getExpeditionRank();
        return rankRequirement(expedition.getExpeditionId(), rank);
    }

    @Redirect(method = "drawAvailableExpeditions", at = @At(value = "INVOKE",
            target = "Lcom/cobblemonexpeditions/gui/BoardPreviewMath;effectiveDurationSeconds(Lcom/cobblemonexpeditions/network/s2c/SyncStatePayload;Lcom/cobblemonexpeditions/network/s2c/SyncStatePayload$AvailableExpeditionInfo;)I"))
    private int cobblemonNoMansLands$redirectExplorationDuration(
            com.cobblemonexpeditions.gui.BoardPreviewMath math, SyncStatePayload state,
            SyncStatePayload.AvailableExpeditionInfo expedition) {
        if (!explorationMode) {
            return math.effectiveDurationSeconds(state, expedition);
        }
        int rank = syncedState == null ? 0 : syncedState.getExpeditionRank();
        return explorationDurationMinutes(expedition.getExpeditionId(), rank) * 60;
    }

    @Redirect(method = "drawExpeditionTooltip", at = @At(value = "INVOKE",
            target = "Lcom/cobblemonexpeditions/gui/BoardPreviewMath;effectiveDurationSeconds(Lcom/cobblemonexpeditions/network/s2c/SyncStatePayload;Lcom/cobblemonexpeditions/network/s2c/SyncStatePayload$AvailableExpeditionInfo;)I"))
    private int cobblemonNoMansLands$redirectExplorationTooltipDuration(
            com.cobblemonexpeditions.gui.BoardPreviewMath math, SyncStatePayload state,
            SyncStatePayload.AvailableExpeditionInfo expedition) {
        if (!explorationMode) {
            return math.effectiveDurationSeconds(state, expedition);
        }
        int rank = syncedState == null ? 0 : syncedState.getExpeditionRank();
        return explorationDurationMinutes(expedition.getExpeditionId(), rank) * 60;
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
        var partyStatus = ExplorationPartyClientState.peek(blockPos);
        if (partyStatus != null) {
            currentPokemon = partyStatus.currentPokemon();
            maximumPokemon = partyStatus.maximumPokemon();
            routedCurrentPower = partyStatus.currentPower();
        }
        List<SyncStatePayload.AvailableExpeditionInfo> expeditions = getAvailableExpeditions();
        int requiredPower = routedRequiredPower;
        if (selectedExpeditionIndex >= 0 && selectedExpeditionIndex < expeditions.size()) {
            requiredPower = routedRequiredPower > 0 ? routedRequiredPower
                : rankRequirement(expeditions.get(selectedExpeditionIndex).getExpeditionId());
        }
        int currentPower = routedCurrentPower;
        int textX = rect.getX() + 4;
        int textY = rect.getY() + 5;
        if (explorationSelected) {
            String teamText = "Team: " + currentPokemon + "/" + maximumPokemon;
            String powerText = "Pwr: " + currentPower + "/" + requiredPower;
            int powerColor = currentPower >= requiredPower ? 0xFF55FF55 : 0xFFFF5555;
            int teamColor = currentPokemon == 0 || currentPokemon > maximumPokemon ? 0xFFFF5555 : theme.getTextOnPanel();
            graphics.drawString(font, Component.literal(teamText), textX, textY, teamColor, false);
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
        ExplorationSelectionResultPayload result = ExplorationSelectionClientState.peek(blockPos);
        if (result != null && result != synchronizedResult) {
            synchronizedResult = result;
            explorationConfirmed = result.accepted();
            activeExplorationId = result.accepted() ? result.explorationId() : null;
            routedCurrentPower = result.currentPower();
            routedRequiredPower = result.requiredPower();
            routedDurationMinutes = result.durationMinutes();
            routedStartedAt = result.startedAt();
            routedExpiresAt = result.expiresAt();
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
                if (currentPokemon == 0) {
                    showError("Add pokemon to your party!");
                } else if (currentPokemon > maximumPokemon) {
                    showError("Max " + maximumPokemon + " pokemon!");
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
        String expeditionName = expeditions.get(selectedExpeditionIndex).getName();
        PacketDistributor.sendToServer(new ExplorationSelectionPayload(
                blockPos,
            ResourceLocation.fromNamespaceAndPath("cobblemon_expeditions", toIdPath(expeditionName))
        ));
        explorationConfirmed = false;
        activeExplorationId = null;
    }

    private boolean canConfirmExploration() {
        List<SyncStatePayload.AvailableExpeditionInfo> expeditions = getAvailableExpeditions();
        if (!explorationSelected || selectedExpeditionIndex < 0 || selectedExpeditionIndex >= expeditions.size()) {
            return false;
        }
        int rank = syncedState == null ? 0 : syncedState.getExpeditionRank();
        return currentPokemon > 0 && currentPokemon <= maximumPokemon
            && routedCurrentPower >= rankRequirement(expeditions.get(selectedExpeditionIndex).getExpeditionId(), rank);
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

    private int explorationDurationMinutes(ResourceLocation explorationId, int rank) {
        int minimum = switch (explorationId.getPath()) {
            case "forest_forage" -> 12;
            case "shoreline_survey" -> 14;
            case "berry_grove_harvest" -> 18;
            case "cave_delve" -> 20;
            case "deep_sea_dive" -> 28;
            case "volcanic_survey" -> 30;
            case "frozen_ruins" -> 36;
            default -> 40;
        };
        int maximum = switch (explorationId.getPath()) {
            case "forest_forage" -> 28;
            case "shoreline_survey" -> 32;
            case "berry_grove_harvest" -> 36;
            case "cave_delve" -> 40;
            case "deep_sea_dive" -> 46;
            case "volcanic_survey" -> 48;
            case "frozen_ruins" -> 52;
            default -> 56;
        };
        return (int) Math.round(minimum + (maximum - minimum) * Math.max(0, Math.min(10, rank)) / 10.0D);
    }

    private void cancelExploration() {
        if (blockPos != null) {
            PacketDistributor.sendToServer(new ExplorationCancelPayload(blockPos));
        }
        explorationSelected = false;
        explorationConfirmed = false;
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

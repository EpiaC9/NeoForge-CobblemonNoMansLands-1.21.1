package com.epiac9.cobblemonnomanslands.portal.expedition;

import com.cobblemonexpeditions.data.ExpeditionDefinition;
import com.epiac9.cobblemonnomanslands.expedition.manager.route.DungeonRouteResult;
import com.epiac9.cobblemonnomanslands.expedition.manager.route.ExpeditionRouteService;
import com.epiac9.cobblemonnomanslands.portal.anchor.PortalAnchorRegistry;
import com.epiac9.cobblemonnomanslands.portal.anchor.PortalAnchorState;
import com.epiac9.cobblemonnomanslands.portal.anchor.PortalService;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Set;

public class ExpeditionPortalCoordinator {
    private final ExpeditionRouteService routeService;
    private final PortalAnchorRegistry anchorRegistry;
    private final PortalService portalService;
    //call routes and portal services

    public ExpeditionPortalCoordinator(int lotCount, Set<String> validDungeon) {
        this.routeService = new ExpeditionRouteService(lotCount, validDungeon);
        this.anchorRegistry = new PortalAnchorRegistry();
        this.portalService = new PortalService();
    }

    public DungeonRouteResult routeAndActivate(ServerPlayer player, ExpeditionDefinition expedition, int selectedPokemonCount,
                                             BlockPos boardPosition, BlockState activePortalState) {
        if (player == null) {
            return rejected("Player was null");
        }
        if (expedition == null) {
            return rejected("Expedition was null");
        }
        if (boardPosition == null) {
            return rejected("Board position was null");
        }
        if (activePortalState == null) {
            return rejected("Active portal state was null");
        }
        ServerLevel level = player.serverLevel();
        PortalAnchorState anchor = anchorRegistry.find(level.dimension(),boardPosition);
        if (anchor == null) {
            return rejected("No active anchor was found");
        }
        DungeonRouteResult result = routeService.routeExpedition(player.getUUID(), expedition, selectedPokemonCount);
        if (!result.isAccepted()) {
            return result;
        }
        anchor.setInstanceId(result.getPendingInstanceId());
        portalService.activatePortal(level, anchor, activePortalState);
        return result;
    }

    public PortalAnchorRegistry getAnchorRegistry() {
        return anchorRegistry;
    }
    public PortalService getPortalService() {
        return portalService;
    }

    private DungeonRouteResult rejected(String reason) {
        return new DungeonRouteResult(false,null,null,reason,null,null);
    }
}

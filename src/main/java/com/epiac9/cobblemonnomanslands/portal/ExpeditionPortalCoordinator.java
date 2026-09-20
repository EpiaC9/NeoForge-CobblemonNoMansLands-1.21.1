package com.epiac9.cobblemonnomanslands.portal;

import com.cobblemonexpeditions.data.ExpeditionDefinition;
import com.epiac9.cobblemonnomanslands.expedition.manager.route.DungeonRouteResult;
import com.epiac9.cobblemonnomanslands.expedition.manager.route.ExpeditionRouteService;
import com.epiac9.cobblemonnomanslands.portal.anchor.PortalAnchorRegistry;
import com.epiac9.cobblemonnomanslands.portal.anchor.PortalAnchorState;
import com.epiac9.cobblemonnomanslands.portal.anchor.PortalService;
import com.epiac9.cobblemonnomanslands.structure.connection.RoomConnection;
import com.epiac9.cobblemonnomanslands.structure.connection.RoomConnectionRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Set;

public class ExpeditionPortalCoordinator {
    private final ExpeditionRouteService routeService;
    private final PortalAnchorRegistry anchorRegistry;
    private final PortalService portalService;
    private final RoomConnectionRegistry connectionRegistry;
    //call routes and portal services

    public ExpeditionPortalCoordinator(int lotCount, Set<String> validDungeon, RoomConnectionRegistry connectionRegistry) {
        this.routeService = new ExpeditionRouteService(lotCount, validDungeon);
        this.anchorRegistry = new PortalAnchorRegistry();
        this.portalService = new PortalService();
        this.connectionRegistry = connectionRegistry;

        if (connectionRegistry == null) {
            throw new IllegalArgumentException("Connection registry cannot be null");
        }
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
        RoomConnection roomConnection = connectionRegistry.findByBoard(player.serverLevel().dimension(), boardPosition);
        if (roomConnection == null) {
            return rejected("Room connection was null");
        }
        if (activePortalState == null) {
            return rejected("Active portal state was null");
        }
        if (!roomConnection.containsBoard(boardPosition)) {
            return rejected("Board does not exist");
        }
        ServerLevel level = player.serverLevel();
        PortalAnchorState anchor = roomConnection.findAvailablePortal();
        if (anchor == null) {
            return rejected("No available portal was found");
        }
        DungeonRouteResult result = routeService.routeExpedition(player.getUUID(), expedition, selectedPokemonCount);
        if (!result.isAccepted()) {
            return result;
        }

        anchor.setInstanceId(result.getPendingInstanceId());
        portalService.activatePortal(level, anchor, activePortalState);
        return result;
    }

    private DungeonRouteResult rejected(String reason) {
        return new DungeonRouteResult(false,null,null,reason,null,null);
    }
}

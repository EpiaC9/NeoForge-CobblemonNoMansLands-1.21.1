package com.epiac9.cobblemonnomanslands.structure;

import com.epiac9.cobblemonnomanslands.CobblemonNoMansLands;
import com.epiac9.cobblemonnomanslands.marker.base.BaseMarkerBlock;
import com.epiac9.cobblemonnomanslands.marker.base.BoardMarkerBlock;
import com.epiac9.cobblemonnomanslands.marker.base.PortalMarkerBlock;
import com.epiac9.cobblemonnomanslands.marker.room.RoomMarkerBlock;
import com.epiac9.cobblemonnomanslands.marker.room.StarterStructureMarker;
import com.epiac9.cobblemonnomanslands.portal.anchor.PortalAnchorState;
import com.epiac9.cobblemonnomanslands.structure.connection.RoomConnection;
import com.epiac9.cobblemonnomanslands.structure.connection.RoomConnectionRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import com.epiac9.cobblemonnomanslands.registry.ModBlocks;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import java.util.ArrayList;
import java.util.List;

public final class WorldSpawnStructureService {
    private static final String PLACEMENT_STATE_ID = "cobblemonnomanslands_world_spawn_structure";

    private WorldSpawnStructureService() {
    }

    private static final RoomConnectionRegistry CONNECTIONS = new RoomConnectionRegistry();

    public static void generateAtWorldSpawn(ServerLevel level) {
        if (level == null) {
            return;
        }
        if (level.dimension() != Level.OVERWORLD) {
            return;
        }
        WorldSpawnStructureState placementState = level.getDataStorage().computeIfAbsent(
                WorldSpawnStructureState.factory(), PLACEMENT_STATE_ID);
        if (placementState.isPlaced()) {
            RoomConnection restoredConnection = placementState.restoreConnection(level);
            if (restoredConnection != null) {
                CONNECTIONS.register(level.dimension(), restoredConnection);
            }
            return;
        }

        BlockPos spawn = level.getSharedSpawnPos();
        if (hasExistingStarterStructure(level, spawn)) {
            RoomConnection legacyConnection = findExistingConnection(level, spawn);
            if (legacyConnection != null) {
                CONNECTIONS.register(level.dimension(), legacyConnection);
                placementState.recordConnection(legacyConnection);
            }
            placementState.markPlaced();
            return;
        }
        BlockPos ground = findGroundSpawn(level, spawn);
        BlockPos structurePos = ground.above(1);
        ResourceLocation structureId = ResourceLocation.fromNamespaceAndPath(
                CobblemonNoMansLands.MODID,
                "overworld/starter_structure"
        );

        StructureTemplateManager templateManager = level.getStructureManager();
        StructureTemplate template = templateManager.getOrCreate(structureId);

        StructurePlaceSettings settings = new StructurePlaceSettings().setIgnoreEntities(true);
        template.placeInWorld(
                level,
                structurePos,
                structurePos,
                settings,
                level.getRandom(),
                Block.UPDATE_ALL
        );

        RoomConnection connection = resolveMarkers(level, structurePos, new BlockPos(template.getSize()));
        if (connection != null) {
            CONNECTIONS.register(level.dimension(), connection);
            placementState.recordConnection(connection);
            placementState.markPlaced();
        }
    }

    public static RoomConnection findConnection(ServerLevel level, BlockPos boardPosition) {
        if (level == null || boardPosition == null) {
            return null;
        }

        return CONNECTIONS.findByBoard(level.dimension(), boardPosition);
    }

    public static RoomConnectionRegistry getConnectionRegistry() {
        return CONNECTIONS;
    }

    public static RoomConnection resolveMarkers(ServerLevel level, BlockPos origin, BlockPos size) {
        if (level == null || origin == null || size == null) {
            return null;
        }

        List<BaseMarkerBlock.MarkerData> markers = new ArrayList<>();
        List<PendingMarker> pendingMarkers = new ArrayList<>();
        List<PortalAnchorState> portals = new ArrayList<>();
        StarterStructureMarker starterMarker = null;
        BlockPos starterMarkerPosition = null;
        BlockPos max = origin.offset(size.getX() - 1, size.getY() - 1, size.getZ() - 1);

        for (BlockPos pos : BlockPos.betweenClosed(origin, max)) {
            BlockState state = level.getBlockState(pos);
            if (state.getBlock() instanceof StarterStructureMarker roomMarker) {
                // Record the structure and its marker data.
                starterMarker =  roomMarker;
                starterMarkerPosition = pos.immutable();
                continue;
            }
            if (!(state.getBlock() instanceof BaseMarkerBlock markerBlock)) {
                //record non room markers
                continue;
            }
            pendingMarkers.add(new PendingMarker(pos.immutable(), markerBlock, state));
        }

        if (starterMarker == null) {
            return null;
        }

        for (PendingMarker pendingMarker : pendingMarkers) {
            BlockPos position = pendingMarker.position();
            BaseMarkerBlock markerBlock = pendingMarker.markerBlock();
            BlockState markerState = pendingMarker.state();
            markers.add(markerBlock.createMarkerData(position));
            if (markerBlock instanceof PortalMarkerBlock portalMarker) {
                PortalAnchorState anchor = portalMarker.placePortal(level, position);
                if (anchor != null) {
                    portals.add(anchor);
                }
            } else if (markerBlock instanceof BoardMarkerBlock boardMarker) {
                boardMarker.placeBoard(level, position, markerState);
            }
        }

        RoomMarkerBlock.RoomData roomData = starterMarker.recordStructure(starterMarkerPosition, origin, size, markers);
        starterMarker.removeMarker(level, starterMarkerPosition);
        return new RoomConnection(roomData, portals);
    }

    private record PendingMarker(BlockPos position, BaseMarkerBlock markerBlock, BlockState state) {
    }

    private static BlockPos findGroundSpawn(ServerLevel level, BlockPos spawn) {
        int x = spawn.getX();
        int z = spawn.getZ();

        for (int y = level.getMaxBuildHeight(); y >= level.getMinBuildHeight(); y--) {
            BlockPos pos = new BlockPos(x, y, z);
            BlockState state = level.getBlockState(pos);

            if (state.isAir()) {
                continue;
            }
            if (state.is(BlockTags.LEAVES) || state.is(BlockTags.FLOWERS)
                    || state.is(BlockTags.SAPLINGS) || state.is(BlockTags.REPLACEABLE)
                    || state.is(BlockTags.LOGS)) {
                continue;
            }
            return pos;
        }
        return new BlockPos(x, level.getSeaLevel(), z);
    }

    private static boolean hasExistingStarterStructure(ServerLevel level, BlockPos spawn) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        int minX = spawn.getX() - 32;
        int maxX = spawn.getX() + 32;
        int minY = Math.max(level.getMinBuildHeight(), spawn.getY() - 16);
        int maxY = Math.min(level.getMaxBuildHeight() - 1, spawn.getY() + 32);
        int minZ = spawn.getZ() - 32;
        int maxZ = spawn.getZ() + 32;

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    cursor.set(x, y, z);
                    if (level.getBlockState(cursor).is(ModBlocks.DUNGEON_PORTAL.get())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static RoomConnection findExistingConnection(ServerLevel level, BlockPos spawn) {
        List<BlockPos> boardPositions = new ArrayList<>();
        List<BlockPos> portalPositions = new ArrayList<>();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        ResourceLocation boardId = ResourceLocation.fromNamespaceAndPath(
                "cobblemon_expeditions", "copper_expedition_board");

        int minX = spawn.getX() - 32;
        int maxX = spawn.getX() + 32;
        int minY = Math.max(level.getMinBuildHeight(), spawn.getY() - 16);
        int maxY = Math.min(level.getMaxBuildHeight() - 1, spawn.getY() + 32);
        int minZ = spawn.getZ() - 32;
        int maxZ = spawn.getZ() + 32;

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    cursor.set(x, y, z);
                    BlockState state = level.getBlockState(cursor);
                    if (BuiltInRegistries.BLOCK.getKey(state.getBlock()).equals(boardId)) {
                        boardPositions.add(cursor.immutable());
                    } else if (state.is(ModBlocks.DUNGEON_PORTAL.get())
                            && state.getValue(com.epiac9.cobblemonnomanslands.portal.DungeonPortalBlock.CELL) == 4) {
                        portalPositions.add(cursor.immutable());
                    }
                }
            }
        }

        if (boardPositions.isEmpty() || portalPositions.isEmpty()) {
            return null;
        }

        List<BaseMarkerBlock.MarkerData> markers = boardPositions.stream()
                .map(position -> new BaseMarkerBlock.MarkerData(position, "board"))
                .toList();
        RoomMarkerBlock.RoomData roomData = new RoomMarkerBlock.RoomData(
                BlockPos.ZERO, BlockPos.ZERO, BlockPos.ZERO, markers);
        List<PortalAnchorState> portals = portalPositions.stream()
                .map(PortalAnchorState::new)
                .toList();
        return new RoomConnection(roomData, portals);
    }
}

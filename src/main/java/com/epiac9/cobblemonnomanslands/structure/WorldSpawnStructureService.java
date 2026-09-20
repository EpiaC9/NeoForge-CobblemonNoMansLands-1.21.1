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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
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

        BlockPos spawn = level.getSharedSpawnPos();
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
            markers.add(markerBlock.createMarkerData(pos));
            if (markerBlock instanceof PortalMarkerBlock portalMarker) {
                PortalAnchorState anchor = portalMarker.placePortal(level, pos);
                // Portal placement
                if (anchor != null) {
                    portals.add(anchor);
                }
            } else if (markerBlock instanceof BoardMarkerBlock boardMarker) {
                // Board replacement
                boardMarker.placeBoard(level, pos, state);
            }
        }

        if (starterMarker == null) {
            return null;
        }

        RoomMarkerBlock.RoomData roomData = starterMarker.recordStructure(starterMarkerPosition, origin, size, markers);
        starterMarker.removeMarker(level, starterMarkerPosition);
        return new RoomConnection(roomData, portals);
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
}

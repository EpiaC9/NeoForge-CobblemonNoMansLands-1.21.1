package com.epiac9.cobblemonnomanslands.marker.base;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

public class BoardMarkerBlock extends BaseMarkerBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    private static final ResourceLocation BOARD_ID = ResourceLocation.fromNamespaceAndPath(
            "cobblemon_expeditions",
            "copper_expedition_board"
    );

    public BoardMarkerBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, net.minecraft.core.Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public MarkerData createMarkerData(BlockPos pos) {
        return new MarkerData(pos, "board");
    }

    public boolean placeBoard(ServerLevel level, BlockPos pos, BlockState markerState) {
        if (level == null ||  pos == null || markerState == null) {
            return false;
        }

        Direction facing = markerState.getValue(FACING);
        Block boardBlock = BuiltInRegistries.BLOCK.getOptional(BOARD_ID).orElse(null);
        if (boardBlock == null) {
            return false;
        }

        BlockState boardState = boardBlock.defaultBlockState();
        if (boardState.hasProperty(HorizontalDirectionalBlock.FACING)) {
            boardState = boardState.setValue(HorizontalDirectionalBlock.FACING, facing);
        }

        return level.setBlock(pos, boardState, Block.UPDATE_ALL);
    }
}

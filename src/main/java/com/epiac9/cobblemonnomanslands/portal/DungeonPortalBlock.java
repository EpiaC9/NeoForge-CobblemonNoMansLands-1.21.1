package com.epiac9.cobblemonnomanslands.portal;

import com.epiac9.cobblemonnomanslands.portal.runtime.PortalRuntimeEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;

public class DungeonPortalBlock extends Block {
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
    public static final IntegerProperty CELL = IntegerProperty.create("cell", 0, 8);

    public DungeonPortalBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(ACTIVE, false).setValue(CELL, 4));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ACTIVE, CELL);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hitResult) {
        if (!state.getValue(ACTIVE) || level.isClientSide || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.PASS;
        }
        boolean entered = PortalRuntimeEvents.getRuntime().getExplorationSelectionService()
            .enterPortal(serverPlayer, (ServerLevel) level, pos);
        return entered ? InteractionResult.CONSUME : InteractionResult.PASS;
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (state.getValue(ACTIVE) && !level.isClientSide && entity instanceof ServerPlayer serverPlayer) {
            PortalRuntimeEvents.getRuntime().getExplorationSelectionService()
                .enterPortal(serverPlayer, (ServerLevel) level, pos);
        }
        super.entityInside(state, level, pos, entity);
    }
}

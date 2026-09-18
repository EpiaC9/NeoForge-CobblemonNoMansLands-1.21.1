package com.epiac9.cobblemonnomanslands.portal.anchor;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class PortalAnchorRegistry {
    private final Map<ResourceKey<Level>, Map<BlockPos, PortalAnchorState>> anchorsByDimension;
    public PortalAnchorRegistry() {
        this.anchorsByDimension = new HashMap<>();
    }

    public void register(ResourceKey<Level> dimension, BlockPos boardPosition, PortalAnchorState anchor) {
        if (dimension == null) {
            throw new IllegalArgumentException("Dimension cannot be null");
        }
        if (boardPosition == null) {
            throw new IllegalArgumentException("Pos cannot be null");
        }
        if (anchor == null) {
            throw new IllegalArgumentException("Anchor cannot be null");
        }
        anchorsByDimension.computeIfAbsent(dimension, ignored -> new HashMap<>()).put(boardPosition.immutable(), anchor);
    } //register dimension, portal position and anchor

    public PortalAnchorState find(ResourceKey<Level> dimension, BlockPos boardPosition) {
        Map<BlockPos, PortalAnchorState> anchors = anchorsByDimension.get(dimension);
        if (anchors == null) {
            return null;
        }
        return anchors.get(boardPosition);
    } //locate anchor

    public boolean unregister(ResourceKey<Level> dimension, BlockPos boardPosition) {
        Map<BlockPos, PortalAnchorState> anchors = anchorsByDimension.get(dimension);
        if (anchors == null) {
            return false;
        }
        PortalAnchorState removed = anchors.remove(boardPosition);
        if (anchors.isEmpty()) {
            anchorsByDimension.remove(dimension);
        }
        return removed != null;
    } //unregister anchor
}

package com.epiac9.cobblemonnomanslands.portal.runtime;

import com.epiac9.cobblemonnomanslands.dimension.generation.DungeonGenerationQueue;
import com.epiac9.cobblemonnomanslands.portal.ExpeditionPortalCoordinator;
import com.epiac9.cobblemonnomanslands.structure.WorldSpawnStructureService;
import com.epiac9.cobblemonnomanslands.structure.connection.RoomConnectionRegistry;

import java.util.Set;

public class PortalSystemRuntime {
    private final ExpeditionPortalCoordinator coordinator;
    private final DungeonGenerationQueue generationQueue;

    public PortalSystemRuntime() {
        Set<String> validDungeon = Set.of(
                "nml_berry_grove",
                "nml_forest",
                "nml_shoreline",
                "nml_cavern",
                "nml_volcanic",
                "nml_deep_sea",
                "nml_frozen_ruins",
                "nml_distortion_rift"
        );
        RoomConnectionRegistry connectionRegistry = WorldSpawnStructureService.getConnectionRegistry();
        this.coordinator = new ExpeditionPortalCoordinator(4, validDungeon, connectionRegistry);
        this.generationQueue = new DungeonGenerationQueue();
    } //assign profile ids to portal

    public ExpeditionPortalCoordinator getCoordinator() {
        return coordinator;
    }
    public DungeonGenerationQueue getGenerationQueue() {
        return generationQueue;
    }
}

package com.epiac9.cobblemonnomanslands.portal.expedition;

import java.util.Set;

public class PortalSystemRuntime {
    private final ExpeditionPortalCoordinator coordinator;

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
        this.coordinator = new ExpeditionPortalCoordinator(4, validDungeon);
    } //assign profile ids to portal

    public ExpeditionPortalCoordinator getCoordinator() {
        return coordinator;
    }
}

package com.epiac9.cobblemonnomanslands.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class ModNetwork {
    private ModNetwork() {
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(
                ExplorationSelectionPayload.TYPE,
                ExplorationSelectionPayload.STREAM_CODEC,
                ExplorationSelectionPayload::handle
        );
        registrar.playToClient(
            ExplorationSelectionResultPayload.TYPE,
            ExplorationSelectionResultPayload.STREAM_CODEC,
            ExplorationSelectionResultPayload::handle
        );
    }
}
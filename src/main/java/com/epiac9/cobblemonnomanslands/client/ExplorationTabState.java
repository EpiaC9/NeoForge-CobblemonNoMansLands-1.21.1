package com.epiac9.cobblemonnomanslands.client;

public final class ExplorationTabState {
    private static boolean explorationMode;

    private ExplorationTabState() {
    }

    public static boolean isExplorationMode() {
        return explorationMode;
    }
    public static void setExplorationMode(boolean value) {
        explorationMode = value;
    }
}
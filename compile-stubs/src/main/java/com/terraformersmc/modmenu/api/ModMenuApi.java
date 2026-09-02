package com.terraformersmc.modmenu.api;

import java.util.Map;
import java.util.Collections;

public interface ModMenuApi {
    default ConfigScreenFactory<?> getModConfigScreenFactory() {
        return null;
    }

    default Map<String, ConfigScreenFactory<?>> getProvidedConfigScreenFactories() {
        return Collections.emptyMap();
    }
}

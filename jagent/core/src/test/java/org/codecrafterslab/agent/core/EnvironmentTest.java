package org.codecrafterslab.agent.core;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.net.URLClassLoader;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

class EnvironmentTest {

    @Test
    void legacyConstructorKeepsPluginClassLoaderNull() {
        Instrumentation inst = mock(Instrumentation.class);
        File jar = new File("/tmp/JAgent.jar");
        Environment env = new Environment(inst, jar, false);
        assertNull(env.getPluginClassLoader(),
            "legacy constructor must default pluginClassLoader to null");
    }

    @Test
    void newConstructorExposesPluginClassLoader() {
        Instrumentation inst = mock(Instrumentation.class);
        File jar = new File("/tmp/JAgent.jar");
        URLClassLoader cl = new URLClassLoader(new java.net.URL[0]);
        Environment env = new Environment(inst, jar, "myapp", false, cl);
        assertNotNull(env.getPluginClassLoader(),
            "new constructor must expose pluginClassLoader");
    }
}

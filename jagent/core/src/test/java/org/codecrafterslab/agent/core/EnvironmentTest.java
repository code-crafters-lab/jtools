package org.codecrafterslab.agent.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.net.URLClassLoader;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

@DisplayName("Environment 运行时环境测试")
class EnvironmentTest {

    @Test
    @DisplayName("旧构造函数中 agentClassLoader 默认为 null")
    void legacyConstructorKeepsPluginClassLoaderNull() {
        Instrumentation inst = mock(Instrumentation.class);
        File jar = new File("/tmp/JAgent.jar");
        Environment env = new Environment(inst, jar, false);
        assertNull(env.getAgentClassLoader(),
            "legacy constructor must default pluginClassLoader to null");
    }

    @Test
    @DisplayName("新构造函数正确暴露 agentClassLoader")
    void newConstructorExposesPluginClassLoader() {
        Instrumentation inst = mock(Instrumentation.class);
        File jar = new File("/tmp/JAgent.jar");
        URLClassLoader cl = new URLClassLoader(new java.net.URL[0]);
        Environment env = new Environment(inst, jar, "myapp", false, cl);
        assertNotNull(env.getAgentClassLoader(),
            "new constructor must expose pluginClassLoader");
    }
}

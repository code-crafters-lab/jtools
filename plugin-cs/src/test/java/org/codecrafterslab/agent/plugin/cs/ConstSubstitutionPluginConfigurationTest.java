package org.codecrafterslab.agent.plugin.cs;

import org.codecrafterslab.agent.utils.ConfigLoader;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.Opcodes;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class ConstSubstitutionPluginConfigurationTest {

    @Test
    void testCSPluginConfiguration() {
        Path path = Paths.get("plugin-cs.toml").toAbsolutePath();
        ConstSubstitutionPluginConfiguration config = ConfigLoader.load(path,
                ConstSubstitutionPluginConfiguration.class);
        assertNotNull(config);
        assertFalse(config.isDisabled());
        assertEquals(1, config.rules.size());
        assertEquals(Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC, config.rules.get(0).getMethodInfo().getAccess());
    }
}

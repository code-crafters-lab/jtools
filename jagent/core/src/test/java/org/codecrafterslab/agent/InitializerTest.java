package org.codecrafterslab.agent;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InitializerTest {

    @Test
    void readsBootstrapPriorityFromManifest(@TempDir Path tmp) throws Exception {
        File jar = new File(tmp.toFile(), "p-0.1.0.bootstrap.jar");
        Manifest mf = new Manifest();
        mf.getMainAttributes().putValue(Attributes.Name.MANIFEST_VERSION.toString(), "1.0");
        mf.getMainAttributes().putValue("Plugin-Bootstrap-Priority", "42");
        try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(jar), mf)) {
            jos.putNextEntry(new java.util.jar.JarEntry("META-INF/"));
            jos.closeEntry();
        }
        int p = invokeReadBootstrapPriority(jar);
        assertEquals(42, p);
    }

    @Test
    void readsBootstrapPriorityDefaultsToMaxWhenAbsent(@TempDir Path tmp) throws Exception {
        File jar = new File(tmp.toFile(), "p-0.1.0.bootstrap.jar");
        Manifest mf = new Manifest();
        try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(jar), mf)) {
            jos.putNextEntry(new java.util.jar.JarEntry("META-INF/"));
            jos.closeEntry();
        }
        int p = invokeReadBootstrapPriority(jar);
        assertEquals(Integer.MAX_VALUE, p);
    }

    private static int invokeReadBootstrapPriority(File jar) throws Exception {
        java.lang.reflect.Method m = Initializer.class
            .getDeclaredMethod("readBootstrapPriority", File.class);
        m.setAccessible(true);
        return (int) m.invoke(null, jar);
    }
}

package org.codecrafterslab.agent.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AgentUtilTest {

    @Test
    void getAgentJarFile() {
    }

    @Test
    void init() {
    }

    @Test
    void getAgentClass() {
        String canonicalName = AgentUtilTest.class.getCanonicalName();
        String name = AgentUtilTest.class.getName();
        assertEquals("org.codecrafterslab.agent.utils.AgentUtilTest", canonicalName);
    }
}

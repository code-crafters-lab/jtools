package org.codecrafterslab.agent.utils;

import lombok.extern.slf4j.Slf4j;
import org.codecrafterslab.agent.Agent;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.util.Textifier;

@Slf4j
class MethodDescTest {

    @Test
    public void Fine8() throws Exception {
//        log.warn("====================== ASMifier ======================");
//        ASMifier.main(new String[]{Agent.class.getCanonicalName()});
        log.warn("====================== Textifier ======================");
        Textifier.main(new String[]{Agent.class.getCanonicalName()});
    }

}

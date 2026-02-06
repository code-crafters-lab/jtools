package org.codecrafterslab.agent.api;


import java.lang.instrument.ClassFileTransformer;
import java.util.Collection;

public interface AgentDispatcher<T> extends ClassFileTransformer {
    void addTransformer(T transformer);

    default void addTransformers(Collection<T> transformers) {
        if (null == transformers) return;
        for (T transformer : transformers) {
            addTransformer(transformer);
        }
    }

    default void exportClazzToFile(String dir, String className, String suffix, byte[] data) {

    }
}

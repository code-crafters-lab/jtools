import org.codecrafterslab.agent.core.Environment;
import org.codecrafterslab.agent.core.Transformer;
import org.codecrafterslab.agent.core.plugin.PluginConfig;
import org.codecrafterslab.agent.core.plugin.PluginEntry;

import java.util.ArrayList;
import java.util.List;

public class ModulusPlugin implements PluginEntry {
    private final List<Transformer> transformers = new ArrayList<>();

    @Override
    public void init(Environment environment, PluginConfig config) {
        transformers.add(new ModulusTransformer());
    }

    @Override
    public String getName() {
        return "modulus";
    }

    @Override
    public String getAuthor() {
        return "coffee377";
    }

    @Override
    public String getVersion() {
        return PluginEntry.super.getVersion();
    }

    @Override
    public List<Transformer> getTransformers() {
        return transformers;
    }
}

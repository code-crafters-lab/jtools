package org.codecrafterslab.agent.plugin.timing;

import com.google.auto.service.AutoService;
import org.codecrafterslab.agent.api.ITransformer;
import org.codecrafterslab.agent.api.Plugin;
import org.codecrafterslab.agent.core.plugin.BasePlugin;

import java.util.List;

@AutoService(Plugin.class)
public class MethodTrackingPlugin extends BasePlugin {

    @Override
    public List<ITransformer> getTransformers() {
        return null;
    }

}

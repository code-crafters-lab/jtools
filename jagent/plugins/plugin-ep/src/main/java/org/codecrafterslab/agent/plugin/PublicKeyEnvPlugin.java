package org.codecrafterslab.agent.plugin;

import com.google.auto.service.AutoService;
import lombok.extern.slf4j.Slf4j;
import org.codecrafterslab.agent.api.ITransformer;
import org.codecrafterslab.agent.api.Plugin;
import org.codecrafterslab.agent.core.plugin.BasePlugin;

import java.util.Collections;
import java.util.List;

@Slf4j
@AutoService(Plugin.class)
public class PublicKeyEnvPlugin extends BasePlugin {

    @Override
    public List<ITransformer> getTransformers() {
        return Collections.singletonList(new RSAPublicKeyTransformer());
    }

}

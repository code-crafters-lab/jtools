package org.codecrafterslab.agent.plugin.cs;

import com.google.auto.service.AutoService;
import com.moandjiezana.toml.Toml;
import org.codecrafterslab.agent.api.AppContext;
import org.codecrafterslab.agent.api.Plugin;
import org.codecrafterslab.agent.core.Environment;
import org.codecrafterslab.agent.api.ITransformer;
import org.codecrafterslab.agent.core.plugin.BasePlugin;
import org.codecrafterslab.agent.core.plugin.PluginConfig;
import org.codecrafterslab.agent.core.plugin.PluginEntry;
import org.objectweb.asm.Opcodes;

import java.util.*;

@AutoService(Plugin.class)
public class ConstSubstitutionPlugin extends BasePlugin implements Plugin, PluginEntry {
    // 原始待替换的两个字符串（复制自目标类）
    public static String ORIGINAL_STR1 = "tgoYDy+InG+V+F4gU9ssbjuTHXzHaXwFzyF+SA85fe4AeN1N5jzxA2MzXT8VsArKZ9Ugz2rYPp9kOhpwiSq2QSfxE+axl8403O9JkcB9826e7Co3WjZYOMbfKWLrRJFTWatkEIRJvP2ocOEtYCLDOaET08OCAnSFAcO7fReSd00=";
    public static String ORIGINAL_STR2 = "+US+f7+816LsJEa/Gd0daNSWKehDM7uF72OpCHfSECbjX9WlrrOxIq8kOgIVxkDfJeWmP6OwZw9Xn+VJa8Sxze7jKUopRH1awfK1p+RiQnOcmqUpi4GTUHK+6F9nSc/Y0T5H7pgDNSk8CkB/LwfaaCj0FmrEXns8fguG2l3VrCU=";

    // 自定义替换后的两个值A和B（请替换为你的合法Base64字符串）
    public static String CUSTOM_STR_A = "AKr5BPhLiCKb3Rc0ZUVFMpSQUYX8CVac2akqS+C24k9eHgLmcTUcmWsuGrPbaAE6uxkEw7LPFJb4EtF0/YdWG7MsJZVOzC7fQ44+nt2L1SOwan5ZFLXlychnLi6VWMdB8d20Trcrq483JtTpWj+Af3rdnEecxijKK6PKDQVXKJPUKg31pHUQBeVUoLJaUpDJtJAyHXp8bY0OUMGp8GCnoF8UPOkCHLtbsx8VrMezfNFoWzoad3Dvg85ebUDJN0qsnmv7V9p+BgiOcuUzVdJ3Xnnv9PVsjm9bm5dWu//NcdrdErIMSpMqZWwaO3KppEokYni5BEvM69jfY5//XRCAYW0="; // 你的第一个自定义值
    public static String CUSTOM_STR_B = "AJdDILyIft6d5cnu8khv06/SU4TYJhcfs090NvgchAySu0F3MvVA0ZxeIsSeun978CJfxY9DBlUlb3ReQzHlTiYD4SwoXtQ15wXEfR56sKNk1nrfmZ+nwej9G+n4ZIKGLwG9ikCiLqbgifYVWW0tm2Euxt81c9CDmMxCygSezpQOQNoP6zRWd+KlT6P5TQ7AVGqSgfr1qLQpQ6xjkw+s0UnHE5a4jWQim9E6k5HV/0P5X9yIv+vL3dh97hSTEpacyMzqD3o47Y9mvXjg+rE5XV7Zj0xQ2YVsgX0aqrsiiwSGUL2Mu42p7j0CBnVaSAuWNgh1v34OGqNMNdpdBfZGT8U="; // 你的第二个自定义值

    private final List<ITransformer> transformers = new ArrayList<>();

    @Override
    public void init(Environment environment, PluginConfig config) {
        this.getRules(config).stream().map(ConstSubstitutionTransformer::new).forEach(transformers::add);
    }

    @Override
    public void init(AppContext appContext, Toml pluginConfig) {
        this.getRules(pluginConfig).stream().map(ConstSubstitutionTransformer::new).forEach(transformers::add);
    }

    private List<ConstSubstitutionRule<String>> getRules(Object config) {
        // TODO: 2026/02/07 00:33 从配置获取 rule
        ConstSubstitutionRule<String> rule = new ConstSubstitutionRule<>();
        rule.setClassName("com.grapecity.documents.excel.internals.aX.a");
        rule.setMethodSupport((access, name, desc, signature, exceptions) ->
                access == (Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC)
                        && "a".equals(name)
                        && "()Ljava/security/PublicKey;".equals(desc));
        rule.setConstantMap(new HashMap<String, String>() {{
            put(ORIGINAL_STR1, CUSTOM_STR_A);
            put(ORIGINAL_STR2, CUSTOM_STR_B);
        }});
        return Collections.singletonList(rule);
    }

    @Override
    public List<ITransformer> getTransformers() {
        return transformers;
    }

}

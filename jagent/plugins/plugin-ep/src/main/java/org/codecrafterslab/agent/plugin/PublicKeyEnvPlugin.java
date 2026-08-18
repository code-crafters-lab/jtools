package org.codecrafterslab.agent.plugin;

import com.google.auto.service.AutoService;
import lombok.extern.slf4j.Slf4j;
import org.codecrafterslab.agent.api.ITransformer;
import org.codecrafterslab.agent.api.Plugin;

import java.util.Collections;
import java.util.List;

/**
 * RSA 公钥环境变量插件
 *
 * <p>通过 Java Agent 的字节码修改能力，在运行时拦截 {@code RSAPublicKeySpec} 的构造过程，
 * 将构造参数（模数 modulus、指数 exponent）替换为预设的环境变量值。
 *
 * <p>核心机制：
 * <ul>
 *   <li>使用 ASM 框架修改 {@code java.security.spec.RSAPublicKeySpec} 类的字节码</li>
 *   <li>在三参数构造方法中注入 {@code ArgsFilter.match()} 调用</li>
 *   <li>当公钥参数匹配预设规则时，自动替换为环境变量中的值</li>
 * </ul>
 *
 * <p>使用场景：在受控环境中动态替换 RSA 公钥参数，用于密钥管理或环境适配
 *
 * @author Wu Yujie
 * @since 1.0.0
 * @see RSAPublicKeyTransformer
 * @see ArgsFilter
 */
@AutoService(Plugin.class)
public class PublicKeyEnvPlugin extends BasePlugin {

    /**
     * 获取本插件注册的字节码转换器列表
     *
     * <p>本插件仅注册一个转换器，负责修改 RSAPublicKeySpec 类的构造方法
     *
     * @return 包含单个 {@link RSAPublicKeyTransformer} 的不可变列表
     */
    @Override
    public List<ITransformer> getTransformers() {
        return Collections.singletonList(new RSAPublicKeyTransformer());
    }

}

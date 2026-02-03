//import com.janetfilter.core.Environment;
//import com.janetfilter.core.plugin.MyTransformer;
//import com.janetfilter.core.plugin.PluginConfig;
//import com.janetfilter.core.plugin.PluginEntry;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class GrapeCityPlugin implements PluginEntry {
//    private final List<MyTransformer> transformers = new ArrayList<>();
//
//    @Override
//    public void init(Environment environment, PluginConfig config) {
//        transformers.add(new GrapeCityTransformer());
//    }
//
//    @Override
//    public String getName() {
//        return "GrapeCityPublicKey";
//    }
//
//    @Override
//    public String getAuthor() {
//        return "coffee377";
//    }
//
//    @Override
//    public String getVersion() {
//        return PluginEntry.super.getVersion();
//    }
//
//    @Override
//    public List<MyTransformer> getTransformers() {
//        return transformers;
//    }
//}

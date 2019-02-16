package test;

import light.rpc.conf.RawConfig;
import light.rpc.util.json.JacksonHelper;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.junit.Assert;
import org.junit.Ignore;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by boying on 2018/11/19.
 */
@Ignore
public class YamlTest {
    @org.junit.Test
    public void test() throws URISyntaxException, IOException {
        Yaml yaml = new Yaml(new Constructor(RawConfig.class));
        String path = "ConfigureNoZoo_gai.yaml";
        Path p;
        if (path.startsWith("/")) {
            p = FileSystems.getDefault().getPath(path);
        } else {
            p = Paths.get(YamlTest.class.getClassLoader().getResource(path).toURI());
        }
        String yamlStr = new String(Files.readAllBytes(p));
        RawConfig configYaml = yaml.load(yamlStr);

        path = "ConfigureNoZoo_gai.json";
        if (path.startsWith("/")) {
            p = FileSystems.getDefault().getPath(path);
        } else {
            p = Paths.get(YamlTest.class.getClassLoader().getResource(path).toURI());
        }
        String yamlJson = new String(Files.readAllBytes(p));
        RawConfig configJson = JacksonHelper.getMapper().readValue(yamlJson, RawConfig.class);

        String s1 = JacksonHelper.getMapper().writeValueAsString(configJson);
        System.out.println(s1);

        String s2 = JacksonHelper.getMapper().writeValueAsString(configYaml);
        System.out.println(s2);

        Assert.assertTrue(EqualsBuilder.reflectionEquals(configJson, configYaml));


    }
}

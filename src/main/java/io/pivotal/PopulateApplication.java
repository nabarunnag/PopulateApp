package io.pivotal;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Properties;
import java.util.stream.IntStream;

import org.apache.geode.cache.Region;
import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.client.ClientCacheFactory;
import org.apache.geode.cache.client.ClientRegionShortcut;
import org.apache.geode.pdx.ReflectionBasedAutoSerializer;

public class PopulateApplication {
  public static void main(String[] args) {
    Properties properties = new Properties();
    String statsFile = new File("home/vcap/logs/stats.gfs").getAbsolutePath();
    properties.setProperty("enable-time-statistics", "true");
    properties.setProperty("log-level", "config");
    properties.setProperty("statistic-sampling-enabled", "true");
    properties.setProperty("member-timeout", "8000");
//    properties.setProperty("log-file", "home/vcap/logs/client.log");
    properties.setProperty("security-client-auth-init", "io.pivotal.ClientAuthInitialize.create");

    ClientCacheFactory ccf = new ClientCacheFactory(properties);
    ccf.setPdxSerializer(new ReflectionBasedAutoSerializer("benchmark.geode.data.*"));
    ccf.set("statistic-archive-file", statsFile);
    try {
      List<URI> locatorList = EnvParser.getInstance().getLocators();
      for (URI locator : locatorList) {
        ccf.addPoolLocator(locator.getHost(), locator.getPort());
      }
      ClientCache clientCache = ccf.create();
      Region
          region =
          clientCache.createClientRegionFactory(ClientRegionShortcut.PROXY).create("region");
      IntStream.range(1, 1000000).parallel()
          .forEach(i -> region.put(i, new benchmark.geode.data.Portfolio(i)));
    }catch (IOException | URISyntaxException e){
      throw new RuntimeException("Could not deploy application", e);
    }
    try {
      Thread.sleep(Long.MAX_VALUE);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }


  }
}

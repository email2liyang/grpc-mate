package io.datanerd.es.guice;

import com.google.common.io.Resources;
import com.google.inject.Guice;
import com.google.inject.Injector;

import org.apache.commons.configuration2.Configuration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

public class ElasticSearchModuleTest {

  private static Logger log = LoggerFactory.getLogger(ElasticSearchModuleTest.class); //NOPMD

  @Before
  public void setUp() throws Exception {
    String path = Resources.getResource("test.properties").getFile();
    System.setProperty(Constants.CONFIG_PROP_FILE_PATH,path);
  }

  @After
  public void tearDown() throws Exception {
    System.clearProperty(Constants.CONFIG_PROP_FILE_PATH);
  }

  @Test
  public void testConfigure() throws Exception {
    Injector injector  = Guice.createInjector(new ElasticSearchModule());
    Configuration configuration = injector.getInstance(Configuration.class);

    assertThat(configuration.getString("key1")).isEqualTo("value1");
  }
}
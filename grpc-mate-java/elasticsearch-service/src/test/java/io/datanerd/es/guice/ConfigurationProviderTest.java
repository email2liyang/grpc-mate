package io.datanerd.es.guice;

import com.google.common.io.Resources;

import org.apache.commons.configuration2.Configuration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class ConfigurationProviderTest {

  private ConfigurationProvider configurationProvider;
  private String path;

  @Before
  public void setUp() throws Exception {
    path = Resources.getResource("test.properties").getFile();
    System.setProperty(Constants.CONFIG_PROP_FILE_PATH, path);
    configurationProvider = new ConfigurationProvider();
  }

  @After
  public void tearDown() throws Exception {
    System.clearProperty(Constants.CONFIG_PROP_FILE_PATH);
  }

  @Test
  public void get() throws Exception {
    Configuration configuration = configurationProvider.get();
    assertThat(configuration.getString("key1")).isEqualTo("value1");
  }

  @Test(expected = IllegalStateException.class)
  public void properties_file_not_found() throws Exception {
    System.setProperty(Constants.CONFIG_PROP_FILE_PATH, "xxx");
    configurationProvider = new ConfigurationProvider();
    configurationProvider.get();
    fail("should meet IllegalStateException");
  }

  @Test
  public void getPropertyFilePath() throws Exception {
    assertThat(configurationProvider.getPropertyFilePath()).isEqualTo(path);
  }

  @Test
  public void setPropertyFilePath() throws Exception {
  }

}
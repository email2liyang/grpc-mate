/*
 * Copyright (c) 2015 Practice Insight Pty Ltd.
 */

package io.datanerd.es.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

import org.apache.commons.configuration2.Configuration;

/**
 * Main Guice Config Module
 * @author email2liyang@gmail.com
 */
public class ElasticSearchModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(Configuration.class).toProvider(ConfigurationProvider.class).in(Singleton.class);
  }
}

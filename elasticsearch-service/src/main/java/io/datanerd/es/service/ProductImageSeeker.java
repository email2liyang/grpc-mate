/*
 * Copyright (c) 2017 Practice Insight Pty Ltd.
 */

package io.datanerd.es.service;

import com.google.inject.Singleton;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * @author ivan.li@practiceinsight.io
 */
@Singleton
public class ProductImageSeeker {

  public InputStream seekProductImage(long productId) {
    return new ByteArrayInputStream("I'm fake".getBytes());
  }
}

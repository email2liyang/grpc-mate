package io.datanerd.es.dao;

import com.google.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datanerd.generated.common.Product;

@Singleton
public class ProductDao {

  private static Logger log = LoggerFactory.getLogger(ProductDao.class); //NOPMD

  public void upsertProduct(Product product) {
    //TODO(IL)
    log.info("save product into ES");
  }
}

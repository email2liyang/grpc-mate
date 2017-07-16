package io.datanerd.es.dao;

import com.google.inject.Guice;
import com.google.inject.Injector;

import com.github.javafaker.Faker;

import org.junit.Before;
import org.junit.Test;

import io.datanerd.generated.common.Product;
import io.datanerd.generated.common.ProductStatus;

public class ProductDaoTest {

  private Faker faker;
  private ProductDao productDao;
  private Injector injector;

  @Before
  public void setUp() throws Exception {
    faker = new Faker();
    injector = Guice.createInjector();
    productDao = injector.getInstance(ProductDao.class);
  }

  @Test
  public void upsertProduct() throws Exception {
    Product product = Product.newBuilder()
        .setProductId(faker.number().randomNumber())
        .setProductName(faker.company().name())
        .setProductPrice(faker.number().randomDouble(2, 10, 100))
        .setProductStatus(ProductStatus.InStock)
        .build();
    productDao.upsertProduct(product);
  }
}
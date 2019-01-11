package org.molgenis.dataexplorer.negotiator.config;

import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.meta.AbstractSystemEntityTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

// NOTE: when the following exception occurs;
// java.lang.ArrayStoreException: sun.reflect.annotation.TypeNotPresentExceptionProxy,
// this means you are missing this dependency:
// <dependency>
//  <groupId>org.springframework.security</groupId>
//  <artifactId>spring-security-test</artifactId>
//  <scope>test</scope>
// </dependency>

@ContextConfiguration(
    classes = {
      EntityBaseTestConfig.class,
      NegotiatorConfigMetadata.class,
      NegotiatorConfigFactory.class,
      NegotiatorPackage.class
    })
public class NegotiatorConfigTest extends AbstractSystemEntityTest {

  @Autowired NegotiatorConfigMetadata metadata;
  @Autowired NegotiatorConfigFactory factory;

  @Test
  public void testSystemEntity() {
    internalTestAttributes(
        metadata, NegotiatorConfig.class, factory, getOverriddenReturnTypes(), getExcludedAttrs());
  }
}

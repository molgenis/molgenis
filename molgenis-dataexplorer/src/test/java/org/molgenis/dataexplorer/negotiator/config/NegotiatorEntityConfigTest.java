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
      NegotiatorEntityConfigMetadata.class,
      NegotiatorEntityConfigFactory.class,
      NegotiatorConfigMetadata.class,
      NegotiatorPackage.class
    })
public class NegotiatorEntityConfigTest extends AbstractSystemEntityTest {

  @Autowired NegotiatorEntityConfigMetadata metadata;
  @Autowired NegotiatorEntityConfigFactory factory;

  @Test
  public void testSystemEntity() {
    internalTestAttributes(
        metadata,
        NegotiatorEntityConfig.class,
        factory,
        getOverriddenReturnTypes(),
        getExcludedAttrs());
  }
}

package org.molgenis.core.ui.settings;

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
    classes = {EntityBaseTestConfig.class, StaticContentMetadata.class, StaticContentFactory.class})
public class StaticContentTest extends AbstractSystemEntityTest {

  @Autowired StaticContentMetadata metadata;
  @Autowired StaticContentFactory factory;

  @Test
  public void testSystemEntity() {
    internalTestAttributes(
        metadata,
        StaticContentMetadata.class,
        factory,
        getOverriddenReturnTypes(),
        getExcludedAttrs(),
        true);
  }
}

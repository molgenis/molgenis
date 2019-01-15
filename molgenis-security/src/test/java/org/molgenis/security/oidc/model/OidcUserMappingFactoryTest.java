package org.molgenis.security.oidc.model;

import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.meta.AbstractEntityFactoryTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(
    classes = {
      EntityBaseTestConfig.class,
      OidcUserMappingMetadata.class,
      OidcUserMappingFactory.class,
      OidcTestConfig.class,
      OidcPackage.class
    })
public class OidcUserMappingFactoryTest extends AbstractEntityFactoryTest {

  @Autowired OidcUserMappingFactory factory;

  @Override
  @Test
  public void testCreate() {
    super.testCreate(factory, OidcUserMapping.class);
  }

  @Override
  @Test
  public void testCreateWithId() {
    super.testCreateWithId(factory, OidcUserMapping.class);
  }

  @Override
  @Test
  public void testCreateWithEntity() {
    super.testCreateWithEntity(factory, OidcUserMapping.class);
  }
}

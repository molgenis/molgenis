package org.molgenis.security.oidc.model;

import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.meta.AbstractEntityFactoryTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(
    classes = {
      EntityBaseTestConfig.class,
      OidcClientMetadata.class,
      OidcClientFactory.class,
      OidcTestConfig.class,
      OidcPackage.class
    })
public class OidcClientFactoryTest extends AbstractEntityFactoryTest {

  @Autowired OidcClientFactory factory;

  @Override
  @Test
  public void testCreate() {
    super.testCreate(factory, OidcClient.class);
  }

  @Override
  @Test
  public void testCreateWithId() {
    super.testCreateWithId(factory, OidcClient.class);
  }

  @Override
  @Test
  public void testCreateWithEntity() {
    super.testCreateWithEntity(factory, OidcClient.class);
  }
}

package org.molgenis.security.oidc.model;

import java.util.HashMap;
import java.util.Map;
import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.meta.AbstractSystemEntityTest;
import org.molgenis.util.Pair;
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
public class OidcClientTest extends AbstractSystemEntityTest {

  @Autowired OidcClientMetadata metadata;
  @Autowired OidcClientFactory factory;

  @Override
  protected Map<String, Pair<Class, Object>> getOverriddenReturnTypes() {
    Pair<Class, Object> scopesPair = new Pair<>();
    scopesPair.setA(String[].class);
    scopesPair.setB(new String[] {"a", "b"});

    Map<String, Pair<Class, Object>> map = new HashMap<>();
    map.put(OidcClientMetadata.SCOPES, scopesPair);
    return map;
  }

  @Test
  public void testSystemEntity() {
    internalTestAttributes(
        metadata, OidcClient.class, factory, getOverriddenReturnTypes(), getExcludedAttrs());
  }
}

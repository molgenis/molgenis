package org.molgenis.data.security.auth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.meta.AbstractSystemEntityTest;
import org.molgenis.data.security.config.GroupTestConfig;
import org.molgenis.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(
    classes = {
      EntityBaseTestConfig.class,
      GroupMetadata.class,
      GroupFactory.class,
      SecurityPackage.class,
      GroupTestConfig.class
    })
public class GroupMetadataTest extends AbstractSystemEntityTest {

  @Autowired GroupMetadata metadata;
  @Autowired GroupFactory factory;

  private Map<String, Pair<Class, Object>> getOverriddenReturnTypes() {
    // Add attributes with 'smart' getters and setters that convert back and forth to correct values
    // for MOLGENIS datatypes
    // Provide the attribute name as key, and a pair of return type (Class) and a Object to be used
    // as test value
    return new HashMap<>();
  }

  private List<String> getExcludedAttrs() {
    return new ArrayList<>();
  }

  @Test
  public void testSystemEntity() {
    internalTestAttributes(
        metadata, Group.class, factory, getOverriddenReturnTypes(), getExcludedAttrs());
  }
}

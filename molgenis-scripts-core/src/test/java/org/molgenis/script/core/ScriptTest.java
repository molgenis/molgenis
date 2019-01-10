package org.molgenis.script.core;

import static java.util.Arrays.asList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.molgenis.data.meta.AbstractSystemEntityTest;
import org.molgenis.script.core.config.ScriptTestConfig;
import org.molgenis.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = {ScriptTestConfig.class})
public class ScriptTest extends AbstractSystemEntityTest {

  @Autowired ScriptMetadata metadata;
  @Autowired ScriptFactory factory;

  @Override
  protected Map<String, Pair<Class, Object>> getOverriddenReturnTypes() {
    Map<String, Pair<Class, Object>> map = new HashMap<>();

    Pair<Class, Object> parameters = new Pair<>();
    parameters.setA(List.class);
    parameters.setB(asList("param1", "param2"));

    map.put(ScriptMetadata.PARAMETERS, parameters);
    return map;
  }

  @Test
  public void testSystemEntity() {
    internalTestAttributes(
        metadata, Script.class, factory, getOverriddenReturnTypes(), getExcludedAttrs());
  }
}

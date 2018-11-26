package org.molgenis.script;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.meta.AbstractSystemEntityTest;
import org.molgenis.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

//NOTE: when the following exception occurs;
//java.lang.ArrayStoreException: sun.reflect.annotation.TypeNotPresentExceptionProxy,
//this means you are missing this dependency:
//<dependency>
//  <groupId>org.springframework.security</groupId>
//  <artifactId>spring-security-test</artifactId>
//  <scope>test</scope>
//</dependency>

@ContextConfiguration(
    classes = {
      EntityBaseTestConfig.class,
      ${ENTITY_CLASSNAME}Metadata.class,
      ${ENTITY_CLASSNAME}Factory.class,
      ${ENTITYPACKAGE}.class 
      #if (${CONFIGCLASS} && ${CONFIGCLASS} != "") ,${CONFIGCLASS}.class #end})
public class ${ENTITY_CLASSNAME}Test extends AbstractSystemEntityTest {
  @Autowired ${ENTITY_CLASSNAME}Metadata metadata;
  @Autowired ${ENTITY_CLASSNAME}Factory factory;

  private Map<String, Pair<Class, Object>> getOverriddenReturnTypes(){
    //Add attributes with 'smart' getters and setters that convert back and forth to correct values for MOLGENIS datatypes
    //Provide the attribute name as key, and a pair of return type (Class) and an Object to be used as test value
    return new HashMap<>();
    }

  private List<String> getExcludedAttrs() {
    return new ArrayList<>();
  }

  @Test
  public void testSystemEntity() {
    internalTestAttributes(
        metadata, ${ENTITY_CLASSNAME}.class, factory, getOverriddenReturnTypes(), getExcludedAttrs());
  }
}

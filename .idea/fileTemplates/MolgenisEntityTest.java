package org.molgenis.script;

//NOTE: when the following exception occurs;
//java.lang.ArrayStoreException: sun.reflect.annotation.TypeNotPresentExceptionProxy,
//this means you are missing this dependency:
//<dependency>
//  <groupId>org.springframework.security</groupId>
//  <artifactId>spring-security-test</artifactId>
//  <scope>test</scope>
//</dependency>
//TODO remove this NOTE

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

  // TODO override or remove:
  // getOverridenReturnTypes() if getters/setters use a return type that differs from the metadata
  // getExcludedAttrs() if you want to exclude some attributes from the test

  @Test
  public void testSystemEntity() {
    internalTestAttributes(
        metadata, ${ENTITY_CLASSNAME}.class, factory, getOverriddenReturnTypes(), getExcludedAttrs());
  }
}

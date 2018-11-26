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
      ${ENTITY_CLASSNAME}Metadata.class,
      ${ENTITY_CLASSNAME}Factory.class,
      ${ENTITYPACKAGE}.class 
      #if (${CONFIGCLASS} && ${CONFIGCLASS} != "") ,${CONFIGCLASS}.class #end})
public class ${ENTITY_CLASSNAME}Test extends AbstractSystemEntityTest {
  @Autowired ${ENTITY_CLASSNAME}Metadata metadata;
  @Autowired ${ENTITY_CLASSNAME}Factory factory;

  private Map<String, Pair<Class, Object>> getOverriddenAttributes(){
    //Add attributes with 'smart' getters and setters that covert back anf forth to correct values for MOLGENIS datatypes
    //Provide the attribute name as key, and a pair of returntype (Class) and a Object to be used as test value
    Map<String, Pair<Class, Object>> map = new HashMap<>();
    return map;
    }

  @Test
  public void testSystemEntity(){
    internalTestAttributes(metadata, ${ENTITY_CLASSNAME}.class, factory, getOverriddenAttributes());
  }
}

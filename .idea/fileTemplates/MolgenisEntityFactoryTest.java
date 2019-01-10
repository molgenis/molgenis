import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.meta.AbstractEntityFactoryTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = {EntityBaseTestConfig.class, ${ENTITY_CLASSNAME}Metadata.class, ${ENTITY_CLASSNAME}Factory.class, ${ENTITYPACKAGE}.class #if (${CONFIGCLASS} && ${CONFIGCLASS} != "") ,${CONFIGCLASS}.class #end})
public class ${ENTITY_CLASSNAME}FactoryTest extends AbstractEntityFactoryTest {

  @Autowired ${ENTITY_CLASSNAME}Factory factory;

  @Override
  @Test
  public void testCreate() {
    super.testCreate(factory, ${ENTITY_CLASSNAME}.class);
  }

  @Override
  @Test
  public void testCreateWithId() {
    super.testCreateWithId(factory, ${ENTITY_CLASSNAME}.class);
  }

  @Override
  @Test
  public void testCreateWithEntity() {
    super.testCreateWithEntity(factory, ${ENTITY_CLASSNAME}.class);
  }
}
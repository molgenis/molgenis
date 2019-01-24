package org.molgenis.core.ui.data.system.core;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import org.mockito.Mockito;
import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.meta.AbstractSystemEntityTest;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.system.model.RootSystemPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(
    classes = {
      EntityBaseTestConfig.class,
      FreemarkerTemplateMetadata.class,
      FreemarkerTemplateFactory.class,
      RootSystemPackage.class
    })
public class FreemarkerTemplateTest extends AbstractSystemEntityTest {

  @Autowired FreemarkerTemplateMetadata metadata;
  @Autowired FreemarkerTemplateFactory factory;

  @Test
  public void testSystemEntity() {
    internalTestAttributes(
        metadata,
        FreemarkerTemplate.class,
        factory,
        getOverriddenReturnTypes(),
        getExcludedAttrs());
  }

  @Test
  public void testGetNameWithoutExtensionEndsWithFtl() throws Exception {
    FreemarkerTemplate freemarkerTemplate = getFreemarkerTemplateSpy();
    when(freemarkerTemplate.getName()).thenReturn("template.ftl");
    assertEquals(freemarkerTemplate.getNameWithoutExtension(), "template");
  }

  @Test
  public void testGetNameWithoutExtensionNotEndsWithFtl() throws Exception {
    FreemarkerTemplate freemarkerTemplate = getFreemarkerTemplateSpy();
    when(freemarkerTemplate.getName()).thenReturn("template");
    assertEquals(freemarkerTemplate.getNameWithoutExtension(), "template");
  }

  private FreemarkerTemplate getFreemarkerTemplateSpy() {
    EntityType entityType = mock(EntityType.class);
    return Mockito.spy(new FreemarkerTemplate(entityType));
  }
}

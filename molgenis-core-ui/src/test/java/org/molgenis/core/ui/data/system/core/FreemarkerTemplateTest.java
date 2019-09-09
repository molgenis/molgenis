package org.molgenis.core.ui.data.system.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.meta.AbstractSystemEntityTest;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.system.model.RootSystemPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(
    classes = {
      EntityBaseTestConfig.class,
      FreemarkerTemplateMetadata.class,
      FreemarkerTemplateFactory.class,
      RootSystemPackage.class
    })
class FreemarkerTemplateTest extends AbstractSystemEntityTest {

  @Autowired FreemarkerTemplateMetadata metadata;
  @Autowired FreemarkerTemplateFactory factory;

  @Test
  protected void testSystemEntity() {
    internalTestAttributes(
        metadata,
        FreemarkerTemplate.class,
        factory,
        getOverriddenReturnTypes(),
        getExcludedAttrs());
  }

  @Test
  void testGetNameWithoutExtensionEndsWithFtl() throws Exception {
    FreemarkerTemplate freemarkerTemplate = getFreemarkerTemplateSpy();
    when(freemarkerTemplate.getName()).thenReturn("template.ftl");
    assertEquals(freemarkerTemplate.getNameWithoutExtension(), "template");
  }

  @Test
  void testGetNameWithoutExtensionNotEndsWithFtl() throws Exception {
    FreemarkerTemplate freemarkerTemplate = getFreemarkerTemplateSpy();
    when(freemarkerTemplate.getName()).thenReturn("template");
    assertEquals(freemarkerTemplate.getNameWithoutExtension(), "template");
  }

  private FreemarkerTemplate getFreemarkerTemplateSpy() {
    EntityType entityType = mock(EntityType.class);
    return Mockito.spy(new FreemarkerTemplate(entityType));
  }
}

package org.molgenis.data.i18n.model;

import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.i18n.config.I18nTestConfig;
import org.molgenis.data.meta.AbstractSystemEntityTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(
    classes = {
      EntityBaseTestConfig.class,
      LanguageMetadata.class,
      LanguageFactory.class,
      I18nTestConfig.class
    })
public class LanguageTest extends AbstractSystemEntityTest {

  @Autowired LanguageMetadata metadata;
  @Autowired LanguageFactory factory;

  @Test
  public void testSystemEntity() {
    internalTestAttributes(
        metadata, Language.class, factory, getOverriddenReturnTypes(), getExcludedAttrs(), true);
  }
}

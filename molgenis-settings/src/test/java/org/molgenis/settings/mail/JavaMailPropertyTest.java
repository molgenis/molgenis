package org.molgenis.settings.mail;

import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.meta.AbstractSystemEntityTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(
    classes = {
      EntityBaseTestConfig.class,
      JavaMailPropertyType.class,
      JavaMailPropertyFactory.class,
      MailTestConfig.class,
      MailPackage.class
    })
public class JavaMailPropertyTest extends AbstractSystemEntityTest {

  @Autowired JavaMailPropertyType metadata;
  @Autowired JavaMailPropertyFactory factory;

  @Test
  public void testSystemEntity() {
    internalTestAttributes(
        metadata, JavaMailProperty.class, factory, getOverriddenReturnTypes(), getExcludedAttrs());
  }
}

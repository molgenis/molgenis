package org.molgenis.settings.mail;

import org.junit.jupiter.api.Test;
import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.meta.AbstractSystemEntityTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(
    classes = {
      EntityBaseTestConfig.class,
      JavaMailPropertyType.class,
      JavaMailPropertyFactory.class,
      MailTestConfig.class,
      MailPackage.class
    })
class JavaMailPropertyTest extends AbstractSystemEntityTest {

  @Autowired JavaMailPropertyType metadata;
  @Autowired JavaMailPropertyFactory factory;

  @SuppressWarnings("squid:S2699") // Tests should include assertions
  @Test
  protected void testSystemEntity() {
    internalTestAttributes(
        metadata, JavaMailProperty.class, factory, getOverriddenReturnTypes(), getExcludedAttrs());
  }
}

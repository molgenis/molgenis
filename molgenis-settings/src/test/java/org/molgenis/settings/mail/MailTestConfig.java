package org.molgenis.settings.mail;

import org.molgenis.data.EntityManagerImpl;
import org.molgenis.data.listeners.EntityListenersService;
import org.molgenis.settings.PropertyType;
import org.molgenis.settings.SettingsEntityType;
import org.molgenis.settings.SettingsPackage;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
  PropertyType.class,
  MailSettingsImpl.class,
  EntityManagerImpl.class,
  SettingsEntityType.class,
  SettingsPackage.class,
  EntityListenersService.class,
})
public class MailTestConfig {}

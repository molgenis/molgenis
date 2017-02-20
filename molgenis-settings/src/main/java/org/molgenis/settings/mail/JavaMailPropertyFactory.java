package org.molgenis.settings.mail;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JavaMailPropertyFactory
		extends AbstractSystemEntityFactory<JavaMailProperty, JavaMailPropertyType, String>
{
	@Autowired
	JavaMailPropertyFactory(JavaMailPropertyType mailSenderPropertyType, EntityPopulator entityPopulator)
	{
		super(JavaMailProperty.class, mailSenderPropertyType, entityPopulator);
	}
}

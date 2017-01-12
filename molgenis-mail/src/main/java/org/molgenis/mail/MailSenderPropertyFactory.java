package org.molgenis.mail;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MailSenderPropertyFactory
		extends AbstractSystemEntityFactory<MailSenderProperty, MailSenderPropertyType, String>
{
	@Autowired
	MailSenderPropertyFactory(MailSenderPropertyType mailSenderPropertyType, EntityPopulator entityPopulator)
	{
		super(MailSenderProperty.class, mailSenderPropertyType, entityPopulator);
	}
}

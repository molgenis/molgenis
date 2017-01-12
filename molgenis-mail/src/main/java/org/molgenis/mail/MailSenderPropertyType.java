package org.molgenis.mail;

import org.molgenis.data.meta.PropertyType;
import org.molgenis.data.meta.SystemEntityType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.mail.MailPackage.PACKAGE_MAIL;

@Component
public class MailSenderPropertyType extends SystemEntityType
{
	private static final String SIMPLE_NAME = "MailSenderProperty";
	public static final String MAIL_SENDER_PROPERTY = PACKAGE_MAIL + PACKAGE_SEPARATOR + SIMPLE_NAME;

	private final MailPackage mailPackage;
	private final PropertyType propertyType;

	@Autowired
	public MailSenderPropertyType(MailPackage mailPackage, PropertyType propertyType)
	{
		super(SIMPLE_NAME, PACKAGE_MAIL);
		this.mailPackage = requireNonNull(mailPackage);
		this.propertyType = requireNonNull(propertyType);
	}

	@Override
	public void init()
	{
		setLabel("Mail sender properties.");
		setDescription(
				"See https://javamail.java.net/nonav/docs/api/ for a description of the properties you can use.");
		setPackage(mailPackage);
		setExtends(propertyType);
	}
}
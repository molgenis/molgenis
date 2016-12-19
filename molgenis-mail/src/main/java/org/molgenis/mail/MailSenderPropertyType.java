package org.molgenis.mail;

import org.molgenis.data.meta.PropertyType;
import org.molgenis.data.meta.SystemEntityType;
import org.molgenis.data.settings.SettingsPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.data.settings.SettingsPackage.PACKAGE_SETTINGS;

@Component
public class MailSenderPropertyType extends SystemEntityType
{
	private static final String SIMPLE_NAME = "MailSenderProp";
	public static final String MAIL_SENDER_PROPERTY = PACKAGE_SETTINGS + PACKAGE_SEPARATOR + SIMPLE_NAME;

	private final SettingsPackage settingsPackage;
	private final PropertyType propertyType;

	@Autowired
	public MailSenderPropertyType(SettingsPackage settingsPackage, PropertyType propertyType)
	{
		super(SIMPLE_NAME, PACKAGE_SETTINGS);
		this.settingsPackage = requireNonNull(settingsPackage);
		this.propertyType = requireNonNull(propertyType);
	}

	@Override
	public void init()
	{
		setLabel("Mail sender properties.");
		setDescription(
				"See https://javamail.java.net/nonav/docs/api/ for a description of the properties you can use.");
		setPackage(settingsPackage);
		setExtends(propertyType);
	}
}
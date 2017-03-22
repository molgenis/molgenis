package org.molgenis.settings.mail;

import org.molgenis.data.meta.SystemEntityType;
import org.molgenis.settings.PropertyType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.AttributeType.XREF;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;

@Component
public class JavaMailPropertyType extends SystemEntityType
{
	private static final String SIMPLE_NAME = "JavaMailProperty";
	public static final String JAVA_MAIL_PROPERTY = MailPackage.PACKAGE_MAIL + PACKAGE_SEPARATOR + SIMPLE_NAME;

	private Meta mailSettings;
	private final MailPackage mailPackage;
	private final PropertyType propertyType;
	public static final String MAIL_SETTINGS_REF = "mailSettings";

	@Autowired
	public JavaMailPropertyType(MailPackage mailPackage, PropertyType propertyType)
	{
		super(SIMPLE_NAME, MailPackage.PACKAGE_MAIL);
		this.mailPackage = requireNonNull(mailPackage);
		this.propertyType = requireNonNull(propertyType);
	}

	@Autowired
	public void setMailSettingsMetadata(Meta mailSettings)
	{
		this.mailSettings = requireNonNull(mailSettings);
	}

	@Override
	public void init()
	{
		setLabel("Mail sender properties.");
		setDescription(
				"See https://javamail.java.net/nonav/docs/api/ for a description of the properties you can use.");
		setPackage(mailPackage);
		setExtends(propertyType);
		addAttribute(MAIL_SETTINGS_REF).setDataType(XREF).setRefEntity(mailSettings).setLabel("MailSettings")
				.setDescription("Reference to the (unique) MailSettings entity that these properties belong to.")
				.setVisible(false).setNillable(false).setReadOnly(true);
	}
}
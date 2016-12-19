package org.molgenis.mail;

import org.molgenis.data.settings.DefaultSettingsEntity;
import org.molgenis.data.settings.DefaultSettingsEntityType;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;

import static org.molgenis.data.meta.AttributeType.INT;

@Component
public class MailSettingsImpl extends DefaultSettingsEntity implements MailSettings
{
	private static final long serialVersionUID = 1L;

	private static final String ID = "MailSettings";

	public MailSettingsImpl()
	{
		super(ID);
	}

	@Component
	public static class Meta extends DefaultSettingsEntityType
	{
		private static final String HOST = "host";
		private static final String PORT = "port";
		private static final String PROTOCOL = "protocol";
		private static final String USERNAME = "username";
		private static final String PASSWORD = "password";
		private static final String DEFAULT_ENCODING = "defaultEncoding";

		public Meta()
		{
			super(ID);
		}

		@Override
		public void init()
		{
			super.init();
			setLabel("Mail settings");
			setDescription(
					"Configuration properties for email support. Will be used to send email from Molgenis. See also the MailSenderProp entity.");
			addAttribute(HOST).setDefaultValue("smtp.gmail.com").setNillable(false).setDescription("SMTP server host.");
			addAttribute(PORT).setDataType(INT).setDefaultValue("587").setNillable(false)
					.setDescription("SMTP server port.");
			addAttribute(PROTOCOL).setDefaultValue("smtp").setNillable(false)
					.setDescription("Protocol used by the SMTP server.");
			addAttribute(USERNAME).setDescription("Login user of the SMTP server.");
			addAttribute(PASSWORD).setDescription("Login password of the SMTP server.");
			addAttribute(DEFAULT_ENCODING).setDefaultValue("UTF-8").setNillable(false)
					.setDescription("Default MimeMessage encoding.");
		}
	}

	@Override
	public String getHost()
	{
		return getString(Meta.HOST);
	}

	@Override
	public int getPort()
	{
		return getInt(Meta.PORT);
	}

	@Override
	public String getProtocol()
	{
		return getString(Meta.PROTOCOL);
	}

	@Override
	public String getUsername()
	{
		return getString(Meta.USERNAME);
	}

	@Override
	public String getPassword()
	{
		return getString(Meta.PASSWORD);
	}

	@Override
	public Charset getDefaultEncoding()
	{
		return Charset.forName(getString(Meta.DEFAULT_ENCODING));
	}
}

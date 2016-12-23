package org.molgenis.mail;

import org.molgenis.data.settings.DefaultSettingsEntity;
import org.molgenis.data.settings.DefaultSettingsEntityType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;

import static org.molgenis.data.meta.AttributeType.BOOL;
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
		/**
		 * For conversion: Pick up defaults from molgenis-server.properties file where these settings used to be defined.
		 */
		@Value("${mail.host:smtp.gmail.com}")
		private String mailHost;
		@Value("${mail.port:587}")
		private String mailPort;
		@Value("${mail.protocol:smtp}")
		private String mailProtocol;
		@Value("${mail.username}")
		private String mailUsername;
		@Value("${mail.password}")
		private String mailPassword;
		@Value("${mail.java.auth:true}")
		private String mailJavaAuth;
		@Value("${mail.java.starttls.enable:true}")
		private String mailJavaStartTlsEnable;
		@Value("${mail.java.quitwait:false}")
		private String mailJavaQuitWait;

		private static final String HOST = "host";
		private static final String PORT = "port";
		private static final String PROTOCOL = "protocol";
		private static final String USERNAME = "username";
		private static final String PASSWORD = "password";
		private static final String DEFAULT_ENCODING = "defaultEncoding";
		private static final String TEST_CONNECTION = "testConnection";

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
			addAttribute(HOST).setDefaultValue(mailHost).setNillable(false).setDescription("SMTP server host.");
			addAttribute(PORT).setDataType(INT).setDefaultValue(mailPort).setNillable(false)
					.setDescription("SMTP server port.");
			addAttribute(PROTOCOL).setDefaultValue(mailProtocol).setNillable(false)
					.setDescription("Protocol used by the SMTP server.");
			addAttribute(USERNAME).setDefaultValue(mailUsername).setDescription("Login user of the SMTP server.");
			addAttribute(PASSWORD).setDefaultValue(mailPassword).setDescription("Login password of the SMTP server.");
			addAttribute(DEFAULT_ENCODING).setDefaultValue("UTF-8").setNillable(false)
					.setDescription("Default MimeMessage encoding.");
			addAttribute(TEST_CONNECTION).setDataType(BOOL).setDefaultValue("true").setNillable(true)
					.setDescription("Test mail connection on startup.");
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

	@Override
	public boolean isTestConnection()
	{
		return getBoolean(Meta.TEST_CONNECTION);
	}
}

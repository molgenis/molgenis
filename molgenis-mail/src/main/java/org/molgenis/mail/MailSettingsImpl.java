package org.molgenis.mail;

import org.molgenis.data.settings.DefaultSettingsEntity;
import org.molgenis.data.settings.DefaultSettingsEntityType;
import org.springframework.stereotype.Component;

import static org.molgenis.data.meta.AttributeType.*;

@Component
public class MailSettingsImpl extends DefaultSettingsEntity implements MailSettings
{
	private static final long serialVersionUID = 1L;

	private static final String ID = "mail";

	public MailSettingsImpl()
	{
		super(ID);
	}

	@Component
	private static class Meta extends DefaultSettingsEntityType
	{
		private static final String HOST = "host";
		private static final String PORT = "port";
		private static final String PROTOCOL = "protocol";
		private static final String USERNAME = "username";
		private static final String PASSWORD = "password";
		private static final String AUTH = "auth";
		private static final String START_TLS_ENABLED = "startTLSEnabled";
		private static final String QUIT_WAIT = "quitWait";

		public Meta()
		{
			super(ID);
		}

		@Override
		public void init()
		{
			super.init();
			setLabel("Mail settings");
			setDescription("Settings for the Java MailSender. Will be used to send email from Molgenis.");
			addAttribute(HOST).setDataType(STRING).setDefaultValue("smtp.gmail.com").setNillable(false);
			addAttribute(PORT).setDataType(INT).setDefaultValue("587").setNillable(false);
			addAttribute(PROTOCOL).setDataType(STRING).setDefaultValue("smtp").setNillable(false);
			addAttribute(USERNAME).setDataType(STRING);
			addAttribute(PASSWORD).setDataType(STRING);
			addAttribute(AUTH).setDataType(BOOL).setDefaultValue("true").setNillable(false);
			addAttribute(START_TLS_ENABLED).setDataType(BOOL).setDefaultValue("true").setNillable(false);
			addAttribute(QUIT_WAIT).setDataType(BOOL).setDefaultValue("false").setNillable(false);
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
	public boolean isAuth()
	{
		return getBoolean(Meta.AUTH);
	}

	@Override
	public boolean isStartTlsEnable()
	{
		return getBoolean(Meta.START_TLS_ENABLED);
	}

	@Override
	public boolean isQuitWait()
	{
		return getBoolean(Meta.QUIT_WAIT);
	}
}

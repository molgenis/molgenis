package org.molgenis.settings.mail;

import org.molgenis.data.Entity;
import org.molgenis.data.settings.DefaultSettingsEntity;
import org.molgenis.util.mail.MailSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;
import java.util.Properties;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.StreamSupport.stream;
import static org.molgenis.settings.mail.Meta.*;

@Component
public class MailSettingsImpl extends DefaultSettingsEntity implements MailSettings
{
	private static final long serialVersionUID = 1L;
	static final String ID = "MailSettings";

	private static final Logger LOG = LoggerFactory.getLogger(MailSettingsImpl.class);

	public MailSettingsImpl(Entity entity)
	{
		super(ID);
		set(entity);
	}

	public MailSettingsImpl()
	{
		super(ID);
	}

	@Override
	public String getHost()
	{
		return getString(HOST);
	}

	@Override
	public int getPort()
	{
		return getInt(PORT);
	}

	@Override
	public String getProtocol()
	{
		return getString(PROTOCOL);
	}

	@Override
	public String getUsername()
	{
		return getString(USERNAME);
	}

	@Override
	public String getPassword()
	{
		return getString(PASSWORD);
	}

	@Override
	public Charset getDefaultEncoding()
	{
		return Charset.forName(getString(DEFAULT_ENCODING));
	}

	@Override
	public Properties getJavaMailProperties()
	{
		Properties result = new Properties();
		result.putAll(stream(getEntities(JAVA_MAIL_PROPS, JavaMailProperty.class).spliterator(),
						false)
				.collect(toMap(JavaMailProperty::getKey, JavaMailProperty::getValue)));
		return result;
	}

	@Override
	public boolean isTestConnection()
	{
		return getBoolean(TEST_CONNECTION);
	}
}

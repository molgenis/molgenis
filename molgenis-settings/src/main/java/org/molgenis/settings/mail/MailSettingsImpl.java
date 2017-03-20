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
		return getString(MailSettingsImplMetadata.HOST);
	}

	@Override
	public int getPort()
	{
		return getInt(MailSettingsImplMetadata.PORT);
	}

	@Override
	public String getProtocol()
	{
		return getString(MailSettingsImplMetadata.PROTOCOL);
	}

	@Override
	public String getUsername()
	{
		return getString(MailSettingsImplMetadata.USERNAME);
	}

	@Override
	public String getPassword()
	{
		return getString(MailSettingsImplMetadata.PASSWORD);
	}

	@Override
	public Charset getDefaultEncoding()
	{
		return Charset.forName(getString(MailSettingsImplMetadata.DEFAULT_ENCODING));
	}

	@Override
	public Properties getJavaMailProperties()
	{
		Properties result = new Properties();
		result.putAll(
				stream(getEntities(MailSettingsImplMetadata.JAVA_MAIL_PROPS, JavaMailProperty.class).spliterator(),
						false)
				.collect(toMap(JavaMailProperty::getKey, JavaMailProperty::getValue)));
		return result;
	}

	@Override
	public boolean isTestConnection()
	{
		return getBoolean(MailSettingsImplMetadata.TEST_CONNECTION);
	}
}

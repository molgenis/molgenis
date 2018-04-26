package org.molgenis.settings.mail;

import org.molgenis.data.Entity;
import org.molgenis.data.Sort;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.SystemEntityType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.settings.DefaultSettingsEntity;
import org.molgenis.settings.DefaultSettingsEntityType;
import org.molgenis.util.mail.MailSettings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.StreamSupport.stream;
import static org.molgenis.data.meta.AttributeType.BOOL;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.settings.PropertyType.KEY;
import static org.molgenis.settings.SettingsPackage.PACKAGE_SETTINGS;
import static org.molgenis.settings.mail.JavaMailPropertyType.JAVA_MAIL_PROPERTY;
import static org.molgenis.settings.mail.JavaMailPropertyType.MAIL_SETTINGS_REF;

@Component
public class MailSettingsImpl extends DefaultSettingsEntity implements MailSettings
{
	private static final long serialVersionUID = 1L;
	private static final String ID = "MailSettings";

	public MailSettingsImpl(Entity entity)
	{
		super(ID);
		set(entity);
	}

	public MailSettingsImpl()
	{
		super(ID);
	}

	// workaround for dependency error running platform integration tests on build server
	@DependsOn(value = "org.molgenis.settings.mail.JavaMailPropertyType")
	@Component
	public static class Meta extends DefaultSettingsEntityType
	{
		@SuppressWarnings("unused")
		public static final String MAIL_SETTINGS = PACKAGE_SETTINGS + PACKAGE_SEPARATOR + ID;
		/**
		 * For conversion: Pick up defaults from molgenis-server.properties file where these settings used to be defined.
		 */
		@Value("${mail.host:smtp.gmail.com}")
		private String mailHost;
		@Value("${mail.port:587}")
		private String mailPort;
		@Value("${mail.protocol:smtp}")
		private String mailProtocol;
		@Value("${mail.username:#{null}}")
		private String mailUsername;
		@Value("${mail.password:#{null}}")
		private String mailPassword;
		@Value("${mail.java.auth:true}")
		private String mailJavaAuth;
		@Value("${mail.java.starttls.enable:true}")
		private String mailJavaStartTlsEnable;
		@Value("${mail.java.quitwait:false}")
		private String mailJavaQuitWait;

		public static final String HOST = "host";
		public static final String PORT = "port";
		public static final String PROTOCOL = "protocol";
		public static final String USERNAME = "username";
		public static final String PASSWORD = "password";
		public static final String DEFAULT_ENCODING = "defaultEncoding";
		public static final String JAVA_MAIL_PROPS = "props";
		public static final String TEST_CONNECTION = "testConnection";
		private JavaMailPropertyType mailSenderPropertyType;

		public Meta(JavaMailPropertyType mailSenderPropertyType)
		{
			super(ID);
			this.mailSenderPropertyType = Objects.requireNonNull(mailSenderPropertyType);
		}

		@Override
		public void init()
		{
			super.init();
			setLabel("Mail settings");
			setDescription(
					"Configuration properties for email support. Will be used to send email from Molgenis. See also the MailSenderProp entity.");
			addAttribute(HOST).setDefaultValue(mailHost).setNillable(false).setDescription("SMTP server host.");
			addAttribute(PORT).setDataType(AttributeType.INT)
							  .setDefaultValue(mailPort)
							  .setNillable(false)
							  .setDescription("SMTP server port.");
			addAttribute(PROTOCOL).setDefaultValue(mailProtocol)
								  .setNillable(false)
								  .setDescription("Protocol used by the SMTP server.");
			addAttribute(USERNAME).setDefaultValue(mailUsername).setDescription("Login user of the SMTP server.");
			addAttribute(PASSWORD).setDefaultValue(mailPassword).setDescription("Login password of the SMTP server.");
			addAttribute(DEFAULT_ENCODING).setDefaultValue("UTF-8")
										  .setNillable(false)
										  .setDescription("Default MimeMessage encoding.");
			Attribute refAttr = mailSenderPropertyType.getAttribute(MAIL_SETTINGS_REF);
			addAttribute(JAVA_MAIL_PROPS).setDataType(AttributeType.ONE_TO_MANY)
										 .setRefEntity(mailSenderPropertyType)
										 .setMappedBy(refAttr)
										 .setOrderBy(new Sort(KEY))
										 .setNillable(true)
										 .setLabel("Properties")
										 .setDescription(
												 "JavaMail properties. The default values are tuned to connect with Google mail."
														 + "If you want to connect to a different provider, these properties should be edited in the Data Explorer."
														 + "Select the " + JAVA_MAIL_PROPERTY + " entity.");
			addAttribute(TEST_CONNECTION).setDataType(BOOL).setDefaultValue("true").setNillable(false).
					setDescription("Indicates if the connection should be tested when saving these settings.");
		}

		@Override
		public Set<SystemEntityType> getDependencies()
		{
			return Collections.singleton(mailSenderPropertyType);
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
	@Nullable
	public String getUsername()
	{
		return getString(Meta.USERNAME);
	}

	@Override
	@Nullable
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
	public Properties getJavaMailProperties()
	{
		Properties result = new Properties();
		result.putAll(stream(getEntities(Meta.JAVA_MAIL_PROPS, JavaMailProperty.class).spliterator(), false).collect(
				toMap(JavaMailProperty::getKey, JavaMailProperty::getValue)));
		return result;
	}

	@Override
	public boolean isTestConnection()
	{
		return getBoolean(Meta.TEST_CONNECTION);
	}
}

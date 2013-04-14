package org.molgenis.util;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.security.Login;
import org.molgenis.framework.server.MolgenisSettings;
import org.springframework.mail.javamail.JavaMailSender;

/**
 * Util class for non-spring managed classes to get a reference to beans
 * 
 * @author erwin
 * 
 */
public class WebAppUtil
{
	public static Database getDatabase()
	{
		return ApplicationContextProvider.getApplicationContext().getBean("database", Database.class);
	}

	public static Login getLogin()
	{
		return ApplicationContextProvider.getApplicationContext().getBean("login", Login.class);
	}

	public static JavaMailSender getMailSender()
	{
		return ApplicationContextProvider.getApplicationContext().getBean("mailSender", JavaMailSender.class);
	}

	public static MolgenisSettings getMolgenisSettings()
	{
		return ApplicationContextProvider.getApplicationContext().getBean("molgenisSettings", MolgenisSettings.class);
	}
}

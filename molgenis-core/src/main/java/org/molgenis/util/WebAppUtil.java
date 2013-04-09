package org.molgenis.util;

import org.molgenis.framework.db.Database;
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

	public static JavaMailSender getMailSender()
	{
		return ApplicationContextProvider.getApplicationContext().getBean("mailSender", JavaMailSender.class);
	}
}

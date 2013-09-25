package org.molgenis.util;

import javax.persistence.EntityManagerFactory;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.EntitiesImporter;
import org.molgenis.framework.db.EntitiesValidator;
import org.molgenis.framework.server.MolgenisSettings;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextException;
import org.springframework.mail.javamail.JavaMailSender;

/**
 * Util class for non-spring managed classes to get a reference to beans
 * 
 * @author erwin
 * 
 */
public class ApplicationUtil
{
	public static Database getDatabase()
	{
		return getApplicationContext().getBean("database", Database.class);
	}

	public static Database getUnauthorizedDatabase()
	{
		return getDatabase();
	}

	public static Database getUnauthorizedPrototypeDatabase()
	{
		return getDatabase();
	}

	public static EntityManagerFactory getEntityManagerFactory()
	{
		return getApplicationContext().getBean("entityManagerFactory", EntityManagerFactory.class);
	}

	public static JavaMailSender getMailSender()
	{
		return getApplicationContext().getBean("mailSender", JavaMailSender.class);
	}

	public static MolgenisSettings getMolgenisSettings()
	{
		return getApplicationContext().getBean("molgenisSettings", MolgenisSettings.class);
	}

	public static EntitiesImporter getEntitiesImporter()
	{
		return getApplicationContext().getBean(EntitiesImporter.class);
	}

	public static EntitiesValidator getEntitiesValidator()
	{
		return getApplicationContext().getBean(EntitiesValidator.class);
	}

	private static ApplicationContext getApplicationContext()
	{
		ApplicationContext applicationContext = ApplicationContextProvider.getApplicationContext();
		if (applicationContext == null)
		{
			throw new RuntimeException(new ApplicationContextException("missing required application context"));
		}
		return applicationContext;
	}
}

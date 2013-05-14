package ${package};

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.jpa.JpaDatabase;
import org.molgenis.framework.security.Login;
import org.molgenis.framework.server.TokenFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;

@Configuration
public class DatabaseConfig
{
	/**
	 * Entitymanager-per-HTTP-request pattern in a multi-user client/server application authenticated for current user
	 * 
	 * @return
	 * @throws DatabaseException
	 */
	@Bean(destroyMethod = "close")
	@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS, value = "request")
	public Database database() throws DatabaseException
	{
		Database db = new ${package}.JpaDatabase(entityManagerFactory());
		Login login = login();
		// login with anonymous user if not logged in
		if (login.getUserName() == null)
		{
			try
			{
				login.login(db, Login.USER_ANONYMOUS_NAME, Login.USER_ANONYMOUS_PASSWORD);
			}
			catch (Exception e)
			{
				throw new DatabaseException(e);
			}
		}
		db.setLogin(login);
		return db;
	}

	/**
	 * Entitymanager-per-HTTP-request pattern in a multi-user client/server application authenticated for system user
	 * 
	 * @return
	 * @throws DatabaseException
	 */
	@Bean(destroyMethod = "close")
	@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS, value = "request")
	public Database unauthorizedDatabase() throws DatabaseException
	{
		return new ${package}.JpaDatabase(entityManagerFactory());
	}
	
	/**
	 * Entitymanager-per-bean-request pattern in a standalone application
	 * Important: User is responsible for closing the Database instance
	 * 
	 * @return
	 * @throws DatabaseException
	 */
	@Bean
	@Scope("prototype")
	public Database unauthorizedPrototypeDatabase() throws DatabaseException
	{
		return new ${package}.JpaDatabase(entityManagerFactory());
	}
	
	@Bean(destroyMethod = "close")
	public EntityManagerFactory entityManagerFactory()
	{
		return Persistence.createEntityManagerFactory(JpaDatabase.DEFAULT_PERSISTENCE_UNIT_NAME);
	}

	@Bean
	@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS, value = "session")
	public Login login()
	{
		return new ${auth_loginclass}(tokenFactory());
	}

	@Bean
	public TokenFactory tokenFactory()
	{
		return new TokenFactory();
	}
}

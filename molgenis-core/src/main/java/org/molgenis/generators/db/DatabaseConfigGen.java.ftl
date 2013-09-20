package ${package};

import java.beans.PropertyVetoException;
import java.text.ParseException;
import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.sql.DataSource;

import org.molgenis.SecuredJpaDatabase;
import org.molgenis.UnsecuredJpaDatabase;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.security.Login;
import org.molgenis.framework.server.TokenFactory;
import org.molgenis.util.Entity;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.EnableLoadTimeWeaving;
import org.springframework.context.annotation.EnableLoadTimeWeaving.AspectJWeaving;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.orm.jpa.JpaDialect;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.EclipseLinkJpaDialect;
import org.springframework.orm.jpa.vendor.EclipseLinkJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.TransactionManagementConfigurer;
import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * Database configuration
 */
@Configuration
@EnableTransactionManagement
public class DatabaseConfig implements TransactionManagementConfigurer
{
	private static final String DEFAULT_PERSISTENCE_UNIT_NAME = "molgenis";

	@Value("${r"${db_driver:@null}"}")
	private String dbDriverClass;
	@Value("${r"${db_uri:@null}"}")
	private String dbJdbcUri;
	@Value("${r"${db_user:@null}"}")
	private String dbUser;
	@Value("${r"${db_password:@null}"}")
	private String dbPassword;
		
	@Bean
	public Database database() throws DatabaseException
	{
		return new SecuredJpaDatabase(jdbcMetaDatabase());
	}

	@Bean
	@Qualifier("unsecuredDatabase")
	public Database unsecuredDatabase() throws DatabaseException
	{
		return new UnsecuredJpaDatabase(jdbcMetaDatabase());
	}
	
	@Bean
	public JDBCMetaDatabase jdbcMetaDatabase() throws DatabaseException
	{
		return new JDBCMetaDatabase();
	}
	
	@Bean
	public DataSource dataSource()
	{
		if(dbDriverClass == null) throw new IllegalArgumentException("db_driver is null");
		if(dbJdbcUri == null) throw new IllegalArgumentException("db_uri is null");
		if(dbUser == null) throw new IllegalArgumentException("db_user is null");
		if(dbPassword == null) throw new IllegalArgumentException("db_password is null");
		
		ComboPooledDataSource dataSource = new ComboPooledDataSource();
		try
		{
			dataSource.setDriverClass(dbDriverClass);
		}
		catch (PropertyVetoException e)
		{
			throw new RuntimeException(e);
		}
		dataSource
				.setJdbcUrl(dbJdbcUri);
		dataSource.setUser(dbUser);
		dataSource.setPassword(dbPassword);
		dataSource.setMinPoolSize(5);
		dataSource.setMaxPoolSize(200);
		dataSource.setTestConnectionOnCheckin(true);
		dataSource.setIdleConnectionTestPeriod(120);
		return dataSource;
	}

	@Bean
	public JpaDialect jpaDialect()
	{
		return new EclipseLinkJpaDialect();
	}

	@Bean
	public JpaVendorAdapter jpaVendorAdapter()
	{
		EclipseLinkJpaVendorAdapter eclipseLinkJpaVendorAdapter = new EclipseLinkJpaVendorAdapter();
		return eclipseLinkJpaVendorAdapter;
	}

	@Bean
	public FactoryBean<EntityManagerFactory> localEntityManagerFactoryBean()
	{
		LocalContainerEntityManagerFactoryBean entityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
		entityManagerFactoryBean.setPersistenceUnitName(DEFAULT_PERSISTENCE_UNIT_NAME);
		entityManagerFactoryBean.setDataSource(dataSource());
		entityManagerFactoryBean.setJpaDialect(jpaDialect());
		entityManagerFactoryBean.setJpaVendorAdapter(jpaVendorAdapter());
		entityManagerFactoryBean.setJpaPropertyMap(Collections.singletonMap("eclipselink.weaving", "false")); // TODO use load time weaving
		// entityManagerFactoryBean.setLoadTimeWeaver(loadTimeWeaver); // TODO use load time weaving
		return entityManagerFactoryBean;
	}
	
	@Bean
	public PlatformTransactionManager transactionManager()
	{
		return new JpaTransactionManager();
	}
	
	@Override
	public PlatformTransactionManager annotationDrivenTransactionManager()
	{
		return transactionManager();
	}
	
	@Bean
	public Login login()
	{
		return new Login()
		{

			private String name;

			@Override
			public boolean login(Database db, String name, String password) throws Exception
			{
				this.name = name;
				return true;
			}

			@Override
			public void logout(Database db) throws Exception
			{
			}

			@Override
			public void reload(Database db) throws DatabaseException, ParseException, Exception
			{
			}

			@Override
			public boolean isAuthenticated()
			{
				return true;
			}

			@Override
			public String getUserName()
			{
				return name;
			}

			@Override
			public Integer getUserId()
			{
				return null;
			}

			@Override
			public boolean isLoginRequired()
			{
				return false;
			}

			@Override
			public boolean canRead(Class<? extends Entity> entityClass) throws DatabaseException
			{
				return true;
			}

			@Override
			public boolean canRead(Entity entity) throws DatabaseException
			{
				return true;
			}

			@Override
			public boolean canWrite(Class<? extends Entity> entityClass) throws DatabaseException
			{
				return true;
			}

			@Override
			public boolean canWrite(Entity entity) throws DatabaseException
			{
				return true;
			}

			@Override
			public QueryRule getRowlevelSecurityFilters(Class<? extends Entity> klazz)
			{
				return null;
			}

			@Override
			public String getRedirect()
			{
				return null;
			}

			@Override
			public void setAdmin(List<? extends Entity> entities, Database db) throws DatabaseException
			{
			}

			@Override
			public void setRedirect(String redirect)
			{
			}
		};
	}

	@Bean
	public TokenFactory tokenFactory()
	{
		return new TokenFactory();
	}
}

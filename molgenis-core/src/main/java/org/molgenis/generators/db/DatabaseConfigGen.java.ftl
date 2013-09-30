package ${package};

import java.beans.PropertyVetoException;
import java.util.Collections;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.server.TokenFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
	public TokenFactory tokenFactory()
	{
		return new TokenFactory();
	}
}

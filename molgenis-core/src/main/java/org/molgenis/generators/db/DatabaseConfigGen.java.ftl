package ${package};

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.security.Login;
import org.molgenis.framework.server.TokenFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
<#if databaseImp = 'jdbc'>
import org.apache.commons.dbcp.BasicDataSource;
</#if>

@Configuration
public class DatabaseConfig
{
	@Bean(destroyMethod = "close")
	@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS, value = "request")
	public Database database() throws DatabaseException
	{
	<#if databaseImp = 'jpa'>
		Database db = new ${package}.JpaDatabase();
	<#elseif databaseImp = 'jdbc'>
		Database db = new ${package}.JDBCDatabase(dataSource().getConnection());
	</#if>
		db.setLogin(login());
		return db;
	}

	@Bean(destroyMethod = "close")
	@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS, value = "prototype")
	public Database unauthorizedDatabase() throws DatabaseException
	{
	<#if databaseImp = 'jpa'>
		Database db = new ${package}.JpaDatabase();
	<#elseif databaseImp = 'jdbc'>
		Database db = new ${package}.JDBCDatabase(dataSource().getConnection());
	</#if>
		return db;
	}
	
<#if databaseImp = 'jdbc'>
	@Bean
	public DataSource dataSource()
	{
		DataSource dataSource = new BasicDataSource();
		dataSource.setDriverClassName("${db_driver}");
		dataSource.setUsername("${db_user}");
		dataSource.setPassword("${db_password}");
		dataSource.setUrl("${db_uri}");
		<#if db_mode != 'standalone'>
		data_src.setMaxActive(8);
		data_src.setMaxIdle(4);
		<#else>
		data_src.setInitialSize(10);
		data_src.setTestOnBorrow(true);
		</#if>
		return dataSource;
	}

</#if>
	@Bean
	@Scope("session")
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

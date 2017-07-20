package org.molgenis.security.acl;

import org.molgenis.DatabaseConfig;
import org.molgenis.data.security.acl.AclService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.transaction.TransactionAwareCacheDecorator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.acls.AclPermissionEvaluator;
import org.springframework.security.acls.domain.*;
import org.springframework.security.acls.jdbc.JdbcMutableAclService;
import org.springframework.security.acls.jdbc.LookupStrategy;
import org.springframework.security.acls.model.PermissionGrantingStrategy;
import org.springframework.security.acls.model.SidRetrievalStrategy;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * TODO Replace AclCache.NoOpCache with another cache implementation
 * TODO Extract ACL table population to other class
 */
@Import(DatabaseConfig.class)
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class AclConfig extends GlobalMethodSecurityConfiguration
{

	public static final String ROLE_ACL_ADMIN = "ROLE_ACL_ADMIN";
	private static final String SQL_CREATE_TABLE_ACL_SID =
			"CREATE TABLE IF NOT EXISTS acl_sid(\n" + "id BIGSERIAL NOT NULL PRIMARY KEY,\n"
					+ "principal BOOLEAN NOT NULL,\n" + "sid VARCHAR(100) NOT NULL,\n"
					+ "CONSTRAINT unique_uk_1 UNIQUE(sid,principal)\n" + ");";
	private static final String SQL_CREATE_TABLE_ACL_CLASS =
			"CREATE TABLE IF NOT EXISTS acl_class(\n" + "id BIGSERIAL NOT NULL PRIMARY KEY,\n"
					+ "class VARCHAR(100) NOT NULL,\n" + "CONSTRAINT unique_uk_2 UNIQUE(class)\n" + ");";

	// acl_object_identity.object_id_identity
	private static final String SQL_CREATE_TABLE_ACL_OBJECT_IDENTITY =
			"CREATE TABLE IF NOT EXISTS acl_object_identity(\n" + "id BIGSERIAL PRIMARY KEY,\n"
					+ "object_id_class BIGINT NOT NULL,\n" + "object_id_identity VARCHAR NOT NULL,\n"
					+ "parent_object BIGINT,\n" + "owner_sid BIGINT,\n" + "entries_inheriting BOOLEAN NOT NULL,\n"
					+ "CONSTRAINT unique_uk_3 UNIQUE(object_id_class,object_id_identity),\n"
					+ "CONSTRAINT foreign_fk_1 FOREIGN KEY(parent_object)REFERENCES acl_object_identity(id),\n"
					+ "CONSTRAINT foreign_fk_2 FOREIGN KEY(object_id_class)REFERENCES acl_class(id),\n"
					+ "CONSTRAINT foreign_fk_3 FOREIGN KEY(owner_sid)REFERENCES acl_sid(id)\n" + ");";
	private static final String SQL_CREATE_TABLE_ACL_ENTRY =
			"CREATE TABLE IF NOT EXISTS acl_entry(\n" + "id BIGSERIAL PRIMARY KEY,\n"
					+ "acl_object_identity BIGINT NOT NULL,\n" + "ace_order INT NOT NULL,\n" + "sid BIGINT NOT NULL,\n"
					+ "mask INTEGER NOT NULL,\n" + "granting BOOLEAN NOT NULL,\n" + "audit_success BOOLEAN NOT NULL,\n"
					+ "audit_failure BOOLEAN NOT NULL,\n"
					+ "CONSTRAINT unique_uk_4 UNIQUE(acl_object_identity,ace_order),\n"
					+ "CONSTRAINT foreign_fk_4 FOREIGN KEY(acl_object_identity) REFERENCES acl_object_identity(id),\n"
					+ "CONSTRAINT foreign_fk_5 FOREIGN KEY(sid) REFERENCES acl_sid(id)\n" + ");";

	// Constructor autowiring leads to circular dependency
	@SuppressWarnings("SpringAutowiredFieldsWarningInspection")
	@Autowired
	private DataSource dataSource;

	@PostConstruct
	public void init()
	{
		try (Connection connection = dataSource.getConnection())
		{
			try (Statement statement = connection.createStatement())
			{
				statement.execute(SQL_CREATE_TABLE_ACL_SID);
			}
			try (Statement statement = connection.createStatement())
			{
				statement.execute(SQL_CREATE_TABLE_ACL_CLASS);
			}
			try (Statement statement = connection.createStatement())
			{
				statement.execute(SQL_CREATE_TABLE_ACL_OBJECT_IDENTITY);
			}
			try (Statement statement = connection.createStatement())
			{
				statement.execute(SQL_CREATE_TABLE_ACL_ENTRY);
			}
		}
		catch (SQLException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Autowired
	private AclCache aclCache;

	private org.springframework.security.acls.model.AclCache aclCache()
	{
		return new SpringCacheBasedAclCache(new TransactionAwareCacheDecorator(aclCache), permissionGrantingStrategy(),
				aclAuthorizationStrategy());
	}

	private LookupStrategy lookupStrategy()
	{
		return new AclLookupStrategy(dataSource, aclCache(), aclAuthorizationStrategy(), permissionGrantingStrategy());
	}

	@Bean
	AuditLogger auditLogger()
	{
		return new ConsoleAuditLogger();
	}

	private PermissionGrantingStrategy permissionGrantingStrategy()
	{
		return new BitMaskPermissionGrantingStrategy(auditLogger());
	}

	private AclAuthorizationStrategy aclAuthorizationStrategy()
	{
		return new AclAuthorizationStrategyImpl(new SimpleGrantedAuthority(ROLE_ACL_ADMIN),
				new SimpleGrantedAuthority(ROLE_ACL_ADMIN), new SimpleGrantedAuthority(ROLE_ACL_ADMIN));
	}

	@Bean
	SidRetrievalStrategy sidRetrievalStrategy()
	{
		return new SidRetrievalStrategyImpl();
	}

	@Bean
	JdbcMutableAclService aclService()
	{
		JdbcMutableAclService service = new AclService(dataSource, lookupStrategy(), aclCache());
		service.setClassIdentityQuery("select currval(pg_get_serial_sequence('acl_class', 'id'))");
		service.setSidIdentityQuery("select currval(pg_get_serial_sequence('acl_sid', 'id'))");
		return service;
	}

	// TODO kan weg, want we gebruiken ze niet? of wel voor apis, plugins, system entities etc.
	@Override
	protected MethodSecurityExpressionHandler createExpressionHandler()
	{
		DefaultMethodSecurityExpressionHandler expressionHandler = new DefaultMethodSecurityExpressionHandler();
		expressionHandler.setPermissionEvaluator(new AclPermissionEvaluator(aclService()));
		return expressionHandler;
	}
}
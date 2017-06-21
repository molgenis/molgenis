package org.molgenis.security.config;

import org.molgenis.DatabaseConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.support.NoOpCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.acls.AclPermissionEvaluator;
import org.springframework.security.acls.domain.*;
import org.springframework.security.acls.jdbc.BasicLookupStrategy;
import org.springframework.security.acls.jdbc.JdbcMutableAclService;
import org.springframework.security.acls.jdbc.LookupStrategy;
import org.springframework.security.acls.model.AclCache;
import org.springframework.security.acls.model.PermissionGrantingStrategy;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static java.util.Objects.requireNonNull;

/**
 * TODO Replace AclCache.NoOpCache with another cache implementation
 * TODO Extract ACL table population to other class
 * TODO Avoid name collisions between ACL table names and entity type table names
 */
@Import(DatabaseConfig.class)
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class ACLConfig extends GlobalMethodSecurityConfiguration
{

	private static final String SQL_CREATE_TABLE_ACL_SID =
			"create table if not exists acl_sid(\n" + "id bigserial not null primary key,\n"
					+ "principal boolean not null,\n" + "sid varchar(100) not null,\n"
					+ "constraint unique_uk_1 unique(sid,principal)\n" + ");";
	private static final String SQL_CREATE_TABLE_ACL_CLASS =
			"create table if not exists acl_class(\n" + "id bigserial not null primary key,\n"
					+ "class varchar(100) not null,\n" + "constraint unique_uk_2 unique(class)\n" + ");";
	private static final String SQL_CREATE_TABLE_ACL_OBJECT_IDENTITY =
			"create table if not exists acl_object_identity(\n" + "id bigserial primary key,\n"
					+ "object_id_class bigint not null,\n" + "object_id_identity bigint not null,\n"
					+ "parent_object bigint,\n" + "owner_sid bigint,\n" + "entries_inheriting boolean not null,\n"
					+ "constraint unique_uk_3 unique(object_id_class,object_id_identity),\n"
					+ "constraint foreign_fk_1 foreign key(parent_object)references acl_object_identity(id),\n"
					+ "constraint foreign_fk_2 foreign key(object_id_class)references acl_class(id),\n"
					+ "constraint foreign_fk_3 foreign key(owner_sid)references acl_sid(id)\n" + ");";
	private static final String SQL_CREATE_TABLE_ACL_ENTRY =
			"create table if not exists acl_entry(\n" + "id bigserial primary key,\n"
					+ "acl_object_identity bigint not null,\n" + "ace_order int not null,\n" + "sid bigint not null,\n"
					+ "mask integer not null,\n" + "granting boolean not null,\n" + "audit_success boolean not null,\n"
					+ "audit_failure boolean not null,\n"
					+ "constraint unique_uk_4 unique(acl_object_identity,ace_order),\n"
					+ "constraint foreign_fk_4 foreign key(acl_object_identity) references acl_object_identity(id),\n"
					+ "constraint foreign_fk_5 foreign key(sid) references acl_sid(id)\n" + ");";

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

	private AclCache aclCache()
	{
		return new SpringCacheBasedAclCache(new NoOpCache("aclCache"), permissionGrantingStrategy(),
				aclAuthorizationStrategy());
	}

	private LookupStrategy lookupStrategy()
	{
		return new BasicLookupStrategy(dataSource, aclCache(), aclAuthorizationStrategy(), auditLogger());
	}

	@Bean
	AuditLogger auditLogger()
	{
		return new ConsoleAuditLogger();
	}

	private PermissionGrantingStrategy permissionGrantingStrategy()
	{
		return new DefaultPermissionGrantingStrategy(auditLogger());
	}

	private AclAuthorizationStrategy aclAuthorizationStrategy()
	{
		return new AclAuthorizationStrategyImpl(new SimpleGrantedAuthority("ROLE_ACL_ADMIN"),
				new SimpleGrantedAuthority("ROLE_ACL_ADMIN"), new SimpleGrantedAuthority("ROLE_ACL_ADMIN"));
	}

	@Bean
	JdbcMutableAclService aclService()
	{
		JdbcMutableAclService service = new JdbcMutableAclService(dataSource, lookupStrategy(), aclCache());
		service.setClassIdentityQuery("select currval(pg_get_serial_sequence('acl_class', 'id'))");
		service.setSidIdentityQuery("select currval(pg_get_serial_sequence('acl_sid', 'id'))");
		return service;
	}

	@Override
	protected MethodSecurityExpressionHandler createExpressionHandler()
	{
		DefaultMethodSecurityExpressionHandler expressionHandler = new DefaultMethodSecurityExpressionHandler();
		expressionHandler.setPermissionEvaluator(new AclPermissionEvaluator(aclService()));
		return expressionHandler;
	}
}
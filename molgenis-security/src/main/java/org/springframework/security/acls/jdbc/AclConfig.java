package org.springframework.security.acls.jdbc;

import org.molgenis.data.config.DataSourceConfig;
import org.molgenis.security.NoOpAuditLogger;
import org.molgenis.security.acl.BitMaskPermissionGrantingStrategy;
import org.molgenis.security.core.utils.SecurityUtils;
import org.springframework.cache.Cache;
import org.springframework.cache.support.NoOpCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.security.acls.AclPermissionEvaluator;
import org.springframework.security.acls.domain.AclAuthorizationStrategy;
import org.springframework.security.acls.domain.AclAuthorizationStrategyImpl;
import org.springframework.security.acls.domain.AuditLogger;
import org.springframework.security.acls.domain.SpringCacheBasedAclCache;
import org.springframework.security.acls.model.AclCache;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.acls.model.PermissionGrantingStrategy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import javax.sql.DataSource;

import static java.util.Objects.requireNonNull;

/**
 * TODO move to org.molgenis sub-package once we upgraded to a spring-security-acl release with https://github.com/spring-projects/spring-security/issues/4814.
 */
@Configuration
@Import(DataSourceConfig.class)
public class AclConfig
{
	private final DataSource dataSource;

	public AclConfig(DataSource dataSource)
	{
		this.dataSource = requireNonNull(dataSource);
	}

	@Bean
	public AuditLogger auditLogger()
	{
		return new NoOpAuditLogger();
	}

	@Bean
	public PermissionGrantingStrategy permissionGrantingStrategy()
	{
		return new BitMaskPermissionGrantingStrategy(auditLogger());
	}

	@Bean
	public AclAuthorizationStrategy aclAuthorizationStrategy()
	{
		GrantedAuthority gaTakeOwnership = new SimpleGrantedAuthority(SecurityUtils.ROLE_ACL_TAKE_OWNERSHIP);
		GrantedAuthority gaModifyAuditing = new SimpleGrantedAuthority(SecurityUtils.ROLE_ACL_MODIFY_AUDITING);
		GrantedAuthority gaGeneralChanges = new SimpleGrantedAuthority(SecurityUtils.ROLE_ACL_GENERAL_CHANGES);
		return new AclAuthorizationStrategyImpl(gaTakeOwnership, gaModifyAuditing, gaGeneralChanges);
	}

	@Bean
	public AclCache aclCache()
	{
		Cache cache = new NoOpCache("aclCache");
		return new SpringCacheBasedAclCache(cache, permissionGrantingStrategy(), aclAuthorizationStrategy());
	}

	@Bean
	public AclClassIdUtils aclClassIdUtils()
	{
		AclClassIdUtils aclClassIdUtils = new AclClassIdUtils();
		aclClassIdUtils.setConversionService(new DefaultConversionService());
		return aclClassIdUtils;
	}

	@Bean
	public LookupStrategy lookupStrategy()
	{
		BasicLookupStrategy basicLookupStrategy = new BasicLookupStrategy(dataSource, aclCache(),
				aclAuthorizationStrategy(), permissionGrantingStrategy());
		basicLookupStrategy.setAclClassIdSupported(true);
		basicLookupStrategy.setAclClassIdUtils(aclClassIdUtils());
		return basicLookupStrategy;
	}

	@Bean
	public MutableAclService aclService()
	{
		JdbcMutableAclService aclService = new JdbcMutableAclService(dataSource, lookupStrategy(), aclCache());
		aclService.setAclClassIdSupported(true);
		aclService.setAclClassIdUtils(aclClassIdUtils());
		aclService.setClassIdentityQuery("select currval(pg_get_serial_sequence('acl_class', 'id'))");
		aclService.setSidIdentityQuery("select currval(pg_get_serial_sequence('acl_sid', 'id'))");
		return aclService;
	}

	@Bean
	public AclPermissionEvaluator aclPermissionEvaluator()
	{
		return new AclPermissionEvaluator(aclService());
	}
}

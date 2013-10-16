package org.molgenis.service;

import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import javax.sql.DataSource;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.omx.WebAppSecurityConfig;
import org.molgenis.security.SecurityUtils;
import org.molgenis.service.${entity.name}Service;
import org.molgenis.service.${entity.name}ServiceTest.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;
import ${entity.namespace}.${entity.name};

@ContextConfiguration(classes =
{ Config.class, WebAppSecurityConfig.class })
public class ${entity.name}ServiceTest extends AbstractTestNGSpringContextTests
{
	@Configuration
	static class Config
	{
		@Bean
		public Database database()
		{
			return mock(Database.class);
		}

		@Bean
		public Database unsecuredDatabase()
		{
			return mock(Database.class);
		}

		@Bean
		public DataSource dataSource()
		{
			return mock(DataSource.class);
		}

		@Bean
		public ${entity.name}Service ${entity.name?uncap_first}Service()
		{
			return new ${entity.name}Service();
		}
	}

	@Autowired
	public ${entity.name}Service ${entity.name?uncap_first}Service;

	@Test
	public void create_securitySuperUser() throws DatabaseException
	{
		// test passes if no security exception is thrown
		setSecurityContextSuperUser();
		${entity.name} ${entity.name?uncap_first} = mock(${entity.name}.class);
		${entity.name?uncap_first}Service.create(${entity.name?uncap_first});
	}

<#if entity.system>
	@Test(expectedExceptions = AccessDeniedException.class)
	public void create_securityNonSuperUserRead() throws DatabaseException
	{
		// test passes if no security exception is thrown
		setSecurityContextNonSuperUserRead();
		${entity.name} ${entity.name?uncap_first} = mock(${entity.name}.class);
		${entity.name?uncap_first}Service.create(${entity.name?uncap_first});
	}
	
	@Test(expectedExceptions = AccessDeniedException.class)
	public void create_securityNonSuperUserWrite() throws DatabaseException
	{
		setSecurityContextNonSuperUserWrite();
		${entity.name} ${entity.name?uncap_first} = mock(${entity.name}.class);
		${entity.name?uncap_first}Service.create(${entity.name?uncap_first});
	}
<#else>
	@Test(expectedExceptions = AccessDeniedException.class)
	public void create_securityNonSuperUserRead() throws DatabaseException
	{
		// test passes if no security exception is thrown
		setSecurityContextNonSuperUserRead();
		${entity.name} ${entity.name?uncap_first} = mock(${entity.name}.class);
		${entity.name?uncap_first}Service.create(${entity.name?uncap_first});
	}
	
	@Test
	public void create_securityNonSuperUserWrite() throws DatabaseException
	{
		// test passes if no security exception is thrown
		setSecurityContextNonSuperUserWrite();
		${entity.name} ${entity.name?uncap_first} = mock(${entity.name}.class);
		${entity.name?uncap_first}Service.create(${entity.name?uncap_first});
	}
</#if>
	
	private void setSecurityContextSuperUser()
	{
		Collection<? extends GrantedAuthority> authorities = Arrays
				.<SimpleGrantedAuthority> asList(new SimpleGrantedAuthority(SecurityUtils.AUTHORITY_SU));
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken(null, null, authorities));
	}

	private void setSecurityContextNonSuperUserRead()
	{
		Collection<? extends GrantedAuthority> authorities = Arrays
				.<SimpleGrantedAuthority> asList(new SimpleGrantedAuthority(SecurityUtils.AUTHORITY_PLUGIN_READ_PREFIX + "${entity.name?upper_case}"));
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken(null, null, authorities));
	}
	
	private void setSecurityContextNonSuperUserWrite()
	{
		Collection<? extends GrantedAuthority> authorities = Arrays
				.<SimpleGrantedAuthority> asList(new SimpleGrantedAuthority(SecurityUtils.AUTHORITY_PLUGIN_READ_PREFIX + "${entity.name?upper_case}"), new SimpleGrantedAuthority(SecurityUtils.AUTHORITY_PLUGIN_WRITE_PREFIX + "${entity.name?upper_case}"));
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken(null, null, authorities));
	}
}

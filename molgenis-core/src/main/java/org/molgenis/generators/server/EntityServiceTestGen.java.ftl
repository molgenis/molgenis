<#include "GeneratorHelper.ftl">
package org.molgenis.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.lang.Integer;

import javax.sql.DataSource;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.model.elements.Model;
import org.molgenis.security.SecurityUtils;
import org.molgenis.service.${entity.name}Service;
import org.molgenis.service.${entity.name}ServiceTest.Config;
import org.molgenis.framework.db.QueryRule;
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
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.molgenis.security.user.MolgenisUserDetailsService;
import org.springframework.security.authentication.AuthenticationManager;
import org.testng.annotations.Test;
import ${entity.namespace}.${entity.name};

@ContextConfiguration(classes = {Config.class})
public class ${entity.name}ServiceTest extends AbstractTestNGSpringContextTests
{
	@Configuration
	@EnableWebSecurity
	@EnableGlobalMethodSecurity(prePostEnabled = true)
	static class Config extends WebSecurityConfigurerAdapter
	{
		@Bean
		public Database database() throws DatabaseException
		{
			Database db = mock(Database.class);
			Model model = mock(Model.class);
			when(db.getMetaData()).thenReturn(model);
			return db;
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
		
		@Override
		protected UserDetailsService userDetailsService()
		{
			return mock(MolgenisUserDetailsService.class);
		}
	
		@Override
		@Bean
		public UserDetailsService userDetailsServiceBean() throws Exception
		{
			return userDetailsService();
		}
		
		@Bean
		@Override
		public AuthenticationManager authenticationManagerBean() throws Exception
		{
			return super.authenticationManagerBean();
		}
	}

	@Autowired
	public ${entity.name}Service ${entity.name?uncap_first}Service;


	@Test
	public void create_securitySuperUser() throws DatabaseException
	{
		// test passes if no security exception is thrown
		this.setSecurityContextSuperUser();
		${entity.name} ${entity.name?uncap_first} = mock(${entity.name}.class);
		${entity.name?uncap_first}Service.create(${entity.name?uncap_first});
	}

	@Test(expectedExceptions = AccessDeniedException.class)
	public void create_securityNonSuperUserRead() throws DatabaseException
	{
		this.setSecurityContextNonSuperUserRead();
		${entity.name} ${entity.name?uncap_first} = mock(${entity.name}.class);
		${entity.name?uncap_first}Service.create(${entity.name?uncap_first});
	}

<#if entity.system>
	@Test(expectedExceptions = AccessDeniedException.class)
	public void create_securityNonSuperUserWrite() throws DatabaseException
	{
		this.setSecurityContextNonSuperUserWrite();
		${entity.name} ${entity.name?uncap_first} = mock(${entity.name}.class);
		${entity.name?uncap_first}Service.create(${entity.name?uncap_first});
	}
<#else>
	@Test
	public void create_securityNonSuperUserWrite() throws DatabaseException
	{
		// test passes if no security exception is thrown
		this.setSecurityContextNonSuperUserWrite();
		${entity.name} ${entity.name?uncap_first} = mock(${entity.name}.class);
		${entity.name?uncap_first}Service.create(${entity.name?uncap_first});
	}
</#if>


	@Test
	public void read_securitySuperUser() throws DatabaseException
	{
		// test passes if no security exception is thrown
		this.setSecurityContextSuperUser();
		${type(entity.primaryKey)} id = Integer.valueOf(1);
		${entity.name?uncap_first}Service.read(id);
	}
	
<#if entity.system>	
	@Test(expectedExceptions = AccessDeniedException.class)
	public void read_securityNonSuperUserRead() throws DatabaseException
	{
		this.setSecurityContextNonSuperUserRead();
		${type(entity.primaryKey)} id = Integer.valueOf(1);
		${entity.name?uncap_first}Service.read(id);
	}
<#else>
	@Test()
	public void read_securityNonSuperUserRead() throws DatabaseException
	{
		// test passes if no security exception is thrown
		this.setSecurityContextNonSuperUserRead();
		${type(entity.primaryKey)} id = Integer.valueOf(1);
		${entity.name?uncap_first}Service.read(id);
	}
</#if>
	
	
	@Test
	public void update_securitySuperUser() throws DatabaseException
	{
		// test passes if no security exception is thrown
		this.setSecurityContextSuperUser();
		${entity.name} ${entity.name?uncap_first} = mock(${entity.name}.class);
		${entity.name?uncap_first}Service.update(${entity.name?uncap_first});
	}
	
	@Test(expectedExceptions = AccessDeniedException.class)
	public void update_securityNonSuperUserRead() throws DatabaseException
	{
		this.setSecurityContextNonSuperUserRead();
		${entity.name} ${entity.name?uncap_first} = mock(${entity.name}.class);
		${entity.name?uncap_first}Service.update(${entity.name?uncap_first});
	}
	
<#if entity.system>	
	@Test(expectedExceptions = AccessDeniedException.class)
	public void update_securityNonSuperUserWrite() throws DatabaseException
	{
		this.setSecurityContextNonSuperUserWrite();
		${entity.name} ${entity.name?uncap_first} = mock(${entity.name}.class);
		${entity.name?uncap_first}Service.update(${entity.name?uncap_first});
	}
<#else>
	@Test()
	public void update_securityNonSuperUserWrite() throws DatabaseException
	{
		// test passes if no security exception is thrown
		this.setSecurityContextNonSuperUserWrite();
		${entity.name} ${entity.name?uncap_first} = mock(${entity.name}.class);
		${entity.name?uncap_first}Service.update(${entity.name?uncap_first});
	}
</#if>


	@Test
	public void deleteById_securitySuperUser() throws DatabaseException
	{
		// test passes if no security exception is thrown
		this.setSecurityContextSuperUser();
		${type(entity.primaryKey)} id = Integer.valueOf(1);
		${entity.name?uncap_first}Service.deleteById(id);
	}
	
	@Test(expectedExceptions = AccessDeniedException.class)
	public void deleteById_securityNonSuperUserRead() throws DatabaseException
	{
		this.setSecurityContextNonSuperUserRead();
		${type(entity.primaryKey)} id = Integer.valueOf(1);
		${entity.name?uncap_first}Service.deleteById(id);
	}
	
<#if entity.system>	
	@Test(expectedExceptions = AccessDeniedException.class)
	public void deleteById_securityNonSuperUserWrite() throws DatabaseException
	{
		this.setSecurityContextNonSuperUserWrite();
		${type(entity.primaryKey)} id = Integer.valueOf(1);
		${entity.name?uncap_first}Service.deleteById(id);
	}
<#else>
	@Test()
	public void deleteById_securityNonSuperUserWrite() throws DatabaseException
	{
		// test passes if no security exception is thrown
		this.setSecurityContextNonSuperUserWrite();
		${type(entity.primaryKey)} id = Integer.valueOf(1);
		${entity.name?uncap_first}Service.deleteById(id);
	}
</#if>
	
	
	@Test
	public void readAll_securitySuperUser() throws DatabaseException
	{
		// test passes if no security exception is thrown
		this.setSecurityContextSuperUser();
		${entity.name?uncap_first}Service.readAll();
	}
	
<#if entity.system>	
	@Test(expectedExceptions = AccessDeniedException.class)
	public void readAll_securityNonSuperUserRead() throws DatabaseException
	{
		this.setSecurityContextNonSuperUserRead();
		${entity.name?uncap_first}Service.readAll();
	}
<#else>
	@Test()
	public void readAll_securityNonSuperUserWrite() throws DatabaseException
	{
		// test passes if no security exception is thrown
		this.setSecurityContextNonSuperUserRead();
		${entity.name?uncap_first}Service.readAll();
	}
</#if>
	
	
	@Test
	@SuppressWarnings("unchecked")
	public void readAll_EntityPager_securitySuperUser() throws DatabaseException
	{
		// test passes if no security exception is thrown
		this.setSecurityContextSuperUser();
		Integer start = Integer.valueOf(1);
		Integer num = Integer.valueOf(1);
		List<QueryRule> queryRules = (List<QueryRule>) mock(List.class);
		${entity.name?uncap_first}Service.readAll(start.intValue(), num.intValue(), queryRules);
	}
	
<#if entity.system>	
	@Test(expectedExceptions = AccessDeniedException.class)
	@SuppressWarnings("unchecked")
	public void readAll_EntityPager_securityNonSuperUserRead() throws DatabaseException
	{
		this.setSecurityContextNonSuperUserRead();
				Integer start = Integer.valueOf(1);
		Integer num = Integer.valueOf(1);
		List<QueryRule> queryRules = (List<QueryRule>) mock(List.class);
		${entity.name?uncap_first}Service.readAll(start.intValue(), num.intValue(), queryRules);
	}
<#else>
	@Test()
	@SuppressWarnings("unchecked")
	public void readAll_EntityPager_securityNonSuperUserWrite() throws DatabaseException
	{
		// test passes if no security exception is thrown
		this.setSecurityContextNonSuperUserRead();
		Integer start = Integer.valueOf(1);
		Integer num = Integer.valueOf(1);
		List<QueryRule> queryRules = (List<QueryRule>) mock(List.class);
		${entity.name?uncap_first}Service.readAll(start.intValue(), num.intValue(), queryRules);
	}
</#if>


	@Test
	public void getEntity_EntityPager_securitySuperUser() throws DatabaseException
	{
		// test passes if no security exception is thrown
		this.setSecurityContextSuperUser();
		${entity.name?uncap_first}Service.getEntity();
	}
	
<#if entity.system>	
	@Test(expectedExceptions = AccessDeniedException.class)
	public void getEntity_securityNonSuperUserRead() throws DatabaseException
	{
		this.setSecurityContextNonSuperUserRead();
		${entity.name?uncap_first}Service.getEntity();
	}
<#else>
	@Test()
	public void getEntity_securityNonSuperUserWrite() throws DatabaseException
	{
		// test passes if no security exception is thrown
		this.setSecurityContextNonSuperUserRead();
		${entity.name?uncap_first}Service.getEntity();
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
				.<SimpleGrantedAuthority> asList(new SimpleGrantedAuthority(SecurityUtils.AUTHORITY_ENTITY_READ_PREFIX + "${entity.name?upper_case}"));
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken(null, null, authorities));
	}
	
	private void setSecurityContextNonSuperUserWrite()
	{
		Collection<? extends GrantedAuthority> authorities = Arrays
				.<SimpleGrantedAuthority> asList(new SimpleGrantedAuthority(SecurityUtils.AUTHORITY_ENTITY_READ_PREFIX + "${entity.name?upper_case}"), new SimpleGrantedAuthority(SecurityUtils.AUTHORITY_ENTITY_WRITE_PREFIX + "${entity.name?upper_case}"));
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken(null, null, authorities));
	}
}

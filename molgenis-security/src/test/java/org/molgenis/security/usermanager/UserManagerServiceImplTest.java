package org.molgenis.security.usermanager;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.sql.DataSource;
import javax.validation.constraints.AssertTrue;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.Query;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
import org.molgenis.model.elements.Model;
import org.molgenis.omx.auth.MolgenisGroup;
import org.molgenis.omx.auth.MolgenisGroupMember;
import org.molgenis.omx.auth.MolgenisUser;
import org.molgenis.security.SecurityUtils;
import org.molgenis.security.user.MolgenisUserDetailsService;
import org.molgenis.security.usermanager.UserManagerServiceImplTest.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

@ContextConfiguration(classes =
{ Config.class })
public class UserManagerServiceImplTest extends AbstractTestNGSpringContextTests
{
	@Configuration
	@EnableWebSecurity
	@EnableGlobalMethodSecurity(prePostEnabled = true)
	static class Config extends WebSecurityConfigurerAdapter
	{
		@Bean
		public Database databaseBean() throws DatabaseException
		{
			Database db = mock(Database.class);
			Model model = mock(Model.class);
			when(db.getMetaData()).thenReturn(model);
			return db;
		}

		@Bean
		public DataSource dataSourceBean()
		{
			return mock(DataSource.class);
		}

		@Bean
		public UserManagerService userManagerService() throws DatabaseException
		{
			return new UserManagerServiceImpl(databaseBean());
		}

		@Override
		protected UserDetailsService userDetailsService()
		{
			return mock(MolgenisUserDetailsService.class);
		}

		@Bean
		@Override
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
	private UserManagerService userManagerService;

	@Autowired
	private Database database;

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void userManagerServiceImpl()
	{
		new UserManagerServiceImpl(null);
	}

	@Test
	public void getAllMolgenisUsers_SU() throws DatabaseException
	{
		this.setSecurityContextSuperUser();
		userManagerService.getAllMolgenisUsers();
	}

	@Test(expectedExceptions = AccessDeniedException.class)
	public void getAllMolgenisUsers_Non_SU() throws DatabaseException
	{
		this.setSecurityContextNonSuperUserWrite();
		userManagerService.getAllMolgenisUsers();
	}

	@Test
	public void getAllMolgenisGroups_SU() throws DatabaseException
	{
		this.setSecurityContextSuperUser();
		userManagerService.getAllMolgenisGroups();
	}

	@Test(expectedExceptions = AccessDeniedException.class)
	public void getAllMolgenisGroups_Non_SU() throws DatabaseException
	{
		this.setSecurityContextNonSuperUserWrite();
		userManagerService.getAllMolgenisGroups();
	}

	@Test
	public void getGroupsWhereUserIsMember() throws DatabaseException
	{
		this.setSecurityContextSuperUser();


		final MolgenisGroupMember molgenisGroupMemberOne = mock(MolgenisGroupMember.class);
		molgenisGroupMemberOne.setMolgenisGroup(Integer.valueOf("20"));
		molgenisGroupMemberOne.setMolgenisUser(Integer.valueOf("1"));

		final MolgenisGroupMember molgenisGroupMemberTwo = mock(MolgenisGroupMember.class);
		molgenisGroupMemberTwo.setMolgenisGroup(Integer.valueOf("21"));
		molgenisGroupMemberTwo.setMolgenisUser(Integer.valueOf("1"));

		final MolgenisGroupMember molgenisGroupMemberThree = mock(MolgenisGroupMember.class);
		molgenisGroupMemberThree.setMolgenisGroup(Integer.valueOf("22"));
		molgenisGroupMemberThree.setMolgenisUser(Integer.valueOf("2"));
				
		MolgenisUser user1 = when(mock(MolgenisUser.class).getId()).thenReturn(Integer.valueOf("1")).getMock();
		Query<MolgenisUser> queryUser = mock(Query.class);
		Query<MolgenisUser> queryUser1 = mock(Query.class);
		when(queryUser.eq(MolgenisUser.ID, Integer.valueOf("1"))).thenReturn(queryUser1);
		when(queryUser.eq(MolgenisUser.ID, Integer.valueOf("-1"))).thenReturn(queryUser);
		when(queryUser1.find()).thenReturn(Arrays.<MolgenisUser> asList(user1));
		when(database.query(MolgenisUser.class)).thenReturn(queryUser);
		
		when(
				database.find(MolgenisGroupMember.class, new QueryRule(MolgenisGroupMember.MOLGENISUSER,
						Operator.EQUALS, user1))).thenReturn(
				Arrays.asList(molgenisGroupMemberOne, molgenisGroupMemberTwo, molgenisGroupMemberThree));

		List<MolgenisGroup> groups = userManagerService.getGroupsWhereUserIsMember(Integer.valueOf("1"));

		System.out.println("groups.size(): " + groups.size());
		
		assertEquals(groups.size(), 2);
	}

	//
	// public List<MolgenisGroup> getGroupsWhereUserIsMember(Integer userId) throws DatabaseException;
	//
	// public List<MolgenisGroup> getGroupsWhereUserIsNotMember(Integer userId) throws DatabaseException;
	//
	// public List<MolgenisUserViewData> getUsersMemberInGroup(Integer groupId) throws DatabaseException;
	//
	// public Integer addUserToGroup(Integer molgenisGroupId, Integer molgenisUserId) throws DatabaseException;
	//
	// public Integer removeUserFromGroup(Integer molgenisGroupId, Integer molgenisUserId) throws DatabaseException;

	private void setSecurityContextSuperUser()
	{
		Collection<? extends GrantedAuthority> authorities = Arrays
				.<SimpleGrantedAuthority> asList(new SimpleGrantedAuthority(SecurityUtils.AUTHORITY_SU));
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken(null, null, authorities));
	}

	private void setSecurityContextNonSuperUserWrite()
	{
		Collection<? extends GrantedAuthority> authorities = Arrays.<SimpleGrantedAuthority> asList(
				new SimpleGrantedAuthority(SecurityUtils.AUTHORITY_PLUGIN_READ_PREFIX + "USERMANAGER"),
				new SimpleGrantedAuthority(SecurityUtils.AUTHORITY_PLUGIN_WRITE_PREFIX + "USERMANAGER"));
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken(null, null, authorities));
	}
}

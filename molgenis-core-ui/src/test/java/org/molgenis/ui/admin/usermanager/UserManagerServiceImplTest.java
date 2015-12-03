package org.molgenis.ui.admin.usermanager;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.molgenis.auth.MolgenisGroup;
import org.molgenis.auth.MolgenisGroupMember;
import org.molgenis.auth.MolgenisUser;
import org.molgenis.data.DataService;
import org.molgenis.data.Query;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.core.utils.SecurityUtils;
import org.molgenis.security.user.MolgenisUserDetailsService;
import org.molgenis.ui.admin.usermanager.MolgenisUserViewData;
import org.molgenis.ui.admin.usermanager.UserManagerService;
import org.molgenis.ui.admin.usermanager.UserManagerServiceImpl;
import org.molgenis.ui.admin.usermanager.UserManagerServiceImplTest.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@ContextConfiguration(classes =
{ Config.class })
public class UserManagerServiceImplTest extends AbstractTestNGSpringContextTests
{
	private static Authentication AUTHENTICATION_PREVIOUS;

	@Configuration
	@EnableWebSecurity
	@EnableGlobalMethodSecurity(prePostEnabled = true)
	static class Config extends WebSecurityConfigurerAdapter
	{
		@Bean
		public DataService dataService()
		{
			return mock(DataService.class);
		}

		@Bean
		public UserManagerService userManagerService()
		{
			return new UserManagerServiceImpl(dataService());
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

		@Autowired
		@Override
		public void configure(AuthenticationManagerBuilder auth) throws Exception
		{
			auth.inMemoryAuthentication().withUser("user").password("password").authorities("ROLE_USER");
		}
	}

	@Autowired
	private UserManagerService userManagerService;

	@Autowired
	private DataService dataService;

	@BeforeClass
	public void setUpBeforeClass()
	{
		AUTHENTICATION_PREVIOUS = SecurityContextHolder.getContext().getAuthentication();
	}

	@AfterClass
	public static void tearDownAfterClass()
	{
		SecurityContextHolder.getContext().setAuthentication(AUTHENTICATION_PREVIOUS);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void userManagerServiceImpl()
	{
		new UserManagerServiceImpl(null);
	}

	@Test
	public void getAllMolgenisUsersSu()
	{
		this.setSecurityContextSuperUser();
		this.userManagerService.getAllMolgenisUsers();
	}

	@Test(expectedExceptions = AccessDeniedException.class)
	public void getAllMolgenisUsersNonSu()
	{
		this.setSecurityContextNonSuperUserWrite();
		this.userManagerService.getAllMolgenisUsers();
	}

	@Test
	public void getAllMolgenisGroupsSu()
	{
		this.setSecurityContextSuperUser();
		this.userManagerService.getAllMolgenisGroups();
	}

	@Test(expectedExceptions = AccessDeniedException.class)
	public void getAllMolgenisGroups_Non_SU()
	{
		this.setSecurityContextNonSuperUserWrite();
		this.userManagerService.getAllMolgenisGroups();
	}

	@Test(expectedExceptions = AccessDeniedException.class)
	public void getGroupsWhereUserIsMemberNonUs()
	{
		this.setSecurityContextNonSuperUserWrite();
		this.userManagerService.getGroupsWhereUserIsMember("1");
	}

	@Test
	public void getGroupsWhereUserIsMemberSu()
	{
		this.setSecurityContextSuperUser();

		MolgenisUser user1 = when(mock(MolgenisUser.class).getId()).thenReturn("1").getMock();
		MolgenisGroup group20 = mock(MolgenisGroup.class);
		MolgenisGroup group21 = mock(MolgenisGroup.class);

		final MolgenisGroupMember molgenisGroupMemberOne = mock(MolgenisGroupMember.class);
		molgenisGroupMemberOne.setMolgenisGroup(group20);
		molgenisGroupMemberOne.setMolgenisUser(user1);

		final MolgenisGroupMember molgenisGroupMemberTwo = mock(MolgenisGroupMember.class);
		molgenisGroupMemberTwo.setMolgenisGroup(group21);
		molgenisGroupMemberTwo.setMolgenisUser(user1);

		when(dataService.findOne(MolgenisUser.ENTITY_NAME, "1", MolgenisUser.class)).thenReturn(user1);
		when(
				dataService.findAll(MolgenisGroupMember.ENTITY_NAME,
						new QueryImpl().eq(MolgenisGroupMember.MOLGENISUSER, user1), MolgenisGroupMember.class))
				.thenReturn(Arrays.asList(molgenisGroupMemberOne, molgenisGroupMemberTwo));
		List<MolgenisGroup> groups = this.userManagerService.getGroupsWhereUserIsMember("1");

		assertEquals(groups.size(), 2);
	}

	@Test(expectedExceptions = AccessDeniedException.class)
	public void getUsersMemberInGroupNonUs()
	{
		this.setSecurityContextNonSuperUserWrite();
		this.userManagerService.getUsersMemberInGroup("22");
	}

	@Test
	public void getUsersMemberInGroup()
	{
		this.setSecurityContextSuperUser();

		MolgenisUser user1 = mock(MolgenisUser.class);
		when(user1.getId()).thenReturn("1");
		when(user1.getUsername()).thenReturn("Jonathan");

		MolgenisGroup group22 = when(mock(MolgenisGroup.class).getId()).thenReturn("22").getMock();

		MolgenisGroupMember molgenisGroupMember = mock(MolgenisGroupMember.class);
		when(molgenisGroupMember.getMolgenisUser()).thenReturn(user1);
		when(molgenisGroupMember.getMolgenisGroup()).thenReturn(group22);

		when(dataService.findOne(MolgenisGroup.ENTITY_NAME, "22", MolgenisGroup.class)).thenReturn(group22);
		when(
				dataService.findAll(MolgenisGroupMember.ENTITY_NAME,
						new QueryImpl().eq(MolgenisGroupMember.MOLGENISGROUP, group22), MolgenisGroupMember.class))
				.thenReturn(Arrays.asList(molgenisGroupMember));

		List<MolgenisUserViewData> users = this.userManagerService.getUsersMemberInGroup("22");
		assertEquals(users.size(), 1);
	}

	@Test(expectedExceptions = AccessDeniedException.class)
	public void getGroupsWhereUserIsNotMemberNonUs()
	{
		this.setSecurityContextNonSuperUserWrite();
		this.userManagerService.getGroupsWhereUserIsNotMember("1");
	}

	@Test
	public void getGroupsWhereUserIsNotMemberUs()
	{
		setSecurityContextSuperUser();

		MolgenisUser user1 = when(mock(MolgenisUser.class).getId()).thenReturn("1").getMock();
		MolgenisGroup group22 = when(mock(MolgenisGroup.class).getId()).thenReturn("22").getMock();
		MolgenisGroup group33 = when(mock(MolgenisGroup.class).getId()).thenReturn("33").getMock();
		MolgenisGroup group44 = when(mock(MolgenisGroup.class).getId()).thenReturn("44").getMock();

		MolgenisGroupMember molgenisGroupMember = mock(MolgenisGroupMember.class);
		when(molgenisGroupMember.getMolgenisUser()).thenReturn(user1);
		when(molgenisGroupMember.getMolgenisGroup()).thenReturn(group22);

		when(dataService.findOne(MolgenisUser.ENTITY_NAME, "1", MolgenisUser.class)).thenReturn(user1);

		when(
				dataService.findAll(MolgenisGroupMember.ENTITY_NAME,
						new QueryImpl().eq(MolgenisGroupMember.MOLGENISUSER, user1), MolgenisGroupMember.class))
				.thenReturn(Arrays.asList(molgenisGroupMember));
		when(dataService.findAll(MolgenisGroup.ENTITY_NAME, MolgenisGroup.class)).thenReturn(
				Arrays.asList(group22, group33, group44));

		List<MolgenisGroup> groups = this.userManagerService.getGroupsWhereUserIsNotMember("1");
		assertEquals(groups.size(), 2);
	}

	@Test
	public void addUserToGroupSu() throws NumberFormatException
	{
		setSecurityContextSuperUser();
		this.userManagerService.addUserToGroup("22", "1");
	}

	@Test(expectedExceptions = AccessDeniedException.class)
	public void addUserToGroupNonSu() throws NumberFormatException
	{
		setSecurityContextNonSuperUserWrite();
		this.userManagerService.addUserToGroup("22", "1");
	}

	@Test
	public void removeUserFromGroupSu() throws NumberFormatException
	{
		setSecurityContextSuperUser();

		MolgenisUser user1 = when(mock(MolgenisUser.class).getId()).thenReturn("1").getMock();
		MolgenisGroup group22 = when(mock(MolgenisGroup.class).getId()).thenReturn("22").getMock();
		MolgenisGroupMember molgenisGroupMember = mock(MolgenisGroupMember.class);
		when(molgenisGroupMember.getMolgenisUser()).thenReturn(user1);
		when(molgenisGroupMember.getMolgenisGroup()).thenReturn(group22);

		when(dataService.findOne(MolgenisUser.ENTITY_NAME, "1", MolgenisUser.class)).thenReturn(user1);
		when(dataService.findOne(MolgenisGroup.ENTITY_NAME, "22", MolgenisGroup.class)).thenReturn(group22);

		Query q = new QueryImpl().eq(MolgenisGroupMember.MOLGENISUSER, user1).and()
				.eq(MolgenisGroupMember.MOLGENISGROUP, group22);

		when(dataService.findAll(MolgenisGroupMember.ENTITY_NAME, q, MolgenisGroupMember.class)).thenReturn(
				Arrays.asList(molgenisGroupMember));

		this.userManagerService.removeUserFromGroup("22", "1");
	}

	@Test(expectedExceptions = AccessDeniedException.class)
	public void removeUserFromGroupNonSu() throws NumberFormatException
	{
		setSecurityContextNonSuperUserWrite();
		this.userManagerService.removeUserFromGroup("22", "1");
	}

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

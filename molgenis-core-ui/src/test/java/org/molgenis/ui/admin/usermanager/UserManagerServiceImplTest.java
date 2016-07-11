package org.molgenis.ui.admin.usermanager;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.molgenis.auth.MolgenisGroupMemberMetaData.MOLGENIS_GROUP_MEMBER;
import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.molgenis.auth.MolgenisGroup;
import org.molgenis.auth.MolgenisGroupMember;
import org.molgenis.auth.MolgenisGroupMemberFactory;
import org.molgenis.auth.MolgenisGroupMemberMetaData;
import org.molgenis.auth.MolgenisGroupMetaData;
import org.molgenis.auth.MolgenisUser;
import org.molgenis.auth.MolgenisUserMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Query;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.core.utils.SecurityUtils;
import org.molgenis.security.user.MolgenisUserDetailsService;
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

@ContextConfiguration(classes = { Config.class })
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
			return new UserManagerServiceImpl(dataService(), molgenisGroupMemberFactory());
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

		@Bean
		MolgenisGroupMemberFactory molgenisGroupMemberFactory()
		{
			return mock(MolgenisGroupMemberFactory.class);
		}
	}

	@Autowired
	private MolgenisGroupMemberFactory molgenisGroupMemberFactory;

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

	@Test(expectedExceptions = NullPointerException.class)
	public void userManagerServiceImpl()
	{
		new UserManagerServiceImpl(null, molgenisGroupMemberFactory);
	}

	@Test
	public void getAllMolgenisUsersSu()
	{
		String molgenisUserId0 = "id0";
		String molgenisUserName0 = "user0";
		MolgenisUser molgenisUser0 = when(mock(MolgenisUser.class).getId()).thenReturn(molgenisUserId0).getMock();
		when(molgenisUser0.getUsername()).thenReturn(molgenisUserName0);
		String molgenisUserId1 = "id1";
		String molgenisUserName1 = "user1";
		MolgenisUser molgenisUser1 = when(mock(MolgenisUser.class).getId()).thenReturn(molgenisUserId1).getMock();
		when(molgenisUser1.getUsername()).thenReturn(molgenisUserName1);
		when(dataService.findOneById(MolgenisUserMetaData.MOLGENIS_USER, molgenisUserId0, MolgenisUser.class))
				.thenReturn(molgenisUser0);
		when(dataService.findOneById(MolgenisUserMetaData.MOLGENIS_USER, molgenisUserId1, MolgenisUser.class))
				.thenReturn(molgenisUser1);
		when(dataService.findAll(MolgenisUserMetaData.MOLGENIS_USER, MolgenisUser.class))
				.thenReturn(Stream.of(molgenisUser0, molgenisUser1));
		MolgenisGroupMember molgenisGroupMember0 = mock(MolgenisGroupMember.class);
		MolgenisGroup molgenisGroup0 = mock(MolgenisGroup.class);
		when(molgenisGroupMember0.getMolgenisGroup()).thenReturn(molgenisGroup0);
		MolgenisGroupMember molgenisGroupMember1 = mock(MolgenisGroupMember.class);
		MolgenisGroup molgenisGroup1 = mock(MolgenisGroup.class);
		when(molgenisGroupMember1.getMolgenisGroup()).thenReturn(molgenisGroup1);
		when(dataService.findAll(MOLGENIS_GROUP_MEMBER,
				new QueryImpl<MolgenisGroupMember>().eq(MolgenisGroupMemberMetaData.MOLGENIS_USER, molgenisUser0),
				MolgenisGroupMember.class)).thenReturn(Stream.of(molgenisGroupMember0));
		when(dataService.findAll(MOLGENIS_GROUP_MEMBER,
				new QueryImpl<MolgenisGroupMember>().eq(MolgenisGroupMemberMetaData.MOLGENIS_USER, molgenisUser1),
				MolgenisGroupMember.class)).thenReturn(Stream.of(molgenisGroupMember1));
		this.setSecurityContextSuperUser();
		assertEquals(userManagerService.getAllMolgenisUsers(),
				Arrays.asList(new MolgenisUserViewData(molgenisUser0, singletonList(molgenisGroup0)),
						new MolgenisUserViewData(molgenisUser1, singletonList(molgenisGroup1))));
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
		MolgenisGroup molgenisGroup0 = mock(MolgenisGroup.class);
		MolgenisGroup molgenisGroup1 = mock(MolgenisGroup.class);
		when(dataService.findAll(MolgenisGroupMetaData.MOLGENIS_GROUP, MolgenisGroup.class))
				.thenReturn(Stream.of(molgenisGroup0, molgenisGroup1));
		this.setSecurityContextSuperUser();
		assertEquals(userManagerService.getAllMolgenisGroups(), Arrays.asList(molgenisGroup0, molgenisGroup1));
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

		when(dataService.findOneById(MolgenisUserMetaData.MOLGENIS_USER, "1", MolgenisUser.class)).thenReturn(user1);
		when(dataService.findAll(MOLGENIS_GROUP_MEMBER,
				new QueryImpl<MolgenisGroupMember>().eq(MolgenisGroupMemberMetaData.MOLGENIS_USER, user1),
				MolgenisGroupMember.class)).thenReturn(Stream.of(molgenisGroupMemberOne, molgenisGroupMemberTwo));
		List<MolgenisGroup> groups = this.userManagerService.getGroupsWhereUserIsMember("1");

		assertEquals(groups.size(), 2);
	}

	@Test(expectedExceptions = AccessDeniedException.class)
	public void getUsersMemberInGroupNonSu()
	{
		this.setSecurityContextNonSuperUserWrite();
		this.userManagerService.getUsersMemberInGroup("22");
	}

	@Test
	public void getUsersMemberInGroup()
	{
		reset(dataService);

		this.setSecurityContextSuperUser();

		MolgenisUser user1 = mock(MolgenisUser.class);
		when(user1.getId()).thenReturn("1");
		when(user1.getUsername()).thenReturn("Jonathan");

		MolgenisGroup group22 = when(mock(MolgenisGroup.class).getId()).thenReturn("22").getMock();

		MolgenisGroupMember molgenisGroupMember = mock(MolgenisGroupMember.class);
		when(molgenisGroupMember.getMolgenisUser()).thenReturn(user1);
		when(molgenisGroupMember.getMolgenisGroup()).thenReturn(group22);

		when(dataService.findOneById(MolgenisUserMetaData.MOLGENIS_USER, "1", MolgenisUser.class)).thenReturn(user1);
		when(dataService.findOneById(MolgenisGroupMetaData.MOLGENIS_GROUP, "22", MolgenisGroup.class))
				.thenReturn(group22);

		when(dataService.findAll(MOLGENIS_GROUP_MEMBER,
				new QueryImpl<MolgenisGroupMember>().eq(MolgenisGroupMemberMetaData.MOLGENIS_USER, user1),
				MolgenisGroupMember.class)).thenAnswer(new Answer<Stream<MolgenisGroupMember>>()
		{
			@Override
			public Stream<MolgenisGroupMember> answer(InvocationOnMock invocation) throws Throwable
			{
				return Stream.of(molgenisGroupMember);
			}
		});

		when(dataService.findAll(MOLGENIS_GROUP_MEMBER,
				new QueryImpl<MolgenisGroupMember>().eq(MolgenisGroupMemberMetaData.MOLGENIS_GROUP, group22),
				MolgenisGroupMember.class)).thenAnswer(new Answer<Stream<MolgenisGroupMember>>()
		{
			@Override
			public Stream<MolgenisGroupMember> answer(InvocationOnMock invocation) throws Throwable
			{
				return Stream.of(molgenisGroupMember);
			}
		});

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
	public void getGroupsWhereUserIsNotMemberSu()
	{
		setSecurityContextSuperUser();

		MolgenisUser user1 = when(mock(MolgenisUser.class).getId()).thenReturn("1").getMock();
		MolgenisGroup group22 = when(mock(MolgenisGroup.class).getId()).thenReturn("22").getMock();
		MolgenisGroup group33 = when(mock(MolgenisGroup.class).getId()).thenReturn("33").getMock();
		MolgenisGroup group44 = when(mock(MolgenisGroup.class).getId()).thenReturn("44").getMock();

		MolgenisGroupMember molgenisGroupMember = mock(MolgenisGroupMember.class);
		when(molgenisGroupMember.getMolgenisUser()).thenReturn(user1);
		when(molgenisGroupMember.getMolgenisGroup()).thenReturn(group22);

		when(dataService.findOneById(MolgenisUserMetaData.MOLGENIS_USER, "1", MolgenisUser.class)).thenReturn(user1);

		when(dataService.findAll(MolgenisGroupMemberMetaData.MOLGENIS_USER,
				new QueryImpl<MolgenisGroupMember>().eq(MolgenisGroupMemberMetaData.MOLGENIS_USER, user1),
				MolgenisGroupMember.class)).thenReturn(Stream.of(molgenisGroupMember));
		when(dataService.findAll(MolgenisGroupMetaData.MOLGENIS_GROUP, MolgenisGroup.class))
				.thenReturn(Stream.of(group22, group33, group44));

		molgenisGroupMember = mock(MolgenisGroupMember.class);
		when(molgenisGroupMember.getMolgenisGroup()).thenReturn(group22);

		List groupMemberships = new ArrayList<MolgenisGroupMember>();
		groupMemberships.add(molgenisGroupMember);

		when(dataService.findAll(MOLGENIS_GROUP_MEMBER,
				new QueryImpl<MolgenisGroupMember>().eq(MolgenisGroupMemberMetaData.MOLGENIS_USER, user1),
				MolgenisGroupMember.class)).thenReturn(groupMemberships.stream());

		List<MolgenisGroup> groups = this.userManagerService.getGroupsWhereUserIsNotMember("1");
		assertEquals(groups.size(), 2);
	}

	@Test
	public void addUserToGroupSu() throws NumberFormatException
	{
		when(molgenisGroupMemberFactory.create()).thenReturn(mock(MolgenisGroupMember.class));
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

		when(dataService.findOneById(MolgenisUserMetaData.MOLGENIS_USER, "1", MolgenisUser.class)).thenReturn(user1);
		when(dataService.findOneById(MolgenisGroupMetaData.MOLGENIS_GROUP, "22", MolgenisGroup.class))
				.thenReturn(group22);

		Query<MolgenisGroupMember> q = new QueryImpl<MolgenisGroupMember>()
				.eq(MolgenisGroupMemberMetaData.MOLGENIS_USER, user1).and()
				.eq(MolgenisGroupMemberMetaData.MOLGENIS_GROUP, group22);

		when(dataService.findAll(MOLGENIS_GROUP_MEMBER, q, MolgenisGroupMember.class))
				.thenReturn(Stream.of(molgenisGroupMember));

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
		Collection<? extends GrantedAuthority> authorities = Arrays.<SimpleGrantedAuthority>asList(
				new SimpleGrantedAuthority(SecurityUtils.AUTHORITY_SU));
		SecurityContextHolder.getContext()
				.setAuthentication(new UsernamePasswordAuthenticationToken(null, null, authorities));
	}

	private void setSecurityContextNonSuperUserWrite()
	{
		Collection<? extends GrantedAuthority> authorities = Arrays.<SimpleGrantedAuthority>asList(
				new SimpleGrantedAuthority(SecurityUtils.AUTHORITY_PLUGIN_READ_PREFIX + "USERMANAGER"),
				new SimpleGrantedAuthority(SecurityUtils.AUTHORITY_PLUGIN_WRITE_PREFIX + "USERMANAGER"));
		SecurityContextHolder.getContext()
				.setAuthentication(new UsernamePasswordAuthenticationToken(null, null, authorities));
	}
}

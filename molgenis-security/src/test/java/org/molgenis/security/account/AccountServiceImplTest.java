package org.molgenis.security.account;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;

@ContextConfiguration
public class AccountServiceImplTest extends AbstractTestNGSpringContextTests
{
	//	@Autowired
	//	private AccountService accountService;
	//
	//	@Autowired
	//	private DataService dataService;
	//
	//	@Autowired
	//	private JavaMailSender javaMailSender;
	//
	//	@Autowired
	//	private AppSettings appSettings;
	//
	//	@BeforeMethod
	//	public void setUp()
	//	{
	//		reset(dataService);
	//		when(appSettings.getSignUpModeration()).thenReturn(false);
	//
	//		MolgenisGroup allUsersGroup = mock(MolgenisGroup.class);
	//		when(dataService.findAll(MolgenisGroupMetaData.MOLGENIS_GROUP_MEMBER,
	//				new QueryImpl<MolgenisGroup>().eq(MolgenisGroupMetaData.NAME, AccountService.ALL_USER_GROUP), MolgenisGroup.class))
	//						.thenReturn(Arrays.asList(allUsersGroup).stream());
	//		reset(javaMailSender);
	//		MimeMessage mimeMessage = mock(MimeMessage.class);
	//		when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
	//	}
	//
	//	@Test
	//	public void activateUser()
	//	{
	//		when(dataService.findOne(MolgenisUserMetaData.MOLGENIS_GROUP_MEMBER,
	//				new QueryImpl<MolgenisUser>().eq(MolgenisUserMetaData.ACTIVE, false).and().eq(MolgenisUserMetaData.ACTIVATIONCODE, "123"),
	//				MolgenisUser.class)).thenReturn(new MolgenisUser());
	//
	//		accountService.activateUser("123");
	//
	//		ArgumentCaptor<MolgenisUser> argument = ArgumentCaptor.forClass(MolgenisUser.class);
	//		verify(dataService).update(eq(MolgenisUserMetaData.MOLGENIS_GROUP_MEMBER), argument.capture());
	//		assertTrue(argument.getValue().isActive());
	//		verify(javaMailSender).send(any(SimpleMailMessage.class));
	//		// TODO improve test
	//	}
	//
	//	@Test(expectedExceptions = MolgenisUserException.class)
	//	public void activateUser_invalidActivationCode()
	//	{
	//		accountService.activateUser("invalid");
	//	}
	//
	//	@Test(expectedExceptions = MolgenisUserException.class)
	//	public void activateUser_alreadyActivated()
	//	{
	//		when(dataService.findOne(MolgenisUserMetaData.MOLGENIS_GROUP_MEMBER,
	//				new QueryImpl<MolgenisUser>().eq(MolgenisUserMetaData.ACTIVE, false).eq(MolgenisUserMetaData.ACTIVATIONCODE, "456"),
	//				MolgenisUser.class)).thenReturn(null);
	//
	//		accountService.activateUser("456");
	//	}
	//
	//	@Test
	//	public void createUser() throws URISyntaxException, UsernameAlreadyExistsException, EmailAlreadyExistsException
	//	{
	//		MolgenisUser molgenisUser = new MolgenisUser();
	//		molgenisUser.setEmail("user@molgenis.org");
	//		accountService.createUser(molgenisUser, "http://molgenis.org/activate");
	//		ArgumentCaptor<MolgenisUser> argument = ArgumentCaptor.forClass(MolgenisUser.class);
	//		verify(dataService).add(eq(MolgenisUserMetaData.MOLGENIS_GROUP_MEMBER), argument.capture());
	//		assertFalse(argument.getValue().isActive());
	//		verify(javaMailSender).send(any(SimpleMailMessage.class));
	//		// TODO improve test
	//	}
	//
	//	@SuppressWarnings("unchecked")
	//	@Test
	//	public void resetPassword()
	//	{
	//		MolgenisUser molgenisUser = mock(MolgenisUser.class);
	//		when(molgenisUser.getPassword()).thenReturn("password");
	//		when(dataService.findOne(eq(MolgenisUserMetaData.MOLGENIS_GROUP_MEMBER), any(Query.class),
	//				(Class<Entity>) Matchers.notNull(MolgenisUser.class.getClass()))).thenReturn(molgenisUser);
	//
	//		accountService.resetPassword("user@molgenis.org");
	//		ArgumentCaptor<MolgenisUser> argument = ArgumentCaptor.forClass(MolgenisUser.class);
	//		verify(dataService).update(eq(MolgenisUserMetaData.MOLGENIS_GROUP_MEMBER), argument.capture());
	//		assertNotNull(argument.getValue().getPassword());
	//		verify(javaMailSender).send(any(SimpleMailMessage.class));
	//	}
	//
	//	@Test(expectedExceptions = MolgenisUserException.class)
	//	public void resetPassword_invalidEmailAddress()
	//	{
	//		MolgenisUser molgenisUser = mock(MolgenisUser.class);
	//		when(molgenisUser.getPassword()).thenReturn("password");
	//		when(dataService.findOne(MolgenisUserMetaData.MOLGENIS_GROUP_MEMBER,
	//				new QueryImpl<MolgenisUser>().eq(MolgenisUserMetaData.EMAIL, "invalid-user@molgenis.org"), MolgenisUser.class))
	//						.thenReturn(null);
	//
	//		accountService.resetPassword("invalid-user@molgenis.org");
	//	}
	//
	//	@Test
	//	public void changePassword()
	//	{
	//		MolgenisUser user = new MolgenisUser();
	//		user.setUsername("test");
	//		user.setPassword("oldpass");
	//
	//		when(dataService.findOne(
	//				MolgenisUserMetaData.MOLGENIS_GROUP_MEMBER, new QueryImpl<MolgenisUser>().eq(MolgenisUserMetaData.USERNAME, "test"),
	//				MolgenisUser.class)).thenReturn(user);
	//
	//		accountService.changePassword("test", "newpass");
	//
	//		verify(dataService).update(MolgenisUserMetaData.MOLGENIS_GROUP_MEMBER, user);
	//		assertNotEquals(user.getPassword(), "oldpass");
	//	}
	//
	//	@Configuration
	//	static class Config
	//	{
	//		@Bean
	//		public AccountService accountService()
	//		{
	//			return new AccountServiceImpl(dataService(), mailSender(), molgenisUserService(), appSettings(), );
	//		}
	//
	//		@Bean
	//		public DataService dataService()
	//		{
	//			return mock(DataService.class);
	//		}
	//
	//		@Bean
	//		public AppSettings appSettings()
	//		{
	//			return mock(AppSettings.class);
	//		}
	//
	//		@Bean
	//		public JavaMailSender mailSender()
	//		{
	//			return mock(JavaMailSender.class);
	//		}
	//
	//		@Bean
	//		public MolgenisUserService molgenisUserService()
	//		{
	//			return mock(MolgenisUserService.class);
	//		}
	//	}
}

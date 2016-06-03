package org.molgenis.security.account;

import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;

//import org.molgenis.security.account.AccountControllerTest.Config;

//@WebAppConfiguration
//@ContextConfiguration(classes =
//{ Config.class, GsonConfig.class })
public class AccountControllerTest extends AbstractTestNGSpringContextTests
{
	//	@Autowired
	//	private AccountController authenticationController;
	//
	//	@Autowired
	//	private AccountService accountService;
	//
	//	@Autowired
	//	private CaptchaService captchaService;
	//
	//	@Autowired
	//	private GsonHttpMessageConverter gsonHttpMessageConverter;
	//
	//	@Autowired
	//	private AppSettings appSettings;
	//
	//	private MockMvc mockMvc;
	//
	//	@BeforeMethod
	//	public void setUp() throws CaptchaException
	//	{
	//		FreeMarkerViewResolver freeMarkerViewResolver = new FreeMarkerViewResolver();
	//		freeMarkerViewResolver.setSuffix(".ftl");
	//		mockMvc = MockMvcBuilders.standaloneSetup(authenticationController)
	//				.setMessageConverters(new FormHttpMessageConverter(), gsonHttpMessageConverter).build();
	//
	//		reset(appSettings);
	//		reset(captchaService);
	//		when(captchaService.validateCaptcha("validCaptcha")).thenReturn(true);
	//		reset(accountService); // mocks in the config class are not resetted after each test
	//	}
	//
	//	@Test
	//	public void getLoginForm() throws Exception
	//	{
	//		this.mockMvc.perform(get("/account/login")).andExpect(status().isOk()).andExpect(view().name("login-modal"));
	//	}
	//
	//	@Test
	//	public void getPasswordResetForm() throws Exception
	//	{
	//		this.mockMvc.perform(get("/account/password/reset")).andExpect(status().isOk())
	//				.andExpect(view().name("resetpassword-modal"));
	//	}
	//
	//	@Test
	//	public void getRegisterForm() throws Exception
	//	{
	//		this.mockMvc.perform(get("/account/bootstrap")).andExpect(status().isOk())
	//				.andExpect(view().name("bootstrap-modal")).andExpect(model().attributeExists("countries"));
	//	}
	//
	//	@Test
	//	public void activateUser() throws Exception
	//	{
	//		this.mockMvc.perform(get("/account/activate/123")).andExpect(view().name("forward:/"));
	//		verify(accountService).activateUser("123");
	//	}
	//
	//	@Test
	//	public void registerUser_activationModeUserProxy() throws Exception
	//	{
	//		when(appSettings.getSignUp()).thenReturn(true);
	//		when(appSettings.getSignUpModeration()).thenReturn(false);
	//
	//		this.mockMvc
	//				.perform(post("/account/bootstrap").header("X-Forwarded-Host", "website.com").param("username", "admin")
	//						.param("password", "adminpw-invalid").param("confirmPassword", "adminpw-invalid")
	//						.param("email", "admin@molgenis.org").param("lastname", "min").param("firstname", "ad")
	//						.param("captcha", "validCaptcha").contentType(MediaType.APPLICATION_FORM_URLENCODED))
	//				.andExpect(status().isOk()).andExpect(content()
	//						.string("{\"message\":\"" + AccountController.REGISTRATION_SUCCESS_MESSAGE_USER + "\"}"));
	//		ArgumentCaptor<MolgenisUser> molgenisUserCaptor = ArgumentCaptor.forClass(MolgenisUser.class);
	//		ArgumentCaptor<String> baseActivationUriCaptor = ArgumentCaptor.forClass(String.class);
	//		verify(accountService).createUser(molgenisUserCaptor.capture(), baseActivationUriCaptor.capture());
	//		assertEquals(baseActivationUriCaptor.getValue(), "http://website.com/account/activate");
	//	}
	//
	//	@Test
	//	public void registerUser_activationModeUserProxyWithScheme() throws Exception
	//	{
	//		when(appSettings.getSignUp()).thenReturn(true);
	//		when(appSettings.getSignUpModeration()).thenReturn(false);
	//
	//		this.mockMvc
	//				.perform(post("/account/bootstrap").header("X-Forwarded-Proto", "https")
	//						.header("X-Forwarded-Host", "website.com").param("username", "admin")
	//						.param("password", "adminpw-invalid").param("confirmPassword", "adminpw-invalid")
	//						.param("email", "admin@molgenis.org").param("lastname", "min").param("firstname", "ad")
	//						.param("captcha", "validCaptcha").contentType(MediaType.APPLICATION_FORM_URLENCODED))
	//				.andExpect(status().isOk()).andExpect(content()
	//						.string("{\"message\":\"" + AccountController.REGISTRATION_SUCCESS_MESSAGE_USER + "\"}"));
	//		ArgumentCaptor<MolgenisUser> molgenisUserCaptor = ArgumentCaptor.forClass(MolgenisUser.class);
	//		ArgumentCaptor<String> baseActivationUriCaptor = ArgumentCaptor.forClass(String.class);
	//		verify(accountService).createUser(molgenisUserCaptor.capture(), baseActivationUriCaptor.capture());
	//		assertEquals(baseActivationUriCaptor.getValue(), "https://website.com/account/activate");
	//	}
	//
	//	@Test
	//	public void registerUser_activationModeUser() throws Exception
	//	{
	//		when(appSettings.getSignUp()).thenReturn(true);
	//		when(appSettings.getSignUpModeration()).thenReturn(false);
	//
	//		this.mockMvc
	//				.perform(post("/account/bootstrap").param("username", "admin").param("password", "adminpw-invalid")
	//						.param("confirmPassword", "adminpw-invalid").param("email", "admin@molgenis.org")
	//						.param("lastname", "min").param("firstname", "ad").param("captcha", "validCaptcha")
	//						.contentType(MediaType.APPLICATION_FORM_URLENCODED))
	//				.andExpect(status().isOk()).andExpect(content()
	//						.string("{\"message\":\"" + AccountController.REGISTRATION_SUCCESS_MESSAGE_USER + "\"}"));
	//		verify(captchaService).validateCaptcha("validCaptcha");
	//	}
	//
	//	@Test
	//	public void registerUser_activationModeAdmin() throws Exception
	//	{
	//		when(appSettings.getSignUp()).thenReturn(true);
	//		when(appSettings.getSignUpModeration()).thenReturn(true);
	//
	//		this.mockMvc
	//				.perform(post("/account/bootstrap").param("username", "admin").param("password", "adminpw-invalid")
	//						.param("confirmPassword", "adminpw-invalid").param("email", "admin@molgenis.org")
	//						.param("lastname", "min").param("firstname", "ad").param("captcha", "validCaptcha")
	//						.contentType(MediaType.APPLICATION_FORM_URLENCODED))
	//				.andExpect(status().isOk()).andExpect(content()
	//						.string("{\"message\":\"" + AccountController.REGISTRATION_SUCCESS_MESSAGE_ADMIN + "\"}"));
	//		verify(captchaService).validateCaptcha("validCaptcha");
	//	}
	//
	//	@Test
	//	public void registerUser_invalidRegisterRequest() throws Exception
	//	{
	//		// when(accountService.isSelfRegistrationEnabled()).thenReturn(true);
	//		this.mockMvc
	//				.perform(post("/account/bootstrap").param("username", "admin").param("password", "adminpw-invalid")
	//						.param("confirmPassword", "adminpw-invalid").param("lastname", "min").param("firstname", "ad")
	//						.param("captcha", "validCaptcha").contentType(MediaType.APPLICATION_FORM_URLENCODED))
	//				.andExpect(status().isBadRequest());
	//		verify(captchaService, times(0)).validateCaptcha("validCaptcha");
	//	}
	//
	//	@Test
	//	public void registerUser_passwordNotEqualsConfirmPassword() throws Exception
	//	{
	//		when(appSettings.getSignUp()).thenReturn(true);
	//		this.mockMvc.perform(post("/account/bootstrap").param("username", "admin").param("password", "adminpw-invalid")
	//				.param("confirmPassword", "adminpw-invalid-typo").param("email", "admin@molgenis.org")
	//				.param("lastname", "min").param("firstname", "ad").param("captcha", "validCaptcha")
	//				.contentType(MediaType.APPLICATION_FORM_URLENCODED)).andExpect(status().isBadRequest());
	//		verify(captchaService, times(0)).validateCaptcha("validCaptcha");
	//	}
	//
	//	@Test
	//	public void registerUser_invalidCaptcha() throws Exception
	//	{
	//		when(appSettings.getSignUp()).thenReturn(true);
	//		this.mockMvc.perform(post("/account/bootstrap").param("username", "admin").param("password", "adminpw-invalid")
	//				.param("confirmPassword", "adminpw-invalid").param("email", "admin@molgenis.org")
	//				.param("lastname", "min").param("firstname", "ad").param("captcha", "invalidCaptcha")
	//				.contentType(MediaType.APPLICATION_FORM_URLENCODED)).andExpect(status().isBadRequest());
	//	}
	//
	//	@Test
	//	public void resetPassword() throws Exception
	//	{
	//		this.mockMvc.perform(post("/account/password/reset").param("email", "admin@molgenis.org")
	//				.contentType(MediaType.APPLICATION_FORM_URLENCODED)).andExpect(status().isNoContent());
	//		verify(accountService).resetPassword("admin@molgenis.org");
	//	}
	//
	//	// @Test
	//	// public void registerUser_invalidUserField() throws Exception
	//	// {
	//	// this.mockMvc.perform(
	//	// post("/account/bootstrap").param("username", "admin").param("password", "adminpw-invalid")
	//	// .param("email", "admin@molgenis.org").param("lastname", "min").param("firstname", "ad")
	//	// .param("captcha", "validCaptcha").contentType(MediaType.APPLICATION_FORM_URLENCODED))
	//	// .andExpect(status().isNoContent());
	//	// }
	//
	//	@Configuration
	//	public static class Config
	//	{
	//		@Bean
	//		public AccountController accountController()
	//		{
	//			return new AccountController(accountService(), captchaService(), redirectStrategy(), appSettings(), );
	//		}
	//
	//		@Bean
	//		public AccountService accountService()
	//		{
	//			return mock(AccountService.class);
	//		}
	//
	//		@Bean
	//		public CaptchaService captchaService()
	//		{
	//			return mock(CaptchaService.class);
	//		}
	//
	//		@Bean
	//		public RedirectStrategy redirectStrategy()
	//		{
	//			return mock(RedirectStrategy.class);
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
	//		public DataService dataService()
	//		{
	//			DataService dataService = mock(DataService.class);
	//			MolgenisUser molgenisUser = mock(MolgenisUser.class);
	//			when(dataService.findAll(MolgenisUserMetaData.TAG,
	//					new QueryImpl<Entity>().eq(MolgenisUserMetaData.EMAIL, "admin@molgenis.org")))
	//							.thenReturn(Collections.<Entity> singletonList(molgenisUser).stream());
	//
	//			return dataService;
	//		}
	//
	//		@Bean
	//		public MolgenisUserService molgenisUserService()
	//		{
	//			return mock(MolgenisUserService.class);
	//		}
	//	}
}

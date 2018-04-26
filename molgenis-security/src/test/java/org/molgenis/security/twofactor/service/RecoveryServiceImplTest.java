package org.molgenis.security.twofactor.service;

import org.molgenis.data.DataService;
import org.molgenis.data.populate.IdGenerator;
import org.molgenis.data.populate.IdGeneratorImpl;
import org.molgenis.data.security.auth.User;
import org.molgenis.data.security.user.UserService;
import org.molgenis.data.security.user.UserServiceImpl;
import org.molgenis.data.support.DataServiceImpl;
import org.molgenis.security.twofactor.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;
import static org.molgenis.security.twofactor.model.RecoveryCodeMetadata.*;
import static org.testng.Assert.assertEquals;

@ContextConfiguration(classes = { RecoveryServiceImplTest.Config.class })
@TestExecutionListeners(listeners = { WithSecurityContextTestExecutionListener.class })
public class RecoveryServiceImplTest extends AbstractTestNGSpringContextTests
{
	private final static String USERNAME = "molgenisUser";
	private final static String ROLE_SU = "SU";

	@Autowired
	private RecoveryService recoveryService;
	@Autowired
	private UserService userService;
	@Autowired
	private DataService dataService;
	@Autowired
	private RecoveryCodeFactory recoveryCodeFactory;
	private User molgenisUser = mock(User.class);
	private RecoveryCode recoveryCode = mock(RecoveryCode.class);

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		when(userService.getUser(USERNAME)).thenReturn(molgenisUser);
		when(molgenisUser.getUsername()).thenReturn(USERNAME);
		when(molgenisUser.getId()).thenReturn("1234");
		when(dataService.query(RecoveryCodeMetadata.RECOVERY_CODE, RecoveryCode.class)
						.eq(USER_ID, molgenisUser.getId())
						.findAll()).thenReturn(IntStream.range(0, 1).mapToObj(i -> recoveryCode));
	}

	@Test
	@WithMockUser(value = USERNAME, roles = ROLE_SU)
	public void testGenerateRecoveryCodes()
	{
		when(recoveryCodeFactory.create()).thenReturn(recoveryCode);
		Stream<RecoveryCode> recoveryCodeStream = recoveryService.generateRecoveryCodes();
		assertEquals(10, recoveryCodeStream.count());
	}

	@Test
	@WithMockUser(value = USERNAME, roles = ROLE_SU)
	public void testUseRecoveryCode()
	{
		String recoveryCodeId = "lkfsdufash";
		UserSecret userSecret = mock(UserSecret.class);

		when(dataService.query(RECOVERY_CODE, RecoveryCode.class)
						.eq(USER_ID, molgenisUser.getId())
						.and()
						.eq(CODE, recoveryCodeId)
						.findOne()).thenReturn(recoveryCode);
		when(dataService.query(UserSecretMetaData.USER_SECRET, UserSecret.class)
						.eq(UserSecretMetaData.USER_ID, molgenisUser.getId())
						.findOne()).thenReturn(userSecret);
		recoveryService.useRecoveryCode(recoveryCodeId);
		verify(userSecret).setFailedLoginAttempts(0);
		verify(dataService).update(UserSecretMetaData.USER_SECRET, userSecret);
	}

	@Test
	@WithMockUser(value = USERNAME, roles = ROLE_SU)
	public void testGetRecoveryCodes()
	{
		when(dataService.query(RECOVERY_CODE, RecoveryCode.class)
						.eq(USER_ID, molgenisUser.getId())
						.findAll()).thenReturn(Stream.of(recoveryCode));
		Stream<RecoveryCode> recoveryCodeList = recoveryService.getRecoveryCodes();
		assertEquals(1, recoveryCodeList.count());
	}

	@Configuration
	static class Config
	{

		@Bean
		public RecoveryService recoveryService()
		{
			return new RecoveryServiceImpl(dataService(), userService(), recoveryCodeFactory(), idGenerator());
		}

		@Bean
		public DataService dataService()
		{
			return mock(DataServiceImpl.class, RETURNS_DEEP_STUBS);
		}

		@Bean
		public UserService userService()
		{
			return mock(UserServiceImpl.class);
		}

		@Bean
		public IdGenerator idGenerator()
		{
			return new IdGeneratorImpl();
		}

		@Bean
		public RecoveryCodeFactory recoveryCodeFactory()
		{
			return mock(RecoveryCodeFactory.class);
		}

	}
}

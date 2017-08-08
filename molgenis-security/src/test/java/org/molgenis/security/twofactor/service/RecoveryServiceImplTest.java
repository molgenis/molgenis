package org.molgenis.security.twofactor.service;

import org.molgenis.data.DataService;
import org.molgenis.data.populate.IdGenerator;
import org.molgenis.data.populate.IdGeneratorImpl;
import org.molgenis.data.support.DataServiceImpl;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.twofactor.meta.RecoveryCode;
import org.molgenis.security.twofactor.meta.RecoveryCodeFactory;
import org.molgenis.security.twofactor.meta.RecoveryCodeMetadata;
import org.molgenis.security.user.UserService;
import org.molgenis.security.user.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.security.twofactor.meta.RecoveryCodeMetadata.*;
import static org.testng.Assert.assertEquals;

/**
 * <p>Used for generating and revoering recovery codes</p>
 */
@ContextConfiguration(classes = { RecoveryServiceImplTest.Config.class })
@TestExecutionListeners(listeners = WithSecurityContextTestExecutionListener.class)
public class RecoveryServiceImplTest extends AbstractTestNGSpringContextTests
{

	private final static String USERNAME = "molgenisUser";
	private final static String ROLE_SU = "SU";

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
			return mock(DataServiceImpl.class);
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

	@Autowired
	private RecoveryService recoveryService;

	@Autowired
	private UserService userService;

	@Autowired
	private DataService dataService;

	@Autowired
	private RecoveryCodeFactory recoveryCodeFactory;

	private org.molgenis.auth.User molgenisUser = mock(org.molgenis.auth.User.class);

	private RecoveryCode recoveryCode = mock(RecoveryCode.class);

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		when(userService.getUser(USERNAME)).thenReturn(molgenisUser);
		when(molgenisUser.getUsername()).thenReturn(USERNAME);
		when(molgenisUser.getId()).thenReturn("1234");
		when(dataService.findAll(RecoveryCodeMetadata.RECOVERY_CODE,
				new QueryImpl<RecoveryCode>().eq(USER_ID, molgenisUser.getId()), RecoveryCode.class)).thenReturn(
				IntStream.range(0, 1).mapToObj(i -> recoveryCode));
	}

	@Test
	@WithMockUser(value = USERNAME, roles = ROLE_SU)
	@WithUserDetails(USERNAME)
	public void testGenerateRecoveryCodes()
	{
		when(recoveryCodeFactory.create()).thenReturn(recoveryCode);
		Stream<RecoveryCode> recoveryCodeStream = recoveryService.generateRecoveryCodes();
		assertEquals(10, recoveryCodeStream.count());
	}

	@Test
	@WithMockUser(value = USERNAME, roles = ROLE_SU)
	@WithUserDetails(USERNAME)
	public void testUseRecoveryCode()
	{
		String recoveryCodeId = "lkfsdufash";
		when(dataService.findOne(RECOVERY_CODE,
				new QueryImpl<RecoveryCode>().eq(USER_ID, molgenisUser.getId()).and().eq(CODE, recoveryCodeId),
				RecoveryCode.class)).thenReturn(recoveryCode);
		recoveryService.useRecoveryCode(recoveryCodeId);
	}

	@Test
	@WithMockUser(value = USERNAME, roles = ROLE_SU)
	@WithUserDetails(USERNAME)
	public void testGetRecoveryCodes()
	{
		when(dataService.findAll(RECOVERY_CODE, new QueryImpl<RecoveryCode>().eq(USER_ID, molgenisUser.getId()),
				RecoveryCode.class)).thenReturn(Stream.of(recoveryCode));
		Stream<RecoveryCode> recoveryCodeList = recoveryService.getRecoveryCodes();
		assertEquals(1, recoveryCodeList.count());
	}
}

package org.molgenis.security.twofactor.service;

import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.Query;
import org.molgenis.data.populate.IdGenerator;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.core.model.User;
import org.molgenis.security.core.service.UserAccountService;
import org.molgenis.security.twofactor.model.RecoveryCode;
import org.molgenis.security.twofactor.model.RecoveryCodeFactory;
import org.molgenis.security.twofactor.model.UserSecret;
import org.molgenis.security.twofactor.model.UserSecretMetaData;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.security.twofactor.model.RecoveryCodeMetadata.*;
import static org.molgenis.security.twofactor.model.UserSecretMetaData.USER_SECRET;
import static org.testng.Assert.assertEquals;

public class RecoveryServiceImplTest extends AbstractMockitoTest
{
	private final static String USERNAME = "molgenisUser";

	private RecoveryServiceImpl recoveryService;
	@Mock
	private UserAccountService userAccountService;
	@Mock
	private DataService dataService;
	@Mock
	private RecoveryCodeFactory recoveryCodeFactory;
	@Mock
	private User molgenisUser;
	@Mock
	private RecoveryCode recoveryCode;
	@Mock
	private IdGenerator idGenerator;
	@Mock
	private UserSecret userSecret;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		recoveryService = new RecoveryServiceImpl(dataService, userAccountService, recoveryCodeFactory, idGenerator);
		when(userAccountService.getCurrentUser()).thenReturn(molgenisUser);
		when(molgenisUser.getUsername()).thenReturn(USERNAME);
		when(molgenisUser.getId()).thenReturn("1234");
		when(dataService.findAll(RECOVERY_CODE, new QueryImpl<RecoveryCode>().eq(USER_ID, "1234"),
				RecoveryCode.class)).thenReturn(IntStream.range(0, 1).mapToObj(i -> recoveryCode));
	}

	@Test
	public void testGenerateRecoveryCodes()
	{
		when(recoveryCodeFactory.create()).thenReturn(recoveryCode);

		Stream<RecoveryCode> recoveryCodeStream = recoveryService.generateRecoveryCodes();

		assertEquals(recoveryCodeStream.count(), 10);
	}

	@Test
	public void testUseRecoveryCode()
	{
		String recoveryCodeId = "lkfsdufash";
		Query<RecoveryCode> recoveryCodeQuery = new QueryImpl<RecoveryCode>().eq(USER_ID, molgenisUser.getId())
																			 .and()
																			 .eq(CODE, recoveryCodeId);
		when(dataService.findOne(RECOVERY_CODE, recoveryCodeQuery, RecoveryCode.class)).thenReturn(recoveryCode);
		when(dataService.findOne(USER_SECRET,
				new QueryImpl<UserSecret>().eq(UserSecretMetaData.USER_ID, molgenisUser.getId()),
				UserSecret.class)).thenReturn(userSecret);

		recoveryService.useRecoveryCode(recoveryCodeId);

		verify(userSecret).setFailedLoginAttempts(0);
		verify(dataService).update(USER_SECRET, userSecret);
	}

	@Test
	public void testGetRecoveryCodes()
	{
		when(dataService.findAll(RECOVERY_CODE,
				new QueryImpl<RecoveryCode>().eq(USER_ID, molgenisUser.getId()).and().eq(CODE, recoveryCode),
				RecoveryCode.class)).thenReturn(Stream.of(recoveryCode));

		Stream<RecoveryCode> recoveryCodeList = recoveryService.getRecoveryCodes();

		assertEquals(recoveryCodeList.count(), 1);
	}
}

package org.molgenis.security.twofactor.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.security.twofactor.model.RecoveryCodeMetadata.CODE;
import static org.molgenis.security.twofactor.model.RecoveryCodeMetadata.RECOVERY_CODE;
import static org.molgenis.security.twofactor.model.RecoveryCodeMetadata.USER_ID;

import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.molgenis.data.DataService;
import org.molgenis.data.Query;
import org.molgenis.data.populate.IdGeneratorImpl;
import org.molgenis.data.security.auth.User;
import org.molgenis.data.security.user.UserService;
import org.molgenis.security.twofactor.model.RecoveryCode;
import org.molgenis.security.twofactor.model.RecoveryCodeFactory;
import org.molgenis.security.twofactor.model.RecoveryCodeMetadata;
import org.molgenis.security.twofactor.model.UserSecret;
import org.molgenis.security.twofactor.model.UserSecretMetadata;
import org.springframework.security.test.context.annotation.SecurityTestExecutionListeners;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith({SpringExtension.class, MockitoExtension.class})
@SecurityTestExecutionListeners
class RecoveryServiceImplTest {
  private static final String USERNAME = "molgenisUser";
  private static final String ROLE_SU = "SU";

  @Mock private RecoveryServiceImpl recoveryServiceImpl;
  @Mock private UserService userService;
  @Mock private DataService dataService;
  @Mock private RecoveryCodeFactory recoveryCodeFactory;

  @BeforeEach
  void setUpBeforeMethod() {
    recoveryServiceImpl =
        new RecoveryServiceImpl(
            dataService, userService, recoveryCodeFactory, new IdGeneratorImpl());
  }

  @Test
  @WithMockUser(value = USERNAME, roles = ROLE_SU)
  void testGenerateRecoveryCodes() {
    User molgenisUser = mock(User.class);
    RecoveryCode recoveryCode = mock(RecoveryCode.class);
    when(userService.getUser(USERNAME)).thenReturn(molgenisUser);
    when(molgenisUser.getId()).thenReturn("1234");

    @SuppressWarnings("unchecked")
    Query<RecoveryCode> query = mock(Query.class, RETURNS_SELF);
    when(dataService.query(RecoveryCodeMetadata.RECOVERY_CODE, RecoveryCode.class))
        .thenReturn(query);
    when(query.eq(USER_ID, molgenisUser.getId()).findAll())
        .thenReturn(IntStream.range(0, 1).mapToObj(i -> recoveryCode));

    when(recoveryCodeFactory.create()).thenReturn(recoveryCode);
    Stream<RecoveryCode> recoveryCodeStream = recoveryServiceImpl.generateRecoveryCodes();
    assertEquals(10, recoveryCodeStream.count());
  }

  @Test
  @WithMockUser(value = USERNAME, roles = ROLE_SU)
  void testUseRecoveryCode() {
    User molgenisUser = mock(User.class);
    RecoveryCode recoveryCode = mock(RecoveryCode.class);
    when(userService.getUser(USERNAME)).thenReturn(molgenisUser);
    when(molgenisUser.getId()).thenReturn("1234");

    String recoveryCodeId = "lkfsdufash";
    UserSecret userSecret = mock(UserSecret.class);

    @SuppressWarnings("unchecked")
    Query<RecoveryCode> recoveryCodeQuery = mock(Query.class, RETURNS_SELF);
    doReturn(recoveryCodeQuery).when(dataService).query(RECOVERY_CODE, RecoveryCode.class);
    when(recoveryCodeQuery
            .eq(USER_ID, molgenisUser.getId())
            .and()
            .eq(CODE, recoveryCodeId)
            .findOne())
        .thenReturn(recoveryCode);

    @SuppressWarnings("unchecked")
    Query<UserSecret> userSecretQuery = mock(Query.class, RETURNS_SELF);
    doReturn(userSecretQuery)
        .when(dataService)
        .query(UserSecretMetadata.USER_SECRET, UserSecret.class);
    when(userSecretQuery.eq(UserSecretMetadata.USER_ID, molgenisUser.getId()).findOne())
        .thenReturn(userSecret);
    recoveryServiceImpl.useRecoveryCode(recoveryCodeId);
    verify(userSecret).setFailedLoginAttempts(0);
    verify(dataService).update(UserSecretMetadata.USER_SECRET, userSecret);
  }

  @Test
  @WithMockUser(value = USERNAME, roles = ROLE_SU)
  void testGetRecoveryCodes() {
    User molgenisUser = mock(User.class);
    RecoveryCode recoveryCode = mock(RecoveryCode.class);
    when(userService.getUser(USERNAME)).thenReturn(molgenisUser);
    when(molgenisUser.getId()).thenReturn("1234");

    @SuppressWarnings("unchecked")
    Query<RecoveryCode> query = mock(Query.class, RETURNS_SELF);
    when(dataService.query(RecoveryCodeMetadata.RECOVERY_CODE, RecoveryCode.class))
        .thenReturn(query);
    when(query.eq(USER_ID, molgenisUser.getId()).findAll())
        .thenReturn(IntStream.range(0, 1).mapToObj(i -> recoveryCode));

    when(dataService
            .query(RECOVERY_CODE, RecoveryCode.class)
            .eq(USER_ID, molgenisUser.getId())
            .findAll())
        .thenReturn(Stream.of(recoveryCode));
    Stream<RecoveryCode> recoveryCodeList = recoveryServiceImpl.getRecoveryCodes();
    assertEquals(1, recoveryCodeList.count());
  }
}

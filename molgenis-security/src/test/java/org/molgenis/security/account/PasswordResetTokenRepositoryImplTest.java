package org.molgenis.security.account;

import static java.time.temporal.ChronoUnit.HOURS;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Answers.RETURNS_SELF;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.data.security.auth.PasswordResetTokenMetadata.PASSWORD_RESET_TOKEN;

import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.Query;
import org.molgenis.data.security.auth.PasswordResetToken;
import org.molgenis.data.security.auth.PasswordResetTokenFactory;
import org.molgenis.data.security.auth.PasswordResetTokenMetadata;
import org.molgenis.data.security.auth.User;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.security.crypto.password.PasswordEncoder;

class PasswordResetTokenRepositoryImplTest extends AbstractMockitoTest {
  @Mock private PasswordResetTokenFactory passwordResetTokenFactory;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private DataService dataService;
  private PasswordResetTokenRepositoryImpl passwordResetTokenRepositoryImpl;

  @BeforeEach
  void setUpBeforeMethod() {
    passwordResetTokenRepositoryImpl =
        new PasswordResetTokenRepositoryImpl(
            passwordResetTokenFactory, passwordEncoder, dataService);
  }

  @Test
  void testCreateToken() {
    User user = when(mock(User.class).isActive()).thenReturn(true).getMock();

    @SuppressWarnings("unchecked")
    Query<PasswordResetToken> passwordResetTokenQuery = mock(Query.class, RETURNS_SELF);
    when(dataService.query(PASSWORD_RESET_TOKEN, PasswordResetToken.class))
        .thenReturn(passwordResetTokenQuery);
    when(passwordResetTokenQuery.eq(PasswordResetTokenMetadata.USER, user).findOne())
        .thenReturn(null);

    PasswordResetToken passwordResetToken = mock(PasswordResetToken.class);
    when(passwordResetTokenFactory.create()).thenReturn(passwordResetToken);

    passwordResetTokenRepositoryImpl.createToken(user);

    verify(passwordResetToken).setUser(user);
    verify(passwordResetToken).setToken(any());
    verify(passwordResetToken).setExpirationDate(any());
    verify(dataService).add(PASSWORD_RESET_TOKEN, passwordResetToken);
  }

  @Test
  void testCreateTokenDeleteExistingToken() {
    User user = when(mock(User.class).isActive()).thenReturn(true).getMock();

    PasswordResetToken existingPasswordResetToken = mock(PasswordResetToken.class);
    @SuppressWarnings("unchecked")
    Query<PasswordResetToken> passwordResetTokenQuery = mock(Query.class, RETURNS_SELF);
    when(dataService.query(PASSWORD_RESET_TOKEN, PasswordResetToken.class))
        .thenReturn(passwordResetTokenQuery);
    when(passwordResetTokenQuery.eq(PasswordResetTokenMetadata.USER, user).findOne())
        .thenReturn(existingPasswordResetToken);

    PasswordResetToken passwordResetToken = mock(PasswordResetToken.class);
    when(passwordResetTokenFactory.create()).thenReturn(passwordResetToken);

    passwordResetTokenRepositoryImpl.createToken(user);

    verify(dataService).delete(PASSWORD_RESET_TOKEN, existingPasswordResetToken);
    verify(passwordResetToken).setUser(user);
    verify(passwordResetToken).setToken(any());
    verify(passwordResetToken).setExpirationDate(any());
    verify(dataService).add(PASSWORD_RESET_TOKEN, passwordResetToken);
  }

  @Test
  void testCreateTokenInactiveUser() {
    User user = mock(User.class);
    assertThrows(
        PasswordResetTokenCreationException.class,
        () -> passwordResetTokenRepositoryImpl.createToken(user));
  }

  @Test
  void testValidateToken() {
    User user = mock(User.class);
    String token = "MyToken";

    @SuppressWarnings("unchecked")
    Query<PasswordResetToken> passwordResetTokenQuery = mock(Query.class, RETURNS_SELF);
    when(dataService.query(PASSWORD_RESET_TOKEN, PasswordResetToken.class))
        .thenReturn(passwordResetTokenQuery);
    PasswordResetToken passwordResetToken = mock(PasswordResetToken.class);
    when(passwordResetTokenQuery.eq(PasswordResetTokenMetadata.USER, user).findOne())
        .thenReturn(passwordResetToken);
    String tokenHash = "MyTokenHash";
    when(passwordResetToken.getToken()).thenReturn(tokenHash);
    when(passwordResetToken.getExpirationDate()).thenReturn(Instant.now().plus(1, HOURS));
    when(passwordEncoder.matches(token, tokenHash)).thenReturn(true);

    passwordResetTokenRepositoryImpl.validateToken(user, token);
  }

  @Test
  void testValidateTokenExpired() {
    User user = mock(User.class);
    String token = "MyToken";

    @SuppressWarnings("unchecked")
    Query<PasswordResetToken> passwordResetTokenQuery = mock(Query.class, RETURNS_SELF);
    when(dataService.query(PASSWORD_RESET_TOKEN, PasswordResetToken.class))
        .thenReturn(passwordResetTokenQuery);
    PasswordResetToken passwordResetToken = mock(PasswordResetToken.class);
    when(passwordResetTokenQuery.eq(PasswordResetTokenMetadata.USER, user).findOne())
        .thenReturn(passwordResetToken);
    String tokenHash = "MyTokenHash";
    when(passwordResetToken.getToken()).thenReturn(tokenHash);
    when(passwordResetToken.getExpirationDate()).thenReturn(Instant.now().minus(1, HOURS));
    when(passwordEncoder.matches(token, tokenHash)).thenReturn(true);

    assertThrows(
        ExpiredPasswordResetTokenException.class,
        () -> passwordResetTokenRepositoryImpl.validateToken(user, token));
  }

  @Test
  void testValidateTokenInvalid() {
    User user = mock(User.class);
    String token = "MyToken";
    @SuppressWarnings("unchecked")
    Query<PasswordResetToken> passwordResetTokenQuery = mock(Query.class, RETURNS_SELF);
    when(dataService.query(PASSWORD_RESET_TOKEN, PasswordResetToken.class))
        .thenReturn(passwordResetTokenQuery);
    PasswordResetToken passwordResetToken = mock(PasswordResetToken.class);
    when(passwordResetTokenQuery.eq(PasswordResetTokenMetadata.USER, user).findOne())
        .thenReturn(passwordResetToken);
    String tokenHash = "MyTokenHash";
    when(passwordResetToken.getToken()).thenReturn(tokenHash);

    assertThrows(
        InvalidPasswordResetTokenException.class,
        () -> passwordResetTokenRepositoryImpl.validateToken(user, token));
  }

  @Test
  void testValidateTokenUnknown() {
    User user = mock(User.class);
    String token = "MyToken";

    @SuppressWarnings("unchecked")
    Query<PasswordResetToken> passwordResetTokenQuery = mock(Query.class, RETURNS_SELF);
    when(dataService.query(PASSWORD_RESET_TOKEN, PasswordResetToken.class))
        .thenReturn(passwordResetTokenQuery);
    when(passwordResetTokenQuery.eq(PasswordResetTokenMetadata.USER, user).findOne())
        .thenReturn(null);

    assertThrows(
        UnknownPasswordResetTokenException.class,
        () -> passwordResetTokenRepositoryImpl.validateToken(user, token));
  }

  @Test
  void testDeleteToken() {
    User user = mock(User.class);
    String token = "MyToken";

    @SuppressWarnings("unchecked")
    Query<PasswordResetToken> passwordResetTokenQuery = mock(Query.class, RETURNS_SELF);
    when(dataService.query(PASSWORD_RESET_TOKEN, PasswordResetToken.class))
        .thenReturn(passwordResetTokenQuery);
    PasswordResetToken passwordResetToken = mock(PasswordResetToken.class);
    when(passwordResetTokenQuery.eq(PasswordResetTokenMetadata.USER, user).findOne())
        .thenReturn(passwordResetToken);
    String tokenHash = "MyTokenHash";
    when(passwordResetToken.getToken()).thenReturn(tokenHash);
    when(passwordEncoder.matches(token, tokenHash)).thenReturn(true);

    passwordResetTokenRepositoryImpl.deleteToken(user, token);

    verify(dataService).delete(PASSWORD_RESET_TOKEN, passwordResetToken);
  }

  @Test
  void testDeleteTokenInvalid() {
    User user = mock(User.class);
    String token = "MyToken";

    @SuppressWarnings("unchecked")
    Query<PasswordResetToken> passwordResetTokenQuery = mock(Query.class, RETURNS_SELF);
    when(dataService.query(PASSWORD_RESET_TOKEN, PasswordResetToken.class))
        .thenReturn(passwordResetTokenQuery);
    PasswordResetToken passwordResetToken = mock(PasswordResetToken.class);
    when(passwordResetTokenQuery.eq(PasswordResetTokenMetadata.USER, user).findOne())
        .thenReturn(passwordResetToken);
    String tokenHash = "MyTokenHash";
    when(passwordResetToken.getToken()).thenReturn(tokenHash);

    assertThrows(
        InvalidPasswordResetTokenException.class,
        () -> passwordResetTokenRepositoryImpl.deleteToken(user, token));
  }
}

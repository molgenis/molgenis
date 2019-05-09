package org.molgenis.security.account;

import static java.time.Instant.now;
import static java.time.temporal.ChronoUnit.HOURS;
import static java.util.Objects.requireNonNull;
import static org.molgenis.data.security.auth.PasswordResetTokenMetadata.PASSWORD_RESET_TOKEN;

import java.time.Instant;
import java.util.Optional;
import org.molgenis.data.DataService;
import org.molgenis.data.security.auth.PasswordResetToken;
import org.molgenis.data.security.auth.PasswordResetTokenFactory;
import org.molgenis.data.security.auth.PasswordResetTokenMetadata;
import org.molgenis.data.security.auth.User;
import org.molgenis.security.token.TokenGenerator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
class PasswordResetTokenRepositoryImpl implements PasswordResetTokenRepository {
  private final PasswordResetTokenFactory passwordResetTokenFactory;
  private final PasswordEncoder passwordEncoder;
  private final DataService dataService;
  private final TokenGenerator tokenGenerator;

  PasswordResetTokenRepositoryImpl(
      PasswordResetTokenFactory passwordResetTokenFactory,
      PasswordEncoder passwordEncoder,
      DataService dataService) {
    this.passwordResetTokenFactory = requireNonNull(passwordResetTokenFactory);
    this.passwordEncoder = requireNonNull(passwordEncoder);
    this.dataService = requireNonNull(dataService);
    this.tokenGenerator = new TokenGenerator();
  }

  @Transactional
  @Override
  public String createToken(User user) {
    if (!user.isActive()) {
      throw new PasswordResetTokenCreationException();
    }

    deleteTokenIfExists(user);
    return addPasswordResetToken(user);
  }

  @Transactional(readOnly = true)
  @Override
  public void validateToken(User user, String token) {
    PasswordResetToken passwordResetToken = getPasswordResetToken(user, token);
    if (passwordResetToken.getExpirationDate().isBefore(now())) {
      throw new ExpiredPasswordResetTokenException(passwordResetToken);
    }
  }

  @Transactional
  @Override
  public void deleteToken(User user, String token) {
    PasswordResetToken passwordResetToken = getPasswordResetToken(user, token);
    dataService.delete(PASSWORD_RESET_TOKEN, passwordResetToken);
  }

  private void deleteTokenIfExists(User user) {
    getPasswordResetToken(user)
        .ifPresent(
            passwordResetToken -> dataService.delete(PASSWORD_RESET_TOKEN, passwordResetToken));
  }

  private PasswordResetToken getPasswordResetToken(User user, String token) {
    Optional<PasswordResetToken> optionalPasswordResetToken = getPasswordResetToken(user);
    if (!optionalPasswordResetToken.isPresent()) {
      throw new UnknownPasswordResetTokenException();
    }

    PasswordResetToken passwordResetToken = optionalPasswordResetToken.get();
    if (!passwordEncoder.matches(token, passwordResetToken.getToken())) {
      throw new InvalidPasswordResetTokenException(passwordResetToken);
    }
    return passwordResetToken;
  }

  private Optional<PasswordResetToken> getPasswordResetToken(User user) {
    PasswordResetToken passwordResetToken =
        dataService
            .query(PASSWORD_RESET_TOKEN, PasswordResetToken.class)
            .eq(PasswordResetTokenMetadata.USER, user)
            .findOne();
    return Optional.ofNullable(passwordResetToken);
  }

  private String addPasswordResetToken(User user) {
    String token = tokenGenerator.generateToken();
    Instant expirationDate = now().plus(2, HOURS);

    String tokenHash = passwordEncoder.encode(token);

    PasswordResetToken passwordResetToken = passwordResetTokenFactory.create();
    passwordResetToken.setUser(user);
    passwordResetToken.setToken(tokenHash);
    passwordResetToken.setExpirationDate(expirationDate);

    dataService.add(PASSWORD_RESET_TOKEN, passwordResetToken);

    return token;
  }
}

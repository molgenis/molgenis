package org.molgenis.security.account;

import static java.util.Objects.requireNonNull;

import java.net.URI;
import org.molgenis.data.security.auth.User;
import org.molgenis.data.security.user.InvalidEmailAddressException;
import org.molgenis.data.security.user.UnknownUserException;
import org.molgenis.data.security.user.UserService;
import org.molgenis.security.core.runas.RunAsSystem;
import org.molgenis.security.core.utils.SecurityUtils;
import org.molgenis.settings.AppSettings;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Component
class PasswordResetterImpl implements PasswordResetter {
  private final PasswordResetTokenRepository passwordResetTokenService;
  private final UserService userService;
  private final MailSender mailSender;
  private final AppSettings appSettings;

  PasswordResetterImpl(
      PasswordResetTokenRepository passwordResetTokenRepository,
      UserService userService,
      MailSender mailSender,
      AppSettings appSettings) {
    this.passwordResetTokenService = requireNonNull(passwordResetTokenRepository);
    this.userService = requireNonNull(userService);
    this.mailSender = requireNonNull(mailSender);
    this.appSettings = requireNonNull(appSettings);
  }

  @Transactional
  @RunAsSystem
  @Override
  public void resetPassword(String emailAddress) {
    User user = getUserByEmail(emailAddress);
    String token = passwordResetTokenService.createToken(user);
    sendPasswordResetMail(user, token);
  }

  @Transactional(readOnly = true)
  @RunAsSystem
  @Override
  public void validatePasswordResetToken(String username, String token) {
    User user = getUser(username);
    passwordResetTokenService.validateToken(user, token);
  }

  @Transactional
  @RunAsSystem
  @Override
  public void changePassword(String username, String token, String password) {
    User user = getUser(username);

    passwordResetTokenService.validateToken(user, token);
    user.setPassword(password);
    userService.update(user);

    passwordResetTokenService.deleteToken(user, token);
  }

  @Transactional
  @RunAsSystem
  @Override
  public void changePasswordAuthenticatedUser(String password) {
    String username = SecurityUtils.getCurrentUsername();
    if (username == null) {
      throw new AuthenticationCredentialsNotFoundException("not authenticated");
    }

    User user = getUser(username);
    user.setPassword(password);
    userService.update(user);
  }

  private User getUser(String username) {
    User user = userService.getUser(username);
    if (user == null) {
      throw new UnknownUserException(username);
    }
    return user;
  }

  private User getUserByEmail(String emailAddress) {
    User user = userService.getUserByEmail(emailAddress);
    if (user == null) {
      throw new InvalidEmailAddressException();
    }
    return user;
  }

  private URI createPasswordResetUri(String username, String token) {
    ServletUriComponentsBuilder builder = ServletUriComponentsBuilder.fromCurrentServletMapping();
    builder.encode();
    builder.path("/account/password/change");
    builder.queryParam("username", username);
    builder.queryParam("token", token);
    return builder.build().toUri();
  }

  private void sendPasswordResetMail(User user, String token) {
    URI passwordResetUri = createPasswordResetUri(user.getUsername(), token);

    SimpleMailMessage mailMessage = new SimpleMailMessage();
    mailMessage.setTo(user.getEmail());
    mailMessage.setSubject(String.format("Password reset on %s", appSettings.getTitle()));
    mailMessage.setText(
        "Hello,\n\nYou are receiving this email because we received a password reset request for your account.\n\n"
            + passwordResetUri.toString()
            + "\n\nIf you did not request a password reset, you can safely ignore this email.");
    mailSender.send(mailMessage);
  }
}

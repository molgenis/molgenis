package org.molgenis.security.account;

import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static org.molgenis.data.populate.IdGenerator.Strategy.SECURE_RANDOM;
import static org.molgenis.data.populate.IdGenerator.Strategy.SHORT_SECURE_RANDOM;
import static org.molgenis.data.security.auth.UserMetadata.ACTIVATIONCODE;
import static org.molgenis.data.security.auth.UserMetadata.ACTIVE;
import static org.molgenis.data.security.auth.UserMetadata.EMAIL;
import static org.molgenis.data.security.auth.UserMetadata.USER;
import static org.molgenis.data.security.auth.UserMetadata.USERNAME;

import java.net.URI;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.DataService;
import org.molgenis.data.populate.IdGenerator;
import org.molgenis.data.security.auth.User;
import org.molgenis.data.security.user.UserService;
import org.molgenis.security.account.exception.AdminEmailMissingException;
import org.molgenis.security.account.exception.EmailAlreadyExistsException;
import org.molgenis.security.account.exception.InvalidUsernameCharacterException;
import org.molgenis.security.account.exception.MissingEmailException;
import org.molgenis.security.account.exception.UsernameAlreadyExistsException;
import org.molgenis.security.core.runas.RunAsSystem;
import org.molgenis.security.login.MolgenisLoginController;
import org.molgenis.security.settings.AuthenticationSettings;
import org.molgenis.security.user.MolgenisUserException;
import org.molgenis.settings.AppSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.authentication.DisabledException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountServiceImpl implements AccountService {
  private static final Logger LOG = LoggerFactory.getLogger(AccountServiceImpl.class);

  private final DataService dataService;
  private final MailSender mailSender;
  private final UserService userService;
  private final AppSettings appSettings;
  private final AuthenticationSettings authenticationSettings;
  private final IdGenerator idGenerator;

  AccountServiceImpl(
      DataService dataService,
      MailSender mailSender,
      UserService userService,
      AppSettings appSettings,
      AuthenticationSettings authenticationSettings,
      IdGenerator idGenerator) {
    this.dataService = requireNonNull(dataService);
    this.mailSender = requireNonNull(mailSender);
    this.userService = requireNonNull(userService);
    this.appSettings = requireNonNull(appSettings);
    this.authenticationSettings = requireNonNull(authenticationSettings);
    this.idGenerator = requireNonNull(idGenerator);
  }

  @Override
  @RunAsSystem
  @Transactional
  public void createUser(User user, String baseActivationUri) {
    // Check if username already exists
    if (userService.getUser(user.getUsername()) != null) {
      throw new UsernameAlreadyExistsException(user.getUsername());
    }

    // Check if email already exists
    if (userService.getUserByEmail(user.getEmail()) != null) {
      throw new EmailAlreadyExistsException(user.getEmail());
    }

    // Check if username starts or ends with spaces
    String username = user.getUsername();
    if (user.getUsername().startsWith(" ")) {
      throw new InvalidUsernameCharacterException(username, 0, username.charAt(0));
    }
    if (user.getUsername().endsWith(" ")) {
      throw new InvalidUsernameCharacterException(
          username, username.length(), username.charAt(username.length() - 1));
    }

    // collect activation info
    String activationCode = idGenerator.generateId(SECURE_RANDOM);
    List<String> activationEmailAddresses;
    if (authenticationSettings.getSignUpModeration()) {
      activationEmailAddresses = userService.getSuEmailAddresses();
      if (activationEmailAddresses == null || activationEmailAddresses.isEmpty())
        throw new AdminEmailMissingException();
    } else {
      String activationEmailAddress = user.getEmail();
      if (activationEmailAddress == null || activationEmailAddress.isEmpty())
        throw new MissingEmailException(user.getUsername());
      activationEmailAddresses = singletonList(activationEmailAddress);
    }

    // create user
    user.setActivationCode(activationCode);
    user.setActive(false);
    dataService.add(USER, user);
    LOG.debug("created user {}", user.getUsername());

    // send activation email
    URI activationUri = URI.create(baseActivationUri + '/' + activationCode);

    try {
      SimpleMailMessage mailMessage = new SimpleMailMessage();
      mailMessage.setTo(activationEmailAddresses.toArray(new String[] {}));
      mailMessage.setSubject("User registration for " + appSettings.getTitle());
      mailMessage.setText(createActivationEmailText(user, activationUri));
      mailSender.send(mailMessage);
    } catch (MailException mce) {
      LOG.error("Could not send signup mail", mce);
      throw new MolgenisUserException(
          "An error occurred. Please contact the administrator. You are not signed up!");
    }
    if (LOG.isDebugEnabled()) {
      LOG.debug(
          "send activation email for user {} to {}",
          user.getUsername(),
          StringUtils.join(activationEmailAddresses, ','));
    }
  }

  @Override
  @RunAsSystem
  public void activateUser(String activationCode) {
    User user =
        dataService
            .query(USER, User.class)
            .eq(ACTIVE, false)
            .and()
            .eq(ACTIVATIONCODE, activationCode)
            .findOne();

    if (user != null) {
      user.setActive(true);
      dataService.update(USER, user);

      // send activated email to user
      SimpleMailMessage mailMessage = new SimpleMailMessage();
      mailMessage.setTo(user.getEmail());
      mailMessage.setSubject("Your registration request for " + appSettings.getTitle());
      mailMessage.setText(createActivatedEmailText(user, appSettings.getTitle()));
      mailSender.send(mailMessage);
    } else {
      throw new MolgenisUserException("Invalid activation code or account already activated.");
    }
  }

  @Override
  @RunAsSystem
  public void changePassword(String username, String newPassword) {
    User user = dataService.query(USER, User.class).eq(USERNAME, username).findOne();
    if (user == null) {
      throw new MolgenisUserException(format("Unknown user [%s]", username));
    }
    if (!user.isActive()) {
      throw new DisabledException(MolgenisLoginController.ERROR_MESSAGE_DISABLED);
    }
    user.setPassword(newPassword);
    user.setChangePassword(false);
    dataService.update(USER, user);

    LOG.info("Changed password of user [{}]", username);
  }

  @Override
  @RunAsSystem
  public void resetPassword(String userEmail) {
    User user = dataService.query(USER, User.class).eq(EMAIL, userEmail).findOne();

    if (user != null) {
      if (!user.isActive()) {
        throw new DisabledException(MolgenisLoginController.ERROR_MESSAGE_DISABLED);
      }

      String newPassword = idGenerator.generateId(SHORT_SECURE_RANDOM);
      user.setPassword(newPassword);
      user.setChangePassword(true);
      dataService.update(USER, user);

      // send password reseted email to user
      SimpleMailMessage mailMessage = new SimpleMailMessage();
      mailMessage.setTo(user.getEmail());
      mailMessage.setSubject("Your new password request");
      mailMessage.setText(createPasswordResettedEmailText(newPassword));
      mailSender.send(mailMessage);
    } else {
      throw new MolgenisUserException("Invalid email address.");
    }
  }

  private String createActivationEmailText(User user, URI activationUri) {
    return "User registration for "
        + appSettings.getTitle()
        + '\n'
        + "User name: "
        + user.getUsername()
        + " Full name: "
        + user.getFirstName()
        + ' '
        + user.getLastName()
        + '\n'
        + "In order to activate the user visit the following URL:"
        + '\n'
        + activationUri
        + '\n'
        + '\n';
  }

  private String createActivatedEmailText(User user, String appName) {
    return "Dear "
        + user.getFirstName()
        + " "
        + user.getLastName()
        + ",\n\n"
        + "your registration request for "
        + appName
        + " was approved.\n"
        + "Your account is now active.\n";
  }

  private String createPasswordResettedEmailText(String newPassword) {
    return "Somebody, probably you, requested a new password for "
        + appSettings.getTitle()
        + ".\n"
        + "The new password is: "
        + newPassword
        + '\n'
        + "Note: we strongly recommend you reset your password after log-in!";
  }
}

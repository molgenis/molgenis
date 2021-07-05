package org.molgenis.security.oidc;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.security.auth.UserMetadata.USER;
import static org.molgenis.data.security.auth.UserMetadata.USERNAME;
import static org.molgenis.security.oidc.model.OidcUserMappingMetadata.OIDC_CLIENT;
import static org.molgenis.security.oidc.model.OidcUserMappingMetadata.OIDC_USERNAME;
import static org.molgenis.security.oidc.model.OidcUserMappingMetadata.OIDC_USER_MAPPING;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Optional;
import java.util.UUID;
import org.molgenis.data.DataService;
import org.molgenis.data.security.auth.User;
import org.molgenis.data.security.auth.UserFactory;
import org.molgenis.data.security.auth.UserMetadata;
import org.molgenis.security.exception.UserHasDifferentEmailAddressException;
import org.molgenis.security.oidc.model.OidcClient;
import org.molgenis.security.oidc.model.OidcUserMapping;
import org.molgenis.security.oidc.model.OidcUserMappingFactory;
import org.slf4j.Logger;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.transaction.annotation.Transactional;

/**
 * Maps {@link OidcUser} to a Molgenis {@link User}. The mapping is stored as an editable {@link
 * OidcUserMapping}.
 *
 * <p><img src="{@docRoot}/doc-files/OidcUserMapperImpl.png" width="640">
 */
public class OidcUserMapperImpl implements OidcUserMapper {

  private final DataService dataService;
  private final OidcUserMappingFactory oidcUserMappingFactory;
  private final UserFactory userFactory;

  private static final Logger LOGGER = getLogger(OidcUserMapperImpl.class);

  public OidcUserMapperImpl(
      DataService dataService,
      OidcUserMappingFactory oidcUserMappingFactory,
      UserFactory userFactory) {
    this.dataService = requireNonNull(dataService);
    this.oidcUserMappingFactory = requireNonNull(oidcUserMappingFactory);
    this.userFactory = requireNonNull(userFactory);
  }

  @Transactional
  @Override
  public String toUser(OidcUser oidcUser, OidcClient oidcClient) {
    verifyOidcUser(oidcUser);
    var existingUser = getUser(oidcUser, oidcClient);
    return existingUser.orElseGet(() -> createUserMapping(oidcUser, oidcClient));
  }

  private void verifyOidcUser(OidcUser oidcUser) {
    if (oidcUser.getEmail() == null) {
      throw new OidcUserMissingEmailException(oidcUser);
    }
    Boolean emailVerified = oidcUser.getEmailVerified();
    if (emailVerified != null && !emailVerified) {
      throw new OidcUserEmailVerificationException(oidcUser);
    }
  }

  private Optional<String> getUser(OidcUser oidcUser, OidcClient oidcClient) {
    var registrationId = oidcClient.getRegistrationId();
    var subject = oidcUser.getSubject();
    var result =
        Optional.ofNullable(
                dataService
                    .query(OIDC_USER_MAPPING, OidcUserMapping.class)
                    .eq(OIDC_CLIENT, registrationId)
                    .and()
                    .eq(OIDC_USERNAME, subject)
                    .findOne())
            .map(OidcUserMapping::getUser)
            .map(User::getUsername);
    result.ifPresent(
        userName ->
            LOGGER.debug(
                "Found existing user mapping for registrationId '{}' and subject '{}' to user '{}'.",
                registrationId,
                subject,
                userName));
    return result;
  }

  private String createUserMapping(OidcUser oidcUser, OidcClient oidcClient) {
    var email = oidcUser.getEmail();
    User user = dataService.query(USER, User.class).eq(UserMetadata.EMAIL, email).findOne();
    if (user == null) {
      LOGGER.debug(
          "No user found for email address '{}', registering new user with username'{}'.",
          email,
          oidcUser.getName());
      user = createUser(oidcUser);
    } else {
      LOGGER.debug("Found existing user '{}' with email address '{}'.", user.getUsername(), email);
    }

    OidcUserMapping oidcUserMapping = oidcUserMappingFactory.create();
    oidcUserMapping.setLabel(oidcClient.getRegistrationId() + ':' + oidcUser.getSubject());
    oidcUserMapping.setOidcClient(oidcClient);
    oidcUserMapping.setOidcUsername(oidcUser.getSubject());
    oidcUserMapping.setUser(user);
    LOGGER.debug("Registering new OidcUserMapping...");
    dataService.add(OIDC_USER_MAPPING, oidcUserMapping);

    return user.getUsername();
  }

  private User createUser(OidcUser oidcUser) {
    var username = oidcUser.getName();
    if (!isUsernameAvailable(username)) {
      LOGGER.debug("Username {} is not available.", username);
      throw new UserHasDifferentEmailAddressException(oidcUser.getName(), oidcUser.getEmail());
    }

    User user = userFactory.create();
    user.setUsername(username);
    user.setPassword(UUID.randomUUID().toString());
    user.setEmail(oidcUser.getEmail());
    user.setActive(true);
    user.setFirstName(oidcUser.getGivenName());
    user.setMiddleNames(oidcUser.getMiddleName());
    user.setLastName(oidcUser.getFamilyName());

    dataService.add(USER, user);

    return user;
  }

  private boolean isUsernameAvailable(String username) {
    return dataService.query(USER, User.class).eq(USERNAME, username).count() == 0;
  }
}

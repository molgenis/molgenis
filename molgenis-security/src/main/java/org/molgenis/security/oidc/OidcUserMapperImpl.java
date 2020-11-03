package org.molgenis.security.oidc;

import static java.util.Objects.requireNonNull;
import static org.molgenis.security.core.runas.RunAsSystemAspect.runAsSystem;
import static org.molgenis.security.oidc.model.OidcUserMappingMetadata.OIDC_CLIENT;
import static org.molgenis.security.oidc.model.OidcUserMappingMetadata.OIDC_USERNAME;
import static org.molgenis.security.oidc.model.OidcUserMappingMetadata.OIDC_USER_MAPPING;

import java.util.Optional;
import java.util.UUID;
import org.molgenis.data.DataService;
import org.molgenis.data.security.auth.User;
import org.molgenis.data.security.auth.UserFactory;
import org.molgenis.data.security.auth.UserMetadata;
import org.molgenis.security.oidc.model.OidcClient;
import org.molgenis.security.oidc.model.OidcUserMapping;
import org.molgenis.security.oidc.model.OidcUserMappingFactory;
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
  public User toUser(OidcUser oidcUser, OidcClient oidcClient) {
    verifyOidcUser(oidcUser);
    return runAsSystem(
        () ->
            getUser(oidcUser, oidcClient).orElseGet(() -> createUserMapping(oidcUser, oidcClient)));
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

  private Optional<User> getUser(OidcUser oidcUser, OidcClient oidcClient) {
    OidcUserMapping oidcUserMapping =
        dataService
            .query(OIDC_USER_MAPPING, OidcUserMapping.class)
            .eq(OIDC_CLIENT, oidcClient.getRegistrationId())
            .and()
            .eq(OIDC_USERNAME, oidcUser.getSubject())
            .findOne();
    return oidcUserMapping != null ? Optional.of(oidcUserMapping.getUser()) : Optional.empty();
  }

  private User createUserMapping(OidcUser oidcUser, OidcClient oidcClient) {
    User user =
        dataService
            .query(UserMetadata.USER, User.class)
            .eq(UserMetadata.EMAIL, oidcUser.getEmail())
            .findOne();
    if (user == null) {
      user = createUser(oidcUser);
    }

    OidcUserMapping oidcUserMapping = oidcUserMappingFactory.create();
    oidcUserMapping.setLabel(oidcClient.getRegistrationId() + ':' + oidcUser.getSubject());
    oidcUserMapping.setOidcClient(oidcClient);
    oidcUserMapping.setOidcUsername(oidcUser.getSubject());
    oidcUserMapping.setUser(user);
    dataService.add(OIDC_USER_MAPPING, oidcUserMapping);

    return user;
  }

  private User createUser(OidcUser oidcUser) {
    User user = userFactory.create();
    user.setUsername(oidcUser.getEmail());
    user.setPassword(UUID.randomUUID().toString());
    user.setEmail(oidcUser.getEmail());
    user.setActive(true);
    user.setFirstName(oidcUser.getGivenName());
    user.setLastName(oidcUser.getFamilyName());

    dataService.add(UserMetadata.USER, user);

    return user;
  }
}

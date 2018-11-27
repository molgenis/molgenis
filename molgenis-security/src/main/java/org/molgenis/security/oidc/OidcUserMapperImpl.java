package org.molgenis.security.oidc;

import static java.util.Objects.requireNonNull;
import static org.molgenis.security.core.runas.RunAsSystemAspect.runAsSystem;
import static org.molgenis.security.oidc.model.OidcUserMappingMetadata.OIDC_CLIENT;
import static org.molgenis.security.oidc.model.OidcUserMappingMetadata.OIDC_USERNAME;
import static org.molgenis.security.oidc.model.OidcUserMappingMetadata.OIDC_USER_MAPPING;

import java.util.Optional;
import java.util.UUID;
import org.molgenis.data.DataService;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.security.auth.User;
import org.molgenis.data.security.auth.UserFactory;
import org.molgenis.data.security.auth.UserMetaData;
import org.molgenis.security.oidc.model.OidcClient;
import org.molgenis.security.oidc.model.OidcClientMetadata;
import org.molgenis.security.oidc.model.OidcUserMapping;
import org.molgenis.security.oidc.model.OidcUserMappingFactory;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.transaction.annotation.Transactional;

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
  public User toUser(OidcUser oidcUser, OidcUserRequest userRequest) {
    verifyOidcUser(oidcUser);
    return runAsSystem(
        () ->
            getUser(oidcUser, userRequest)
                .orElseGet(() -> createUserMapping(oidcUser, userRequest)));
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

  private Optional<User> getUser(OidcUser oidcUser, OidcUserRequest userRequest) {
    OidcUserMapping oidcUserMapping =
        dataService
            .query(OIDC_USER_MAPPING, OidcUserMapping.class)
            .eq(OIDC_CLIENT, userRequest.getClientRegistration().getRegistrationId())
            .and()
            .eq(OIDC_USERNAME, oidcUser.getSubject())
            .findOne();
    return oidcUserMapping != null ? Optional.of(oidcUserMapping.getUser()) : Optional.empty();
  }

  private User createUserMapping(OidcUser oidcUser, OidcUserRequest userRequest) {
    User user =
        dataService
            .query(UserMetaData.USER, User.class)
            .eq(UserMetaData.EMAIL, oidcUser.getEmail())
            .findOne();
    if (user == null) {
      user = createUser(oidcUser);
    }

    OidcClient oidcClient = getOidcClient(userRequest);

    OidcUserMapping oidcUserMapping = oidcUserMappingFactory.create();
    oidcUserMapping.setLabel(
        userRequest.getClientRegistration().getRegistrationId() + ':' + oidcUser.getSubject());
    oidcUserMapping.setOidcClient(oidcClient);
    oidcUserMapping.setOidcUsername(oidcUser.getSubject());
    oidcUserMapping.setUser(user);
    dataService.add(OIDC_USER_MAPPING, oidcUserMapping);

    return user;
  }

  private User createUser(OidcUser oidcUser) {
    User user = userFactory.create();
    user.setUsername(oidcUser.getEmail());
    user.setPassword_(UUID.randomUUID().toString());
    user.setEmail(oidcUser.getEmail());
    user.setActive(true);
    user.setFirstName(oidcUser.getGivenName());
    user.setLastName(oidcUser.getFamilyName());

    dataService.add(UserMetaData.USER, user);

    return user;
  }

  private OidcClient getOidcClient(OidcUserRequest userRequest) {
    String registrationId = userRequest.getClientRegistration().getRegistrationId();
    OidcClient oidcClient =
        dataService.findOneById(OidcClientMetadata.OIDC_CLIENT, registrationId, OidcClient.class);
    if (oidcClient == null) {
      throw new UnknownEntityException(OidcClientMetadata.OIDC_CLIENT, registrationId);
    }
    return oidcClient;
  }
}

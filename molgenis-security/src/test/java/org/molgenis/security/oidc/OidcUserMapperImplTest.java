package org.molgenis.security.oidc;

import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.security.oidc.model.OidcUserMappingMetadata.OIDC_CLIENT;
import static org.molgenis.security.oidc.model.OidcUserMappingMetadata.OIDC_USERNAME;
import static org.molgenis.security.oidc.model.OidcUserMappingMetadata.OIDC_USER_MAPPING;
import static org.testng.Assert.assertEquals;

import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.security.auth.User;
import org.molgenis.data.security.auth.UserFactory;
import org.molgenis.data.security.auth.UserMetadata;
import org.molgenis.security.oidc.model.OidcClient;
import org.molgenis.security.oidc.model.OidcClientMetadata;
import org.molgenis.security.oidc.model.OidcUserMapping;
import org.molgenis.security.oidc.model.OidcUserMappingFactory;
import org.molgenis.security.oidc.model.OidcUserMappingMetadata;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class OidcUserMapperImplTest extends AbstractMockitoTest {
  @Mock(answer = RETURNS_DEEP_STUBS)
  private DataService dataService;

  @Mock private OidcUserMappingFactory oidcUserMappingFactory;
  @Mock private UserFactory userFactory;
  private OidcUserMapperImpl oidcUserMapperImpl;

  @BeforeMethod
  public void setUpBeforeMethod() {
    oidcUserMapperImpl = new OidcUserMapperImpl(dataService, oidcUserMappingFactory, userFactory);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void testOidcUserMapperImpl() {
    new OidcUserMapperImpl(null, null, null);
  }

  @Test
  public void testToUserExistingUserMapping() {
    String email = "e@mail.com";
    String username = "username";

    OidcUser oidcUser = mock(OidcUser.class);
    when(oidcUser.getEmail()).thenReturn(email);
    when(oidcUser.getEmailVerified()).thenReturn(true);
    when(oidcUser.getSubject()).thenReturn(username);

    String registrationId = "google";
    ClientRegistration clientRegistration =
        CommonOAuth2Provider.GOOGLE
            .getBuilder(registrationId)
            .clientId("clientId")
            .clientSecret("clientSecret")
            .build();

    OidcUserRequest oidcUserRequest = mock(OidcUserRequest.class);
    when(oidcUserRequest.getClientRegistration()).thenReturn(clientRegistration);

    User user = mock(User.class);

    OidcUserMapping oidcUserMapping = mock(OidcUserMapping.class);
    when(oidcUserMapping.getUser()).thenReturn(user);

    when(dataService
            .query(OIDC_USER_MAPPING, OidcUserMapping.class)
            .eq(OIDC_CLIENT, registrationId)
            .and()
            .eq(OIDC_USERNAME, username)
            .findOne())
        .thenReturn(oidcUserMapping);

    assertEquals(oidcUserMapperImpl.toUser(oidcUser, oidcUserRequest), user);
  }

  @Test
  public void testToUserMissingUserMappingExistingUser() {
    String email = "e@mail.com";
    String username = "username";

    OidcUser oidcUser = mock(OidcUser.class);
    when(oidcUser.getEmail()).thenReturn(email);
    when(oidcUser.getEmailVerified()).thenReturn(true);
    when(oidcUser.getSubject()).thenReturn(username);

    String registrationId = "google";
    ClientRegistration clientRegistration =
        CommonOAuth2Provider.GOOGLE
            .getBuilder(registrationId)
            .clientId("clientId")
            .clientSecret("clientSecret")
            .build();

    OidcUserRequest oidcUserRequest = mock(OidcUserRequest.class);
    when(oidcUserRequest.getClientRegistration()).thenReturn(clientRegistration);

    OidcClient oidcClient = mock(OidcClient.class);
    when(dataService.findOneById(OidcClientMetadata.OIDC_CLIENT, registrationId, OidcClient.class))
        .thenReturn(oidcClient);

    User user = mock(User.class);
    when(dataService.query(UserMetadata.USER, User.class).eq(UserMetadata.EMAIL, email).findOne())
        .thenReturn(user);

    OidcUserMapping oidcUserMapping = mock(OidcUserMapping.class);

    when(dataService
            .query(OIDC_USER_MAPPING, OidcUserMapping.class)
            .eq(OIDC_CLIENT, registrationId)
            .and()
            .eq(OIDC_USERNAME, username)
            .findOne())
        .thenReturn(null);

    when(oidcUserMappingFactory.create()).thenReturn(oidcUserMapping);

    assertEquals(oidcUserMapperImpl.toUser(oidcUser, oidcUserRequest), user);
    verify(dataService).add(OidcUserMappingMetadata.OIDC_USER_MAPPING, oidcUserMapping);
    verify(oidcUserMapping).setLabel("google:username");
    verify(oidcUserMapping).setOidcClient(oidcClient);
    verify(oidcUserMapping).setOidcUsername("username");
    verify(oidcUserMapping).setUser(user);
  }

  @Test
  public void testToUserMissingUserMappingMissingUser() {
    String email = "e@mail.com";
    String username = "username";
    String givenName = "MOL";
    String familyName = "GENIS";

    OidcUser oidcUser = mock(OidcUser.class);
    when(oidcUser.getEmail()).thenReturn(email);
    when(oidcUser.getEmailVerified()).thenReturn(true);
    when(oidcUser.getSubject()).thenReturn(username);
    when(oidcUser.getGivenName()).thenReturn(givenName);
    when(oidcUser.getFamilyName()).thenReturn(familyName);
    String registrationId = "google";
    ClientRegistration clientRegistration =
        CommonOAuth2Provider.GOOGLE
            .getBuilder(registrationId)
            .clientId("clientId")
            .clientSecret("clientSecret")
            .build();

    OidcUserRequest oidcUserRequest = mock(OidcUserRequest.class);
    when(oidcUserRequest.getClientRegistration()).thenReturn(clientRegistration);

    OidcClient oidcClient = mock(OidcClient.class);
    when(dataService.findOneById(OidcClientMetadata.OIDC_CLIENT, registrationId, OidcClient.class))
        .thenReturn(oidcClient);

    when(dataService.query(UserMetadata.USER, User.class).eq(UserMetadata.EMAIL, email).findOne())
        .thenReturn(null);

    OidcUserMapping oidcUserMapping = mock(OidcUserMapping.class);

    when(dataService
            .query(OIDC_USER_MAPPING, OidcUserMapping.class)
            .eq(OIDC_CLIENT, registrationId)
            .and()
            .eq(OIDC_USERNAME, username)
            .findOne())
        .thenReturn(null);

    when(oidcUserMappingFactory.create()).thenReturn(oidcUserMapping);

    User user = mock(User.class);
    when(userFactory.create()).thenReturn(user);

    assertEquals(oidcUserMapperImpl.toUser(oidcUser, oidcUserRequest), user);
    verify(dataService).add(OidcUserMappingMetadata.OIDC_USER_MAPPING, oidcUserMapping);
    verify(oidcUserMapping).setLabel("google:username");
    verify(oidcUserMapping).setOidcClient(oidcClient);
    verify(oidcUserMapping).setOidcUsername("username");
    verify(oidcUserMapping).setUser(user);

    verify(dataService).add(UserMetadata.USER, user);
    verify(user).setUsername(email); // email, not username
    verify(user).setEmail(email);
    verify(user).setFirstName(givenName);
    verify(user).setLastName(familyName);
  }

  @Test(expectedExceptions = OidcUserMissingEmailException.class)
  public void testToUserEmailMissing() {
    OidcUser oidcUser = mock(OidcUser.class);
    OidcUserRequest oidcUserRequest = mock(OidcUserRequest.class);
    oidcUserMapperImpl.toUser(oidcUser, oidcUserRequest);
  }

  @Test(expectedExceptions = OidcUserEmailVerificationException.class)
  public void testToUserEmailNotVerified() {
    OidcUser oidcUser = mock(OidcUser.class);
    when(oidcUser.getEmail()).thenReturn("e@mail.com");
    when(oidcUser.getEmailVerified()).thenReturn(false);
    OidcUserRequest oidcUserRequest = mock(OidcUserRequest.class);
    oidcUserMapperImpl.toUser(oidcUser, oidcUserRequest);
  }
}

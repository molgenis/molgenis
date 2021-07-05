package org.molgenis.security.oidc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.security.oidc.model.OidcUserMappingMetadata.OIDC_CLIENT;
import static org.molgenis.security.oidc.model.OidcUserMappingMetadata.OIDC_USERNAME;
import static org.molgenis.security.oidc.model.OidcUserMappingMetadata.OIDC_USER_MAPPING;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.Query;
import org.molgenis.data.security.auth.User;
import org.molgenis.data.security.auth.UserFactory;
import org.molgenis.data.security.auth.UserMetadata;
import org.molgenis.security.exception.UserHasDifferentEmailAddressException;
import org.molgenis.security.oidc.model.OidcClient;
import org.molgenis.security.oidc.model.OidcUserMapping;
import org.molgenis.security.oidc.model.OidcUserMappingFactory;
import org.molgenis.security.oidc.model.OidcUserMappingMetadata;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

class OidcUserMapperImplTest extends AbstractMockitoTest {
  @Mock private DataService dataService;
  @Mock private OidcUserMappingFactory oidcUserMappingFactory;
  @Mock private UserFactory userFactory;
  @Mock private OidcClient oidcClient;
  @Mock private User user;
  @Mock private OidcUser oidcUser;
  @Mock private OidcUserMapping oidcUserMapping;
  private OidcUserMapperImpl oidcUserMapperImpl;

  @BeforeEach
  void setUpBeforeMethod() {
    oidcUserMapperImpl = new OidcUserMapperImpl(dataService, oidcUserMappingFactory, userFactory);
  }

  @Test
  void testOidcUserMapperImpl() {
    assertThrows(NullPointerException.class, () -> new OidcUserMapperImpl(null, null, null));
  }

  @Test
  void testToUserExistingUserMapping() {
    String email = "e@mail.com";
    String username = "username";

    when(oidcUser.getEmail()).thenReturn(email);
    when(oidcUser.getEmailVerified()).thenReturn(true);
    when(oidcUser.getSubject()).thenReturn(username);

    when(oidcUserMapping.getUser()).thenReturn(user);
    when(user.getUsername()).thenReturn(username);

    String registrationId = "google";
    when(oidcClient.getRegistrationId()).thenReturn(registrationId);

    @SuppressWarnings("unchecked")
    Query<OidcUserMapping> query = mock(Query.class, RETURNS_SELF);
    when(dataService.query(OIDC_USER_MAPPING, OidcUserMapping.class)).thenReturn(query);
    when(query.eq(OIDC_CLIENT, registrationId).and().eq(OIDC_USERNAME, username).findOne())
        .thenReturn(oidcUserMapping);

    assertEquals(username, oidcUserMapperImpl.toUser(oidcUser, oidcClient));
  }

  @Test
  void testToUserMissingUserMappingExistingUser() {
    String email = "e@mail.com";
    String username = "username";

    when(oidcUser.getEmail()).thenReturn(email);
    when(oidcUser.getEmailVerified()).thenReturn(true);
    when(oidcUser.getSubject()).thenReturn(username);

    String registrationId = "google";
    when(oidcClient.getRegistrationId()).thenReturn(registrationId);

    @SuppressWarnings("unchecked")
    Query<User> query = mock(Query.class, RETURNS_SELF);
    doReturn(query).when(dataService).query(UserMetadata.USER, User.class);
    when(query.eq(UserMetadata.EMAIL, email).findOne()).thenReturn(user);

    @SuppressWarnings("unchecked")
    Query<OidcUserMapping> oidcUserMappingQuery = mock(Query.class, RETURNS_SELF);
    doReturn(oidcUserMappingQuery)
        .when(dataService)
        .query(OIDC_USER_MAPPING, OidcUserMapping.class);
    when(oidcUserMappingQuery
            .eq(OIDC_CLIENT, registrationId)
            .and()
            .eq(OIDC_USERNAME, username)
            .findOne())
        .thenReturn(null);

    when(oidcUserMappingFactory.create()).thenReturn(oidcUserMapping);
    when(user.getUsername()).thenReturn(username);

    assertEquals(username, oidcUserMapperImpl.toUser(oidcUser, oidcClient));
    verify(dataService).add(OidcUserMappingMetadata.OIDC_USER_MAPPING, oidcUserMapping);
    verify(oidcUserMapping).setLabel("google:username");
    verify(oidcUserMapping).setOidcClient(oidcClient);
    verify(oidcUserMapping).setOidcUsername("username");
    verify(oidcUserMapping).setUser(user);
  }

  @Test
  void testToUserMissingUserMappingMissingUser() {
    String email = "e@mail.com";
    String subject = "d0197ff1-b8c8-43a4-b9b8-0cf6ff33a6c8";
    String username = "username";
    String givenName = "MOL";
    String familyName = "GENIS";

    when(oidcUser.getName()).thenReturn(username);
    when(oidcUser.getEmail()).thenReturn(email);
    when(oidcUser.getEmailVerified()).thenReturn(true);
    when(oidcUser.getSubject()).thenReturn(subject);
    when(oidcUser.getGivenName()).thenReturn(givenName);
    when(oidcUser.getFamilyName()).thenReturn(familyName);
    String registrationId = "google";
    when(oidcClient.getRegistrationId()).thenReturn(registrationId);

    @SuppressWarnings("unchecked")
    Query<User> userQuery = mock(Query.class, RETURNS_SELF);
    doReturn(userQuery).when(dataService).query(UserMetadata.USER, User.class);
    when(userQuery.eq(UserMetadata.EMAIL, email).findOne()).thenReturn(null);
    when(userQuery.eq(UserMetadata.USERNAME, "username").count()).thenReturn(0L);

    @SuppressWarnings("unchecked")
    Query<OidcUserMapping> oidcUserMappingQuery = mock(Query.class, RETURNS_SELF);
    doReturn(oidcUserMappingQuery)
        .when(dataService)
        .query(OIDC_USER_MAPPING, OidcUserMapping.class);
    when(oidcUserMappingQuery
            .eq(OIDC_CLIENT, registrationId)
            .and()
            .eq(OIDC_USERNAME, subject)
            .findOne())
        .thenReturn(null);

    when(oidcUserMappingFactory.create()).thenReturn(oidcUserMapping);

    when(userFactory.create()).thenReturn(user);
    when(user.getUsername()).thenReturn(username);

    assertEquals(username, oidcUserMapperImpl.toUser(oidcUser, oidcClient));
    verify(dataService).add(OidcUserMappingMetadata.OIDC_USER_MAPPING, oidcUserMapping);
    verify(oidcUserMapping).setLabel("google:d0197ff1-b8c8-43a4-b9b8-0cf6ff33a6c8");
    verify(oidcUserMapping).setOidcClient(oidcClient);
    verify(oidcUserMapping).setOidcUsername(subject);
    verify(oidcUserMapping).setUser(user);

    verify(dataService).add(UserMetadata.USER, user);
    // Fills in whatever getName() returns, configurable in OidcClient
    verify(user).setUsername(username);
    verify(user).setEmail(email);
    verify(user).setFirstName(givenName);
    verify(user).setLastName(familyName);
  }

  @Test
  void testToUserMissingUserUsernameTaken() {
    String email = "e@mail.com";
    String subject = "d0197ff1-b8c8-43a4-b9b8-0cf6ff33a6c8";
    String username = "username";

    when(oidcUser.getName()).thenReturn(username);
    when(oidcUser.getSubject()).thenReturn(subject);
    when(oidcUser.getEmailVerified()).thenReturn(null);
    when(oidcUser.getEmail()).thenReturn(email);
    String registrationId = "google";
    when(oidcClient.getRegistrationId()).thenReturn(registrationId);

    @SuppressWarnings("unchecked")
    Query<OidcUserMapping> oidcUserMappingQuery = mock(Query.class, RETURNS_SELF);
    doReturn(oidcUserMappingQuery)
        .when(dataService)
        .query(OIDC_USER_MAPPING, OidcUserMapping.class);
    when(oidcUserMappingQuery
            .eq(OIDC_CLIENT, registrationId)
            .and()
            .eq(OIDC_USERNAME, subject)
            .findOne())
        .thenReturn(null);

    @SuppressWarnings("unchecked")
    Query<User> userQuery = mock(Query.class, RETURNS_SELF);
    doReturn(userQuery).when(dataService).query(UserMetadata.USER, User.class);
    when(userQuery.eq(UserMetadata.EMAIL, email).findOne()).thenReturn(null);
    when(userQuery.eq(UserMetadata.USERNAME, "username").count()).thenReturn(1L);

    assertThrows(
        UserHasDifferentEmailAddressException.class,
        () -> oidcUserMapperImpl.toUser(oidcUser, oidcClient));
  }

  @Test
  void testToUserEmailMissing() {
    assertThrows(
        OidcUserMissingEmailException.class, () -> oidcUserMapperImpl.toUser(oidcUser, oidcClient));
  }

  @Test
  void testToUserEmailNotVerified() {
    when(oidcUser.getEmail()).thenReturn("e@mail.com");
    when(oidcUser.getEmailVerified()).thenReturn(false);
    assertThrows(
        OidcUserEmailVerificationException.class,
        () -> oidcUserMapperImpl.toUser(oidcUser, oidcClient));
  }
}

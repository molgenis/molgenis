package org.molgenis.security.settings;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.security.oidc.model.OidcClient;
import org.molgenis.test.AbstractMockitoTest;

class AuthenticationSettingsImplTest extends AbstractMockitoTest {
  @Mock private DataService dataService;
  private AuthenticationSettingsImpl authenticationSettingsImpl;

  @BeforeEach
  void setUpBeforeMethod() {
    authenticationSettingsImpl = new AuthenticationSettingsImpl();
    authenticationSettingsImpl.setDataService(dataService);
  }

  @Test
  void testGetOidcClients() {
    Entity entity = mock(Entity.class);
    OidcClient oidcClient = mock(OidcClient.class);
    when(entity.getEntities("oidcClients", OidcClient.class)).thenReturn(singletonList(oidcClient));
    when(entity.getBoolean("signup")).thenReturn(true);
    when(dataService.findOneById("sys_set_auth", "auth")).thenReturn(entity);
    assertEquals(
        singletonList(oidcClient), newArrayList(authenticationSettingsImpl.getOidcClients()));
  }

  @Test
  void testGetOidcClientsSignUpDisabled() {
    Entity entity = mock(Entity.class);
    when(dataService.findOneById("sys_set_auth", "auth")).thenReturn(entity);
    assertEquals(emptyList(), newArrayList(authenticationSettingsImpl.getOidcClients()));
  }

  @Test
  void testGetOidcClientsSignUpModerationEnabled() {
    Entity entity = mock(Entity.class);
    when(dataService.findOneById("sys_set_auth", "auth")).thenReturn(entity);
    doReturn(true).when(entity).getBoolean("signup");
    doReturn(true).when(entity).getBoolean("signup_moderation");
    assertEquals(emptyList(), newArrayList(authenticationSettingsImpl.getOidcClients()));
  }
}

package org.molgenis.security.settings;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.listeners.EntityListenersService;
import org.molgenis.security.oidc.model.OidcClient;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class AuthenticationSettingsImplTest extends AbstractMockitoTest {
  @Mock private DataService dataService;
  @Mock private EntityListenersService entityListenersService;
  private AuthenticationSettingsImpl authenticationSettingsImpl;

  @BeforeMethod
  public void setUpBeforeMethod() {
    authenticationSettingsImpl = new AuthenticationSettingsImpl();
    authenticationSettingsImpl.setDataService(dataService);
    authenticationSettingsImpl.setEntityListenersService(entityListenersService);
  }

  @Test
  public void testGetOidcClients() {
    Entity entity = mock(Entity.class);
    OidcClient oidcClient = mock(OidcClient.class);
    when(entity.getEntities("oidcClients", OidcClient.class)).thenReturn(singletonList(oidcClient));
    when(entity.getBoolean("signup")).thenReturn(true);
    when(dataService.findOneById("sys_set_auth", "auth")).thenReturn(entity);
    assertEquals(
        newArrayList(authenticationSettingsImpl.getOidcClients()), singletonList(oidcClient));
  }

  @Test
  public void testGetOidcClientsSignUpDisabled() {
    Entity entity = mock(Entity.class);
    when(dataService.findOneById("sys_set_auth", "auth")).thenReturn(entity);
    assertEquals(newArrayList(authenticationSettingsImpl.getOidcClients()), emptyList());
  }

  @Test
  public void testGetOidcClientsSignUpModerationEnabled() {
    Entity entity = mock(Entity.class);
    when(dataService.findOneById("sys_set_auth", "auth")).thenReturn(entity);
    doReturn(true).when(entity).getBoolean("signup");
    doReturn(true).when(entity).getBoolean("signup_moderation");
    assertEquals(newArrayList(authenticationSettingsImpl.getOidcClients()), emptyList());
  }
}

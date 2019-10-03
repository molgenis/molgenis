package org.molgenis.core.ui;

import static com.google.common.collect.ImmutableList.of;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.core.ui.MolgenisInterceptor.ATTRIBUTE_ENVIRONMENT_TYPE;
import static org.molgenis.web.PluginAttributes.KEY_APP_SETTINGS;
import static org.molgenis.web.PluginAttributes.KEY_AUTHENTICATION_OIDC_CLIENTS;
import static org.molgenis.web.PluginAttributes.KEY_GSON;
import static org.molgenis.web.PluginAttributes.KEY_RESOURCE_FINGERPRINT_REGISTRY;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.core.ui.style.ThemeFingerprintRegistry;
import org.molgenis.core.util.ResourceFingerprintRegistry;
import org.molgenis.data.transaction.TransactionManager;
import org.molgenis.security.oidc.model.OidcClient;
import org.molgenis.security.settings.AuthenticationSettings;
import org.molgenis.settings.AppSettings;
import org.molgenis.test.AbstractMockitoTest;
import org.molgenis.web.PluginAttributes;
import org.springframework.context.MessageSource;
import org.springframework.web.servlet.ModelAndView;

class MolgenisInterceptorTest extends AbstractMockitoTest {
  @Mock private ResourceFingerprintRegistry resourceFingerprintRegistry;
  @Mock private ThemeFingerprintRegistry themeFingerprintRegistry;
  @Mock private AppSettings appSettings;
  @Mock private AuthenticationSettings authenticationSettings;
  @Mock private MessageSource messageSource;
  @Mock private TransactionManager transactionManager;

  private String environment;
  private Gson gson;
  private MolgenisInterceptor molgenisInterceptor;

  @BeforeEach
  void setUpBeforeEach() {
    environment = "development";
    gson = new Gson();
    molgenisInterceptor =
        new MolgenisInterceptor(
            resourceFingerprintRegistry,
            themeFingerprintRegistry,
            appSettings,
            authenticationSettings,
            environment,
            messageSource,
            gson,
            transactionManager);
  }

  @Test
  void MolgenisInterceptor() {
    assertThrows(
        NullPointerException.class,
        () -> new MolgenisInterceptor(null, null, null, null, null, null, null, null));
  }

  @Test
  @SuppressWarnings("unchecked")
  void postHandle() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    Object handler = mock(Object.class);
    ModelAndView modelAndView = new ModelAndView();
    molgenisInterceptor.postHandle(request, response, handler, modelAndView);

    Map<String, Object> model = modelAndView.getModel();
    assertEquals(resourceFingerprintRegistry, model.get(KEY_RESOURCE_FINGERPRINT_REGISTRY));

    Map<String, String> environmentAttributes =
        gson.fromJson(String.valueOf(model.get(PluginAttributes.KEY_ENVIRONMENT)), HashMap.class);

    assertNotNull(model.get(KEY_APP_SETTINGS));
    assertEquals(environment, environmentAttributes.get(ATTRIBUTE_ENVIRONMENT_TYPE));
    assertTrue(model.containsKey(PluginAttributes.KEY_I18N));
    assertSame(model.get(KEY_GSON), gson);
  }

  @Test
  void postHandleWithOidcClient() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    Object handler = mock(Object.class);
    ModelAndView modelAndView = new ModelAndView();

    OidcClient oidcClient = mock(OidcClient.class);
    when(authenticationSettings.getOidcClients()).thenReturn(ImmutableList.of(oidcClient));
    when(oidcClient.getRegistrationId()).thenReturn("registrationId");
    when(oidcClient.getClientName()).thenReturn("clientName");

    molgenisInterceptor.postHandle(request, response, handler, modelAndView);

    Map<String, Object> model = modelAndView.getModel();
    assertEquals(
        of(
            ImmutableMap.of(
                "name",
                "clientName",
                "registrationId",
                "registrationId",
                "requestUri",
                "/oauth2/authorization/registrationId")),
        model.get(KEY_AUTHENTICATION_OIDC_CLIENTS));
  }
}

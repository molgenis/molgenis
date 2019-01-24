package org.molgenis.core.ui;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.web.PluginAttributes.KEY_GSON;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import com.google.gson.Gson;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.molgenis.core.ui.style.ThemeFingerprintRegistry;
import org.molgenis.core.util.ResourceFingerprintRegistry;
import org.molgenis.security.settings.AuthenticationSettings;
import org.molgenis.settings.AppSettings;
import org.molgenis.web.PluginAttributes;
import org.springframework.context.MessageSource;
import org.springframework.web.servlet.ModelAndView;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class MolgenisInterceptorTest {
  private ResourceFingerprintRegistry resourceFingerprintRegistry;
  private ThemeFingerprintRegistry themeFingerprintRegistry;
  private AppSettings appSettings;
  private AuthenticationSettings authenticationSettings;
  private MessageSource messageSource;
  private Gson gson;

  @BeforeMethod
  public void setUp() {
    resourceFingerprintRegistry = mock(ResourceFingerprintRegistry.class);
    themeFingerprintRegistry = mock(ThemeFingerprintRegistry.class);
    appSettings = when(mock(AppSettings.class).getLanguageCode()).thenReturn("en").getMock();
    authenticationSettings = mock(AuthenticationSettings.class);
    messageSource = mock(MessageSource.class);
    gson = new Gson();
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void MolgenisInterceptor() {
    new MolgenisInterceptor(null, null, null, null, null, null, null);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void postHandle() throws Exception {
    String environment = "development";
    MolgenisInterceptor molgenisInterceptor =
        new MolgenisInterceptor(
            resourceFingerprintRegistry,
            themeFingerprintRegistry,
            appSettings,
            authenticationSettings,
            environment,
            messageSource,
            gson);
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    Object handler = mock(Object.class);
    ModelAndView modelAndView = new ModelAndView();
    molgenisInterceptor.postHandle(request, response, handler, modelAndView);

    Map<String, Object> model = modelAndView.getModel();
    assertEquals(
        model.get(PluginAttributes.KEY_RESOURCE_FINGERPRINT_REGISTRY), resourceFingerprintRegistry);

    Map<String, String> environmentAttributes =
        gson.fromJson(String.valueOf(model.get(PluginAttributes.KEY_ENVIRONMENT)), HashMap.class);

    assertEquals(model.get(PluginAttributes.KEY_APP_SETTINGS), appSettings);
    assertEquals(
        environmentAttributes.get(MolgenisInterceptor.ATTRIBUTE_ENVIRONMENT_TYPE), environment);
    assertTrue(model.containsKey(PluginAttributes.KEY_I18N));
    assertSame(model.get(KEY_GSON), gson);
  }
}

package org.molgenis.core.ui;

import static com.google.common.collect.Streams.stream;
import static java.util.Objects.requireNonNull;
import static org.molgenis.security.core.runas.RunAsSystemAspect.runAsSystem;
import static org.molgenis.util.stream.MapCollectors.toLinkedMap;
import static org.molgenis.web.PluginAttributes.KEY_APP_SETTINGS;
import static org.molgenis.web.PluginAttributes.KEY_AUTHENTICATION_OIDC_CLIENTS;
import static org.molgenis.web.PluginAttributes.KEY_AUTHENTICATION_SIGN_UP;
import static org.molgenis.web.PluginAttributes.KEY_ENVIRONMENT;
import static org.molgenis.web.PluginAttributes.KEY_GSON;
import static org.molgenis.web.PluginAttributes.KEY_I18N;
import static org.molgenis.web.PluginAttributes.KEY_RESOURCE_FINGERPRINT_REGISTRY;
import static org.molgenis.web.PluginAttributes.KEY_THEME_FINGERPRINT_REGISTRY;
import static org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter.DEFAULT_AUTHORIZATION_REQUEST_BASE_URI;

import com.google.gson.Gson;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.molgenis.core.ui.style.ThemeFingerprintRegistry;
import org.molgenis.core.util.ResourceFingerprintRegistry;
import org.molgenis.security.oidc.model.OidcClient;
import org.molgenis.security.settings.AuthenticationSettings;
import org.molgenis.settings.AppSettings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.MessageSourceResourceBundle;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/** Interceptor that adds default model objects to all requests that return a view. */
public class MolgenisInterceptor extends HandlerInterceptorAdapter {
  private final ResourceFingerprintRegistry resourceFingerprintRegistry;
  private final ThemeFingerprintRegistry themeFingerprintRegistry;
  private final AuthenticationSettings authenticationSettings;
  private final AppSettings appSettings;
  private final String environment;
  private final MessageSource messageSource;
  private final Gson gson;

  public static final String ATTRIBUTE_ENVIRONMENT_TYPE = "environmentType";

  public MolgenisInterceptor(
      ResourceFingerprintRegistry resourceFingerprintRegistry,
      ThemeFingerprintRegistry themeFingerprintRegistry,
      AppSettings appSettings,
      AuthenticationSettings authenticationSettings,
      @Value("${environment}") String environment,
      MessageSource messageSource,
      Gson gson) {
    this.resourceFingerprintRegistry = requireNonNull(resourceFingerprintRegistry);
    this.themeFingerprintRegistry = requireNonNull(themeFingerprintRegistry);
    this.appSettings = requireNonNull(appSettings);
    this.authenticationSettings = requireNonNull(authenticationSettings);
    this.environment = requireNonNull(environment);
    this.messageSource = requireNonNull(messageSource);
    this.gson = requireNonNull(gson);
  }

  @Override
  public void postHandle(
      HttpServletRequest request,
      HttpServletResponse response,
      Object handler,
      ModelAndView modelAndView) {
    if (modelAndView != null) {
      modelAndView.addObject(KEY_RESOURCE_FINGERPRINT_REGISTRY, resourceFingerprintRegistry);
      modelAndView.addObject(KEY_THEME_FINGERPRINT_REGISTRY, themeFingerprintRegistry);
      modelAndView.addObject(KEY_APP_SETTINGS, appSettings);
      modelAndView.addObject(KEY_AUTHENTICATION_OIDC_CLIENTS, runAsSystem(this::getOidcClients));
      modelAndView.addObject(KEY_AUTHENTICATION_SIGN_UP, authenticationSettings.getSignUp());
      modelAndView.addObject(KEY_ENVIRONMENT, getEnvironmentAttributes());
      modelAndView.addObject(
          KEY_I18N,
          new MessageSourceResourceBundle(messageSource, LocaleContextHolder.getLocale()));
      modelAndView.addObject(KEY_GSON, gson);
    }
  }

  /**
   * Make sure Spring does not add the attributes as query parameters to the url when doing a
   * redirect. You can do this by introducing an object instead of a string key value pair.
   *
   * <p>See <a
   * href="https://github.com/molgenis/molgenis/issues/6515">https://github.com/molgenis/molgenis/issues/6515</a>
   *
   * @return environmentAttributeMap
   */
  private Map<String, String> getEnvironmentAttributes() {
    Map<String, String> environmentAttributes = new HashMap<>();
    environmentAttributes.put(ATTRIBUTE_ENVIRONMENT_TYPE, environment);
    return environmentAttributes;
  }

  private Map<String, String> getOidcClients() {
    return stream(authenticationSettings.getOidcClients())
        .collect(
            toLinkedMap(this::getOidcClientAuthorizationRequestUri, OidcClient::getClientName));
  }

  private String getOidcClientAuthorizationRequestUri(OidcClient oidcClient) {
    return DEFAULT_AUTHORIZATION_REQUEST_BASE_URI + '/' + oidcClient.getRegistrationId();
  }
}

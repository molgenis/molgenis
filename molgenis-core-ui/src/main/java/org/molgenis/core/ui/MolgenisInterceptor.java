package org.molgenis.core.ui;

import static com.google.common.collect.Streams.stream;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.molgenis.security.core.runas.RunAsSystemAspect.runAsSystem;
import static org.molgenis.web.PluginAttributes.KEY_APP_SETTINGS;
import static org.molgenis.web.PluginAttributes.KEY_AUTHENTICATION_OIDC_CLIENTS;
import static org.molgenis.web.PluginAttributes.KEY_AUTHENTICATION_SIGN_UP_FORM;
import static org.molgenis.web.PluginAttributes.KEY_ENVIRONMENT;
import static org.molgenis.web.PluginAttributes.KEY_GSON;
import static org.molgenis.web.PluginAttributes.KEY_I18N;
import static org.molgenis.web.PluginAttributes.KEY_PRIVACY_POLICY_TEXT;
import static org.molgenis.web.PluginAttributes.KEY_RESOURCE_FINGERPRINT_REGISTRY;
import static org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter.DEFAULT_AUTHORIZATION_REQUEST_BASE_URI;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.molgenis.core.util.ResourceFingerprintRegistry;
import org.molgenis.security.oidc.model.OidcClient;
import org.molgenis.security.settings.AuthenticationSettings;
import org.molgenis.security.settings.PrivacyPolicyLevel;
import org.molgenis.security.user.UserAccountService;
import org.molgenis.settings.AppSettings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.MessageSourceResourceBundle;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/** Interceptor that adds default model objects to all requests that return a view. */
public class MolgenisInterceptor extends HandlerInterceptorAdapter {

  public static final String KEY_LANGUAGE = "lng";
  public static final String KEY_FALLBACK_LANGUAGE = "fallbackLng";
  public static final String KEY_SUPER_USER = "isSuperUser";
  private final ResourceFingerprintRegistry resourceFingerprintRegistry;
  private final AuthenticationSettings authenticationSettings;
  private final AppSettings appSettings;
  private final String environment;
  private final MessageSource messageSource;
  private final Gson gson;
  private final PlatformTransactionManager transactionManager;
  private final UserAccountService userAccountService;

  public static final String ATTRIBUTE_ENVIRONMENT_TYPE = "environmentType";

  public MolgenisInterceptor(
      ResourceFingerprintRegistry resourceFingerprintRegistry,
      AppSettings appSettings,
      AuthenticationSettings authenticationSettings,
      @Value("${environment}") String environment,
      MessageSource messageSource,
      Gson gson,
      PlatformTransactionManager transactionManager,
      UserAccountService userAccountService) {
    this.resourceFingerprintRegistry = requireNonNull(resourceFingerprintRegistry);
    this.appSettings = requireNonNull(appSettings);
    this.authenticationSettings = requireNonNull(authenticationSettings);
    this.environment = requireNonNull(environment);
    this.messageSource = requireNonNull(messageSource);
    this.gson = requireNonNull(gson);
    this.transactionManager = requireNonNull(transactionManager);
    this.userAccountService = requireNonNull(userAccountService);
  }

  @Override
  public void postHandle(
      HttpServletRequest request,
      HttpServletResponse response,
      Object handler,
      ModelAndView modelAndView) {
    if (modelAndView != null) {
      String viewName = modelAndView.getViewName();
      if (viewName == null || !viewName.startsWith("forward:")) {
        // use programmatic transaction instead of transaction annotation since we only want to
        // start a read-only transaction when a modelAndView exists
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setReadOnly(true);
        transactionTemplate.execute(
            status -> {
              modelAndView.addObject(
                  KEY_RESOURCE_FINGERPRINT_REGISTRY, resourceFingerprintRegistry);
              modelAndView.addObject(KEY_APP_SETTINGS, createAppSettings());
              modelAndView.addObject(
                  KEY_AUTHENTICATION_OIDC_CLIENTS, runAsSystem(this::getOidcClients));
              modelAndView.addObject(
                  KEY_PRIVACY_POLICY_TEXT, runAsSystem(this::getPrivacyPolicyText));
              modelAndView.addObject(
                  KEY_AUTHENTICATION_SIGN_UP_FORM, authenticationSettings.getSignUpForm());
              modelAndView.addObject(KEY_ENVIRONMENT, getEnvironmentAttributes());
              modelAndView.addObject(
                  KEY_I18N,
                  new MessageSourceResourceBundle(messageSource, LocaleContextHolder.getLocale()));
              modelAndView.addObject(KEY_GSON, gson);
              modelAndView.addObject(KEY_LANGUAGE, LocaleContextHolder.getLocale().getLanguage());
              modelAndView.addObject(KEY_FALLBACK_LANGUAGE, appSettings.getLanguageCode());
              modelAndView.addObject(
                  KEY_SUPER_USER, userAccountService.getCurrentUser().isSuperuser());
              return null;
            });
      }
    }
  }

  private Map<String, Object> createAppSettings() {
    Map<String, Object> modelValue = new HashMap<>();
    modelValue.put("themeURL", appSettings.getThemeURL());
    modelValue.put("legacyThemeURL", appSettings.getLegacyThemeURL());
    modelValue.put("cssHref", appSettings.getCssHref());
    modelValue.put("customJavascript", appSettings.getCustomJavascript());
    modelValue.put("footer", appSettings.getFooter());
    modelValue.put(
        "googleAnalyticsIpAnonymization", appSettings.getGoogleAnalyticsIpAnonymization());
    modelValue.put("googleAnalyticsTrackingId", appSettings.getGoogleAnalyticsTrackingId());
    modelValue.put(
        "googleAnalyticsTrackingIdMolgenis", appSettings.getGoogleAnalyticsTrackingIdMolgenis());
    modelValue.put(
        "googleAnalyticsAccountPrivacyFriendly",
        appSettings.getGoogleAnalyticsAccountPrivacyFriendly());
    modelValue.put(
        "googleAnalyticsAccountPrivacyFriendlyMolgenis",
        appSettings.getGoogleAnalyticsAccountPrivacyFriendlyMolgenis());
    modelValue.put("logoNavBarHref", appSettings.getLogoNavBarHref());
    modelValue.put("logoTopHref", appSettings.getLogoTopHref());
    modelValue.put("logoTopMaxHeight", appSettings.getLogoTopMaxHeight());
    modelValue.put("title", appSettings.getTitle());
    modelValue.put("trackingCodeFooter", appSettings.getTrackingCodeFooter());
    return modelValue;
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

  private List<Map<String, String>> getOidcClients() {
    return stream(authenticationSettings.getOidcClients()).map(this::clientAsMap).collect(toList());
  }

  private String getPrivacyPolicyText() {
    var level = authenticationSettings.getPrivacyPolicyLevel();
    if (level == PrivacyPolicyLevel.CUSTOM) {
      return authenticationSettings.getPrivacyPolicyCustomText();
    } else {
      return level.getPolicy();
    }
  }

  private Map<String, String> clientAsMap(OidcClient client) {
    return ImmutableMap.of(
        "name",
        client.getClientName(),
        "registrationId",
        client.getRegistrationId(),
        "requestUri",
        DEFAULT_AUTHORIZATION_REQUEST_BASE_URI + '/' + client.getRegistrationId());
  }
}

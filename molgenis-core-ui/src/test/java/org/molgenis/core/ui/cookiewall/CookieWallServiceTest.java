package org.molgenis.core.ui.cookiewall;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.molgenis.settings.AppSettings;

class CookieWallServiceTest {

  private CookieWallService service;

  private AppSettings appSettings;

  @BeforeEach
  void setUp() {
    appSettings = mock(AppSettings.class);
    service = new CookieWallService(appSettings);
  }

  @Test
  void noCookieWallIfIpAnonymization() {
    when(appSettings.getGoogleAnalyticsIpAnonymization()).thenReturn(true);
    assertFalse(service.showCookieWall());
  }

  @Test
  void noCookieWallIfNotAnonymizationButNoTrackingSet() {
    when(appSettings.getGoogleAnalyticsIpAnonymization()).thenReturn(false);
    when(appSettings.getGoogleAnalyticsTrackingId()).thenReturn(null);
    when(appSettings.getGoogleAnalyticsTrackingIdMolgenis()).thenReturn(null);

    assertFalse(service.showCookieWall());
  }

  @Test
  void cookieWallIfNotAnonymizationAndTrackingSet() {
    when(appSettings.getGoogleAnalyticsIpAnonymization()).thenReturn(false);
    when(appSettings.getGoogleAnalyticsTrackingId()).thenReturn("set");
    when(appSettings.getGoogleAnalyticsTrackingIdMolgenis()).thenReturn(null);

    assertTrue(service.showCookieWall());
  }

  @Test
  void cookieWallIfNotAnonymizationAndMolgenisTrackingSet() {
    when(appSettings.getGoogleAnalyticsIpAnonymization()).thenReturn(false);
    when(appSettings.getGoogleAnalyticsTrackingId()).thenReturn(null);
    when(appSettings.getGoogleAnalyticsTrackingIdMolgenis()).thenReturn("set");

    assertTrue(service.showCookieWall());
  }

  @Test
  void noCookieWallIfNotAnonymizationAndMolgenisTrackingSetButPrivacyFriendly() {
    when(appSettings.getGoogleAnalyticsIpAnonymization()).thenReturn(false);
    when(appSettings.getGoogleAnalyticsTrackingId()).thenReturn(null);
    when(appSettings.getGoogleAnalyticsTrackingIdMolgenis()).thenReturn("set");
    when(appSettings.getGoogleAnalyticsAccountPrivacyFriendlyMolgenis()).thenReturn(true);

    assertFalse(service.showCookieWall());
  }

  @Test
  void noCookieWallIfNotAnonymizationAndUserTrackingSetButPrivacyFriendly() {
    when(appSettings.getGoogleAnalyticsIpAnonymization()).thenReturn(false);
    when(appSettings.getGoogleAnalyticsTrackingId()).thenReturn("set");
    when(appSettings.getGoogleAnalyticsTrackingIdMolgenis()).thenReturn(null);
    when(appSettings.getGoogleAnalyticsAccountPrivacyFriendly()).thenReturn(true);

    assertFalse(service.showCookieWall());
  }
}

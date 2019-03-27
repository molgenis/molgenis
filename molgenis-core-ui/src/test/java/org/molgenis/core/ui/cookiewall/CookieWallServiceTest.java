package org.molgenis.core.ui.cookiewall;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;

import org.molgenis.settings.AppSettings;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class CookieWallServiceTest {

  private CookieWallService service;

  private AppSettings appSettings;

  @BeforeMethod
  public void setUp() {
    appSettings = mock(AppSettings.class);
    service = new CookieWallService(appSettings);
  }

  @Test
  public void noCookieWallIfIpAnonymization() {
    when(appSettings.getGoogleAnalyticsIpAnonymization()).thenReturn(true);
    assertFalse(service.showCookieWall());
  }

  @Test
  public void noCookieWallIfNotAnonymizationButNoTrackingSet() {
    when(appSettings.getGoogleAnalyticsIpAnonymization()).thenReturn(false);
    when(appSettings.getGoogleAnalyticsTrackingId()).thenReturn(null);
    when(appSettings.getGoogleAnalyticsTrackingIdMolgenis()).thenReturn(null);

    assertFalse(service.showCookieWall());
  }

  @Test
  public void cookieWallIfNotAnonymizationAndTrackingSet() {
    when(appSettings.getGoogleAnalyticsIpAnonymization()).thenReturn(false);
    when(appSettings.getGoogleAnalyticsTrackingId()).thenReturn("set");
    when(appSettings.getGoogleAnalyticsTrackingIdMolgenis()).thenReturn(null);

    assertTrue(service.showCookieWall());
  }

  @Test
  public void cookieWallIfNotAnonymizationAndMolgenisTrackingSet() {
    when(appSettings.getGoogleAnalyticsIpAnonymization()).thenReturn(false);
    when(appSettings.getGoogleAnalyticsTrackingId()).thenReturn(null);
    when(appSettings.getGoogleAnalyticsTrackingIdMolgenis()).thenReturn("set");

    assertTrue(service.showCookieWall());
  }

  @Test
  public void noCookieWallIfNotAnonymizationAndMolgenisTrackingSetButPrivacyFriendly() {
    when(appSettings.getGoogleAnalyticsIpAnonymization()).thenReturn(false);
    when(appSettings.getGoogleAnalyticsTrackingId()).thenReturn(null);
    when(appSettings.getGoogleAnalyticsTrackingIdMolgenis()).thenReturn("set");
    when(appSettings.getGoogleAnalyticsAccountPrivacyFriendlyMolgenis()).thenReturn(true);

    assertFalse(service.showCookieWall());
  }

  @Test
  public void noCookieWallIfNotAnonymizationAndUserTrackingSetButPrivacyFriendly() {
    when(appSettings.getGoogleAnalyticsIpAnonymization()).thenReturn(false);
    when(appSettings.getGoogleAnalyticsTrackingId()).thenReturn("set");
    when(appSettings.getGoogleAnalyticsTrackingIdMolgenis()).thenReturn(null);
    when(appSettings.getGoogleAnalyticsAccountPrivacyFriendly()).thenReturn(true);

    assertFalse(service.showCookieWall());
  }
}

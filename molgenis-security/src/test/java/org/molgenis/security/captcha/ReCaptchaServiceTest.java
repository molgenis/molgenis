package org.molgenis.security.captcha;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.mockito.Mock;
import org.molgenis.settings.AppSettings;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ReCaptchaServiceTest extends AbstractMockitoTest {

  private ReCaptchaService reCaptchaService;

  @Mock private AppSettings appSettings;

  @Mock private CloseableHttpClient httpClient;

  @Mock private ReCaptchaHttpPostFactory reCaptchaHttpPostFactory;

  @BeforeMethod
  public void setUpBeforeMethod() {
    reCaptchaService = new ReCaptchaService(httpClient, appSettings, reCaptchaHttpPostFactory);
  }

  @Test
  public void testValidate() throws IOException {
    mockVerificationSetup(0.5, 0.6);
    String token = "test-token";
    assertTrue(reCaptchaService.validate(token));
  }

  @Test
  public void testValidateUnderThreshold() throws IOException {
    mockVerificationSetup(0.5, 0.4);
    String token = "test-token";
    assertFalse(reCaptchaService.validate(token));
  }

  private void mockVerificationSetup(double threshold, double score) throws IOException {
    CloseableHttpResponse closeableHttpResponse =
        mock(CloseableHttpResponse.class, RETURNS_DEEP_STUBS);
    HttpPost httpPost = mock(HttpPost.class);
    HttpEntity entity = mock(HttpEntity.class);

    when(appSettings.getRecaptchaVerifyURI()).thenReturn("http://verify.test.org");
    when(appSettings.getRecaptchaBotThreshold()).thenReturn(threshold);
    when(reCaptchaHttpPostFactory.create(appSettings.getRecaptchaVerifyURI())).thenReturn(httpPost);

    String jsonResponse = "{ success: true, score: " + score + " }";

    when(entity.getContent()).thenReturn(new ByteArrayInputStream(jsonResponse.getBytes()));
    when(closeableHttpResponse.getStatusLine().getStatusCode()).thenReturn(HttpStatus.SC_OK);
    when(closeableHttpResponse.getEntity()).thenReturn(entity);

    when(httpClient.execute(httpPost)).thenReturn(closeableHttpResponse);
  }
}

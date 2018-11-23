package org.molgenis.security.captcha;

import static java.util.Objects.requireNonNull;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.micrometer.core.annotation.Timed;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.molgenis.settings.AppSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ReCaptchaService {

  private static final Logger LOG = LoggerFactory.getLogger(ReCaptchaService.class);
  private static final String LOG_MESSAGE = "Failed reCaptcha validation";

  private final AppSettings appSettings;
  private final CloseableHttpClient httpClient;
  private final ReCaptchaHttpPostFactory reCaptchaHttpPostFactory;

  public ReCaptchaService(
      CloseableHttpClient httpClient,
      AppSettings appSettings,
      ReCaptchaHttpPostFactory reCaptchaHttpPostFactory) {
    this.httpClient = requireNonNull(httpClient);
    this.appSettings = requireNonNull(appSettings);
    this.reCaptchaHttpPostFactory = requireNonNull(reCaptchaHttpPostFactory);
  }

  @Timed(
      value = "recaptcha.validate",
      description = "Timing information for the reCaptcha validation service.",
      histogram = true)
  public boolean validate(String token) {
    HttpPost httpPost = null;
    boolean isValid = false;
    try {
      httpPost = reCaptchaHttpPostFactory.create(appSettings.getRecaptchaVerifyURI());
      NameValuePair secret = new BasicNameValuePair("secret", appSettings.getRecaptchaPrivateKey());
      NameValuePair responseToken = new BasicNameValuePair("response", token);
      httpPost.setEntity(new UrlEncodedFormEntity(Arrays.asList(secret, responseToken)));
    } catch (UnsupportedEncodingException err) {
      LOG.error(LOG_MESSAGE, err);
    }

    try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
      int statusCode = response.getStatusLine().getStatusCode();
      if (statusCode >= HttpStatus.SC_OK && statusCode < HttpStatus.SC_MULTIPLE_CHOICES) {
        String result = EntityUtils.toString(response.getEntity());
        Gson gson = new Gson();
        JsonParser parser = new JsonParser();
        JsonObject object = (JsonObject) parser.parse(result); // response will be the json String
        ReCaptchaValidationResponse validationResult =
            gson.fromJson(object, ReCaptchaValidationResponse.class);

        isValid =
            validationResult.isSuccess()
                && validationResult.getScore() > appSettings.getRecaptchaBotThreshold();
      }
    } catch (NumberFormatException | IOException err) {
      LOG.error(LOG_MESSAGE, err);
    }
    return isValid;
  }
}

package org.molgenis.security.captcha;

import java.net.URI;
import org.apache.http.client.methods.HttpPost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ReCaptchaHttpPostFactory {

  private static final Logger LOG = LoggerFactory.getLogger(ReCaptchaHttpPostFactory.class);

  public ReCaptchaHttpPostFactory() {}

  HttpPost create(String uri) {
    if (uri.isEmpty()) {
      LOG.error("No valid verification url provided");
    }
    return new HttpPost(URI.create(uri));
  }
}

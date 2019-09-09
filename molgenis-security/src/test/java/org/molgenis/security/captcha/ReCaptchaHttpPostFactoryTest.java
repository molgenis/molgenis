package org.molgenis.security.captcha;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import org.apache.http.client.methods.HttpPost;
import org.junit.jupiter.api.Test;

class ReCaptchaHttpPostFactoryTest {

  private ReCaptchaHttpPostFactory factory = new ReCaptchaHttpPostFactory();

  @Test
  void testHttpPostCreate() {
    HttpPost post = factory.create("http://verify.test.org");
    assertEquals(post.getURI(), URI.create("http://verify.test.org"));
  }

  @Test
  void testHttpPostCreateEmptyURI() {
    factory.create("");
  }
}

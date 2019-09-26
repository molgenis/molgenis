package org.molgenis.security.captcha;

import static java.net.URI.create;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.http.client.methods.HttpPost;
import org.junit.jupiter.api.Test;

class ReCaptchaHttpPostFactoryTest {

  private ReCaptchaHttpPostFactory factory = new ReCaptchaHttpPostFactory();

  @Test
  void testHttpPostCreate() {
    HttpPost post = factory.create("http://verify.test.org");
    assertEquals(create("http://verify.test.org"), post.getURI());
  }

  @Test
  void testHttpPostCreateEmptyURI() {
    assertDoesNotThrow(() -> factory.create(""));
  }
}

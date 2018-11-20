package org.molgenis.security.captcha;

import static org.testng.Assert.assertEquals;

import java.net.URI;
import org.apache.http.client.methods.HttpPost;
import org.testng.annotations.Test;

public class ReCaptchaHttpPostFactoryTest {

  private ReCaptchaHttpPostFactory factory = new ReCaptchaHttpPostFactory();

  @Test
  public void testHttpPostCreate() {
    HttpPost post = factory.create("http://verify.test.org");
    assertEquals(post.getURI(), URI.create("http://verify.test.org"));
  }

  @Test
  public void testHttpPostCreateEmptyURI() {
    factory.create("");
  }
}

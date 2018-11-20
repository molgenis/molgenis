package org.molgenis.security.captcha;

import java.util.Objects;

public class ReCaptchaValidationRequest {

  private String token;

  public ReCaptchaValidationRequest(String token) {
    this.token = token;
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ReCaptchaValidationRequest that = (ReCaptchaValidationRequest) o;
    return Objects.equals(token, that.token);
  }

  @Override
  public int hashCode() {
    return Objects.hash(token);
  }
}

package org.molgenis.security.captcha;

import java.util.List;
import java.util.Objects;

public class ReCaptchaValidationResponse {

  private boolean success;
  private double score;
  private String action;
  private String
      challenge_ts; // timestamp of the challenge load (ISO format yyyy-MM-dd'T'HH:mm:ssZZ)
  private String string;
  private String hostname;
  private List<String> errorCodes;

  public ReCaptchaValidationResponse(
      boolean success,
      double score,
      String action,
      String challenge_ts,
      String string,
      String hostname,
      List<String> errorCodes) {
    this.success = success;
    this.score = score;
    this.action = action;
    this.challenge_ts = challenge_ts;
    this.string = string;
    this.hostname = hostname;
    this.errorCodes = errorCodes;
  }

  public boolean isSuccess() {
    return success;
  }

  public void setSuccess(boolean success) {
    this.success = success;
  }

  public double getScore() {
    return score;
  }

  public void setScore(double score) {
    this.score = score;
  }

  public String getAction() {
    return action;
  }

  public void setAction(String action) {
    this.action = action;
  }

  public String getChallenge_ts() {
    return challenge_ts;
  }

  public void setChallenge_ts(String challenge_ts) {
    this.challenge_ts = challenge_ts;
  }

  public String getString() {
    return string;
  }

  public void setString(String string) {
    this.string = string;
  }

  public String getHostname() {
    return hostname;
  }

  public void setHostname(String hostname) {
    this.hostname = hostname;
  }

  public List<String> getErrorCodes() {
    return errorCodes;
  }

  public void setErrorCodes(List<String> errorCodes) {
    this.errorCodes = errorCodes;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ReCaptchaValidationResponse that = (ReCaptchaValidationResponse) o;
    return success == that.success
        && Double.compare(that.score, score) == 0
        && Objects.equals(action, that.action)
        && Objects.equals(challenge_ts, that.challenge_ts)
        && Objects.equals(string, that.string)
        && Objects.equals(hostname, that.hostname)
        && Objects.equals(errorCodes, that.errorCodes);
  }

  @Override
  public int hashCode() {
    return Objects.hash(success, score, action, challenge_ts, string, hostname, errorCodes);
  }
}

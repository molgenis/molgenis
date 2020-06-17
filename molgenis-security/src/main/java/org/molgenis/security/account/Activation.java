package org.molgenis.security.account;

import java.util.Objects;

public class Activation {

  private final String activationCode;

  public Activation(String activationCode) {
    this.activationCode = activationCode;
  }

  public String getActivationCode() {
    return activationCode;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Activation that = (Activation) o;
    return activationCode.equals(that.activationCode);
  }

  @Override
  public int hashCode() {
    return Objects.hash(activationCode);
  }

  @Override
  public String toString() {
    return "Activation{" +
        "activation='" + activationCode + '\'' +
        '}';
  }
}

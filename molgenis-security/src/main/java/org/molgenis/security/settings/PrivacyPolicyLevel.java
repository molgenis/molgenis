package org.molgenis.security.settings;

public enum PrivacyPolicyLevel {
  DISABLED("Disabled", ""),
  CUSTOM("Custom", "");

  private final String label;
  private final String policy;

  PrivacyPolicyLevel(String label, String policy) {
    this.label = label;
    this.policy = policy;
  }

  public String getLabel() {
    return label;
  }

  public String getPolicy() {
    return policy;
  }

  public static PrivacyPolicyLevel fromLabel(String label) {
    return valueOf(label.toUpperCase().replace(' ', '_'));
  }
}

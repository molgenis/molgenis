package org.molgenis.security.settings;

public enum PrivacyPolicyLevel {
  DISABLED("Disabled", ""),
  LEVEL_1(
      "Level 1",
      "I have read and agree to the privacy policy described at <a href=\"https://www.molgenis.org/MOLGENIS_Privacy_Policy.pdf\">https://www.molgenis.org/MOLGENIS_Privacy_Policy.pdf</a>"),
  LEVEL_2("Level 2", "I have read and agree to the privacy policy described at <a><TODO></a>"),
  LEVEL_3("Level 3", "I have read and agree to the privacy policy described at <a><TODO></a>"),
  LEVEL_4("Level 4", "I have read and agree to the privacy policy described at <a><TODO></a>"),
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

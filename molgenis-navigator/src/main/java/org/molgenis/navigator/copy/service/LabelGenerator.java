package org.molgenis.navigator.copy.service;

import java.util.Set;

class LabelGenerator {
  private static final String POSTFIX = " (Copy)";

  private LabelGenerator() {}

  /** Makes sure that a given label will be unique amongst a set of other labels. */
  static String generateUniqueLabel(String label, Set<String> existingLabels) {
    StringBuilder newLabel = new StringBuilder(label);
    while (existingLabels.contains(newLabel.toString())) {
      newLabel.append(POSTFIX);
    }
    return newLabel.toString();
  }
}

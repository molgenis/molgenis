package org.molgenis.navigator.copy.service;

import static com.google.common.collect.Sets.newHashSet;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;
import org.junit.jupiter.api.Test;

class LabelGeneratorTest {
  @Test
  void testGenerateLabelAlreadyUnique() {
    Set<String> existingLabels = newHashSet("labelA", "labelB", "labelC");

    String result = LabelGenerator.generateUniqueLabel("labelX", existingLabels);

    assertEquals(result, "labelX");
  }

  @Test
  void testGenerateUniqueLabel() {
    Set<String> existingLabels = newHashSet("labelA", "labelB", "labelC");

    String result = LabelGenerator.generateUniqueLabel("labelB", existingLabels);

    assertEquals(result, "labelB (Copy)");
  }

  @Test
  void testGenerateUniqueLabelRecursive() {
    Set<String> existingLabels = newHashSet("labelA", "labelB", "labelB (Copy)", "labelC");

    String result = LabelGenerator.generateUniqueLabel("labelB", existingLabels);

    assertEquals(result, "labelB (Copy) (Copy)");
  }
}

package org.molgenis.navigator.copy.service;

import static com.google.common.collect.Sets.newHashSet;
import static org.testng.Assert.assertEquals;

import java.util.Set;
import org.testng.annotations.Test;

public class LabelGeneratorTest {
  @Test
  public void testGenerateLabelAlreadyUnique() {
    Set<String> existingLabels = newHashSet("labelA", "labelB", "labelC");

    String result = LabelGenerator.generateUniqueLabel("labelX", existingLabels);

    assertEquals(result, "labelX");
  }

  @Test
  public void testGenerateUniqueLabel() {
    Set<String> existingLabels = newHashSet("labelA", "labelB", "labelC");

    String result = LabelGenerator.generateUniqueLabel("labelB", existingLabels);

    assertEquals(result, "labelB (Copy)");
  }

  @Test
  public void testGenerateUniqueLabelRecursive() {
    Set<String> existingLabels = newHashSet("labelA", "labelB", "labelB (Copy)", "labelC");

    String result = LabelGenerator.generateUniqueLabel("labelB", existingLabels);

    assertEquals(result, "labelB (Copy) (Copy)");
  }
}

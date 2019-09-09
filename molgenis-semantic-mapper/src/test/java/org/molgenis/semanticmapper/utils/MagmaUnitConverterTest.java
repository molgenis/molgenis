package org.molgenis.semanticmapper.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.Sets;
import java.util.Set;
import javax.measure.unit.Unit;
import org.junit.jupiter.api.Test;

class MagmaUnitConverterTest {
  MagmaUnitConverter unitConverter = new MagmaUnitConverter();

  @Test
  void convertUnit() {
    assertEquals(unitConverter.convertUnit(Unit.valueOf("kg"), Unit.valueOf("g")), ".div(1000.0)");
    assertEquals(unitConverter.convertUnit(Unit.valueOf("m"), Unit.valueOf("cm")), ".div(100.0)");
    assertEquals(
        unitConverter.convertUnit(Unit.valueOf("kg/m²"), Unit.valueOf("g")), ".div(1000.0)");
    assertEquals(
        unitConverter.convertUnit(Unit.valueOf("kg/m²"), Unit.valueOf("cm")), ".div(100.0)");
  }

  @Test
  void findCompositeUnitNames() {
    Set<String> findCompositeUnitNames = unitConverter.findCompositeUnitNames("kg/m²");
    assertTrue(Sets.newHashSet("kg", "m", "kg/m²").containsAll(findCompositeUnitNames));
  }
}

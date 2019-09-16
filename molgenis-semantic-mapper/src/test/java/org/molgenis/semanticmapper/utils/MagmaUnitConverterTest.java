package org.molgenis.semanticmapper.utils;

import static javax.measure.unit.Unit.valueOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.Sets;
import java.util.Set;
import org.junit.jupiter.api.Test;

class MagmaUnitConverterTest {
  MagmaUnitConverter unitConverter = new MagmaUnitConverter();

  @Test
  void convertUnit() {
    assertEquals(".div(1000.0)", unitConverter.convertUnit(valueOf("kg"), valueOf("g")));
    assertEquals(".div(100.0)", unitConverter.convertUnit(valueOf("m"), valueOf("cm")));
    assertEquals(".div(1000.0)", unitConverter.convertUnit(valueOf("kg/m²"), valueOf("g")));
    assertEquals(".div(100.0)", unitConverter.convertUnit(valueOf("kg/m²"), valueOf("cm")));
  }

  @Test
  void findCompositeUnitNames() {
    Set<String> findCompositeUnitNames = unitConverter.findCompositeUnitNames("kg/m²");
    assertTrue(Sets.newHashSet("kg", "m", "kg/m²").containsAll(findCompositeUnitNames));
  }
}

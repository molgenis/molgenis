package org.molgenis.semanticmapper.service;

import javax.measure.quantity.Quantity;
import javax.measure.unit.Unit;
import org.molgenis.data.meta.model.Attribute;

public interface UnitResolver {

  /**
   * Determine attribute unit based on meta data
   *
   * @param attr attribute for which to determine unit
   * @return unit or null
   */
  Unit<? extends Quantity> resolveUnit(Attribute attr);
}

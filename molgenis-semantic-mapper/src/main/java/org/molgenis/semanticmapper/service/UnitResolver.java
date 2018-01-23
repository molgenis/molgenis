package org.molgenis.semanticmapper.service;

import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;

import javax.measure.quantity.Quantity;
import javax.measure.unit.Unit;

public interface UnitResolver
{
	/**
	 * Determine attribute unit based on meta data
	 *
	 * @param attr       attribute for which to determine unit
	 * @param entityType corresponding entity meta data for attribute
	 * @return unit or null
	 */
	Unit<? extends Quantity> resolveUnit(Attribute attr, EntityType entityType);
}

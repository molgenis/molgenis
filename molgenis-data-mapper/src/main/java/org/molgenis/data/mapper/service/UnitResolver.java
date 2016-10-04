package org.molgenis.data.mapper.service;

import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityMetaData;

import javax.measure.quantity.Quantity;
import javax.measure.unit.Unit;

public interface UnitResolver
{
	/**
	 * Determine attribute unit based on meta data
	 *
	 * @param attr       attribute for which to determine unit
	 * @param entityMeta corresponding entity meta data for attribute
	 * @return unit or null
	 */
	Unit<? extends Quantity> resolveUnit(Attribute attr, EntityMetaData entityMeta);
}

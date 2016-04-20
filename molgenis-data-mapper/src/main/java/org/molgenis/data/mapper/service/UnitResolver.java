package org.molgenis.data.mapper.service;

import javax.measure.quantity.Quantity;
import javax.measure.unit.Unit;

import org.molgenis.data.meta.AttributeMetaData;
import org.molgenis.data.meta.EntityMetaData;

public interface UnitResolver
{
	/**
	 * Determine attribute unit based on meta data
	 * 
	 * @param attr
	 *            attribute for which to determine unit
	 * @param entityMeta
	 *            corresponding entity meta data for attribute
	 * @return unit or null
	 */
	Unit<? extends Quantity> resolveUnit(AttributeMetaData attr, EntityMetaData entityMeta);
}

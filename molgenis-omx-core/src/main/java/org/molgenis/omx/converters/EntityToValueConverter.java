package org.molgenis.omx.converters;

import org.molgenis.data.Entity;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.value.Value;
import org.molgenis.util.Cell;

public interface EntityToValueConverter<S extends Value, T>
{
	/**
	 * Converts a tuple column value to a value entity
	 * 
	 * @param entity
	 * @param attributeName
	 * @param feature
	 * @return
	 * @throws ValueConverterException
	 */
	public S fromEntity(Entity tuple, String attributeName, ObservableFeature feature) throws ValueConverterException;

	/**
	 * Updates an existing value by converting a tuple column
	 * 
	 * @param entity
	 * @param attributeName
	 * @param feature
	 * @param value
	 * @return
	 * @throws ValueConverterException
	 */
	public S updateFromEntity(Entity entity, String attributeName, ObservableFeature feature, Value value)
			throws ValueConverterException;

	/**
	 * Returns the java type value contained by a value entity
	 * 
	 * @param value
	 * @return
	 */
	public Cell<T> toCell(Value value) throws ValueConverterException;
}
package org.molgenis.omx.converters;

import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.value.Value;
import org.molgenis.util.tuple.Cell;
import org.molgenis.util.tuple.Tuple;

public interface TupleToValueConverter<S extends Value, T>
{
	/**
	 * Converts a tuple column value to a value entity
	 * 
	 * @param tuple
	 * @param colName
	 * @param feature
	 * @return
	 * @throws ValueConverterException
	 */
	public S fromTuple(Tuple tuple, String colName, ObservableFeature feature) throws ValueConverterException;

	/**
	 * Updates an existing value by converting a tuple column
	 * 
	 * @param tuple
	 * @param colName
	 * @param feature
	 * @param value
	 * @return
	 * @throws ValueConverterException
	 */
	public S updateFromTuple(Tuple tuple, String colName, ObservableFeature feature, Value value)
			throws ValueConverterException;

	/**
	 * Returns the java type value contained by a value entity
	 * 
	 * @param value
	 * @return
	 */
	public Cell<T> toCell(Value value) throws ValueConverterException;
}
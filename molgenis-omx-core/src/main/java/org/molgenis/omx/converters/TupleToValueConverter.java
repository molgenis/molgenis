package org.molgenis.omx.converters;

import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.value.Value;
import org.molgenis.util.tuple.Tuple;

public interface TupleToValueConverter<S extends Value, T>
{
	/**
	 * Converts a tuple column value to a value entity
	 * 
	 * @param <S>
	 * @param <T>
	 */
	public S fromTuple(Tuple tuple, String colName, ObservableFeature feature) throws ValueConverterException;

	/**
	 * Returns the java type value contained by a value entity
	 * 
	 * @param value
	 * @return
	 */
	public T extractValue(Value value);
}
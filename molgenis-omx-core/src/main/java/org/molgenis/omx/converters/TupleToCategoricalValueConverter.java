package org.molgenis.omx.converters;

import org.molgenis.data.DataService;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Query;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.omx.observ.Category;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.value.CategoricalValue;
import org.molgenis.omx.observ.value.Value;
import org.molgenis.omx.utils.ValueCell;
import org.molgenis.util.tuple.Cell;
import org.molgenis.util.tuple.Tuple;

public class TupleToCategoricalValueConverter implements TupleToValueConverter<CategoricalValue, String>
{
	private final DataService dataService;

	public TupleToCategoricalValueConverter(DataService dataService)
	{
		if (dataService == null) throw new IllegalArgumentException("Database is null");
		this.dataService = dataService;
	}

	@Override
	public CategoricalValue fromTuple(Tuple tuple, String colName, ObservableFeature feature)
			throws ValueConverterException
	{
		return updateFromTuple(tuple, colName, feature, new CategoricalValue());
	}

	@Override
	public CategoricalValue updateFromTuple(Tuple tuple, String colName, ObservableFeature feature, Value value)
			throws ValueConverterException
	{
		if (!(value instanceof CategoricalValue))
		{
			throw new ValueConverterException("value is not a " + CategoricalValue.class.getSimpleName());
		}

		String categoryValueCode = tuple.getString(colName);
		if (categoryValueCode == null) return null;

		Category category;
		try
		{
			Query q = new QueryImpl().eq(Category.OBSERVABLEFEATURE, feature).and()
					.eq(Category.VALUECODE, categoryValueCode);
			category = dataService.findOne(Category.ENTITY_NAME, q);
			if (category == null)
			{
				throw new ValueConverterException("unknown category value code [" + categoryValueCode + ']');
			}
		}
		catch (MolgenisDataException e)
		{
			throw new ValueConverterException(e);
		}
		CategoricalValue categoricalValue = (CategoricalValue) value;
		categoricalValue.setValue(category);
		return categoricalValue;
	}

	@Override
	public Cell<String> toCell(Value value) throws ValueConverterException
	{
		if (!(value instanceof CategoricalValue))
		{
			throw new ValueConverterException("value is not a " + CategoricalValue.class.getSimpleName());
		}
		Category category = ((CategoricalValue) value).getValue();
		return new ValueCell<String>(category.getIdentifier(), category.getName());
	}
}

package org.molgenis.omx.converters;

import java.util.List;

import org.molgenis.data.DataService;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.QueryRule;
import org.molgenis.data.QueryRule.Operator;
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
			List<Category> categories = dataService.findAllAsList(Category.ENTITY_NAME, new QueryRule(
					Category.OBSERVABLEFEATURE, Operator.EQUALS, feature), new QueryRule(Category.VALUECODE,
					Operator.EQUALS, categoryValueCode));

			if (categories == null || categories.isEmpty())
			{
				throw new ValueConverterException("unknown category value code [" + categoryValueCode + ']');
			}
			category = categories.get(0);
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

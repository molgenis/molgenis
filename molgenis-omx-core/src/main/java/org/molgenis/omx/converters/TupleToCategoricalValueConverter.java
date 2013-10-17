package org.molgenis.omx.converters;

import java.util.List;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
import org.molgenis.omx.observ.Category;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.value.CategoricalValue;
import org.molgenis.omx.observ.value.Value;
import org.molgenis.omx.utils.ValueCell;
import org.molgenis.util.tuple.Cell;
import org.molgenis.util.tuple.Tuple;

public class TupleToCategoricalValueConverter implements TupleToValueConverter<CategoricalValue, String>
{
	private final Database database;

	public TupleToCategoricalValueConverter(Database database)
	{
		if (database == null) throw new IllegalArgumentException("Database is null");
		this.database = database;
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
			List<Category> categories = database.find(Category.class, new QueryRule(Category.OBSERVABLEFEATURE,
					Operator.EQUALS, feature), new QueryRule(Category.VALUECODE, Operator.EQUALS, categoryValueCode));
			if (categories == null || categories.isEmpty())
			{
				throw new ValueConverterException("unknown category value code [" + categoryValueCode + ']');
			}
			category = categories.get(0);
		}
		catch (DatabaseException e)
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

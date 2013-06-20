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
import org.molgenis.util.tuple.Tuple;

public class TupleToCategoricalValueConverter implements TupleToValueConverter<CategoricalValue, String>
{
	@Override
	public CategoricalValue fromTuple(Tuple tuple, String colName, Database db, ObservableFeature feature)
			throws ValueConverterException
	{
		String categoryValueCode = tuple.getString(colName);
		if (categoryValueCode == null) return null;

		Category category;
		try
		{
			List<Category> categories = db.find(Category.class, new QueryRule(Category.OBSERVABLEFEATURE,
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
		CategoricalValue categoricalValue = new CategoricalValue();
		categoricalValue.setValue(category);
		return categoricalValue;
	}

	@Override
	public String extractValue(Value value)
	{
		return ((CategoricalValue) value).getValue().getName(); // TODO name or value code or maybe even identifier?
	}
}

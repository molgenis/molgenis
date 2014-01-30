package org.molgenis.omx.converters;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Query;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.omx.observ.Category;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.value.CategoricalValue;
import org.molgenis.omx.observ.value.Value;
import org.molgenis.omx.utils.ValueCell;
import org.molgenis.util.Cell;

public class EntityToCategoricalValueConverter implements EntityToValueConverter<CategoricalValue, String>
{
	private final DataService dataService;

	public EntityToCategoricalValueConverter(DataService dataService)
	{
		if (dataService == null) throw new IllegalArgumentException("Database is null");
		this.dataService = dataService;
	}

	@Override
	public CategoricalValue fromEntity(Entity entity, String attributeName, ObservableFeature feature)
			throws ValueConverterException
	{
		return updateFromEntity(entity, attributeName, feature, new CategoricalValue());
	}

	@Override
	public CategoricalValue updateFromEntity(Entity entity, String attributeName, ObservableFeature feature, Value value)
			throws ValueConverterException
	{
		if (!(value instanceof CategoricalValue))
		{
			throw new ValueConverterException("value is not a " + CategoricalValue.class.getSimpleName());
		}

		String categoryValueCode = entity.getString(attributeName);
		if (categoryValueCode == null) return null;

		Category category;
		try
		{
			Query q = new QueryImpl().eq(Category.OBSERVABLEFEATURE, feature).and()
					.eq(Category.VALUECODE, categoryValueCode);
			category = dataService.findOne(Category.ENTITY_NAME, q, Category.class);
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

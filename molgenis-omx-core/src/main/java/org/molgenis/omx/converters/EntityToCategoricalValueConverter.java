package org.molgenis.omx.converters;

import org.apache.commons.io.IOUtils;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Query;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.data.support.QueryResolver;
import org.molgenis.omx.observ.Category;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.value.CategoricalValue;
import org.molgenis.omx.observ.value.Value;
import org.molgenis.omx.protocol.OmxLookupTableRepository;
import org.molgenis.omx.utils.ValueCell;
import org.molgenis.util.Cell;

public class EntityToCategoricalValueConverter implements EntityToValueConverter<CategoricalValue, String>
{
	private final DataService dataService;
	private final QueryResolver queryResolver;

	public EntityToCategoricalValueConverter(DataService dataService)
	{
		if (dataService == null) throw new IllegalArgumentException("Database is null");
		this.dataService = dataService;
		this.queryResolver = new QueryResolver(dataService);
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

		String valueCode = entity.getString(attributeName);
		if (valueCode == null) return null;

		Category category;
		try
		{

			Query q = new QueryImpl().eq(Category.VALUECODE, valueCode).and().eq(Category.OBSERVABLEFEATURE, feature);
			category = dataService.findOne(Category.ENTITY_NAME, q, Category.class);
			if (category == null)
			{
				throw new ValueConverterException("unknown category valueCode [" + valueCode + ']');
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
	public Cell<String> toCell(Value value, ObservableFeature feature) throws ValueConverterException
	{
		if (!(value instanceof CategoricalValue))
		{
			throw new ValueConverterException("value is not a " + CategoricalValue.class.getSimpleName());
		}

		Category category = ((CategoricalValue) value).getValue();
		OmxLookupTableRepository lutRepo = new OmxLookupTableRepository(dataService, feature.getIdentifier(),
				queryResolver);
		try
		{
			Query q = new QueryImpl().eq(Category.VALUECODE, category.getValueCode());
			Entity entity = lutRepo.findOne(q);
			String label = entity.getString(lutRepo.getEntityMetaData().getLabelAttribute().getName());
			return new ValueCell<String>(category.getId(), label, label);
		}
		finally
		{
			IOUtils.closeQuietly(lutRepo);
		}
	}
}

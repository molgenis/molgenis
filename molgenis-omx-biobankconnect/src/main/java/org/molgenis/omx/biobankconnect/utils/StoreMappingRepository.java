package org.molgenis.omx.biobankconnect.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.support.AbstractRepository;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.omx.converters.ValueConverter;
import org.molgenis.omx.converters.ValueConverterException;
import org.molgenis.omx.observ.CategoryMetaData;
import org.molgenis.omx.observ.CharacteristicMetaData;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.ObservationSet;
import org.molgenis.omx.observ.ObservedValue;
import org.molgenis.omx.observ.Protocol;

import com.google.common.collect.Lists;

public class StoreMappingRepository extends AbstractRepository
{
	private final Iterable<ObservedValue> observedValues;
	private final ValueConverter valueConverter;
	private final DataSet dataSet;
	private DefaultEntityMetaData metaData = null;

	public StoreMappingRepository(DataSet dataSet, DataService dataService)
	{
		this.dataSet = dataSet;
		Iterable<ObservationSet> observationSets = dataService.findAll(ObservationSet.ENTITY_NAME,
				new QueryImpl().eq(ObservationSet.PARTOFDATASET, dataSet), ObservationSet.class);
		observedValues = dataService.findAll(ObservedValue.ENTITY_NAME,
				new QueryImpl().in(ObservedValue.OBSERVATIONSET, Lists.newArrayList(observationSets)),
				ObservedValue.class);
		valueConverter = new ValueConverter(dataService);

	}

	public StoreMappingRepository(DataSet dataSet, List<ObservedValue> observedValues, DataService dataService)
	{
		this.dataSet = dataSet;
		this.observedValues = observedValues;
		this.valueConverter = new ValueConverter(dataService);
	}

	@Override
	public Class<? extends Entity> getEntityClass()
	{
		return MapEntity.class;
	}

	@Override
	public Iterator<Entity> iterator()
	{
		List<Entity> entities = new ArrayList<Entity>();

		try
		{
			Map<Integer, Entity> storeMapping = new HashMap<Integer, Entity>();

			for (ObservedValue ov : observedValues)
			{
				Entity entity = null;
				Integer observationId = ov.getObservationSet().getId();
				if (storeMapping.containsKey(observationId)) entity = storeMapping.get(observationId);
				else entity = new MapEntity();
				entity.set(ov.getFeature().getIdentifier(), valueConverter.toCell(ov.getValue()));
				storeMapping.put(observationId, entity);
			}

			for (Entity entity : storeMapping.values())
				entities.add(entity);
		}
		catch (ValueConverterException e)
		{
			throw new RuntimeException("Failed to index mapping table : " + dataSet.getName() + " error : "
					+ e.getMessage());
		}

		return entities.iterator();
	}

	@Override
	public void close() throws IOException
	{
		// Nothing
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		if (metaData == null)
		{
			metaData = new DefaultEntityMetaData(dataSet.getIdentifier());
			metaData.setLabel(dataSet.getLabelValue());

			Protocol protocol = dataSet.getProtocolUsed();
			if (protocol != null)
			{
				// Get all features from protocol including subprotocols
				List<ObservableFeature> features = new ArrayList<ObservableFeature>();
				getFeatures(protocol, features);

				for (ObservableFeature feature : features)
				{
					FieldTypeEnum fieldType = MolgenisFieldTypes.getType(feature.getDataType()).getEnumType();
					DefaultAttributeMetaData attr = new DefaultAttributeMetaData(feature.getIdentifier(), fieldType);

					attr.setDescription(feature.getDescription());
					attr.setLabel(feature.getName());
					attr.setIdAttribute(false);
					attr.setLabelAttribute(false);

					if (fieldType.equals(FieldTypeEnum.XREF) || fieldType.equals(FieldTypeEnum.MREF))
					{
						attr.setRefEntity(new CharacteristicMetaData());
					}
					else if (fieldType.equals(FieldTypeEnum.CATEGORICAL))
					{
						attr.setRefEntity(new CategoryMetaData());
					}

					metaData.addAttributeMetaData(attr);
				}
			}
		}

		return metaData;
	}

	private void getFeatures(Protocol protocol, List<ObservableFeature> features)
	{
		// store features
		features.addAll(protocol.getFeatures());

		for (Protocol subProtocol : protocol.getSubprotocols())
		{
			getFeatures(subProtocol, features);
		}
	}
}

package org.molgenis.omx.biobankconnect.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.Countable;
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
import org.molgenis.omx.observ.Category;
import org.molgenis.omx.observ.Characteristic;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.ObservationSet;
import org.molgenis.omx.observ.ObservedValue;
import org.molgenis.omx.observ.Protocol;
import org.molgenis.omx.observ.value.XrefValue;

public class StoreMappingRepository extends AbstractRepository<Entity> implements Countable
{
	private DataService dataService;
	private static final String OBSERVATION_SET = "observation_set";
	private static final String STORE_MAPPING_CONFIRM_MAPPING = "store_mapping_confirm_mapping";
	private static final String STORE_MAPPING_SCORE = "store_mapping_score";
	private static final String STORE_MAPPING_ABSOLUTE_SCORE = "store_mapping_absolute_score";
	private static final List<String> NON_XREF_FIELDS = Arrays.asList(STORE_MAPPING_ABSOLUTE_SCORE,
			STORE_MAPPING_SCORE, STORE_MAPPING_CONFIRM_MAPPING);
	private final List<ObservationSet> observationSets;
	private final ValueConverter valueConverter;
	private Integer numberOfRows = null;
	private final DataSet dataSet;
	private DefaultEntityMetaData metaData = null;

	public StoreMappingRepository(String dataSetIdentifier, DataService dataService)
	{
		this.dataSet = dataService.findOne(DataSet.ENTITY_NAME,
				new QueryImpl().eq(DataSet.IDENTIFIER, dataSetIdentifier));

		this.observationSets = dataService.findAllAsList(ObservationSet.ENTITY_NAME,
				new QueryImpl().eq(ObservationSet.PARTOFDATASET, dataSet));

		this.valueConverter = new ValueConverter(dataService);
		this.dataService = dataService;
	}

	public StoreMappingRepository(String dataSetIdentifier, List<ObservationSet> observationSets,
			DataService dataService)
	{
		this.dataSet = dataService.findOne(DataSet.ENTITY_NAME,
				new QueryImpl().eq(DataSet.IDENTIFIER, dataSetIdentifier));

		this.observationSets = observationSets;
		this.valueConverter = new ValueConverter(dataService);
	}

	@Override
	public long count()
	{
		if (numberOfRows == null)
		{
			numberOfRows = observationSets.size();
		}

		return numberOfRows;
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

		if (!observationSets.isEmpty())
		{
			try
			{
				Map<Integer, Entity> storeMapping = new HashMap<Integer, Entity>();

				Iterable<ObservedValue> values = dataService.findAll(ObservedValue.ENTITY_NAME,
						new QueryImpl().in(ObservedValue.OBSERVATIONSET, observationSets));

				for (ObservedValue ov : values)
				{
					Entity entity = null;
					Integer observationId = ov.getObservationSet().getId();
					if (storeMapping.containsKey(observationId)) entity = storeMapping.get(observationId);
					else entity = new MapEntity();
					if (NON_XREF_FIELDS.contains(ov.getFeature().getIdentifier()))
					{
						entity.set(ov.getFeature().getIdentifier(), valueConverter.toCell(ov.getValue()));
					}
					else
					{
						Characteristic xrefCharacteristic = ((XrefValue) ov.getValue()).getValue();
						entity.set(ov.getFeature().getIdentifier(), xrefCharacteristic.getId());
					}
					if (entity.get(OBSERVATION_SET) == null) entity.set(OBSERVATION_SET, observationId);
					storeMapping.put(observationId, entity);
				}

				for (Entity entity : storeMapping.values())
					entities.add(entity);
			}
			catch (ValueConverterException e)
			{
				new RuntimeException("Failed to index mapping table : " + dataSet.getName() + " error : "
						+ e.getMessage());
			}
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
						attr.setRefEntityName(Characteristic.ENTITY_NAME);
					}
					else if (fieldType.equals(FieldTypeEnum.CATEGORICAL))
					{
						attr.setRefEntityName(Category.ENTITY_NAME);
					}

					metaData.addAttributeMetaData(attr);
				}

				metaData.addAttributeMetaData(new DefaultAttributeMetaData(OBSERVATION_SET, FieldTypeEnum.STRING));
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

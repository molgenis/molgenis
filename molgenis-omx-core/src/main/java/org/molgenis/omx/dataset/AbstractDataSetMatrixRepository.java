package org.molgenis.omx.dataset;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.support.AbstractRepository;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.omx.observ.Category;
import org.molgenis.omx.observ.Characteristic;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.Protocol;

/**
 * Base class for DataSetMatrixRepository and OmxRepository
 */
public abstract class AbstractDataSetMatrixRepository extends AbstractRepository<Entity>
{
	protected final String dataSetIdentifier;
	protected final DataService dataService;
	private DefaultEntityMetaData metaData = null;

	public AbstractDataSetMatrixRepository(DataService dataService, String dataSetIdentifier)
	{
		this.dataService = dataService;
		this.dataSetIdentifier = dataSetIdentifier;
	}

	@Override
	public Class<? extends Entity> getEntityClass()
	{
		return MapEntity.class;
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
			DataSet dataSet = getDataSet();
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
					attr.setLabelAttribute(false);// TODO??

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

				// Add id attribute (is id of ObservationSet)
				DefaultAttributeMetaData attr = new DefaultAttributeMetaData("id", FieldTypeEnum.INT);
				attr.setDescription("id");
				attr.setLabel("id");
				attr.setIdAttribute(true);
				attr.setLabelAttribute(false);
				metaData.addAttributeMetaData(attr);
			}
		}

		return metaData;
	}

	protected Set<String> getAttributeNames()
	{
		Set<String> attributeNames = new HashSet<String>();

		for (AttributeMetaData attr : getEntityMetaData().getAttributes())
		{
			attributeNames.add(attr.getName());
		}

		return attributeNames;
	}

	protected DataSet getDataSet()
	{
		DataSet dataSet = dataService.findOne(DataSet.ENTITY_NAME,
				new QueryImpl().eq(DataSet.IDENTIFIER, dataSetIdentifier));

		if (dataSet == null)
		{
			throw new UnknownEntityException("DataSet [" + dataSetIdentifier + "] not found");
		}

		return dataSet;
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

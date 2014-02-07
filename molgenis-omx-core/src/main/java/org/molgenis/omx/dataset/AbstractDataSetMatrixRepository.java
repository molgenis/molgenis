package org.molgenis.omx.dataset;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
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
import org.molgenis.omx.observ.Characteristic;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.Protocol;

/**
 * Base class for DataSetMatrixRepository and OmxRepository
 */
public abstract class AbstractDataSetMatrixRepository extends AbstractRepository
{
	private static final Logger logger = Logger.getLogger(AbstractDataSetMatrixRepository.class);
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
		final Characteristic characteristic = getCharacteristic();
		final String identifier = characteristic.getIdentifier();
		final String labelValue = characteristic.getLabelValue();
		final Protocol protocol = getProtocol(characteristic);
		this.metaData = new DefaultEntityMetaData(identifier);
		this.metaData.setLabel(labelValue);

		if (protocol != null)
		{
			final List<ObservableFeature> features = new ArrayList<ObservableFeature>();
			this.getFeatures(protocol, features);
			final List<AttributeMetaData> attributeMetaDataList = new ArrayList<AttributeMetaData>();
			this.populateListWithFeatures(attributeMetaDataList, features);
			this.populateListWithId(attributeMetaDataList);
			this.metaData.addAllAttributeMetaData(attributeMetaDataList);
		}

		return metaData;
	}
	
	@Override
	public Iterable<AttributeMetaData> getLevelOneAttributes() {
		final Characteristic characteristic = getCharacteristic();
		final Protocol protocol = getProtocol(characteristic);
		final List<AttributeMetaData> attributeMetaDataList = new ArrayList<AttributeMetaData>();

		if (protocol != null)
		{
			this.populateListWithFeatures(attributeMetaDataList, protocol.getFeatures());
			this.populateListWithProtocols(attributeMetaDataList, protocol.getSubprotocols());
			this.populateListWithId(attributeMetaDataList);
		}

		return attributeMetaDataList;
	}
	
	/**
	 * TODO
	 * 
	 * @param characteristic
	 * @return
	 */
	protected Protocol getProtocol(Characteristic characteristic) 
	{
		final Protocol protocol;
		
		if(characteristic instanceof DataSet) 
		{
			protocol = ((DataSet) characteristic).getProtocolUsed();
		}
		else if(characteristic instanceof Protocol)
		{
			protocol = (Protocol) characteristic;
		} 
		else 
		{
			protocol = null;
		}
		
		return protocol;
	}
	
	/**
	 * TODO
	 * 
	 * @param defaultAttributeMetaDataList
	 * @param features
	 */
	protected void populateListWithFeatures(List<AttributeMetaData> defaultAttributeMetaDataList,
			List<ObservableFeature> features)
	{
		for (ObservableFeature feature : features)
		{
			FieldTypeEnum fieldType = MolgenisFieldTypes.getType(feature.getDataType()).getEnumType();
			
			if (fieldType.equals(FieldTypeEnum.XREF) || fieldType.equals(FieldTypeEnum.MREF))
			{
				fieldType = FieldTypeEnum.STRING;
			}
			
			DefaultAttributeMetaData attr = new DefaultAttributeMetaData(feature.getIdentifier(), fieldType);
			attr.setDescription(feature.getDescription());
			attr.setLabel(feature.getName());
			attr.setIdAttribute(false);
			attr.setLabelAttribute(false);
			defaultAttributeMetaDataList.add(attr);
		}
	}

	/**
	 * TODO
	 * 
	 * @param defaultAttributeMetaDataList
	 * @param protocols
	 */
	protected void populateListWithProtocols(List<AttributeMetaData> defaultAttributeMetaDataList,
			List<Protocol> protocols)
	{
		for (Protocol subProtocol : protocols)
		{
			DefaultEntityMetaData meta = new DefaultEntityMetaData(subProtocol.getIdentifier());
			//DefaultEntityMetaData meta = new DefaultEntityMetaData(subProtocol.getName());
			meta.setLabel(subProtocol.getLabelValue());
			DefaultAttributeMetaData attr = new DefaultAttributeMetaData(subProtocol.getName(), FieldTypeEnum.HAS);
			attr.setRefEntity(meta);
			defaultAttributeMetaDataList.add(attr);
		}
	}

	/**
	 * TODO
	 * 
	 * @param defaultAttributeMetaDataList
	 */
	protected void populateListWithId(List<AttributeMetaData> defaultAttributeMetaDataList)
	{
		// Add id attribute (is id of ObservationSet)
		DefaultAttributeMetaData attr = new DefaultAttributeMetaData("id", FieldTypeEnum.INT);
		attr.setDescription("id");
		
		attr.setLabel("id");
		attr.setIdAttribute(true);
		attr.setLabelAttribute(false);
		attr.setVisible(false);
		defaultAttributeMetaDataList.add(attr);
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

	protected Characteristic getCharacteristic()
	{
		Characteristic characteristic;
		
		characteristic = dataService.findOne(DataSet.ENTITY_NAME,
				new QueryImpl().eq(DataSet.IDENTIFIER, dataSetIdentifier), DataSet.class);
		
		if (characteristic == null) 
		{
			characteristic = dataService.findOne(Protocol.ENTITY_NAME,
					new QueryImpl().eq(Protocol.IDENTIFIER, dataSetIdentifier), Protocol.class);
		}

		if (characteristic == null)
		{
			throw new UnknownEntityException("Entity [" + dataSetIdentifier + "] not found");
		}

		return characteristic;
	}
	
	protected DataSet getDataSet()
	{
		DataSet dataSet = dataService.findOne(DataSet.ENTITY_NAME,
				new QueryImpl().eq(DataSet.IDENTIFIER, dataSetIdentifier), DataSet.class);

		if (dataSet == null)
		{
			throw new UnknownEntityException("DataSet [" + dataSetIdentifier + "] not found");
		}

		return dataSet;
	}

	/**
	 * Recursive method; 
	 * Gets all features from protocol including subprotocols
	 * 
	 * @param protocol
	 * @param features
	 */
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

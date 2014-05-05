package org.molgenis.omx.dataset;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.support.AbstractEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.omx.protocol.ProtocolEntityMetaData;

public class DataSetEntityMetaData extends AbstractEntityMetaData
{
	private final DataSet dataSet;
	private transient Iterable<AttributeMetaData> cachedAttributes;
	private transient Iterable<AttributeMetaData> cachedAtomicAttributes;

	public DataSetEntityMetaData(DataSet dataSet)
	{
		if (dataSet == null) throw new IllegalArgumentException("DataSet is null");
		this.dataSet = dataSet;
	}

	@Override
	public String getName()
	{
		return dataSet.getIdentifier(); // yes, getIdentifier and not getName
	}

	@Override
	public String getLabel()
	{
		return dataSet.getName(); // yes, getName
	}

	@Override
	public String getDescription()
	{
		return dataSet.getDescription();
	}

	@Override
	public Iterable<AttributeMetaData> getAttributes()
	{
		if (cachedAttributes == null)
		{
			cachedAttributes = new ProtocolEntityMetaData(dataSet.getProtocolUsed()).getAttributes();
		}
		return cachedAttributes;
	}

	@Override
	public Iterable<AttributeMetaData> getAtomicAttributes()
	{
		if (cachedAtomicAttributes == null)
		{
			cachedAtomicAttributes = new ProtocolEntityMetaData(dataSet.getProtocolUsed()).getAtomicAttributes();
		}
		return cachedAtomicAttributes;
	}

	@Override
	public Class<? extends Entity> getEntityClass()
	{
		return MapEntity.class;
	}
}

package org.molgenis.omx.dataset;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.support.AbstractRepository;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.omx.observ.DataSet;

/**
 * Base class for DataSetMatrixRepository and OmxRepository
 */
public abstract class AbstractDataSetMatrixRepository extends AbstractRepository
{
	protected final String dataSetIdentifier;
	protected final DataService dataService;

	public AbstractDataSetMatrixRepository(String url, DataService dataService, String dataSetIdentifier)
	{
		super(url);
		if (dataService == null) throw new IllegalArgumentException("dataService is null");
		if (dataSetIdentifier == null) throw new IllegalArgumentException("dataSetIdentifier is null");
		this.dataService = dataService;
		this.dataSetIdentifier = dataSetIdentifier;
	}

	@Override
	public void close() throws IOException
	{
		// Nothing
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		return new DataSetEntityMetaData(getDataSet());
	}

	protected Set<String> getAttributeNames()
	{
		Set<String> attributeNames = new HashSet<String>();

		for (AttributeMetaData attr : getEntityMetaData().getAtomicAttributes())
		{
			attributeNames.add(attr.getName());
		}

		return attributeNames;
	}

	protected DataSet getDataSet()
	{
		DataSet dataSet = dataService.findOne(DataSet.ENTITY_NAME,
				new QueryImpl().eq(DataSet.IDENTIFIER, dataSetIdentifier), DataSet.class);

		if (dataSet == null)
		{
			throw new UnknownEntityException("DataSet with dataset identifier [" + dataSetIdentifier + "] is not found");
		}

		return dataSet;
	}
}

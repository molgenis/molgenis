package org.molgenis.data.omx;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Query;
import org.molgenis.data.Queryable;
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
import org.molgenis.omx.observ.ObservationSet;
import org.molgenis.omx.observ.Protocol;
import org.molgenis.search.SearchService;

/**
 * Repository around an omx DataSet matrix.
 * 
 * Uses the DataService to get the metadata and the SearchService to get the actual data itself
 */
public class OmxRepository extends AbstractRepository<Entity> implements Queryable<Entity>
{
	private final DataService dataService;
	private final String dataSetIdentifier;
	private DefaultEntityMetaData metaData = null;
	private final SearchService searchService;

	public OmxRepository(DataService dataService, SearchService searchService, String dataSetIdentifier)
	{
		this.dataService = dataService;
		this.searchService = searchService;
		this.dataSetIdentifier = dataSetIdentifier;
	}

	@Override
	public Class<? extends Entity> getEntityClass()
	{
		return MapEntity.class;
	}

	@Override
	public Iterator<Entity> iterator()
	{
		return new OmxRepositoryIterator(dataSetIdentifier, searchService, new QueryImpl(), getAttributeNames());
	}

	@Override
	public long count()
	{
		return count(new QueryImpl());
	}

	@Override
	public long count(Query q)
	{
		return searchService.count(dataSetIdentifier, q);
	}

	@Override
	public Iterable<Entity> findAll(final Query q)
	{
		return new Iterable<Entity>()
		{
			@Override
			public Iterator<Entity> iterator()
			{
				return new OmxRepositoryIterator(dataSetIdentifier, searchService, q, getAttributeNames());
			}
		};
	}

	@Override
	public Entity findOne(Query q)
	{
		q.pageSize(1);
		Iterator<Entity> it = findAll(q).iterator();
		if (!it.hasNext())
		{
			return null;
		}

		return it.next();
	}

	@Override
	public Entity findOne(Integer id)
	{
		Query q = new QueryImpl().eq(ObservationSet.ID, id);
		return findOne(q);
	}

	@Override
	public Iterable<Entity> findAll(Iterable<Integer> ids)
	{
		Query q = new QueryImpl().in(ObservationSet.ID, ids);
		return findAll(q);
	}

	@Override
	public void close() throws IOException
	{
		// Nothing
	}

	@Override
	protected EntityMetaData getEntityMetaData()
	{
		if (metaData == null)
		{
			DataSet dataSet = getDataSet();
			metaData = new DefaultEntityMetaData(dataSet.getName());

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

	private DataSet getDataSet()
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

	private Set<String> getAttributeNames()
	{
		Set<String> attributeNames = new HashSet<String>();

		for (AttributeMetaData attr : this.getEntityMetaData().getAttributes())
		{
			attributeNames.add(attr.getName());
		}

		return attributeNames;
	}
}

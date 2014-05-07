package org.molgenis.elasticsearch.index;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.base.Joiner;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.elasticsearch.util.MapperTypeSanitizer;
import org.molgenis.util.Cell;
import org.molgenis.util.RepositoryUtils;

/**
 * Creates an IndexRequest for indexing entities with ElasticSearch
 * 
 * @author erwin
 * 
 */
public class IndexRequestGenerator
{
	private static final Logger LOG = Logger.getLogger(IndexRequestGenerator.class);
	private final Client client;
	private final String indexName;

	public IndexRequestGenerator(Client client, String indexName)
	{
		if (client == null)
		{
			throw new IllegalArgumentException("Client is null");
		}

		if (indexName == null)
		{
			throw new IllegalArgumentException("IndexName is null");
		}

		this.client = client;
		this.indexName = indexName;
	}

	public Iterable<BulkRequestBuilder> buildIndexRequest(final Repository repository)
	{
		return new Iterable<BulkRequestBuilder>()
		{
			@Override
			public Iterator<BulkRequestBuilder> iterator()
			{
				return indexRequestIterator(repository);
			}
		};
	}

	private Iterator<BulkRequestBuilder> indexRequestIterator(final Repository repository)
	{
		final Set<String> xrefAndMrefColumns = new HashSet<String>();
		for (AttributeMetaData attr : repository.getEntityMetaData().getAtomicAttributes())
		{
			FieldTypeEnum fieldType = attr.getDataType().getEnumType();
			boolean isXrefOrMref = fieldType.equals(FieldTypeEnum.XREF) || fieldType.equals(FieldTypeEnum.MREF);
			if (isXrefOrMref) xrefAndMrefColumns.add(attr.getName());
		}

		return new Iterator<BulkRequestBuilder>()
		{
			private final long rows = RepositoryUtils.count(repository);
			private static final int docsPerBulk = 1000;
			private final Iterator<? extends Entity> it = repository.iterator();
			private int row = 0;

			@Override
			public boolean hasNext()
			{
				return it.hasNext();
			}

			@SuppressWarnings("unchecked")
			@Override
			public BulkRequestBuilder next()
			{
				BulkRequestBuilder bulkRequest = client.prepareBulk();

				final long maxRow = Math.min(row + docsPerBulk, rows);

				for (; row < maxRow; ++row)
				{
					Entity entity = it.next();
					Map<String, Object> doc = new HashMap<String, Object>();

					for (String attrName : entity.getAttributeNames())
					{
						// Serialize collections to be able to sort on them, elasticsearch does not support sorting on
						// list fields
						Object id = null;
						Object key = null;
						Object value = entity.get(attrName);
						if (value instanceof Cell)
						{
							Cell<?> cell = (Cell<?>) value;
							id = cell.getId();
							key = cell.getKey();
							value = cell.getValue();
						}
						if (value instanceof Collection)
						{
							Collection<?> values = (Collection<?>) value;
							if (!values.isEmpty() && values.iterator().next() instanceof Cell)
							{
								List<Integer> mrefIds = null;
								List<String> mrefKeys = null;
								for (Iterator<Cell<?>> it = ((Collection<Cell<?>>) values).iterator(); it.hasNext();)
								{
									Cell<?> cell = it.next();
									Integer cellId = cell.getId();
									if (cellId != null)
									{
										if (mrefIds == null) mrefIds = new ArrayList<Integer>();
										mrefIds.add(cellId);
									}
									String cellKey = cell.getKey();
									if (cellKey != null)
									{
										if (mrefKeys == null) mrefKeys = new ArrayList<String>();
										mrefKeys.add(cellKey);
									}
								}
								if (mrefIds != null) id = mrefIds;
								if (mrefKeys != null) key = mrefKeys;
							}
							value = Joiner.on(",").join((Collection<?>) value);
						}
						if (id != null) doc.put("id-" + attrName, id);
						if (key != null) doc.put("key-" + attrName, key);
						doc.put(attrName, value);
					}

					Set<String> xrefAndMrefValues = new HashSet<String>();
					for (String attrName : entity.getAttributeNames())
					{
						if (xrefAndMrefColumns.contains(attrName))
						{
							Object value = entity.get(attrName);
							if (value instanceof Cell)
							{
								Cell<?> cell = (Cell<?>) value;
								if (cell.getValue() instanceof Collection<?>)
								{
									for (Cell<?> mrefCell : (Collection<Cell<?>>) cell.getValue())
									{
										xrefAndMrefValues.add(mrefCell.getKey());
									}
								}
								else
								{
									xrefAndMrefValues.add(cell.getKey());
								}
							}
						}
					}
					doc.put("_xrefvalue", xrefAndMrefValues);
					IndexRequestBuilder request = client.prepareIndex(indexName,
							MapperTypeSanitizer.sanitizeMapperType(repository.getName()));

					request.setSource(doc);
					bulkRequest.add(request);
					if ((row + 1) % 100 == 0) LOG.info("Added [" + (row + 1) + "] documents");
				}
				LOG.info("Added [" + row + "] documents");

				return bulkRequest;
			}

			@Override
			public void remove()
			{
				throw new UnsupportedOperationException();
			}
		};
	}

}

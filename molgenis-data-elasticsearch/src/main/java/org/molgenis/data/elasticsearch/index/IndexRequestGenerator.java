package org.molgenis.data.elasticsearch.index;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.client.Client;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Repository;
import org.molgenis.data.elasticsearch.util.MapperTypeSanitizer;
import org.molgenis.util.RepositoryUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates an IndexRequest for indexing entities with ElasticSearch
 * 
 * @author erwin
 * 
 */
public class IndexRequestGenerator
{
	private static final Logger LOG = LoggerFactory.getLogger(IndexRequestGenerator.class);

	private final Client client;
	private final String indexName;
	private final EntityToSourceConverter entityToSourceConverter;

	public IndexRequestGenerator(Client client, String indexName, EntityToSourceConverter entityToSourceConverter)
	{
		if (client == null) throw new IllegalArgumentException("Client is null");
		if (indexName == null) throw new IllegalArgumentException("IndexName is null");
		if (entityToSourceConverter == null) throw new IllegalArgumentException("EntityToSourceConverter is null");
		this.client = client;
		this.indexName = indexName;
		this.entityToSourceConverter = entityToSourceConverter;
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
			private final EntityMetaData entityMetaData = repository.getEntityMetaData();
			private int row = 0;

			@Override
			public boolean hasNext()
			{
				return it.hasNext();
			}

			@Override
			public BulkRequestBuilder next()
			{
				BulkRequestBuilder bulkRequest = client.prepareBulk();

				final long maxRow = Math.min(row + docsPerBulk, rows);

				for (; row < maxRow; ++row)
				{
					Entity entity = it.next();
					Map<String, Object> doc = entityToSourceConverter.convert(entity, entityMetaData);
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

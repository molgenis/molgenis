package org.molgenis.ontology.index;

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;
import static org.molgenis.data.elasticsearch.util.MapperTypeSanitizer.sanitizeMapperType;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.admin.indices.exists.types.TypesExistsRequest;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.ImmutableSettings.Builder;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.node.Node;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.elasticsearch.index.MappingsBuilder;
import org.molgenis.data.elasticsearch.util.Hit;
import org.molgenis.data.elasticsearch.util.MapperTypeSanitizer;
import org.molgenis.data.elasticsearch.util.SearchRequest;
import org.molgenis.data.elasticsearch.util.SearchResult;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.ontology.Ontology;
import org.molgenis.ontology.OntologyService;
import org.molgenis.ontology.repository.OntologyIndexRepository;
import org.molgenis.ontology.repository.OntologyQueryRepository;
import org.molgenis.ontology.repository.OntologyTermIndexRepository;
import org.molgenis.ontology.repository.OntologyTermQueryRepository;
import org.molgenis.ontology.utils.OntologyLoader;
import org.molgenis.security.runas.RunAsSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;

public class AsyncOntologyIndexer implements OntologyIndexer
{
	@Autowired
	private MolgenisSettings molgenisSettings;
	private final DataService dataService;
	private final SearchService searchService;
	private final OntologyService ontologyService;
	private String indexingOntologyIri = null;
	private boolean isCorrectOntology = true;
	private static int BATCH_SIZE = 10000;
	private static final String SYNONYM_FIELDS = "plugin.ontology.synonym.field";
	private static final Logger logger = Logger.getLogger(AsyncOntologyIndexer.class);

	private final AtomicInteger runningIndexProcesses = new AtomicInteger();

	@Autowired
	public AsyncOntologyIndexer(SearchService searchService, DataService dataService, OntologyService ontologyService)
	{
		if (searchService == null) throw new IllegalArgumentException("SearchService is null!");
		if (dataService == null) throw new IllegalArgumentException("DataService is null!");
		if (ontologyService == null) throw new IllegalArgumentException("OntologyService is null!");
		this.searchService = searchService;
		this.dataService = dataService;
		this.ontologyService = ontologyService;
	}

	public boolean isIndexingRunning()
	{
		return (runningIndexProcesses.get() > 0);
	}

	@Override
	@Async
	@RunAsSystem
	public void index(OntologyLoader ontologyLoader)
	{
		isCorrectOntology = true;
		runningIndexProcesses.incrementAndGet();

		try
		{
			String property = molgenisSettings.getProperty(SYNONYM_FIELDS);
			if (!StringUtils.isBlank(property))
			{
				ontologyLoader.addSynonymsProperties(new HashSet<String>(Arrays.asList(property.split(","))));
			}
			indexingOntologyIri = ontologyLoader.getOntologyIRI() == null ? StringUtils.EMPTY : ontologyLoader
					.getOntologyIRI();
			internalIndex(new OntologyIndexRepository(ontologyLoader, OntologyQueryRepository.DEFAULT_ONTOLOGY_REPO,
					searchService), null);
			OntologyTermIndexRepository ontologyTermIndexRepository = new OntologyTermIndexRepository(ontologyLoader,
					ontologyLoader.getOntologyName(), searchService);
			internalIndex(ontologyTermIndexRepository, ontologyTermIndexRepository.getDynamaticFields());
		}
		catch (Exception e)
		{
			isCorrectOntology = false;
			logger.error("Exception imported file is not a valid ontology", e);
		}
		finally
		{
			String ontologyName = ontologyLoader.getOntologyName();
			if (!dataService.hasRepository(ontologyName))
			{
				dataService.addRepository(new OntologyTermQueryRepository(ontologyName, searchService, dataService,
						ontologyService));
			}
			runningIndexProcesses.decrementAndGet();
			indexingOntologyIri = null;
		}
	}

	/**
	 * Created a specific indexer to index list of primitive types (string),
	 * because the standard molgenis index does not handle List<String>
	 * 
	 * @param ontologyLoader
	 * @throws IOException
	 */
	private void internalIndex(Repository repository, Set<String> dynamaticFields) throws IOException
	{
		String indexName = "molgenis";
		Builder builder = ImmutableSettings.settingsBuilder().loadFromClasspath("elasticsearch.yml");
		Settings settings = builder.build();
		Node node = nodeBuilder().settings(settings).local(true).node();
		Client client = node.client();
		try
		{
			String documentType = MapperTypeSanitizer.sanitizeMapperType(repository.getName());
			if (!documentTypeExists(client, indexName, documentType)) createMappings(client, repository);
			long count = 0;
			long start = 0;
			long t0 = System.currentTimeMillis();
			BulkRequestBuilder bulkRequest = null;
			Iterator<Entity> iterator = repository.iterator();
			while (iterator.hasNext())
			{
				if (count % BATCH_SIZE == 0)
				{
					start = count;
					bulkRequest = client.prepareBulk();
				}

				Entity entity = iterator.next();
				Map<String, Object> docs = new HashMap<String, Object>();
				Iterable<String> attributeNames = entity.getAttributeNames();
				for (String attributeName : attributeNames)
				{
					docs.put(attributeName, entity.get(attributeName));
				}
				if (dynamaticFields != null)
				{
					for (String dynamaticField : dynamaticFields)
					{
						if (!docs.containsKey(dynamaticField))
						{
							docs.put(dynamaticField, StringUtils.EMPTY);
						}
					}
				}

				bulkRequest.add(client.prepareIndex(indexName, documentType).setSource(docs));
				count++;

				// Commit if BATCH_SIZE is reached
				if (count == (start + BATCH_SIZE))
				{
					BulkResponse bulkResponse = bulkRequest.execute().actionGet();
					if (bulkResponse.hasFailures())
					{
						throw new RuntimeException("error while indexing row [" + count + "]: " + bulkResponse);
					}

					long t = (System.currentTimeMillis() - t0) / 1000;
					logger.info("Imported [" + count + "] rows in [" + t + "] sec.");
				}
			}

			// Commit the rest
			BulkResponse bulkResponse = bulkRequest.execute().actionGet();
			if (bulkResponse.hasFailures())
			{
				throw new RuntimeException("error while indexing row [" + count + "]: " + bulkResponse);
			}

			long t = (System.currentTimeMillis() - t0) / 1000;
			logger.info("Import of ontology term from ontology [" + documentType + "] completed in " + t
					+ " sec. Added [" + count + "] rows.");
		}
		finally
		{
			if (repository != null) repository.close();
		}
	}

	private void createMappings(Client client, Repository repository) throws IOException
	{
		String documentType = MapperTypeSanitizer.sanitizeMapperType(repository.getName());
		XContentBuilder jsonBuilder = MappingsBuilder.buildMapping(repository);
		logger.info("Going to create mapping [" + jsonBuilder.string() + "]");

		PutMappingResponse response = client.admin().indices().preparePutMapping("molgenis").setType(documentType)
				.setSource(jsonBuilder).execute().actionGet();

		if (!response.isAcknowledged())
		{
			throw new ElasticsearchException("Creation of mapping for documentType [PalgaSample] failed. Response="
					+ response);
		}

		logger.info("Mapping for documentType [" + documentType + "] created");
	}

	@SuppressWarnings("deprecation")
	@Override
	@RunAsSystem
	public void removeOntology(String ontologyIri)
	{
		// TODO : Once the tagService is done, we also need to remove all the
		// tags
		Ontology ontology = ontologyService.getOntology(ontologyIri);
		if (ontology != null)
		{
			SearchResult searchResult = searchService.search(new SearchRequest(
					OntologyQueryRepository.DEFAULT_ONTOLOGY_REPO, new QueryImpl().eq(
							OntologyQueryRepository.ONTOLOGY_IRI, ontologyIri), null));

			for (Hit hit : searchResult.getSearchHits())
			{
				searchService.deleteById(hit.getId(),
						dataService.getEntityMetaData(OntologyQueryRepository.DEFAULT_ONTOLOGY_REPO));
			}
			searchService.deleteDocumentsByType(ontology.getLabel());
		}
	}

	public boolean documentTypeExists(Client client, String indexName, String documentType)
	{
		String documentTypeSantized = sanitizeMapperType(documentType);
		return client.admin().indices().typesExists(new TypesExistsRequest(new String[]
		{ indexName }, documentTypeSantized)).actionGet().isExists();
	}

	public String getOntologyUri()
	{
		return indexingOntologyIri;
	}

	public boolean isCorrectOntology()
	{
		return isCorrectOntology;
	}
}
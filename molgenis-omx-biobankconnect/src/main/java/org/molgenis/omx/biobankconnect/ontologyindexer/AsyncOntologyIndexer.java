package org.molgenis.omx.biobankconnect.ontologyindexer;

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

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
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.collect.Iterables;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.ImmutableSettings.Builder;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.node.Node;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.elasticsearch.index.MappingsBuilder;
import org.molgenis.elasticsearch.util.MapperTypeSanitizer;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.omx.biobankconnect.ontology.repository.OntologyIndexRepository;
import org.molgenis.omx.biobankconnect.ontology.repository.OntologyTermIndexRepository;
import org.molgenis.omx.biobankconnect.ontology.repository.OntologyTermQueryRepository;
import org.molgenis.omx.biobankconnect.utils.OntologyLoader;
import org.molgenis.omx.observ.target.Ontology;
import org.molgenis.omx.observ.target.OntologyTerm;
import org.molgenis.search.Hit;
import org.molgenis.search.SearchRequest;
import org.molgenis.search.SearchResult;
import org.molgenis.search.SearchService;
import org.molgenis.security.runas.RunAsSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;

public class AsyncOntologyIndexer implements OntologyIndexer
{
	@Autowired
	private MolgenisSettings molgenisSettings;
	private final DataService dataService;
	private final SearchService searchService;
	private String indexingOntologyIri = null;
	private boolean isCorrectOntology = true;
	private static int BATCH_SIZE = 10000;
	private static final String SYNONYM_FIELDS = "plugin.ontology.synonym.field";
	private static final Logger logger = Logger.getLogger(AsyncOntologyIndexer.class);

	private final AtomicInteger runningIndexProcesses = new AtomicInteger();

	@Autowired
	public AsyncOntologyIndexer(SearchService searchService, DataService dataService)
	{
		if (searchService == null) throw new IllegalArgumentException("SearchService is null!");
		if (dataService == null) throw new IllegalArgumentException("DataService is null!");
		this.searchService = searchService;
		this.dataService = dataService;
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
			searchService.indexRepository(new OntologyIndexRepository(ontologyLoader,
					createOntologyDocumentType(indexingOntologyIri), searchService));
			internalIndex(ontologyLoader);
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
				dataService.addRepository(new OntologyTermQueryRepository(ontologyName, indexingOntologyIri,
						searchService));
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
	private void internalIndex(OntologyLoader ontologyLoader) throws IOException
	{
		Builder builder = ImmutableSettings.settingsBuilder().loadFromClasspath("elasticsearch.yml");
		Settings settings = builder.build();
		Node node = nodeBuilder().settings(settings).local(true).node();
		Client client = node.client();
		OntologyTermIndexRepository ontologyTermIndexRepository = new OntologyTermIndexRepository(ontologyLoader,
				createOntologyTermDocumentType(indexingOntologyIri), searchService);
		try
		{
			String documentType = MapperTypeSanitizer.sanitizeMapperType(ontologyTermIndexRepository.getName());
			createMappings(client, ontologyTermIndexRepository);
			Iterator<Entity> iterator = ontologyTermIndexRepository.iterator();
			Set<String> dynamaticFields = ontologyTermIndexRepository.getDynamaticFields();
			long count = 0;
			long start = 0;
			long t0 = System.currentTimeMillis();
			BulkRequestBuilder bulkRequest = null;
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
				// Add dynamic fields to all the docs so that they have exactly
				// set
				// of attributes
				for (String dynamaticField : dynamaticFields)
				{
					if (!docs.containsKey(dynamaticField))
					{
						docs.put(dynamaticField, StringUtils.EMPTY);
					}
				}
				bulkRequest.add(client.prepareIndex("molgenis", documentType).setSource(docs));
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
			if (ontologyTermIndexRepository != null) ontologyTermIndexRepository.close();
		}
	}

	private void createMappings(Client client, OntologyTermIndexRepository ontologyTermIndexRepository)
			throws IOException
	{
		String documentType = MapperTypeSanitizer.sanitizeMapperType(ontologyTermIndexRepository.getName());
		XContentBuilder jsonBuilder = MappingsBuilder.buildMapping(ontologyTermIndexRepository);
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

	@Override
	@RunAsSystem
	public void removeOntology(String ontologyIri)
	{
		Iterable<Ontology> ontologies = dataService.findAll(Ontology.ENTITY_NAME,
				new QueryImpl().eq(Ontology.IDENTIFIER, ontologyIri), Ontology.class);

		if (Iterables.size(ontologies) > 0)
		{
			for (Ontology ontology : ontologies)
			{
				Iterable<OntologyTerm> ontologyTerms = dataService.findAll(OntologyTerm.ENTITY_NAME,
						new QueryImpl().eq(OntologyTerm.ONTOLOGY, ontology), OntologyTerm.class);

				if (Iterables.size(ontologyTerms) > 0) dataService.delete(OntologyTerm.ENTITY_NAME, ontologyTerms);
			}
			dataService.delete(Ontology.ENTITY_NAME, ontologies);
		}

		SearchRequest request = new SearchRequest(createOntologyDocumentType(ontologyIri), new QueryImpl().eq(
				OntologyIndexRepository.ONTOLOGY_IRI, ontologyIri), null);
		SearchResult result = searchService.search(request);
		if (result.getTotalHitCount() > 0)
		{
			Hit hit = result.getSearchHits().get(0);
			String ontologyEntityName = hit.getColumnValueMap().get(OntologyIndexRepository.ONTOLOGY_NAME).toString();
			if (dataService.hasRepository(ontologyEntityName))
			{
				dataService.removeRepository(ontologyEntityName);
			}
		}

		searchService.deleteDocumentsByType(createOntologyDocumentType(ontologyIri));
		searchService.deleteDocumentsByType(createOntologyTermDocumentType(ontologyIri));
	}

	public String getOntologyUri()
	{
		return indexingOntologyIri;
	}

	public boolean isCorrectOntology()
	{
		return isCorrectOntology;
	}

	public static String createOntologyDocumentType(String ontologyIri)
	{
		if (StringUtils.isEmpty(ontologyIri)) return null;
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("ontology-").append(ontologyIri);
		return stringBuilder.toString();
	}

	public static String createOntologyTermDocumentType(String ontologyIri)
	{
		if (StringUtils.isEmpty(ontologyIri)) return null;
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("ontologyTerm-").append(ontologyIri);
		return stringBuilder.toString();
	}
}
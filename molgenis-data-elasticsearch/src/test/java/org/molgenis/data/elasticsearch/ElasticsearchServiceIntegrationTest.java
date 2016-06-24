package org.molgenis.data.elasticsearch;

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;
import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_LABEL;
import static org.molgenis.data.transaction.MolgenisTransactionManager.TRANSACTION_ID_RESOURCE_NAME;
import static org.testng.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.ImmutableSettings.Builder;
import org.elasticsearch.node.Node;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityManager;
import org.molgenis.data.EntityManagerImpl;
import org.molgenis.data.elasticsearch.ElasticsearchService.IndexingMode;
import org.molgenis.data.elasticsearch.index.ElasticsearchIndexCreator;
import org.molgenis.data.elasticsearch.index.EntityToSourceConverter;
import org.molgenis.data.elasticsearch.index.SourceToEntityConverter;
import org.molgenis.data.mem.InMemoryRepository;
import org.molgenis.data.support.DataServiceImpl;
import org.molgenis.data.support.DefaultEntity;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.QueryImpl;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;
import com.google.common.io.Files;

public class ElasticsearchServiceIntegrationTest
{
	private static final String INDEX = "test";

	private static File ELASTICSEARCH_DIR;
	private static Client ELASTICSEARCH_CLIENT;
	private static Node NODE;

	private DataService dataService;
	private ElasticsearchService elasticsearchService;
	private DefaultEntityMetaData entityMeta = new DefaultEntityMetaData("entity");
	private String idAttrName = "id";
	private String labelAttrName = "label";

	@BeforeClass
	public static void setUpBeforeClass()
	{
		ELASTICSEARCH_DIR = Files.createTempDir();
		ELASTICSEARCH_DIR.deleteOnExit();

		Builder settingsBuilder = ImmutableSettings.settingsBuilder().put("path.data", ELASTICSEARCH_DIR)
				.put("path.logs", ELASTICSEARCH_DIR);
		NODE = nodeBuilder().settings(settingsBuilder).local(true).node();
		ELASTICSEARCH_CLIENT = NODE.client();

		ELASTICSEARCH_CLIENT.admin().cluster().prepareHealth().setWaitForGreenStatus().execute().actionGet();
	}

	@AfterClass
	public static void tearDownAfterClass() throws IOException, InterruptedException
	{
		try
		{
			ELASTICSEARCH_CLIENT.close();
		}
		finally
		{
			try
			{
				NODE.close();
			}
			finally
			{
				FileUtils.deleteDirectory(ELASTICSEARCH_DIR);
			}
		}
	}

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		new ElasticsearchIndexCreator(ELASTICSEARCH_CLIENT).createIndexIfNotExists(INDEX);

		entityMeta = new DefaultEntityMetaData("entity");
		entityMeta.setBackend(ElasticsearchRepositoryCollection.NAME);
		entityMeta.addAttribute(idAttrName, ROLE_ID);
		entityMeta.addAttribute(labelAttrName, ROLE_LABEL).setNillable(true);
		InMemoryRepository entityRepo = new InMemoryRepository(entityMeta);
		DataServiceImpl dataServiceImpl = new DataServiceImpl();
		dataServiceImpl.addRepository(entityRepo);

		EntityManager entityManager = new EntityManagerImpl(dataServiceImpl);
		this.dataService = dataServiceImpl;
		this.elasticsearchService = new ElasticsearchService(ELASTICSEARCH_CLIENT, INDEX, dataService,
				new ElasticsearchEntityFactory(entityManager, new SourceToEntityConverter(dataService, entityManager),
						new EntityToSourceConverter()));

		this.elasticsearchService.createMappings(entityMeta);
	}

	@AfterMethod
	public void tearDownBeforeMethod()
	{
		DeleteIndexResponse deleteIndexResponse = ELASTICSEARCH_CLIENT.admin().indices().prepareDelete("_all").get();
		if (!deleteIndexResponse.isAcknowledged())
		{
			throw new RuntimeException("Error deleting index");
		}
	}

	@Test
	public void indexAddAndGet()
	{
		Entity entity = new DefaultEntity(entityMeta, dataService);
		entity.set(idAttrName, "0");
		entity.set(labelAttrName, "label");
		elasticsearchService.index(entity, entityMeta, IndexingMode.ADD);
		elasticsearchService.refresh(entityMeta);
		Entity updatedEntity = elasticsearchService.get("0", entityMeta);
		assertEquals(updatedEntity, entity);
	}

	@Test
	public void indexAddAndGetStream()
	{
		Entity entity = new DefaultEntity(entityMeta, dataService);
		entity.set(idAttrName, "0");
		entity.set(labelAttrName, "label");
		elasticsearchService.index(Stream.of(entity), entityMeta, IndexingMode.ADD);
		elasticsearchService.refresh(entityMeta);
		Entity updatedEntity = elasticsearchService.get("0", entityMeta);
		assertEquals(updatedEntity, entity);
	}

	@Test
	public void indexAddDeleteAndGetStream()
	{
		Entity entity0 = new DefaultEntity(entityMeta, dataService);
		entity0.set(idAttrName, "0");
		entity0.set(labelAttrName, "label0");
		Entity entity1 = new DefaultEntity(entityMeta, dataService);
		entity1.set(idAttrName, "1");
		entity1.set(labelAttrName, "label1");

		elasticsearchService.index(Stream.of(entity0, entity1), entityMeta, IndexingMode.ADD);
		elasticsearchService.refresh(entityMeta);
		elasticsearchService.delete(Stream.of(entity0), entityMeta);
		elasticsearchService.refresh(entityMeta);
		Iterable<Entity> updatedEntity = elasticsearchService.search(new QueryImpl(), entityMeta);
		assertEquals(Lists.newArrayList(updatedEntity), Arrays.asList(entity1));
	}

	@Test
	public void transactionalCountAddAndTransactionAdd()
	{
		// entity in existing index
		Entity entity0 = new DefaultEntity(entityMeta, dataService);
		entity0.set(idAttrName, "0");
		entity0.set(labelAttrName, "label0");

		elasticsearchService.index(entity0, entityMeta, IndexingMode.ADD);
		elasticsearchService.refresh(entityMeta);
		assertEquals(elasticsearchService.count(entityMeta), 1l);

		String transactionId = "transaction0";
		TransactionSynchronizationManager.bindResource(TRANSACTION_ID_RESOURCE_NAME, transactionId);
		try
		{
			elasticsearchService.transactionStarted(transactionId);

			Entity entity1 = new DefaultEntity(entityMeta, dataService);
			entity1.set(idAttrName, "1");
			entity1.set(labelAttrName, "label1");

			elasticsearchService.index(entity1, entityMeta, IndexingMode.ADD);
			elasticsearchService.refresh(entityMeta);
			assertEquals(elasticsearchService.count(entityMeta), 2l);

			elasticsearchService.commitTransaction(transactionId);
		}
		finally
		{
			TransactionSynchronizationManager.unbindResource(TRANSACTION_ID_RESOURCE_NAME);
		}
		assertEquals(elasticsearchService.count(entityMeta), 2l);
	}

	@Test
	public void transactionalCountAddAndTransactionUpdate()
	{
		// entity in existing index
		Entity entity0 = new DefaultEntity(entityMeta, dataService);
		entity0.set(idAttrName, "0");
		entity0.set(labelAttrName, "label0");

		elasticsearchService.index(entity0, entityMeta, IndexingMode.ADD);
		elasticsearchService.refresh(entityMeta);
		assertEquals(elasticsearchService.count(entityMeta), 1l);

		String transactionId = "transaction0";
		TransactionSynchronizationManager.bindResource(TRANSACTION_ID_RESOURCE_NAME, transactionId);
		try
		{
			elasticsearchService.transactionStarted(transactionId);

			entity0.set(labelAttrName, "label0-update");

			elasticsearchService.index(entity0, entityMeta, IndexingMode.UPDATE);
			elasticsearchService.refresh(entityMeta);
			assertEquals(elasticsearchService.count(entityMeta), 1l);

			elasticsearchService.commitTransaction(transactionId);
		}
		finally
		{
			TransactionSynchronizationManager.unbindResource(TRANSACTION_ID_RESOURCE_NAME);
		}
		assertEquals(elasticsearchService.count(entityMeta), 1l);
	}

	@Test
	public void transactionalCountAddAndTransactionDelete()
	{
		// entity in existing index
		Entity entity0 = new DefaultEntity(entityMeta, dataService);
		entity0.set(idAttrName, "0");
		entity0.set(labelAttrName, "label0");

		elasticsearchService.index(entity0, entityMeta, IndexingMode.ADD);
		elasticsearchService.refresh(entityMeta);
		assertEquals(elasticsearchService.count(entityMeta), 1l);

		String transactionId = "transaction0";
		TransactionSynchronizationManager.bindResource(TRANSACTION_ID_RESOURCE_NAME, transactionId);
		try
		{
			elasticsearchService.transactionStarted(transactionId);

			entity0.set(labelAttrName, "label0-update");

			elasticsearchService.deleteById(entity0.getIdValue().toString(), entityMeta);
			elasticsearchService.refresh(entityMeta);
			assertEquals(elasticsearchService.count(entityMeta), 0l);

			elasticsearchService.commitTransaction(transactionId);
		}
		finally
		{
			TransactionSynchronizationManager.unbindResource(TRANSACTION_ID_RESOURCE_NAME);
		}
		assertEquals(elasticsearchService.count(entityMeta), 0l);
	}

	@Test
	public void transactionalCountAddAndTransactionDeleteAdd()
	{
		// entity in existing index
		Entity entity0 = new DefaultEntity(entityMeta, dataService);
		entity0.set(idAttrName, "0");
		entity0.set(labelAttrName, "label0");

		elasticsearchService.index(entity0, entityMeta, IndexingMode.ADD);
		elasticsearchService.refresh(entityMeta);
		assertEquals(elasticsearchService.count(entityMeta), 1l);

		String transactionId = "transaction0";
		TransactionSynchronizationManager.bindResource(TRANSACTION_ID_RESOURCE_NAME, transactionId);
		try
		{
			elasticsearchService.transactionStarted(transactionId);

			entity0.set(labelAttrName, "label0-update");

			elasticsearchService.deleteById(entity0.getIdValue().toString(), entityMeta);
			elasticsearchService.refresh(entityMeta);
			assertEquals(elasticsearchService.count(entityMeta), 0l);

			Entity entity1 = new DefaultEntity(entityMeta, dataService);
			entity1.set(idAttrName, "1");
			entity1.set(labelAttrName, "label1");

			elasticsearchService.index(entity1, entityMeta, IndexingMode.ADD);
			elasticsearchService.refresh(entityMeta);
			assertEquals(elasticsearchService.count(entityMeta), 1l);

			elasticsearchService.commitTransaction(transactionId);
		}
		finally
		{
			TransactionSynchronizationManager.unbindResource(TRANSACTION_ID_RESOURCE_NAME);
		}
		assertEquals(elasticsearchService.count(entityMeta), 1l);
	}

	@Test
	public void transactionalCountAddAndTransactionAddUpdate()
	{
		// entity in existing index
		Entity entity0 = new DefaultEntity(entityMeta, dataService);
		entity0.set(idAttrName, "0");
		entity0.set(labelAttrName, "label0");

		elasticsearchService.index(entity0, entityMeta, IndexingMode.ADD);
		elasticsearchService.refresh(entityMeta);
		assertEquals(elasticsearchService.count(entityMeta), 1l);

		String transactionId = "transaction0";
		TransactionSynchronizationManager.bindResource(TRANSACTION_ID_RESOURCE_NAME, transactionId);
		try
		{
			elasticsearchService.transactionStarted(transactionId);

			Entity entity1 = new DefaultEntity(entityMeta, dataService);
			entity1.set(idAttrName, "1");
			entity1.set(labelAttrName, "label1");

			elasticsearchService.index(entity1, entityMeta, IndexingMode.ADD);
			elasticsearchService.refresh(entityMeta);
			assertEquals(elasticsearchService.count(entityMeta), 2l);

			entity0.set(labelAttrName, "label0-update");

			elasticsearchService.index(entity1, entityMeta, IndexingMode.UPDATE);
			elasticsearchService.refresh(entityMeta);
			assertEquals(elasticsearchService.count(entityMeta), 2l);

			elasticsearchService.commitTransaction(transactionId);
		}
		finally
		{
			TransactionSynchronizationManager.unbindResource(TRANSACTION_ID_RESOURCE_NAME);
		}
		assertEquals(elasticsearchService.count(entityMeta), 2l);
	}

	@Test
	public void transactionalCountAddAndTransactionAddDelete()
	{
		// entity in existing index
		Entity entity0 = new DefaultEntity(entityMeta, dataService);
		entity0.set(idAttrName, "0");
		entity0.set(labelAttrName, "label0");

		elasticsearchService.index(entity0, entityMeta, IndexingMode.ADD);
		elasticsearchService.refresh(entityMeta);
		assertEquals(elasticsearchService.count(entityMeta), 1l);

		String transactionId = "transaction0";
		TransactionSynchronizationManager.bindResource(TRANSACTION_ID_RESOURCE_NAME, transactionId);
		try
		{
			elasticsearchService.transactionStarted(transactionId);

			Entity entity1 = new DefaultEntity(entityMeta, dataService);
			entity1.set(idAttrName, "1");
			entity1.set(labelAttrName, "label1");

			elasticsearchService.index(entity1, entityMeta, IndexingMode.ADD);
			elasticsearchService.refresh(entityMeta);
			assertEquals(elasticsearchService.count(entityMeta), 2l);

			elasticsearchService.deleteById(entity1.getIdValue().toString(), entityMeta);
			elasticsearchService.refresh(entityMeta);
			assertEquals(elasticsearchService.count(entityMeta), 1l);

			elasticsearchService.commitTransaction(transactionId);
		}
		finally
		{
			TransactionSynchronizationManager.unbindResource(TRANSACTION_ID_RESOURCE_NAME);
		}
		assertEquals(elasticsearchService.count(entityMeta), 1l);
	}

	@Test
	public void transactionalCountAddAndTransactionDeleteAddUpdate()
	{
		// entity in existing index
		Entity entity0 = new DefaultEntity(entityMeta, dataService);
		entity0.set(idAttrName, "0");
		entity0.set(labelAttrName, "label0");

		elasticsearchService.index(entity0, entityMeta, IndexingMode.ADD);
		elasticsearchService.refresh(entityMeta);
		assertEquals(elasticsearchService.count(entityMeta), 1l);

		String transactionId = "transaction0";
		TransactionSynchronizationManager.bindResource(TRANSACTION_ID_RESOURCE_NAME, transactionId);
		try
		{
			elasticsearchService.transactionStarted(transactionId);

			entity0.set(labelAttrName, "label0-update");

			elasticsearchService.deleteById(entity0.getIdValue().toString(), entityMeta);
			elasticsearchService.refresh(entityMeta);
			assertEquals(elasticsearchService.count(entityMeta), 0l);

			Entity entity1 = new DefaultEntity(entityMeta, dataService);
			entity1.set(idAttrName, "1");
			entity1.set(labelAttrName, "label1");

			elasticsearchService.index(entity1, entityMeta, IndexingMode.ADD);
			elasticsearchService.refresh(entityMeta);
			assertEquals(elasticsearchService.count(entityMeta), 1l);

			entity0.set(labelAttrName, "label0-update");

			elasticsearchService.index(entity1, entityMeta, IndexingMode.UPDATE);
			elasticsearchService.refresh(entityMeta);
			assertEquals(elasticsearchService.count(entityMeta), 1l);

			elasticsearchService.commitTransaction(transactionId);
		}
		finally
		{
			TransactionSynchronizationManager.unbindResource(TRANSACTION_ID_RESOURCE_NAME);
		}
		assertEquals(elasticsearchService.count(entityMeta), 1l);
	}
}

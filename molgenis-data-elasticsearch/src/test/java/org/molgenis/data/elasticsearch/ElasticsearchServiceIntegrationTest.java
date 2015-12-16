package org.molgenis.data.elasticsearch;

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;
import static org.molgenis.data.transaction.MolgenisTransactionManager.TRANSACTION_ID_RESOURCE_NAME;
import static org.testng.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

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
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

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
		catch (Exception e)
		{
			System.err.println("Error closing client");
		}

		try
		{
			NODE.close();
		}
		catch (Exception e)
		{
			System.err.println("Error closing node");
		}

		// resources might not be released, try to delete target directory for max 30s
		boolean ok;
		for (int i = 0; i < 10; ++i)
		{
			ok = ELASTICSEARCH_DIR.delete();
			if (ok)
			{
				break;
			}
			else
			{
				System.err.println("Unable to delete Elasticsearch index, retrying in 1s (attempt " + i + "/10)");
				Thread.sleep(1000);
			}
		}
	}

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		new ElasticsearchIndexCreator(ELASTICSEARCH_CLIENT).createIndexIfNotExists(INDEX);

		entityMeta = new DefaultEntityMetaData("entity");
		entityMeta.setBackend(ElasticsearchRepositoryCollection.NAME);
		entityMeta.addAttribute(idAttrName).setIdAttribute(true).setNillable(false).setUnique(true);
		entityMeta.addAttribute(labelAttrName).setLabelAttribute(true).setNillable(true);

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
	public void transactionalCountAddAndAdd()
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
	public void transactionalCountAddAndUpdate()
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
	public void transactionalCountAddAndDelete()
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
	public void transactionalCountAddAndDeleteAdd()
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
	public void transactionalCountAddAndAddUpdate()
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
	public void transactionalCountAddAndAddDelete()
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
	public void transactionalCountAddAndDeleteAddUpdate()
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

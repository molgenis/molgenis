package org.molgenis.data.elasticsearch;

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;
import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_LABEL;
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
		elasticsearchService.refresh();
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
		elasticsearchService.refresh();
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
		elasticsearchService.refresh();
		elasticsearchService.delete(Stream.of(entity0), entityMeta);
		elasticsearchService.refresh();
		Iterable<Entity> updatedEntity = elasticsearchService.search(new QueryImpl(), entityMeta);
		assertEquals(Lists.newArrayList(updatedEntity), Arrays.asList(entity1));
	}
}

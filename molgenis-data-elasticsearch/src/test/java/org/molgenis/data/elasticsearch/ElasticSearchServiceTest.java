package org.molgenis.data.elasticsearch;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.elasticsearch.action.ListenableActionFuture;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.CrudRepository;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Query;
import org.molgenis.data.elasticsearch.ElasticSearchService;
import org.molgenis.data.elasticsearch.ElasticSearchService.IndexingMode;
import org.molgenis.data.elasticsearch.index.EntityToSourceConverter;
import org.molgenis.data.support.DataServiceImpl;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ElasticSearchServiceTest
{
	private Client client;
	private ElasticSearchService searchService;
	private String indexName;
	private EntityToSourceConverter entityToSourceConverter;
	private DataService dataService;

	@BeforeMethod
	public void beforeMethod()
	{
		indexName = "molgenis";
		client = mock(Client.class);
		entityToSourceConverter = mock(EntityToSourceConverter.class);
		dataService = spy(new DataServiceImpl());
		searchService = spy(new ElasticSearchService(client, indexName, dataService, entityToSourceConverter, false));
		doNothing().when(searchService).refresh();
	}

	@BeforeClass
	public void beforeClass()
	{
	}

	@AfterClass
	public void afterClass()
	{
	}

	@Test
	public void indexEntityAdd()
	{
		DefaultEntityMetaData entityMetaData = createEntityMeta("entity");
		MapEntity entity = createEntityAndRegisterSource(entityMetaData, "id0");

		searchService.index(entity, entityMetaData, IndexingMode.ADD);

		verify(client, times(1)).prepareIndex(eq(indexName), eq("entity"), any(String.class));
		verify(searchService, times(0)).index(Arrays.asList(entity), entityMetaData, IndexingMode.ADD, false);
	}

	@Test
	public void indexEntityUpdateNoRefs()
	{
		DefaultEntityMetaData entityMetaData = createEntityMeta("entity");

		MapEntity entity = createEntityAndRegisterSource(entityMetaData, "id0");

		searchService.index(entity, entityMetaData, IndexingMode.UPDATE);
		verify(client, times(1)).prepareIndex(eq(indexName), eq("entity"), any(String.class));
		verify(searchService, times(0)).index(Arrays.asList(entity), entityMetaData, IndexingMode.UPDATE, false);
	}

	@Test
	public void indexEntityUpdateRefEntity()
	{
		// Create metadata
		DefaultEntityMetaData refEntityMetaData = createEntityMeta("refEntity");
		DefaultEntityMetaData entityMetaData = createEntityMeta("entity");
		addReferenceToMetaData(entityMetaData, refEntityMetaData, "xrefattr");

		// Create repos
		CrudRepository refRepository = mockRepository(refEntityMetaData);
		CrudRepository repository = mockRepository(entityMetaData);
		addRepositories(refRepository, repository);

		// Create entities
		MapEntity refEntity = createEntityAndRegisterSource(refEntityMetaData, "refid");
		MapEntity entity = createEntityAndRegisterSource(entityMetaData, "entityId");
		entity.set("xrefattr", refEntity);
		Query q = new QueryImpl().eq("xrefattr", refEntity);
		when(repository.findAll(q)).thenReturn(Arrays.<Entity> asList(entity));

		try
		{
			searchService.index(refEntity, refEntityMetaData, IndexingMode.UPDATE);
		}
		catch (NullPointerException e)
		{
			fail("Tried to index unexpected entity", e);
		}
		verify(client, times(1)).prepareIndex(indexName, "refEntity", "refid");
		verify(searchService, times(1)).index(Arrays.asList(entity), entityMetaData, IndexingMode.UPDATE, false);
	}

	@Test
	public void indexEntityUpdateSelfReferenceDifferentEntities()
	{
		DefaultEntityMetaData entityMetaData = createEntityMeta("entity");
		addReferenceToMetaData(entityMetaData, entityMetaData, "xrefattr");

		MapEntity refEntity = createEntityAndRegisterSource(entityMetaData, "refId");
		MapEntity entity = createEntityAndRegisterSource(entityMetaData, "id");
		entity.set("xrefattr", refEntity);

		CrudRepository repository = mockRepository(entityMetaData);
		addRepositories(repository);

		Query q = new QueryImpl().eq("xrefattr", refEntity);
		when(repository.findAll(q)).thenReturn(Arrays.<Entity> asList(entity));

		try
		{
			searchService.index(refEntity, entityMetaData, IndexingMode.UPDATE);
		}
		catch (NullPointerException e)
		{
			fail("Tried to index unexpected entity", e);
		}
		verify(client, times(1)).prepareIndex(indexName, "entity", "refId");
		verify(searchService, times(1)).index(Arrays.asList(entity), entityMetaData, IndexingMode.UPDATE, false);
	}

	@Test
	public void indexEntityUpdateSelfReferenceSameEntity()
	{
		DefaultEntityMetaData entityMetaData = createEntityMeta("entity");
		addReferenceToMetaData(entityMetaData, entityMetaData, "xrefattr");

		MapEntity entity = createEntityAndRegisterSource(entityMetaData, "id");
		entity.set("xrefattr", entity);

		CrudRepository repository = mockRepository(entityMetaData);

		Query q = new QueryImpl().eq("xrefattr", entity);
		when(repository.findAll(q)).thenReturn(Arrays.<Entity> asList(entity));

		addRepositories(repository);

		try
		{
			searchService.index(entity, entityMetaData, IndexingMode.UPDATE);
		}
		catch (NullPointerException e)
		{
			fail("Tried to index unexpected entity", e);
		}
		verify(client, times(1)).prepareIndex(indexName, "entity", "id");
		// TODO: is dit wenselijk of een bug?
		verify(searchService, times(1)).index(Arrays.asList(entity), entityMetaData, IndexingMode.UPDATE, false);
	}

	@Test
	public void indexEntityUpdateShouldNotQueryOtherRepositories()
	{
		DefaultEntityMetaData entityMetaData = createEntityMeta("entity");
		DefaultEntityMetaData otherMetaData = createEntityMeta("other");
		DefaultEntityMetaData otherRefMetaData = createEntityMeta("otherRef");
		addReferenceToMetaData(otherMetaData, otherRefMetaData, "xrefattr");

		MapEntity entity = createEntityAndRegisterSource(entityMetaData, "id");

		CrudRepository repository = mockRepository(entityMetaData);
		CrudRepository otherRepository = mockRepository(otherMetaData);
		CrudRepository otherRefRepository = mockRepository(otherRefMetaData);

		addRepositories(repository, otherRepository, otherRefRepository);

		try
		{
			searchService.index(entity, entityMetaData, IndexingMode.UPDATE);
		}
		catch (NullPointerException e)
		{
			fail("Tried to index unexpected entity", e);
		}

		verify(otherRepository, times(0)).findAll(any(Query.class));
		verify(client, times(1)).prepareIndex(indexName, "entity", "id");
	}

	@Test
	public void indexEntityUpdateRefEntityTwoReferringEntityClasses()
	{
		// Create metadata
		DefaultEntityMetaData refEntityMetaData = createEntityMeta("refEntity");
		DefaultEntityMetaData entityMetaData = createEntityMeta("entity");
		DefaultEntityMetaData entity2MetaData = createEntityMeta("entity2");
		addReferenceToMetaData(entityMetaData, refEntityMetaData, "xrefattr");
		addReferenceToMetaData(entity2MetaData, refEntityMetaData, "xrefattr2");

		// Create repos
		CrudRepository refRepository = mockRepository(refEntityMetaData);
		CrudRepository repository = mockRepository(entityMetaData);
		CrudRepository repository2 = mockRepository(entity2MetaData);
		addRepositories(refRepository, repository, repository2);

		// Create entities
		MapEntity refEntity = createEntityAndRegisterSource(refEntityMetaData, "refid");
		MapEntity entity = createEntityAndRegisterSource(entityMetaData, "entityId");
		entity.set("xrefattr", refEntity);
		MapEntity entity2 = createEntityAndRegisterSource(entityMetaData, "entity2Id");
		entity2.set("xrefattr2", refEntity);

		Query q = new QueryImpl().eq("xrefattr", refEntity);
		when(repository.findAll(q)).thenReturn(Arrays.<Entity> asList(entity));
		Query q2 = new QueryImpl().eq("xrefattr2", refEntity);
		when(repository2.findAll(q2)).thenReturn(Arrays.<Entity> asList(entity2));
		try
		{
			searchService.index(refEntity, refEntityMetaData, IndexingMode.UPDATE);
		}
		catch (NullPointerException e)
		{
			fail("Tried to index unexpected entity", e);
		}
		verify(client, times(1)).prepareIndex(indexName, "refEntity", "refid");
		verify(searchService, times(1)).index(Arrays.asList(entity), entityMetaData, IndexingMode.UPDATE, false);
	}

	private void addRepositories(CrudRepository... repositories)
	{
		List<String> repositoryNames = new ArrayList<String>();
		for (CrudRepository repository : repositories)
		{
			dataService.addRepository(repository);
			repositoryNames.add(repository.getName());
		}
		when(dataService.getEntityNames()).thenReturn(repositoryNames);
	}

	private CrudRepository mockRepository(DefaultEntityMetaData refEntityMetaData)
	{
		CrudRepository refRepository = when(mock(CrudRepository.class).getName()).thenReturn(
				refEntityMetaData.getName()).getMock();
		when(refRepository.getEntityMetaData()).thenReturn(refEntityMetaData);
		return refRepository;
	}

	private MapEntity createEntityAndRegisterSource(final EntityMetaData metaData, final String id)
	{
		final String idAttributeName = metaData.getIdAttribute().getName();
		final String entityName = metaData.getName();
		MapEntity entity = new MapEntity(idAttributeName);
		entity.set(idAttributeName, id);
		Map<String, Object> source = getSource(metaData, entity);
		when(entityToSourceConverter.convert(entity, metaData)).thenReturn(source);
		whenIndexEntity(client, id, entityName, source);
		return entity;
	}

	private Map<String, Object> getSource(EntityMetaData metaData, Entity entity)
	{
		final String idAttributeName = metaData.getIdAttribute().getName();
		final String entityName = metaData.getName();
		Map<String, Object> source = new HashMap<String, Object>();
		source.put(idAttributeName, entity.get(idAttributeName));
		source.put("type", entityName);
		return source;
	}

	private void addReferenceToMetaData(final DefaultEntityMetaData from, final DefaultEntityMetaData to,
			final String attributeName)
	{
		from.addAttribute(attributeName).setDataType(MolgenisFieldTypes.XREF).setRefEntity(to);
	}

	private DefaultEntityMetaData createEntityMeta(String refEntityName)
	{
		DefaultEntityMetaData refEntityMetaData = new DefaultEntityMetaData(refEntityName);
		refEntityMetaData.addAttribute("id").setIdAttribute(true).setUnique(true);
		return refEntityMetaData;
	}

	private void whenIndexEntity(Client client, String id, String entityName, Map<String, Object> source)
	{
		IndexRequestBuilder indexRequestBuilder = mock(IndexRequestBuilder.class);
		when(indexRequestBuilder.setSource(eq(source))).thenReturn(indexRequestBuilder);
		@SuppressWarnings("unchecked")
		ListenableActionFuture<IndexResponse> indexResponse = mock(ListenableActionFuture.class);
		when(indexRequestBuilder.execute()).thenReturn(indexResponse);
		when(client.prepareIndex(indexName, entityName, id)).thenReturn(indexRequestBuilder);
	}
}

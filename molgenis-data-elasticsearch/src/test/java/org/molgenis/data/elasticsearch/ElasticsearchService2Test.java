package org.molgenis.data.elasticsearch;

import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.AtomicLongMap;
import org.elasticsearch.action.index.IndexRequest;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.*;
import org.molgenis.data.elasticsearch.index.EntityToSourceConverter;
import org.molgenis.data.elasticsearch.index.SourceToEntityConverter;
import org.molgenis.data.elasticsearch.util.ElasticsearchUtils;
import org.molgenis.data.support.DataServiceImpl;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntity;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static autovalue.shaded.com.google.common.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toList;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_LABEL;
import static org.molgenis.data.elasticsearch.ElasticsearchService.IndexingMode.ADD;
import static org.molgenis.data.elasticsearch.ElasticsearchService.IndexingMode.UPDATE;
import static org.testng.Assert.assertEquals;

public class ElasticsearchService2Test
{
	private ElasticsearchService elasticsearchService;
	@Mock
	private ElasticsearchUtils elasticSearchFacade;
	@Mock
	private DataServiceImpl dataService;

	private EntityManager entityManager;
	@Mock
	private SourceToEntityConverter sourceToEntityConverter;

	private EntityToSourceConverter entityToSourceConverter = new EntityToSourceConverter();

	@Captor
	private ArgumentCaptor<Stream<IndexRequest>> indexRequestsCaptor;

	@Captor
	private ArgumentCaptor<Query<Entity>> queryCaptor;

	@Captor
	private ArgumentCaptor<Stream<String>> idStreamCaptor;

	@Captor
	private ArgumentCaptor<Stream<Object>> idObjectStreamCaptor;

	private DefaultEntityMetaData typeTestMeta;
	private DefaultEntityMetaData typeTestRefMeta;

	private DefaultEntity typeTestRefEntity;
	private DefaultEntity typeTestEntity;

	@BeforeMethod
	public void beforeMethod() throws InterruptedException
	{
		initMocks(this);
		entityManager = new EntityManagerImpl(dataService);

		ElasticsearchEntityFactory elasticsearchEntityFactory = new ElasticsearchEntityFactory(entityManager,
				sourceToEntityConverter, entityToSourceConverter);
		elasticsearchService = new ElasticsearchService(elasticSearchFacade, "molgenis", dataService,
				elasticsearchEntityFactory);

		createEntitiesAndMetadata();

		when(dataService.getEntityNames()).thenReturn(Arrays.stream(new String[] { "TypeTest", "TypeTestRef" }));
		when(dataService.getEntityMetaData("TypeTest")).thenReturn(typeTestMeta);
		when(dataService.getEntityMetaData("TypeTestRef")).thenReturn(typeTestRefMeta);
	}

	private void createEntitiesAndMetadata()
	{
		typeTestRefMeta = new DefaultEntityMetaData("TypeTestRef");
		typeTestRefMeta.addAttribute("id", ROLE_ID);
		typeTestRefMeta.addAttribute("label", ROLE_LABEL);
		typeTestRefEntity = new DefaultEntity(typeTestRefMeta, dataService);
		typeTestRefEntity.set("id", "ABCDE");
		typeTestRefEntity.set("label", "Label 1");

		typeTestMeta = new DefaultEntityMetaData("TypeTest");
		typeTestMeta.addAttribute("id", ROLE_ID);
		DefaultAttributeMetaData xrefAttribute = new DefaultAttributeMetaData("xref",
				MolgenisFieldTypes.FieldTypeEnum.XREF);
		xrefAttribute.setRefEntity(typeTestRefMeta);
		typeTestMeta.addAttributeMetaData(xrefAttribute);
		typeTestEntity = new DefaultEntity(typeTestMeta, dataService);
		typeTestEntity.set("id", "FGHIJ");
		typeTestEntity.set("xref", typeTestRefEntity);
	}

	@BeforeClass
	public void beforeClass()
	{
	}

	@AfterClass
	public void afterClass()
	{
	}

	/**
	 * Asserts that two streams of {@link IndexRequest}s are equal by comparing their toString values
	 */
	private void assertIndexRequestsEqual(Stream<IndexRequest> actual, Stream<IndexRequest> expected)
	{
		assertEquals(actual.map(IndexRequest::toString).collect(toList()),
				expected.map(IndexRequest::toString).collect(toList()));
	}

	@Test
	public void testIndexSingleEntityStreamAddHappyPath()
	{
		AtomicLongMap<String> counts = AtomicLongMap.create();
		counts.put("TypeTestRef", 1);

		when(elasticSearchFacade.index(indexRequestsCaptor.capture(), eq(true))).thenReturn(counts);

		assertEquals(1, elasticsearchService.index(Stream.of(typeTestRefEntity), typeTestRefMeta, ADD));

		assertIndexRequestsEqual(indexRequestsCaptor.getValue(), Stream.of(
				new IndexRequest().index("molgenis").type("TypeTestRef").id("ABCDE")
						.source(ImmutableMap.of("id", "ABCDE", "label", "Label 1"))));
	}

	@Test
	public void testIndexSingleEntityStreamAlsoUpdatesReferencingEntity()
	{
		AtomicLongMap<String> counts = AtomicLongMap.create();
		counts.put("TypeTestRef", 1);
		counts.put("TypeTest", 1);
		when(elasticSearchFacade.index(indexRequestsCaptor.capture(), eq(true))).thenReturn(counts);

		when(elasticSearchFacade.searchForIdsWithScanScroll(any(Consumer.class),
				eq("rules=['xref' = 'Label 1']"), eq("TypeTest"),
				eq("molgenis"))).thenReturn(Stream.of("FGHIJ"));

		Fetch fetch = new Fetch().field("id").field("xref", new Fetch().field("id").field("label"));
		when(dataService.findAll(eq("TypeTest"), idObjectStreamCaptor.capture(), eq(fetch)))
				.thenReturn(Stream.of(typeTestEntity));

		assertEquals(1, elasticsearchService.index(Stream.of(typeTestRefEntity), typeTestRefMeta, UPDATE));

		Map<String, Object> typeTestRefSource = ImmutableMap.of("id", "ABCDE", "label", "Label 1");
		assertIndexRequestsEqual(indexRequestsCaptor.getValue(), Stream.of(
				new IndexRequest().index("molgenis").type("TypeTestRef").id("ABCDE").source(typeTestRefSource),
				new IndexRequest().index("molgenis").type("TypeTest").id("FGHIJ")
						.source(ImmutableMap.of("xref", typeTestRefSource, "id", "FGHIJ"))));

		assertEquals(idObjectStreamCaptor.getValue().collect(toList()), newArrayList("FGHIJ"));

	}

	// TODO: test stream of two entities

	// TODO: test what happens if search for referencing entities throws Exception

	@Test()
	public void testDeleteById()
	{
		elasticsearchService.deleteById(Stream.of("ABCDE"), typeTestRefMeta);
		verify(elasticSearchFacade).deleteById("molgenis", "ABCDE", "TypeTestRef");
		verifyNoMoreInteractions(dataService);
	}
}

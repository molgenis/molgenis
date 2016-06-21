package org.molgenis.ontology.core.repository;

import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;

//@ContextConfiguration(classes = OntologyTermRepositoryTest.Config.class)
public class OntologyTermRepositoryTest extends AbstractTestNGSpringContextTests
{
	//	@Autowired
	//	DataService dataService;
	//
	//	@Autowired
	//	OntologyTermRepository ontologyTermRepository;
	//
	//	@Autowired
	//	OntologyTermMetaData ontologyTermMetaData;
	//
	//	@Autowired
	//	OntologyTermSynonymMetaData ontologyTermSynonymMetaData;
	//
	//	private Entity ontologyTermEntity;
	//
	//	@BeforeTest
	//	public void beforeTest()
	//	{
	//		ontologyTermEntity = new MapEntity(ontologyTermMetaData);
	//		ontologyTermEntity.set(OntologyTermMetaData.ID, "12");
	//		ontologyTermEntity.set(OntologyTermMetaData.ONTOLOGY, "34");
	//		ontologyTermEntity.set(OntologyTermMetaData.ONTOLOGY_TERM_IRI, "http://www.test.nl/iri");
	//		ontologyTermEntity.set(OntologyTermMetaData.ONTOLOGY_TERM_NAME, "Ontology term");
	//	}
	//
	//	@Test
	//	public void testFindExcatOntologyTerms()
	//	{
	//		MapEntity synonymEntity1 = new MapEntity(ontologyTermSynonymMetaData);
	//		synonymEntity1.set(OntologyTermSynonymMetaData.ID, "synonym-1");
	//		synonymEntity1.set(OntologyTermSynonymMetaData.ONTOLOGY_TERM_SYNONYM_ATTR, "Weight Reduction Diet");
	//
	//		MapEntity synonymEntity2 = new MapEntity(ontologyTermSynonymMetaData);
	//		synonymEntity2.set(OntologyTermSynonymMetaData.ID, "synonym-2");
	//		synonymEntity2.set(OntologyTermSynonymMetaData.ONTOLOGY_TERM_SYNONYM_ATTR, "Weight loss Diet");
	//
	//		MapEntity synonymEntity3 = new MapEntity(ontologyTermSynonymMetaData);
	//		synonymEntity3.set(OntologyTermSynonymMetaData.ID, "synonym-3");
	//		synonymEntity3.set(OntologyTermSynonymMetaData.ONTOLOGY_TERM_SYNONYM_ATTR, "Diet, Reducing");
	//
	//		MapEntity ontologyTermEntity1 = new MapEntity(ontologyTermMetaData);
	//		ontologyTermEntity1.set(OntologyTermMetaData.ID, "1");
	//		ontologyTermEntity1.set(OntologyTermMetaData.ONTOLOGY, "34");
	//		ontologyTermEntity1.set(OntologyTermMetaData.ONTOLOGY_TERM_IRI, "http://www.test.nl/iri/1");
	//		ontologyTermEntity1.set(OntologyTermMetaData.ONTOLOGY_TERM_NAME, "Diet, Reducing");
	//		ontologyTermEntity1.set(OntologyTermMetaData.ONTOLOGY_TERM_SYNONYM,
	//				Arrays.asList(synonymEntity1, synonymEntity2, synonymEntity3));
	//
	//		MapEntity synonymEntity4 = new MapEntity(ontologyTermSynonymMetaData);
	//		synonymEntity4.set(OntologyTermSynonymMetaData.ID, "synonym-4");
	//		synonymEntity4.set(OntologyTermSynonymMetaData.ONTOLOGY_TERM_SYNONYM_ATTR, "Weight");
	//
	//		MapEntity ontologyTermEntity2 = new MapEntity(ontologyTermMetaData);
	//		ontologyTermEntity2.set(OntologyTermMetaData.ID, "12");
	//		ontologyTermEntity2.set(OntologyTermMetaData.ONTOLOGY, "34");
	//		ontologyTermEntity2.set(OntologyTermMetaData.ONTOLOGY_TERM_IRI, "http://www.test.nl/iri/2");
	//		ontologyTermEntity2.set(OntologyTermMetaData.ONTOLOGY_TERM_NAME, "Weight");
	//		ontologyTermEntity2.set(OntologyTermMetaData.ONTOLOGY_TERM_SYNONYM, Arrays.asList(synonymEntity4));
	//
	//		ArgumentCaptor<Query<Entity>> queryCaptor = forClass((Class) Query.class);
	//		when(dataService.findAll(eq(ONTOLOGY_TERM), queryCaptor.capture()))
	//				.thenReturn(Stream.of(ontologyTermEntity1, ontologyTermEntity2));
	//
	//		List<OntologyTerm> exactOntologyTerms = ontologyTermRepository
	//				.findExcatOntologyTerms(asList("1", "2"), of("weight"), 100);
	//
	//		Assert.assertEquals(exactOntologyTerms, Arrays.asList(
	//				OntologyTerm.create("http://www.test.nl/iri/2", "Weight", null, Arrays.asList("Weight"))));
	//	}
	//
	//	@Test
	//	public void testFindOntologyTerms()
	//	{
	//		ArgumentCaptor<Query<Entity>> queryCaptor = forClass((Class) Query.class);
	//		when(dataService.findAll(eq(ONTOLOGY_TERM), queryCaptor.capture()))
	//				.thenReturn(Stream.of(ontologyTermEntity));
	//
	//		List<OntologyTerm> terms = ontologyTermRepository
	//				.findOntologyTerms(asList("1", "2"), of("term1", "term2", "term3"), 100);
	//
	//		assertEquals(terms, asList(OntologyTerm
	//				.create("http://www.test.nl/iri", "Ontology term", null, Arrays.asList("Ontology term"))));
	//		assertEquals(queryCaptor.getValue().toString(),
	//				"rules=['ontology' IN [1, 2], AND, ('ontologyTermSynonym' FUZZY_MATCH 'term1', OR, 'ontologyTermSynonym' FUZZY_MATCH 'term2', OR, 'ontologyTermSynonym' FUZZY_MATCH 'term3')], pageSize=100");
	//	}
	//
	//	@Test
	//	public void testGetChildOntologyTermsByNodePath()
	//	{
	//		Entity ontologyEntity = new MapEntity(ImmutableMap
	//				.of(OntologyMetaData.ONTOLOGY_IRI, "http://www.molgenis.org", OntologyMetaData.ONTOLOGY_NAME,
	//						"molgenis"));
	//
	//		Entity nodePathEntity_1 = new MapEntity(
	//				ImmutableMap.of(OntologyTermNodePathMetaData.ONTOLOGY_TERM_NODE_PATH_ATTR, "0[0].1[1]"));
	//		Entity nodePathEntity_2 = new MapEntity(
	//				ImmutableMap.of(OntologyTermNodePathMetaData.ONTOLOGY_TERM_NODE_PATH_ATTR, "0[0].1[1].0[2]"));
	//		Entity nodePathEntity_3 = new MapEntity(
	//				ImmutableMap.of(OntologyTermNodePathMetaData.ONTOLOGY_TERM_NODE_PATH_ATTR, "0[0].1[1].1[2]"));
	//
	//		MapEntity ontologyTerm_2 = new MapEntity();
	//		ontologyTerm_2.set(OntologyTermMetaData.ONTOLOGY, ontologyEntity);
	//		ontologyTerm_2.set(OntologyTermMetaData.ONTOLOGY_TERM_IRI, "iri 2");
	//		ontologyTerm_2.set(OntologyTermMetaData.ONTOLOGY_TERM_NAME, "name 2");
	//		ontologyTerm_2
	//				.set(OntologyTermMetaData.ONTOLOGY_TERM_NODE_PATH, Arrays.asList(nodePathEntity_1, nodePathEntity_2));
	//		ontologyTerm_2.set(OntologyTermMetaData.ONTOLOGY_TERM_SYNONYM, Collections.emptyList());
	//
	//		MapEntity ontologyTerm_3 = new MapEntity();
	//		ontologyTerm_3.set(OntologyTermMetaData.ONTOLOGY, ontologyEntity);
	//		ontologyTerm_3.set(OntologyTermMetaData.ONTOLOGY_TERM_IRI, "iri 3");
	//		ontologyTerm_3.set(OntologyTermMetaData.ONTOLOGY_TERM_NAME, "name 3");
	//		ontologyTerm_3.set(OntologyTermMetaData.ONTOLOGY_TERM_NODE_PATH, Arrays.asList(nodePathEntity_3));
	//		ontologyTerm_3.set(OntologyTermMetaData.ONTOLOGY_TERM_SYNONYM, Collections.emptyList());
	//
	//		when(dataService.findAll(ONTOLOGY_TERM, new QueryImpl<Entity>(
	//				new QueryRule(OntologyTermMetaData.ONTOLOGY_TERM_NODE_PATH, Operator.FUZZY_MATCH, "\"0[0].1[1]\""))
	//				.and().eq(OntologyTermMetaData.ONTOLOGY, ontologyEntity)))
	//				.thenReturn(Stream.of(ontologyTerm_2, ontologyTerm_3));
	//
	//		List<OntologyTerm> childOntologyTermsByNodePath = ontologyTermRepository
	//				.getChildOntologyTermsByNodePath(ontologyEntity, nodePathEntity_1);
	//
	//		assertEquals(childOntologyTermsByNodePath.size(), 2);
	//		assertEquals(childOntologyTermsByNodePath.get(0),
	//				OntologyTerm.create("iri 2", "name 2", null, Arrays.asList("name 2")));
	//		assertEquals(childOntologyTermsByNodePath.get(1),
	//				OntologyTerm.create("iri 3", "name 3", null, Arrays.asList("name 3")));
	//	}
	//
	//	@Test
	//	public void testCalculateNodePathDistance()
	//	{
	//		// Case 1
	//		assertEquals(ontologyTermRepository.calculateNodePathDistance("0[0].0[1]", "0[0].0[1].1[2]"), 1);
	//
	//		// Case 2
	//		assertEquals(ontologyTermRepository.calculateNodePathDistance("0[0].0[1].1[2]", "0[0].0[1]"), 1);
	//
	//		// Case 3
	//		assertEquals(ontologyTermRepository.calculateNodePathDistance("0[0].0[1].1[2].2[3]", "0[0].0[1].0[2].2[3]"), 4);
	//
	//		// Case 4
	//		assertEquals(ontologyTermRepository.calculateNodePathDistance("0[0].0[1]", "0[0].0[1].0[2].1[3].2[4]"), 3);
	//
	//		// Case 5
	//		assertEquals(ontologyTermRepository.calculateNodePathDistance("0[0].0[1]", "0[0].0[1]"), 0);
	//	}
	//
	//	@Test
	//	public void testGetOntologyTerm()
	//	{
	//		when(dataService.findOne(ONTOLOGY_TERM,
	//				QueryImpl.EQ(OntologyTermMetaData.ONTOLOGY_TERM_IRI, "http://www.test.nl/iri")))
	//				.thenReturn(ontologyTermEntity);
	//
	//		String[] iris = { "http://www.test.nl/iri" };
	//
	//		OntologyTerm ontologyTerm = ontologyTermRepository.getOntologyTerm(iris);
	//		assertEquals(ontologyTerm,
	//				OntologyTerm.create("http://www.test.nl/iri", "Ontology term", Arrays.asList("Ontology term")));
	//	}
	//
	//	@Configuration
	//	public static class Config
	//	{
	//		@Bean
	//		public DataService dataService()
	//		{
	//			return mock(DataService.class);
	//		}
	//
	//		@Bean
	//		public OntologyTermRepository ontologyTermRepository()
	//		{
	//			return new OntologyTermRepository(dataService());
	//		}
	//
	//		@Bean
	//		public OntologyTermMetaData ontologyTermMetaData()
	//		{
	//			return new OntologyTermMetaData();
	//		}
	//
	//		@Bean
	//		public OntologyTermSynonymMetaData ontologyTermSynonymMetaData()
	//		{
	//			return new OntologyTermSynonymMetaData();
	//		}
	//	}
}
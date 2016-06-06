package org.molgenis.data.semanticsearch.service.impl;

import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;

//@ContextConfiguration(classes = SemanticSearchServiceHelperTest.Config.class)
public class SemanticSearchServiceHelperTest extends AbstractTestNGSpringContextTests
{
	//	@Autowired
	//	private OntologyService ontologyService;
	//
	//	@Autowired
	//	private SemanticSearchServiceHelper semanticSearchServiceHelper;
	//
	//	@Autowired
	//	private DataService dataService;
	//
	//	@Test
	//	public void testCreateDisMaxQueryRule()
	//	{
	//		List<String> createdTargetAttributeQueries = Arrays.asList("Height", "Standing height in cm", "body_length",
	//				"Sitting height", "sitting_length", "Height", "sature");
	//		QueryRule actualRule = semanticSearchServiceHelper.createDisMaxQueryRuleForTerms(createdTargetAttributeQueries);
	//		String expectedQueryRuleToString = "DIS_MAX ('label' FUZZY_MATCH 'Height', 'description' FUZZY_MATCH 'Height', 'label' FUZZY_MATCH 'Standing height in cm', 'description' FUZZY_MATCH 'Standing height in cm', 'label' FUZZY_MATCH 'body_length', 'description' FUZZY_MATCH 'body_length', 'label' FUZZY_MATCH 'Sitting height', 'description' FUZZY_MATCH 'Sitting height', 'label' FUZZY_MATCH 'sitting_length', 'description' FUZZY_MATCH 'sitting_length', 'label' FUZZY_MATCH 'Height', 'description' FUZZY_MATCH 'Height', 'label' FUZZY_MATCH 'sature', 'description' FUZZY_MATCH 'sature')";
	//		assertEquals(actualRule.getOperator(), Operator.DIS_MAX);
	//		assertEquals(actualRule.toString(), expectedQueryRuleToString);
	//
	//		List<String> createdTargetAttributeQueries2 = Arrays.asList("(Height) [stand^~]");
	//		QueryRule actualRule2 = semanticSearchServiceHelper
	//				.createDisMaxQueryRuleForTerms(createdTargetAttributeQueries2);
	//		String expectedQueryRuleToString2 = "DIS_MAX ('label' FUZZY_MATCH '\\(Height\\) \\[stand^\\~\\]', 'description' FUZZY_MATCH '\\(Height\\) \\[stand^\\~\\]')";
	//		assertEquals(actualRule2.getOperator(), Operator.DIS_MAX);
	//		assertEquals(actualRule2.toString(), expectedQueryRuleToString2);
	//	}
	//
	//	@Test
	//	public void testCreateShouldQueryRule()
	//	{
	//		String multiOntologyTermIri = "http://www.molgenis.org/1,http://www.molgenis.org/2";
	//		OntologyTerm ontologyTerm_1 = OntologyTerm.create("http://www.molgenis.org/1", "molgenis label in the gcc");
	//		OntologyTerm ontologyTerm_2 = OntologyTerm.create("http://www.molgenis.org/2",
	//				"molgenis label 2 in the genetics", Arrays.asList("label 2"));
	//		when(ontologyService.getOntologyTerm(ontologyTerm_1.getIRI())).thenReturn(ontologyTerm_1);
	//		when(ontologyService.getOntologyTerm(ontologyTerm_2.getIRI())).thenReturn(ontologyTerm_2);
	//
	//		QueryRule actualShouldQueryRule = semanticSearchServiceHelper.createShouldQueryRule(multiOntologyTermIri);
	//		String expectedShouldQueryRuleToString = "SHOULD (DIS_MAX ('label' FUZZY_MATCH 'gcc molgenis label', 'description' FUZZY_MATCH 'gcc molgenis label'), DIS_MAX ('label' FUZZY_MATCH '2 label', 'description' FUZZY_MATCH '2 label', 'label' FUZZY_MATCH '2 genetics molgenis label', 'description' FUZZY_MATCH '2 genetics molgenis label'))";
	//
	//		assertEquals(actualShouldQueryRule.toString(), expectedShouldQueryRuleToString);
	//		assertEquals(actualShouldQueryRule.getOperator(), Operator.SHOULD);
	//	}
	//
	//	@Test
	//	public void testCreateTargetAttributeQueryTerms()
	//	{
	//		AttributeMetaData targetAttribute_1 = new AttributeMetaData("targetAttribute 1");
	//		targetAttribute_1.setDescription("Height");
	//
	//		AttributeMetaData targetAttribute_2 = new AttributeMetaData("targetAttribute 2");
	//		targetAttribute_2.setLabel("Height");
	//
	//		Multimap<Relation, OntologyTerm> tags = LinkedHashMultimap.<Relation, OntologyTerm> create();
	//		OntologyTerm ontologyTerm1 = OntologyTerm.create("http://onto/standingheight", "Standing height",
	//				"Description is not used", Arrays.<String> asList("body_length"));
	//		OntologyTerm ontologyTerm2 = OntologyTerm.create("http://onto/sittingheight", "Sitting height",
	//				"Description is not used", Arrays.<String> asList("sitting_length"));
	//		OntologyTerm ontologyTerm3 = OntologyTerm.create("http://onto/height", "Height", "Description is not used",
	//				Arrays.<String> asList("sature"));
	//
	//		tags.put(Relation.isAssociatedWith, ontologyTerm1);
	//		tags.put(Relation.isRealizationOf, ontologyTerm2);
	//		tags.put(Relation.isDefinedBy, ontologyTerm3);
	//
	//		// Case 1
	//		QueryRule actualTargetAttributeQueryTerms_1 = semanticSearchServiceHelper.createDisMaxQueryRuleForAttribute(
	//				Sets.newLinkedHashSet(Arrays.asList("targetAttribute 1", "Height")), tags.values());
	//		String expecteddisMaxQueryRuleToString_1 = "DIS_MAX ('label' FUZZY_MATCH '1 targetattribute', 'description' FUZZY_MATCH '1 targetattribute', 'label' FUZZY_MATCH 'height', 'description' FUZZY_MATCH 'height', 'label' FUZZY_MATCH 'length body', 'description' FUZZY_MATCH 'length body', 'label' FUZZY_MATCH 'standing height', 'description' FUZZY_MATCH 'standing height', 'label' FUZZY_MATCH 'length sitting', 'description' FUZZY_MATCH 'length sitting', 'label' FUZZY_MATCH 'sitting height', 'description' FUZZY_MATCH 'sitting height', 'label' FUZZY_MATCH 'sature', 'description' FUZZY_MATCH 'sature', 'label' FUZZY_MATCH 'height', 'description' FUZZY_MATCH 'height')";
	//		assertEquals(actualTargetAttributeQueryTerms_1.toString(), expecteddisMaxQueryRuleToString_1);
	//
	//		// Case 2
	//		QueryRule expecteddisMaxQueryRuleToString_2 = semanticSearchServiceHelper
	//				.createDisMaxQueryRuleForAttribute(Sets.newHashSet("Height"), tags.values());
	//		String expectedTargetAttributeQueryTermsToString_2 = "DIS_MAX ('label' FUZZY_MATCH 'height', 'description' FUZZY_MATCH 'height', 'label' FUZZY_MATCH 'length body', 'description' FUZZY_MATCH 'length body', 'label' FUZZY_MATCH 'standing height', 'description' FUZZY_MATCH 'standing height', 'label' FUZZY_MATCH 'length sitting', 'description' FUZZY_MATCH 'length sitting', 'label' FUZZY_MATCH 'sitting height', 'description' FUZZY_MATCH 'sitting height', 'label' FUZZY_MATCH 'sature', 'description' FUZZY_MATCH 'sature', 'label' FUZZY_MATCH 'height', 'description' FUZZY_MATCH 'height')";
	//		assertEquals(expecteddisMaxQueryRuleToString_2.toString(), expectedTargetAttributeQueryTermsToString_2);
	//
	//		// Case 3
	//		QueryRule expecteddisMaxQueryRuleToString_3 = semanticSearchServiceHelper
	//				.createDisMaxQueryRuleForAttribute(Sets.newHashSet("targetAttribute 3"), tags.values());
	//		String expectedTargetAttributeQueryTermsToString_3 = "DIS_MAX ('label' FUZZY_MATCH '3 targetattribute', 'description' FUZZY_MATCH '3 targetattribute', 'label' FUZZY_MATCH 'length body', 'description' FUZZY_MATCH 'length body', 'label' FUZZY_MATCH 'standing height', 'description' FUZZY_MATCH 'standing height', 'label' FUZZY_MATCH 'length sitting', 'description' FUZZY_MATCH 'length sitting', 'label' FUZZY_MATCH 'sitting height', 'description' FUZZY_MATCH 'sitting height', 'label' FUZZY_MATCH 'sature', 'description' FUZZY_MATCH 'sature', 'label' FUZZY_MATCH 'height', 'description' FUZZY_MATCH 'height')";
	//		assertEquals(expecteddisMaxQueryRuleToString_3.toString(), expectedTargetAttributeQueryTermsToString_3);
	//	}
	//
	//	@Test
	//	public void testCollectQueryTermsFromOntologyTerm()
	//	{
	//		// Case 1
	//		OntologyTerm ontologyTerm1 = OntologyTerm.create("http://onto/standingheight", "Standing height",
	//				"Description is not used", Arrays.<String> asList("body_length"));
	//		List<String> actual_1 = semanticSearchServiceHelper.parseOntologyTermQueries(ontologyTerm1);
	//		assertEquals(actual_1, Arrays.asList("length body", "standing height"));
	//
	//		// Case 2
	//		OntologyTerm ontologyTerm2 = OntologyTerm.create("http://onto/standingheight", "height",
	//				"Description is not used", Collections.emptyList());
	//
	//		OntologyTerm ontologyTerm3 = OntologyTerm.create("http://onto/standingheight-children", "length",
	//				Arrays.<String> asList("body_length"));
	//
	//		when(ontologyService.getChildren(ontologyTerm2)).thenReturn(Arrays.asList(ontologyTerm3));
	//
	//		when(ontologyService.getOntologyTermDistance(ontologyTerm2, ontologyTerm3)).thenReturn(1);
	//
	//		List<String> actual_2 = semanticSearchServiceHelper.parseOntologyTermQueries(ontologyTerm2);
	//
	//		assertEquals(actual_2, Arrays.asList("height", "length^0.5 body^0.5", "length^0.5"));
	//	}
	//
	//	@Test
	//	public void testGetAttributeIdentifiers()
	//	{
	//		EntityMetaData sourceEntityMetaData = new EntityMetaData("sourceEntityMetaData");
	//		Entity entityMetaDataEntity = mock(DefaultEntity.class);
	//
	//		when(dataService.findOne(ENTITY_META_DATA,
	//				new QueryImpl<Entity>().eq(EntityMetaDataMetaData.FULL_NAME, sourceEntityMetaData.getName())))
	//						.thenReturn(entityMetaDataEntity);
	//
	//		Entity attributeEntity1 = new MapEntity();
	//		attributeEntity1.set(AttributeMetaDataMetaData.IDENTIFIER, "1");
	//		attributeEntity1.set(AttributeMetaDataMetaData.DATA_TYPE, "string");
	//		Entity attributeEntity2 = new MapEntity();
	//		attributeEntity2.set(AttributeMetaDataMetaData.IDENTIFIER, "2");
	//		attributeEntity2.set(AttributeMetaDataMetaData.DATA_TYPE, "string");
	//		when(entityMetaDataEntity.getEntities(EntityMetaDataMetaData.ATTRIBUTES))
	//				.thenReturn(Arrays.<Entity> asList(attributeEntity1, attributeEntity2));
	//
	//		List<String> expactedAttributeIdentifiers = Arrays.<String> asList("1", "2");
	//		assertEquals(semanticSearchServiceHelper.getAttributeIdentifiers(sourceEntityMetaData),
	//				expactedAttributeIdentifiers);
	//	}
	//
	//	@Test
	//	public void testParseBoostQueryString()
	//	{
	//		String description = "falling in the ocean!";
	//		String actual = semanticSearchServiceHelper.parseBoostQueryString(description, 0.5);
	//		assertEquals(actual, "ocean^0.5 falling^0.5");
	//	}
	//
	//	@Test
	//	public void testRemoveStopWords()
	//	{
	//		String description = "falling in the ocean!";
	//		Set<String> actual = semanticSearchServiceHelper.removeStopWords(description);
	//		Set<String> expected = Sets.newHashSet("falling", "ocean");
	//		assertEquals(actual, expected);
	//	}
	//
	//	@Test
	//	public void testFindTagsSync()
	//	{
	//		String description = "Fall " + NGramDistanceAlgorithm.STOPWORDSLIST + " sleep";
	//		List<String> ontologyIds = Arrays.<String> asList("1");
	//		Set<String> searchTerms = Sets.newHashSet("fall", "sleep");
	//		semanticSearchServiceHelper.findTags(description, ontologyIds);
	//		verify(ontologyService).findOntologyTerms(ontologyIds, searchTerms, SemanticSearchServiceHelper.MAX_NUM_TAGS);
	//	}
	//
	//	@Test
	//	public void testSearchCircumflex() throws InterruptedException, ExecutionException
	//	{
	//		String description = "body^0.5 length^0.5";
	//		Set<String> expected = Sets.newHashSet("length", "body", "0.5");
	//		Set<String> actual = semanticSearchServiceHelper.removeStopWords(description);
	//		assertEquals(actual.size(), 3);
	//		assertTrue(actual.containsAll(expected));
	//	}
	//
	//	@Test
	//	public void testSearchTilde() throws InterruptedException, ExecutionException
	//	{
	//		String description = "body~0.5 length~0.5";
	//		Set<String> expected = Sets.newHashSet("length~0.5", "body~0.5");
	//		Set<String> actual = semanticSearchServiceHelper.removeStopWords(description);
	//		assertEquals(actual, expected);
	//	}
	//
	//	@Test
	//	public void testSearchUnderScore() throws InterruptedException, ExecutionException
	//	{
	//		String description = "body_length";
	//		Set<String> expected = Sets.newHashSet("body", "length");
	//		Set<String> actual = semanticSearchServiceHelper.removeStopWords(description);
	//		assertEquals(actual, expected);
	//	}
	//
	//	@Test
	//	public void testSearchIsoLatin() throws InterruptedException, ExecutionException
	//	{
	//		String description = "Standing height (Ångstrøm)";
	//		List<String> ontologyIds = Arrays.<String> asList("1");
	//		Set<String> searchTerms = Sets.newHashSet("standing", "height", "ångstrøm");
	//		semanticSearchServiceHelper.findTags(description, ontologyIds);
	//		verify(ontologyService).findOntologyTerms(ontologyIds, searchTerms, SemanticSearchServiceHelper.MAX_NUM_TAGS);
	//	}
	//
	//	@Test
	//	public void testSearchUnicode() throws InterruptedException, ExecutionException
	//	{
	//		String description = "/əˈnædrəməs/";
	//		List<String> ontologyIds = Arrays.<String> asList("1");
	//		Set<String> searchTerms = Sets.newHashSet("əˈnædrəməs");
	//		semanticSearchServiceHelper.findTags(description, ontologyIds);
	//		verify(ontologyService).findOntologyTerms(ontologyIds, searchTerms, SemanticSearchServiceHelper.MAX_NUM_TAGS);
	//	}
	//
	//	@Test
	//	public void testEscapeCharsExcludingCaretChar()
	//	{
	//		Assert.assertEquals(semanticSearchServiceHelper.escapeCharsExcludingCaretChar("(hypertension^4)~[]"),
	//				"\\(hypertension^4\\)\\~\\[\\]");
	//
	//		Assert.assertEquals(semanticSearchServiceHelper.escapeCharsExcludingCaretChar("hypertension^4"),
	//				"hypertension^4");
	//	}
	//
	//	@Configuration
	//	public static class Config
	//	{
	//		@Bean
	//		OntologyService ontologyService()
	//		{
	//			return mock(OntologyService.class);
	//		}
	//
	//		@Bean
	//		SemanticSearchService semanticSearchService()
	//		{
	//			return new SemanticSearchServiceImpl(dataService(), ontologyService(), metaDataService(),
	//					semanticSearchServiceHelper(), elasticSearchExplainService());
	//		}
	//
	//		@Bean
	//		DataService dataService()
	//		{
	//			return mock(DataService.class);
	//		}
	//
	//		@Bean
	//		MetaDataService metaDataService()
	//		{
	//			return mock(MetaDataService.class);
	//		}
	//
	//		@Bean
	//		ElasticSearchExplainService elasticSearchExplainService()
	//		{
	//			return mock(ElasticSearchExplainService.class);
	//		}
	//
	//		@Bean
	//		TermFrequencyService termFrequencyService()
	//		{
	//			return mock(TermFrequencyService.class);
	//		}
	//
	//		@Bean
	//		SemanticSearchServiceHelper semanticSearchServiceHelper()
	//		{
	//			return new SemanticSearchServiceHelper(dataService(), ontologyService(), termFrequencyService());
	//		}
	//	}
}

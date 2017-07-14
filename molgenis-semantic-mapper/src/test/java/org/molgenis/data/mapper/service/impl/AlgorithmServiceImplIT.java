package org.molgenis.data.mapper.service.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Sets;
import org.molgenis.auth.User;
import org.molgenis.auth.UserFactory;
import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityManager;
import org.molgenis.data.config.UserTestConfig;
import org.molgenis.data.mapper.algorithmgenerator.service.AlgorithmGeneratorService;
import org.molgenis.data.mapper.algorithmgenerator.service.impl.AlgorithmGeneratorServiceImpl;
import org.molgenis.data.mapper.mapping.model.AttributeMapping;
import org.molgenis.data.mapper.mapping.model.EntityMapping;
import org.molgenis.data.mapper.mapping.model.MappingProject;
import org.molgenis.data.mapper.service.AlgorithmService;
import org.molgenis.data.mapper.service.UnitResolver;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.populate.IdGenerator;
import org.molgenis.data.semantic.Relation;
import org.molgenis.data.semanticsearch.explain.bean.ExplainedAttribute;
import org.molgenis.data.semanticsearch.explain.bean.ExplainedQueryString;
import org.molgenis.data.semanticsearch.repository.TagRepository;
import org.molgenis.data.semanticsearch.service.OntologyTagService;
import org.molgenis.data.semanticsearch.service.SemanticSearchService;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.js.magma.JsMagmaScriptEvaluator;
import org.molgenis.js.nashorn.NashornScriptEngine;
import org.molgenis.ontology.core.model.OntologyTerm;
import org.molgenis.ontology.core.service.OntologyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;

import static java.time.ZoneId.systemDefault;
import static java.time.ZoneOffset.UTC;
import static java.util.Collections.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.AttributeType.*;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_LABEL;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

@ContextConfiguration(classes = AlgorithmServiceImplIT.Config.class)
public class AlgorithmServiceImplIT extends AbstractMolgenisSpringTest
{
	@Autowired
	private EntityTypeFactory entityTypeFactory;

	@Autowired
	private AttributeFactory attrMetaFactory;

	@Autowired
	private AlgorithmService algorithmService;

	@Autowired
	private DataService dataService;

	@Autowired
	private EntityManager entityManager;

	@Autowired
	private OntologyTagService ontologyTagService;

	@Autowired
	private SemanticSearchService semanticSearchService;

	@Autowired
	private AlgorithmTemplateService algorithmTemplateService;

	@Autowired
	private UserFactory userFactory;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		when(algorithmTemplateService.find(any())).thenReturn(Stream.empty());
	}

	@Test
	public void testGetSourceAttributeNames()
	{
		assertEquals(algorithmService.getSourceAttributeNames("$('id')"), singletonList("id"));
	}

	@Test
	public void testGetSourceAttributeNamesNoQuotes()
	{
		assertEquals(algorithmService.getSourceAttributeNames("$(id)"), singletonList("id"));
	}

	@DataProvider(name = "testApplyProvider")
	public Object[][] testApplyProvider()
	{
		String april20DMY = "2017-04-20";
		LocalDate april20 = LocalDate.parse(april20DMY);
		Instant april20defaultTimezone = april20.atStartOfDay(systemDefault()).toInstant();
		Instant april20UTC = april20.atStartOfDay(UTC).toInstant();
		return new Object[][] { { INT, 25, "$('source').value()", INT, 25, "map INT value" },
				{ LONG, 529387981723498L, "$('source').value()", LONG, 529387981723498L, "map LONG value" },
				{ BOOL, false, "$('source').value()", BOOL, false, "map BOOL value false" },
				{ BOOL, true, "$('source').value()", BOOL, true, "map BOOL value true" },
				{ DATE, april20, "$('source').value()", DATE, april20, "map DATE to DATE" },
				{ DATE, april20, "$('source').value()", DATE_TIME, april20defaultTimezone,
						"DATE should map to DATE_TIME containing start of day in default timezone" },
				{ STRING, april20DMY, "new Date($('source').value())", DATE_TIME, april20UTC,
						"ymd STRING to DATE_TIME parsed by JavaScript returns start of day in UTC (BEWARE!)" },
				{ STRING, april20DMY,
						"var date = $('source').value().split('-'); new Date(date[0], date[1]-1, date[2])", DATE_TIME,
						april20defaultTimezone,
						"ymd STRING to DATE_TIME parsed manually returns start of day in default timezone" },
				{ STRING, april20DMY,
						"var date = $('source').value().split('-'); new Date(date[0], date[1]-1, date[2])", DATE,
						april20, "ymd STRING to DATE parsed manually returns correct day" },
				{ STRING, april20DMY, "new Date($('source').value())", DATE,
						april20UTC.atZone(ZoneId.systemDefault()).toLocalDate(),
						"ymd STRING to DATE parsed by JavaScript returns wrong day, depending on default timezone (BEWARE!)" },
				{ STRING, april20.toString(),
						"var date = $('source').value().split('-'); new Date(date[0], date[1]-1, date[2])", DATE_TIME,
						april20defaultTimezone,
						"ymd STRING to DATE_TIME parsed manually returns start of day in system default timezone" },
				{ DATE_TIME, april20defaultTimezone, "$('source').value()", DATE, april20,
						"DATE_TIME to DATE returns day in default timeZone" },
				{ DATE_TIME, april20UTC, "$('source').value()", LONG, april20UTC.toEpochMilli(),
						"DATE_TIME to LONG returns epoch millis" },
				{ DATE, april20, "$('source').value()", LONG, april20defaultTimezone.toEpochMilli(),
						"DATE to LONG returns epoch millis at start of day in default timezone" } };
	}

	@Test(dataProvider = "testApplyProvider")
	public void testApply(AttributeType sourceAttributeType, Object sourceAttributeValue, String algorithm,
			AttributeType targetAttributeType, Object expected, String message)
	{
		String idAttrName = "id";
		EntityType entityType = entityTypeFactory.create("LL");
		entityType.addAttribute(attrMetaFactory.create().setName(idAttrName).setDataType(INT), ROLE_ID);
		entityType.addAttribute(attrMetaFactory.create().setName("source").setDataType(sourceAttributeType));
		Entity source = new DynamicEntity(entityType);
		source.set(idAttrName, 1);
		source.set("source", sourceAttributeValue);

		Attribute targetAttribute = attrMetaFactory.create().setName("target");
		targetAttribute.setDataType(targetAttributeType);
		AttributeMapping attributeMapping = new AttributeMapping(targetAttribute);
		attributeMapping.setAlgorithm(algorithm);
		Object result = algorithmService.apply(attributeMapping, source, entityType);
		assertEquals(result, expected, message);
	}

	@Test
	public void testGetAgeScript() throws ParseException
	{
		String idAttrName = "id";
		EntityType entityType = entityTypeFactory.create("LL");
		entityType.addAttribute(attrMetaFactory.create().setName(idAttrName).setDataType(INT), ROLE_ID);
		entityType.addAttribute(attrMetaFactory.create().setName("dob").setDataType(DATE));
		Entity source = new DynamicEntity(entityType);
		source.set(idAttrName, 1);
		source.set("dob", LocalDate.of(1973, Month.AUGUST, 28));

		Attribute targetAttribute = attrMetaFactory.create().setName("age");
		targetAttribute.setDataType(INT);
		AttributeMapping attributeMapping = new AttributeMapping(targetAttribute);
		attributeMapping.setAlgorithm(
				"Math.floor((new Date(2015, 2, 12) - $('dob').value())/(365.2425 * 24 * 60 * 60 * 1000))");
		Object result = algorithmService.apply(attributeMapping, source, entityType);
		assertEquals(result, 41);
	}

	@Test
	public void testGetXrefScript() throws ParseException
	{
		// xref entities
		EntityType entityTypeXref = entityTypeFactory.create("xrefEntity1");
		entityTypeXref.addAttribute(attrMetaFactory.create().setName("id").setDataType(INT), ROLE_ID);
		entityTypeXref.addAttribute(attrMetaFactory.create().setName("field1"));
		Entity xref1a = new DynamicEntity(entityTypeXref);
		xref1a.set("id", 1);
		xref1a.set("field1", "Test");

		EntityType entityTypeXref2 = entityTypeFactory.create("xrefEntity2");
		entityTypeXref2.addAttribute(attrMetaFactory.create().setName("id").setDataType(INT), ROLE_ID);
		entityTypeXref2.addAttribute(attrMetaFactory.create().setName("field2"));
		Entity xref2a = new DynamicEntity(entityTypeXref2);
		xref2a.set("id", 2);
		xref2a.set("field2", "Test");

		// source Entity
		EntityType entityTypeSource = entityTypeFactory.create("Source");
		entityTypeSource.addAttribute(attrMetaFactory.create().setName("id").setDataType(INT), ROLE_ID);
		entityTypeSource.addAttribute(attrMetaFactory.create().setName("xref").setDataType(XREF));
		Entity source = new DynamicEntity(entityTypeSource);
		source.set("id", 1);
		source.set("xref", xref2a);

		Attribute targetAttribute = attrMetaFactory.create().setName("field1");
		targetAttribute.setDataType(XREF);
		targetAttribute.setRefEntity(entityTypeXref);
		AttributeMapping attributeMapping = new AttributeMapping(targetAttribute);
		attributeMapping.setAlgorithm("$('xref').map({'1':'2', '2':'1'}).value();");
		when(entityManager.getReference(entityTypeXref, 1)).thenReturn(xref1a);
		Entity result = (Entity) algorithmService.apply(attributeMapping, source, entityTypeSource);
		assertEquals(result.get("field1"), xref2a.get("field2"));
	}

	@Test
	public void testApplyMref() throws ParseException
	{
		String refEntityName = "refEntity";
		String refEntityIdAttrName = "id";
		String refEntityLabelAttrName = "label";

		String refEntityId0 = "id0";
		String refEntityId1 = "id1";

		String sourceEntityName = "source";
		String sourceEntityAttrName = "mref-source";
		String targetEntityAttrName = "mref-target";

		// ref entities
		EntityType refEntityType = entityTypeFactory.create(refEntityName);
		refEntityType.addAttribute(attrMetaFactory.create().setName(refEntityIdAttrName), ROLE_ID);
		refEntityType.addAttribute(attrMetaFactory.create().setName(refEntityLabelAttrName).setDataType(STRING),
				ROLE_LABEL);

		Entity refEntity0 = new DynamicEntity(refEntityType);
		refEntity0.set(refEntityIdAttrName, refEntityId0);
		refEntity0.set(refEntityLabelAttrName, "label0");

		Entity refEntity1 = new DynamicEntity(refEntityType);
		refEntity1.set(refEntityIdAttrName, refEntityId1);
		refEntity1.set(refEntityLabelAttrName, "label1");

		// mapping
		Attribute targetAttribute = attrMetaFactory.create().setName(targetEntityAttrName);
		targetAttribute.setDataType(MREF).setNillable(false).setRefEntity(refEntityType);
		AttributeMapping attributeMapping = new AttributeMapping(targetAttribute);
		attributeMapping.setAlgorithm("$('" + sourceEntityAttrName + "').value()");

		when(entityManager.getReference(refEntityType, refEntityId0)).thenReturn(refEntity0);
		when(entityManager.getReference(refEntityType, refEntityId1)).thenReturn(refEntity1);

		// source Entity
		EntityType entityTypeSource = entityTypeFactory.create(sourceEntityName);
		entityTypeSource.addAttribute(
				attrMetaFactory.create().setName(refEntityIdAttrName).setDataType(INT).setAuto(true), ROLE_ID);
		entityTypeSource.addAttribute(attrMetaFactory.create()
													 .setName(sourceEntityAttrName)
													 .setDataType(MREF)
													 .setNillable(false)
													 .setRefEntity(refEntityType));
		Entity source = new DynamicEntity(entityTypeSource);
		source.set(sourceEntityAttrName, Arrays.asList(refEntity0, refEntity1));

		Object result = algorithmService.apply(attributeMapping, source, entityTypeSource);
		assertEquals(result, Arrays.asList(refEntity0, refEntity1));
	}

	@Test
	public void testApplyMrefNillable() throws ParseException
	{
		String refEntityName = "refEntity";
		String refEntityIdAttrName = "id";
		String refEntityLabelAttrName = "label";

		String sourceEntityName = "source";
		String sourceEntityAttrName = "mref-source";
		String targetEntityAttrName = "mref-target";

		// ref entities
		EntityType refEntityType = entityTypeFactory.create(refEntityName);
		refEntityType.addAttribute(attrMetaFactory.create().setName(refEntityIdAttrName), ROLE_ID);
		refEntityType.addAttribute(attrMetaFactory.create().setName(refEntityLabelAttrName), ROLE_LABEL);

		// mapping
		Attribute targetAttribute = attrMetaFactory.create().setName(targetEntityAttrName);
		targetAttribute.setDataType(MREF).setNillable(true).setRefEntity(refEntityType);
		AttributeMapping attributeMapping = new AttributeMapping(targetAttribute);
		attributeMapping.setAlgorithm("$('" + sourceEntityAttrName + "').value()");

		// source Entity
		EntityType entityTypeSource = entityTypeFactory.create(sourceEntityName);
		entityTypeSource.addAttribute(
				attrMetaFactory.create().setName(refEntityIdAttrName).setDataType(INT).setAuto(true), ROLE_ID);
		entityTypeSource.addAttribute(attrMetaFactory.create()
													 .setName(sourceEntityAttrName)
													 .setDataType(MREF)
													 .setNillable(true)
													 .setRefEntity(refEntityType));

		Entity source = new DynamicEntity(entityTypeSource);
		source.set(sourceEntityAttrName, emptyList());

		Object result = algorithmService.apply(attributeMapping, source, entityTypeSource);
		assertEquals(result, emptyList());
	}

	@Test
	public void testCreateAttributeMappingIfOnlyOneMatch()
	{
		EntityType targetEntityType = entityTypeFactory.create("target");
		Attribute targetAttribute = attrMetaFactory.create().setName("targetHeight");
		targetAttribute.setDescription("height");
		targetEntityType.addAttribute(targetAttribute);

		EntityType sourceEntityType = entityTypeFactory.create("source");
		Attribute sourceAttribute = attrMetaFactory.create().setName("sourceHeight");
		sourceAttribute.setDescription("height");
		sourceEntityType.addAttribute(sourceAttribute);

		User owner = userFactory.create();
		owner.setUsername("flup");
		owner.setPassword("geheim");
		owner.setId("12345");
		owner.setActive(true);
		owner.setEmail("flup@blah.com");
		owner.setFirstName("Flup");
		owner.setLastName("de Flap");

		MappingProject project = new MappingProject("project", owner);
		project.addTarget(targetEntityType);

		EntityMapping mapping = project.getMappingTarget("target").addSource(sourceEntityType);

		Map<Attribute, ExplainedAttribute> matches = ImmutableMap.of(sourceAttribute,
				ExplainedAttribute.create(sourceAttribute,
						singletonList(ExplainedQueryString.create("height", "height", "height", 100)), true));

		LinkedHashMultimap<Relation, OntologyTerm> ontologyTermTags = LinkedHashMultimap.create();

		when(semanticSearchService.decisionTreeToFindRelevantAttributes(sourceEntityType, targetAttribute,
				ontologyTermTags.values(), null)).thenReturn(matches);

		when(ontologyTagService.getTagsForAttribute(targetEntityType, targetAttribute)).thenReturn(ontologyTermTags);

		algorithmService.autoGenerateAlgorithm(sourceEntityType, targetEntityType, mapping, targetAttribute);

		assertEquals(mapping.getAttributeMapping("targetHeight").getAlgorithm(), "$('sourceHeight').value();");
	}

	@Test
	public void testWhenSourceDoesNotMatchThenNoMappingGetsCreated()
	{
		EntityType targetEntityType = entityTypeFactory.create("target");
		Attribute targetAttribute = attrMetaFactory.create().setName("targetHeight");
		targetAttribute.setDescription("height");
		targetEntityType.addAttribute(targetAttribute);

		EntityType sourceEntityType = entityTypeFactory.create("source");
		Attribute sourceAttribute = attrMetaFactory.create().setName("sourceHeight");
		sourceAttribute.setDescription("weight");
		sourceEntityType.addAttribute(sourceAttribute);

		User owner = userFactory.create();
		owner.setUsername("flup");
		owner.setPassword("geheim");
		owner.setId("12345");
		owner.setActive(true);
		owner.setEmail("flup@blah.com");
		owner.setFirstName("Flup");
		owner.setLastName("de Flap");

		MappingProject project = new MappingProject("project", owner);
		project.addTarget(targetEntityType);

		EntityMapping mapping = project.getMappingTarget("target").addSource(sourceEntityType);

		when(semanticSearchService.findAttributes(sourceEntityType, Sets.newHashSet("targetHeight", "height"),
				Collections.emptyList())).thenReturn(emptyMap());

		when(ontologyTagService.getTagsForAttribute(targetEntityType, targetAttribute)).thenReturn(
				LinkedHashMultimap.create());

		algorithmService.autoGenerateAlgorithm(sourceEntityType, targetEntityType, mapping, targetAttribute);

		assertNull(mapping.getAttributeMapping("targetHeight"));
	}

	@Test
	public void testWhenSourceHasMultipleMatchesThenFirstMappingGetsCreated()
	{
		EntityType targetEntityType = entityTypeFactory.create("target");
		Attribute targetAttribute = attrMetaFactory.create().setName("targetHeight");
		targetAttribute.setDescription("height");
		targetEntityType.addAttribute(targetAttribute);

		EntityType sourceEntityType = entityTypeFactory.create("source");
		Attribute sourceAttribute1 = attrMetaFactory.create().setName("sourceHeight1");
		sourceAttribute1.setDescription("height");
		Attribute sourceAttribute2 = attrMetaFactory.create().setName("sourceHeight2");
		sourceAttribute2.setDescription("height");

		sourceEntityType.addAttributes(Arrays.asList(sourceAttribute1, sourceAttribute2));

		User owner = userFactory.create();
		owner.setUsername("flup");
		owner.setPassword("geheim");
		owner.setId("12345");
		owner.setActive(true);
		owner.setEmail("flup@blah.com");
		owner.setFirstName("Flup");
		owner.setLastName("de Flap");

		MappingProject project = new MappingProject("project", owner);
		project.addTarget(targetEntityType);

		EntityMapping mapping = project.getMappingTarget("target").addSource(sourceEntityType);

		Map<Attribute, ExplainedAttribute> mappings = ImmutableMap.of(sourceAttribute1,
				ExplainedAttribute.create(sourceAttribute1), sourceAttribute2,
				ExplainedAttribute.create(sourceAttribute2));

		LinkedHashMultimap<Relation, OntologyTerm> ontologyTermTags = LinkedHashMultimap.create();

		when(semanticSearchService.decisionTreeToFindRelevantAttributes(sourceEntityType, targetAttribute,
				ontologyTermTags.values(), null)).thenReturn(mappings);

		when(ontologyTagService.getTagsForAttribute(targetEntityType, targetAttribute)).thenReturn(ontologyTermTags);

		algorithmService.autoGenerateAlgorithm(sourceEntityType, targetEntityType, mapping, targetAttribute);

		assertEquals(mapping.getAttributeMapping("targetHeight").getSourceAttributes().get(0), sourceAttribute1);
	}

	@Configuration
	@Import(UserTestConfig.class)
	public static class Config
	{
		@Autowired
		private DataService dataService;

		@Bean
		public SemanticSearchService semanticSearchService()
		{
			return mock(SemanticSearchService.class);
		}

		@Bean
		public UnitResolver unitResolver()
		{
			return new UnitResolverImpl(ontologyService());
		}

		@Bean
		public EntityManager entityManager()
		{
			return mock(EntityManager.class);
		}

		@Bean
		public JsMagmaScriptEvaluator jsScriptEvaluator()
		{
			return new JsMagmaScriptEvaluator(new NashornScriptEngine());
		}

		@Bean
		public AlgorithmService algorithmService()
		{
			return new AlgorithmServiceImpl(ontologyTagService(), semanticSearchService(), algorithmGeneratorService(),
					entityManager(), jsScriptEvaluator());
		}

		@Bean
		public AlgorithmTemplateService algorithmTemplateService()
		{
			return mock(AlgorithmTemplateServiceImpl.class);
		}

		@Bean
		public OntologyService ontologyService()
		{
			return mock(OntologyService.class);
		}

		@Bean
		public TagRepository tagRepository()
		{
			return mock(TagRepository.class);
		}

		@Bean
		IdGenerator idGenerator()
		{
			return mock(IdGenerator.class);
		}

		@Bean
		public OntologyTagService ontologyTagService()
		{
			return mock(OntologyTagService.class);
		}

		@Bean
		public AlgorithmGeneratorService algorithmGeneratorService()
		{
			return new AlgorithmGeneratorServiceImpl(dataService, unitResolver(), algorithmTemplateService());
		}
	}
}

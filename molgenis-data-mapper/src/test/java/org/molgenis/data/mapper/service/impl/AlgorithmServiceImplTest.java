package org.molgenis.data.mapper.service.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Sets;
import org.mockito.ArgumentMatcher;
import org.mockito.Matchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.molgenis.auth.User;
import org.molgenis.auth.UserFactory;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.populate.IdGenerator;
import org.molgenis.data.mapper.algorithmgenerator.service.AlgorithmGeneratorService;
import org.molgenis.data.mapper.algorithmgenerator.service.impl.AlgorithmGeneratorServiceImpl;
import org.molgenis.data.mapper.mapping.model.AttributeMapping;
import org.molgenis.data.mapper.mapping.model.EntityMapping;
import org.molgenis.data.mapper.mapping.model.MappingProject;
import org.molgenis.data.mapper.service.AlgorithmService;
import org.molgenis.data.mapper.service.UnitResolver;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.AttributeMetaDataFactory;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.semantic.Relation;
import org.molgenis.data.semanticsearch.explain.bean.ExplainedAttributeMetaData;
import org.molgenis.data.semanticsearch.explain.bean.ExplainedQueryString;
import org.molgenis.data.semanticsearch.repository.TagRepository;
import org.molgenis.data.semanticsearch.service.OntologyTagService;
import org.molgenis.data.semanticsearch.service.SemanticSearchService;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.ontology.core.model.OntologyTerm;
import org.molgenis.ontology.core.service.OntologyService;
import org.molgenis.test.data.AbstractMolgenisSpringTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.MolgenisFieldTypes.AttributeType.*;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_LABEL;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

@ContextConfiguration(classes = AlgorithmServiceImplTest.Config.class)
public class AlgorithmServiceImplTest extends AbstractMolgenisSpringTest
{
	@Autowired
	private EntityTypeFactory entityTypeFactory;

	@Autowired
	private AttributeMetaDataFactory attrMetaFactory;

	@Autowired
	private AlgorithmService algorithmService;

	@Autowired
	private DataService dataService;

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
		when(algorithmTemplateService.find(Matchers.any())).thenReturn(Stream.empty());
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

	@Test
	public void testInt() throws ParseException
	{
		String identifier = "id";
		String sourceIntAttribute = "age";

		EntityType entityType = entityTypeFactory.create("testInt");
		entityType.addAttribute(attrMetaFactory.create().setName(identifier).setDataType(INT), ROLE_ID);
		entityType.addAttribute(attrMetaFactory.create().setName(sourceIntAttribute).setDataType(INT));

		Entity source = new DynamicEntity(entityType);
		source.set(identifier, 1);
		source.set(sourceIntAttribute, 25);

		String targetIntAttribute = "years_lived";

		AttributeMetaData targetAttributeMetaData = attrMetaFactory.create().setName(targetIntAttribute)
				.setDataType(INT);
		AttributeMapping attributeMapping = new AttributeMapping(targetAttributeMetaData);
		attributeMapping.setAlgorithm("$('age').value()");

		Object result = algorithmService.apply(attributeMapping, source, entityType);
		assertEquals(result, 25);
	}

	@Test
	public void testBool() throws ParseException
	{
		String identifier = "id";
		String sourceBoolAttribute = "has_had_coffee";

		EntityType entityType = entityTypeFactory.create("testInt");
		entityType.addAttribute(attrMetaFactory.create().setName(identifier).setDataType(INT), ROLE_ID);
		entityType.addAttribute(attrMetaFactory.create().setName(sourceBoolAttribute).setDataType(BOOL));

		Entity source = new DynamicEntity(entityType);
		source.set(identifier, 1);
		source.set(sourceBoolAttribute, false);

		String targetBoolAttribute = "awake";

		AttributeMetaData targetAttributeMetaData = attrMetaFactory.create().setName(targetBoolAttribute)
				.setDataType(BOOL);
		AttributeMapping attributeMapping = new AttributeMapping(targetAttributeMetaData);
		attributeMapping.setAlgorithm("$('has_had_coffee').value()");

		Object result = algorithmService.apply(attributeMapping, source, entityType);
		assertEquals(result, false);
	}

	@Test
	public void testLong() throws ParseException
	{
		String identifier = "id";
		String sourceLongAttribute = "serial_number";

		EntityType entityType = entityTypeFactory.create("testInt");
		entityType.addAttribute(attrMetaFactory.create().setName(identifier).setDataType(INT), ROLE_ID);
		entityType.addAttribute(attrMetaFactory.create().setName(sourceLongAttribute).setDataType(LONG));

		Entity source = new DynamicEntity(entityType);
		source.set(identifier, 1);
		source.set(sourceLongAttribute, 529387981723498l);

		String targetLongAttribute = "super_id_code";

		AttributeMetaData targetAttributeMetaData = attrMetaFactory.create().setName(targetLongAttribute)
				.setDataType(LONG);
		AttributeMapping attributeMapping = new AttributeMapping(targetAttributeMetaData);
		attributeMapping.setAlgorithm("$('serial_number').value()");

		Object result = algorithmService.apply(attributeMapping, source, entityType);
		assertEquals(result, 529387981723498l);
	}

	@Test
	public void testDate() throws ParseException
	{
		String idAttrName = "id";
		EntityType entityType = entityTypeFactory.create("LL");
		entityType.addAttribute(attrMetaFactory.create().setName(idAttrName).setDataType(INT), ROLE_ID);
		entityType.addAttribute(attrMetaFactory.create().setName("dob").setDataType(DATE));
		Entity source = new DynamicEntity(entityType);
		source.set(idAttrName, 1);
		source.set("dob", new SimpleDateFormat("dd-MM-yyyy").parse("13-05-2015"));

		AttributeMetaData targetAttributeMetaData = attrMetaFactory.create().setName("bob");
		targetAttributeMetaData.setDataType(DATE);
		AttributeMapping attributeMapping = new AttributeMapping(targetAttributeMetaData);
		attributeMapping.setAlgorithm("$('dob').value()");
		Object result = algorithmService.apply(attributeMapping, source, entityType);
		assertEquals(result.toString(), "Wed May 13 00:00:00 CEST 2015");
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
		source.set("dob", new SimpleDateFormat("dd-MM-yyyy").parse("28-08-1973"));

		AttributeMetaData targetAttributeMetaData = attrMetaFactory.create().setName("age");
		targetAttributeMetaData.setDataType(INT);
		AttributeMapping attributeMapping = new AttributeMapping(targetAttributeMetaData);
		attributeMapping.setAlgorithm(
				"Math.floor((new Date('02/12/2015') - $('dob').value())/(365.2425 * 24 * 60 * 60 * 1000))");
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

		AttributeMetaData targetAttributeMetaData = attrMetaFactory.create().setName("field1");
		targetAttributeMetaData.setDataType(XREF);
		targetAttributeMetaData.setRefEntity(entityTypeXref);
		AttributeMapping attributeMapping = new AttributeMapping(targetAttributeMetaData);
		attributeMapping.setAlgorithm("$('xref').map({'1':'2', '2':'1'}).value();");
		when(dataService.findOneById("xrefEntity1", "1")).thenReturn(xref1a);
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
		refEntityType
				.addAttribute(attrMetaFactory.create().setName(refEntityLabelAttrName).setDataType(STRING), ROLE_LABEL);

		Entity refEntity0 = new DynamicEntity(refEntityType);
		refEntity0.set(refEntityIdAttrName, refEntityId0);
		refEntity0.set(refEntityLabelAttrName, "label0");

		Entity refEntity1 = new DynamicEntity(refEntityType);
		refEntity1.set(refEntityIdAttrName, refEntityId1);
		refEntity1.set(refEntityLabelAttrName, "label1");

		// mapping
		AttributeMetaData targetAttributeMetaData = attrMetaFactory.create().setName(targetEntityAttrName);
		targetAttributeMetaData.setDataType(MREF).setNillable(false).setRefEntity(refEntityType);
		AttributeMapping attributeMapping = new AttributeMapping(targetAttributeMetaData);
		attributeMapping.setAlgorithm("$('" + sourceEntityAttrName + "').value()");

		when(dataService.findAll(eq(refEntityName), argThat(new ArgumentMatcher<Stream<Object>>()
		{
			@SuppressWarnings("unchecked")
			@Override
			public boolean matches(Object argument)
			{
				return ((Stream<Object>) argument).collect(toList()).equals(Arrays.asList(refEntityId0, refEntityId1));
			}
		}))).thenAnswer(new Answer<Stream<Entity>>()
		{
			@Override
			public Stream<Entity> answer(InvocationOnMock invocation) throws Throwable
			{
				return Stream.of(refEntity0, refEntity1);
			}
		});

		// source Entity
		EntityType entityTypeSource = entityTypeFactory.create(sourceEntityName);
		entityTypeSource
				.addAttribute(attrMetaFactory.create().setName(refEntityIdAttrName).setDataType(INT).setAuto(true),
						ROLE_ID);
		entityTypeSource.addAttribute(
				attrMetaFactory.create().setName(sourceEntityAttrName).setDataType(MREF).setNillable(false)
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
		AttributeMetaData targetAttributeMetaData = attrMetaFactory.create().setName(targetEntityAttrName);
		targetAttributeMetaData.setDataType(MREF).setNillable(true).setRefEntity(refEntityType);
		AttributeMapping attributeMapping = new AttributeMapping(targetAttributeMetaData);
		attributeMapping.setAlgorithm("$('" + sourceEntityAttrName + "').value()");

		// source Entity
		EntityType entityTypeSource = entityTypeFactory.create(sourceEntityName);
		entityTypeSource
				.addAttribute(attrMetaFactory.create().setName(refEntityIdAttrName).setDataType(INT).setAuto(true),
						ROLE_ID);
		entityTypeSource.addAttribute(
				attrMetaFactory.create().setName(sourceEntityAttrName).setDataType(MREF).setNillable(true)
						.setRefEntity(refEntityType));

		Entity source = new DynamicEntity(entityTypeSource);
		source.set(sourceEntityAttrName, null);

		Object result = algorithmService.apply(attributeMapping, source, entityTypeSource);
		assertNull(result);
	}

	@Test
	public void testCreateAttributeMappingIfOnlyOneMatch()
	{
		EntityType targetEntityType = entityTypeFactory.create("target");
		AttributeMetaData targetAttribute = attrMetaFactory.create().setName("targetHeight");
		targetAttribute.setDescription("height");
		targetEntityType.addAttribute(targetAttribute);

		EntityType sourceEntityType = entityTypeFactory.create("source");
		AttributeMetaData sourceAttribute = attrMetaFactory.create().setName("sourceHeight");
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

		Map<AttributeMetaData, ExplainedAttributeMetaData> matches = ImmutableMap.of(sourceAttribute,
				ExplainedAttributeMetaData.create(sourceAttribute,
						singletonList(ExplainedQueryString.create("height", "height", "height", 100)), true));

		LinkedHashMultimap<Relation, OntologyTerm> ontologyTermTags = LinkedHashMultimap.create();

		when(semanticSearchService
				.decisionTreeToFindRelevantAttributes(sourceEntityType, targetAttribute, ontologyTermTags.values(),
						null)).thenReturn(matches);

		when(ontologyTagService.getTagsForAttribute(targetEntityType, targetAttribute))
				.thenReturn(ontologyTermTags);

		algorithmService.autoGenerateAlgorithm(sourceEntityType, targetEntityType, mapping, targetAttribute);

		assertEquals(mapping.getAttributeMapping("targetHeight").getAlgorithm(), "$('sourceHeight').value();");
	}

	@Test
	public void testWhenSourceDoesNotMatchThenNoMappingGetsCreated()
	{
		EntityType targetEntityType = entityTypeFactory.create("target");
		AttributeMetaData targetAttribute = attrMetaFactory.create().setName("targetHeight");
		targetAttribute.setDescription("height");
		targetEntityType.addAttribute(targetAttribute);

		EntityType sourceEntityType = entityTypeFactory.create("source");
		AttributeMetaData sourceAttribute = attrMetaFactory.create().setName("sourceHeight");
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

		when(ontologyTagService.getTagsForAttribute(targetEntityType, targetAttribute))
				.thenReturn(LinkedHashMultimap.create());

		algorithmService.autoGenerateAlgorithm(sourceEntityType, targetEntityType, mapping, targetAttribute);

		assertNull(mapping.getAttributeMapping("targetHeight"));
	}

	@Test
	public void testWhenSourceHasMultipleMatchesThenFirstMappingGetsCreated()
	{
		EntityType targetEntityType = entityTypeFactory.create("target");
		AttributeMetaData targetAttribute = attrMetaFactory.create().setName("targetHeight");
		targetAttribute.setDescription("height");
		targetEntityType.addAttribute(targetAttribute);

		EntityType sourceEntityType = entityTypeFactory.create("source");
		AttributeMetaData sourceAttribute1 = attrMetaFactory.create().setName("sourceHeight1");
		sourceAttribute1.setDescription("height");
		AttributeMetaData sourceAttribute2 = attrMetaFactory.create().setName("sourceHeight2");
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

		Map<AttributeMetaData, ExplainedAttributeMetaData> mappings = ImmutableMap
				.of(sourceAttribute1, ExplainedAttributeMetaData.create(sourceAttribute1), sourceAttribute2,
						ExplainedAttributeMetaData.create(sourceAttribute2));

		LinkedHashMultimap<Relation, OntologyTerm> ontologyTermTags = LinkedHashMultimap.create();

		when(semanticSearchService
				.decisionTreeToFindRelevantAttributes(sourceEntityType, targetAttribute, ontologyTermTags.values(),
						null)).thenReturn(mappings);

		when(ontologyTagService.getTagsForAttribute(targetEntityType, targetAttribute))
				.thenReturn(ontologyTermTags);

		algorithmService.autoGenerateAlgorithm(sourceEntityType, targetEntityType, mapping, targetAttribute);

		assertEquals(mapping.getAttributeMapping("targetHeight").getSourceAttributeMetaDatas().get(0),
				sourceAttribute1);
	}

	@Configuration
	@ComponentScan({ "org.molgenis.data.mapper.meta", "org.molgenis.auth" })
	public static class Config
	{
		@Bean
		public DataService dataService()
		{
			return mock(DataService.class);
		}

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
		public AlgorithmService algorithmService()
		{
			return new AlgorithmServiceImpl(dataService(), ontologyTagService(), semanticSearchService(),
					algorithmGeneratorService());
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
			return new AlgorithmGeneratorServiceImpl(dataService(), unitResolver(), algorithmTemplateService());
		}
	}
}

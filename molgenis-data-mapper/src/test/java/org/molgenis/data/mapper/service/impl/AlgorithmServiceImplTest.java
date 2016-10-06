package org.molgenis.data.mapper.service.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Sets;
import org.mockito.ArgumentMatcher;
import org.mockito.Matchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.molgenis.auth.MolgenisUser;
import org.molgenis.auth.MolgenisUserFactory;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.mapper.algorithmgenerator.service.AlgorithmGeneratorService;
import org.molgenis.data.mapper.algorithmgenerator.service.impl.AlgorithmGeneratorServiceImpl;
import org.molgenis.data.mapper.mapping.model.AttributeMapping;
import org.molgenis.data.mapper.mapping.model.EntityMapping;
import org.molgenis.data.mapper.mapping.model.MappingProject;
import org.molgenis.data.mapper.service.AlgorithmService;
import org.molgenis.data.mapper.service.UnitResolver;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.AttributeMetaDataFactory;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.meta.model.EntityMetaDataFactory;
import org.molgenis.data.populate.IdGenerator;
import org.molgenis.data.semantic.Relation;
import org.molgenis.data.semanticsearch.explain.bean.ExplainedMatchCandidate;
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
import static org.molgenis.data.meta.model.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.EntityMetaData.AttributeRole.ROLE_LABEL;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

@ContextConfiguration(classes = AlgorithmServiceImplTest.Config.class)
public class AlgorithmServiceImplTest extends AbstractMolgenisSpringTest
{
	@Autowired
	private EntityMetaDataFactory entityMetaFactory;

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
	private MolgenisUserFactory molgenisUserFactory;

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

		EntityMetaData entityMetaData = entityMetaFactory.create("testInt");
		entityMetaData.addAttribute(attrMetaFactory.create().setName(identifier).setDataType(INT), ROLE_ID);
		entityMetaData.addAttribute(attrMetaFactory.create().setName(sourceIntAttribute).setDataType(INT));

		Entity source = new DynamicEntity(entityMetaData);
		source.set(identifier, 1);
		source.set(sourceIntAttribute, 25);

		String targetIntAttribute = "years_lived";

		AttributeMetaData targetAttributeMetaData = attrMetaFactory.create().setName(targetIntAttribute)
				.setDataType(INT);
		AttributeMapping attributeMapping = new AttributeMapping(targetAttributeMetaData);
		attributeMapping.setAlgorithm("$('age').value()");

		Object result = algorithmService.apply(attributeMapping, source, entityMetaData);
		assertEquals(result, 25);
	}

	@Test
	public void testBool() throws ParseException
	{
		String identifier = "id";
		String sourceBoolAttribute = "has_had_coffee";

		EntityMetaData entityMetaData = entityMetaFactory.create("testInt");
		entityMetaData.addAttribute(attrMetaFactory.create().setName(identifier).setDataType(INT), ROLE_ID);
		entityMetaData.addAttribute(attrMetaFactory.create().setName(sourceBoolAttribute).setDataType(BOOL));

		Entity source = new DynamicEntity(entityMetaData);
		source.set(identifier, 1);
		source.set(sourceBoolAttribute, false);

		String targetBoolAttribute = "awake";

		AttributeMetaData targetAttributeMetaData = attrMetaFactory.create().setName(targetBoolAttribute)
				.setDataType(BOOL);
		AttributeMapping attributeMapping = new AttributeMapping(targetAttributeMetaData);
		attributeMapping.setAlgorithm("$('has_had_coffee').value()");

		Object result = algorithmService.apply(attributeMapping, source, entityMetaData);
		assertEquals(result, false);
	}

	@Test
	public void testLong() throws ParseException
	{
		String identifier = "id";
		String sourceLongAttribute = "serial_number";

		EntityMetaData entityMetaData = entityMetaFactory.create("testInt");
		entityMetaData.addAttribute(attrMetaFactory.create().setName(identifier).setDataType(INT), ROLE_ID);
		entityMetaData.addAttribute(attrMetaFactory.create().setName(sourceLongAttribute).setDataType(LONG));

		Entity source = new DynamicEntity(entityMetaData);
		source.set(identifier, 1);
		source.set(sourceLongAttribute, 529387981723498l);

		String targetLongAttribute = "super_id_code";

		AttributeMetaData targetAttributeMetaData = attrMetaFactory.create().setName(targetLongAttribute)
				.setDataType(LONG);
		AttributeMapping attributeMapping = new AttributeMapping(targetAttributeMetaData);
		attributeMapping.setAlgorithm("$('serial_number').value()");

		Object result = algorithmService.apply(attributeMapping, source, entityMetaData);
		assertEquals(result, 529387981723498l);
	}

	@Test
	public void testDate() throws ParseException
	{
		String idAttrName = "id";
		EntityMetaData entityMetaData = entityMetaFactory.create("LL");
		entityMetaData.addAttribute(attrMetaFactory.create().setName(idAttrName).setDataType(INT), ROLE_ID);
		entityMetaData.addAttribute(attrMetaFactory.create().setName("dob").setDataType(DATE));
		Entity source = new DynamicEntity(entityMetaData);
		source.set(idAttrName, 1);
		source.set("dob", new SimpleDateFormat("dd-MM-yyyy").parse("13-05-2015"));

		AttributeMetaData targetAttributeMetaData = attrMetaFactory.create().setName("bob");
		targetAttributeMetaData.setDataType(DATE);
		AttributeMapping attributeMapping = new AttributeMapping(targetAttributeMetaData);
		attributeMapping.setAlgorithm("$('dob').value()");
		Object result = algorithmService.apply(attributeMapping, source, entityMetaData);
		assertEquals(result.toString(), "Wed May 13 00:00:00 CEST 2015");
	}

	@Test
	public void testGetAgeScript() throws ParseException
	{
		String idAttrName = "id";
		EntityMetaData entityMetaData = entityMetaFactory.create("LL");
		entityMetaData.addAttribute(attrMetaFactory.create().setName(idAttrName).setDataType(INT), ROLE_ID);
		entityMetaData.addAttribute(attrMetaFactory.create().setName("dob").setDataType(DATE));
		Entity source = new DynamicEntity(entityMetaData);
		source.set(idAttrName, 1);
		source.set("dob", new SimpleDateFormat("dd-MM-yyyy").parse("28-08-1973"));

		AttributeMetaData targetAttributeMetaData = attrMetaFactory.create().setName("age");
		targetAttributeMetaData.setDataType(INT);
		AttributeMapping attributeMapping = new AttributeMapping(targetAttributeMetaData);
		attributeMapping.setAlgorithm(
				"Math.floor((new Date('02/12/2015') - $('dob').value())/(365.2425 * 24 * 60 * 60 * 1000))");
		Object result = algorithmService.apply(attributeMapping, source, entityMetaData);
		assertEquals(result, 41);
	}

	@Test
	public void testGetXrefScript() throws ParseException
	{
		// xref entities
		EntityMetaData entityMetaDataXref = entityMetaFactory.create("xrefEntity1");
		entityMetaDataXref.addAttribute(attrMetaFactory.create().setName("id").setDataType(INT), ROLE_ID);
		entityMetaDataXref.addAttribute(attrMetaFactory.create().setName("field1"));
		Entity xref1a = new DynamicEntity(entityMetaDataXref);
		xref1a.set("id", 1);
		xref1a.set("field1", "Test");

		EntityMetaData entityMetaDataXref2 = entityMetaFactory.create("xrefEntity2");
		entityMetaDataXref2.addAttribute(attrMetaFactory.create().setName("id").setDataType(INT), ROLE_ID);
		entityMetaDataXref2.addAttribute(attrMetaFactory.create().setName("field2"));
		Entity xref2a = new DynamicEntity(entityMetaDataXref2);
		xref2a.set("id", 2);
		xref2a.set("field2", "Test");

		// source Entity
		EntityMetaData entityMetaDataSource = entityMetaFactory.create("Source");
		entityMetaDataSource.addAttribute(attrMetaFactory.create().setName("id").setDataType(INT), ROLE_ID);
		entityMetaDataSource.addAttribute(attrMetaFactory.create().setName("xref").setDataType(XREF));
		Entity source = new DynamicEntity(entityMetaDataSource);
		source.set("id", 1);
		source.set("xref", xref2a);

		AttributeMetaData targetAttributeMetaData = attrMetaFactory.create().setName("field1");
		targetAttributeMetaData.setDataType(XREF);
		targetAttributeMetaData.setRefEntity(entityMetaDataXref);
		AttributeMapping attributeMapping = new AttributeMapping(targetAttributeMetaData);
		attributeMapping.setAlgorithm("$('xref').map({'1':'2', '2':'1'}).value();");
		when(dataService.findOneById("xrefEntity1", "1")).thenReturn(xref1a);
		Entity result = (Entity) algorithmService.apply(attributeMapping, source, entityMetaDataSource);
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
		EntityMetaData refEntityMeta = entityMetaFactory.create(refEntityName);
		refEntityMeta.addAttribute(attrMetaFactory.create().setName(refEntityIdAttrName), ROLE_ID);
		refEntityMeta
				.addAttribute(attrMetaFactory.create().setName(refEntityLabelAttrName).setDataType(STRING), ROLE_LABEL);

		Entity refEntity0 = new DynamicEntity(refEntityMeta);
		refEntity0.set(refEntityIdAttrName, refEntityId0);
		refEntity0.set(refEntityLabelAttrName, "label0");

		Entity refEntity1 = new DynamicEntity(refEntityMeta);
		refEntity1.set(refEntityIdAttrName, refEntityId1);
		refEntity1.set(refEntityLabelAttrName, "label1");

		// mapping
		AttributeMetaData targetAttributeMetaData = attrMetaFactory.create().setName(targetEntityAttrName);
		targetAttributeMetaData.setDataType(MREF).setNillable(false).setRefEntity(refEntityMeta);
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
		EntityMetaData entityMetaDataSource = entityMetaFactory.create(sourceEntityName);
		entityMetaDataSource
				.addAttribute(attrMetaFactory.create().setName(refEntityIdAttrName).setDataType(INT).setAuto(true),
						ROLE_ID);
		entityMetaDataSource.addAttribute(
				attrMetaFactory.create().setName(sourceEntityAttrName).setDataType(MREF).setNillable(false)
						.setRefEntity(refEntityMeta));
		Entity source = new DynamicEntity(entityMetaDataSource);
		source.set(sourceEntityAttrName, Arrays.asList(refEntity0, refEntity1));

		Object result = algorithmService.apply(attributeMapping, source, entityMetaDataSource);
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
		EntityMetaData refEntityMeta = entityMetaFactory.create(refEntityName);
		refEntityMeta.addAttribute(attrMetaFactory.create().setName(refEntityIdAttrName), ROLE_ID);
		refEntityMeta.addAttribute(attrMetaFactory.create().setName(refEntityLabelAttrName), ROLE_LABEL);

		// mapping
		AttributeMetaData targetAttributeMetaData = attrMetaFactory.create().setName(targetEntityAttrName);
		targetAttributeMetaData.setDataType(MREF).setNillable(true).setRefEntity(refEntityMeta);
		AttributeMapping attributeMapping = new AttributeMapping(targetAttributeMetaData);
		attributeMapping.setAlgorithm("$('" + sourceEntityAttrName + "').value()");

		// source Entity
		EntityMetaData entityMetaDataSource = entityMetaFactory.create(sourceEntityName);
		entityMetaDataSource
				.addAttribute(attrMetaFactory.create().setName(refEntityIdAttrName).setDataType(INT).setAuto(true),
						ROLE_ID);
		entityMetaDataSource.addAttribute(
				attrMetaFactory.create().setName(sourceEntityAttrName).setDataType(MREF).setNillable(true)
						.setRefEntity(refEntityMeta));

		Entity source = new DynamicEntity(entityMetaDataSource);
		source.set(sourceEntityAttrName, null);

		Object result = algorithmService.apply(attributeMapping, source, entityMetaDataSource);
		assertNull(result);
	}

	@Test
	public void testCreateAttributeMappingIfOnlyOneMatch()
	{
		EntityMetaData targetEntityMetaData = entityMetaFactory.create("target");
		AttributeMetaData targetAttribute = attrMetaFactory.create().setName("targetHeight");
		targetAttribute.setDescription("height");
		targetEntityMetaData.addAttribute(targetAttribute);

		EntityMetaData sourceEntityMetaData = entityMetaFactory.create("source");
		AttributeMetaData sourceAttribute = attrMetaFactory.create().setName("sourceHeight");
		sourceAttribute.setDescription("height");
		sourceEntityMetaData.addAttribute(sourceAttribute);

		MolgenisUser owner = molgenisUserFactory.create();
		owner.setUsername("flup");
		owner.setPassword("geheim");
		owner.setId("12345");
		owner.setActive(true);
		owner.setEmail("flup@blah.com");
		owner.setFirstName("Flup");
		owner.setLastName("de Flap");

		MappingProject project = new MappingProject("project", owner);
		project.addTarget(targetEntityMetaData);

		EntityMapping mapping = project.getMappingTarget("target").addSource(sourceEntityMetaData);

		Map<AttributeMetaData, ExplainedMatchCandidate<AttributeMetaData>> matches = ImmutableMap.of(sourceAttribute,
				ExplainedMatchCandidate.create(sourceAttribute,
						singletonList(ExplainedQueryString.create("height", "height", "height", 100)), true));

		LinkedHashMultimap<Relation, OntologyTerm> ontologyTermTags = LinkedHashMultimap.create();

		when(semanticSearchService
				.decisionTreeToFindRelevantAttributes(sourceEntityMetaData, targetAttribute, ontologyTermTags.values(),
						null)).thenReturn(matches);

		when(ontologyTagService.getTagsForAttribute(targetEntityMetaData, targetAttribute))
				.thenReturn(ontologyTermTags);

		algorithmService.autoGenerateAlgorithm(sourceEntityMetaData, targetEntityMetaData, mapping, targetAttribute);

		assertEquals(mapping.getAttributeMapping("targetHeight").getAlgorithm(), "$('sourceHeight').value();");
	}

	@Test
	public void testWhenSourceDoesNotMatchThenNoMappingGetsCreated()
	{
		EntityMetaData targetEntityMetaData = entityMetaFactory.create("target");
		AttributeMetaData targetAttribute = attrMetaFactory.create().setName("targetHeight");
		targetAttribute.setDescription("height");
		targetEntityMetaData.addAttribute(targetAttribute);

		EntityMetaData sourceEntityMetaData = entityMetaFactory.create("source");
		AttributeMetaData sourceAttribute = attrMetaFactory.create().setName("sourceHeight");
		sourceAttribute.setDescription("weight");
		sourceEntityMetaData.addAttribute(sourceAttribute);

		MolgenisUser owner = molgenisUserFactory.create();
		owner.setUsername("flup");
		owner.setPassword("geheim");
		owner.setId("12345");
		owner.setActive(true);
		owner.setEmail("flup@blah.com");
		owner.setFirstName("Flup");
		owner.setLastName("de Flap");

		MappingProject project = new MappingProject("project", owner);
		project.addTarget(targetEntityMetaData);

		EntityMapping mapping = project.getMappingTarget("target").addSource(sourceEntityMetaData);

		when(semanticSearchService.findAttributes(sourceEntityMetaData, Sets.newHashSet("targetHeight", "height"),
				Collections.emptyList())).thenReturn(emptyMap());

		when(ontologyTagService.getTagsForAttribute(targetEntityMetaData, targetAttribute))
				.thenReturn(LinkedHashMultimap.create());

		algorithmService.autoGenerateAlgorithm(sourceEntityMetaData, targetEntityMetaData, mapping, targetAttribute);

		assertNull(mapping.getAttributeMapping("targetHeight"));
	}

	@Test
	public void testWhenSourceHasMultipleMatchesThenFirstMappingGetsCreated()
	{
		EntityMetaData targetEntityMetaData = entityMetaFactory.create("target");
		AttributeMetaData targetAttribute = attrMetaFactory.create().setName("targetHeight");
		targetAttribute.setDescription("height");
		targetEntityMetaData.addAttribute(targetAttribute);

		EntityMetaData sourceEntityMetaData = entityMetaFactory.create("source");
		AttributeMetaData sourceAttribute1 = attrMetaFactory.create().setName("sourceHeight1");
		sourceAttribute1.setDescription("height");
		AttributeMetaData sourceAttribute2 = attrMetaFactory.create().setName("sourceHeight2");
		sourceAttribute2.setDescription("height");

		sourceEntityMetaData.addAttributes(Arrays.asList(sourceAttribute1, sourceAttribute2));

		MolgenisUser owner = molgenisUserFactory.create();
		owner.setUsername("flup");
		owner.setPassword("geheim");
		owner.setId("12345");
		owner.setActive(true);
		owner.setEmail("flup@blah.com");
		owner.setFirstName("Flup");
		owner.setLastName("de Flap");

		MappingProject project = new MappingProject("project", owner);
		project.addTarget(targetEntityMetaData);

		EntityMapping mapping = project.getMappingTarget("target").addSource(sourceEntityMetaData);

		Map<AttributeMetaData, ExplainedMatchCandidate<AttributeMetaData>> mappings = ImmutableMap
				.of(sourceAttribute1, ExplainedMatchCandidate.create(sourceAttribute1), sourceAttribute2,
						ExplainedMatchCandidate.create(sourceAttribute2));

		LinkedHashMultimap<Relation, OntologyTerm> ontologyTermTags = LinkedHashMultimap.create();

		when(semanticSearchService
				.decisionTreeToFindRelevantAttributes(sourceEntityMetaData, targetAttribute, ontologyTermTags.values(),
						null)).thenReturn(mappings);

		when(ontologyTagService.getTagsForAttribute(targetEntityMetaData, targetAttribute))
				.thenReturn(ontologyTermTags);

		algorithmService.autoGenerateAlgorithm(sourceEntityMetaData, targetEntityMetaData, mapping, targetAttribute);

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

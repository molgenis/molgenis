package org.molgenis.data.mapper.service.impl;

import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toList;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.MolgenisFieldTypes.DATE;
import static org.molgenis.MolgenisFieldTypes.INT;
import static org.molgenis.MolgenisFieldTypes.MREF;
import static org.molgenis.MolgenisFieldTypes.STRING;
import static org.molgenis.MolgenisFieldTypes.XREF;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;

import org.mockito.ArgumentMatcher;
import org.mockito.Matchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.molgenis.auth.MolgenisUser;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.IdGenerator;
import org.molgenis.data.mapper.algorithmgenerator.service.AlgorithmGeneratorService;
import org.molgenis.data.mapper.algorithmgenerator.service.impl.AlgorithmGeneratorServiceImpl;
import org.molgenis.data.mapper.mapping.model.AttributeMapping;
import org.molgenis.data.mapper.mapping.model.EntityMapping;
import org.molgenis.data.mapper.mapping.model.MappingProject;
import org.molgenis.data.mapper.service.AlgorithmService;
import org.molgenis.data.mapper.service.UnitResolver;
import org.molgenis.data.semantic.Relation;
import org.molgenis.data.semanticsearch.explain.bean.ExplainedAttributeMetaData;
import org.molgenis.data.semanticsearch.explain.bean.ExplainedQueryString;
import org.molgenis.data.semanticsearch.repository.TagRepository;
import org.molgenis.data.semanticsearch.service.OntologyTagService;
import org.molgenis.data.semanticsearch.service.SemanticSearchService;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.ontology.core.model.OntologyTerm;
import org.molgenis.ontology.core.service.OntologyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Sets;

@ContextConfiguration(classes = AlgorithmServiceImplTest.Config.class)
public class AlgorithmServiceImplTest extends AbstractTestNGSpringContextTests
{
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

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		when(algorithmTemplateService.find(Matchers.<Map<AttributeMetaData, ExplainedAttributeMetaData>> any()))
				.thenReturn(Stream.empty());
	}

	@Test
	public void testGetSourceAttributeNames()
	{
		assertEquals(algorithmService.getSourceAttributeNames("$('id')"), Collections.singletonList("id"));
	}

	@Test
	public void testGetSourceAttributeNamesNoQuotes()
	{
		assertEquals(algorithmService.getSourceAttributeNames("$(id)"), Collections.singletonList("id"));
	}

	@Test
	public void testDate() throws ParseException
	{
		DefaultEntityMetaData entityMetaData = new DefaultEntityMetaData("LL");
		entityMetaData.addAttribute("id").setDataType(INT).setIdAttribute(true);
		entityMetaData.addAttribute("dob").setDataType(DATE);
		Entity source = new MapEntity(entityMetaData);
		source.set("id", 1);
		source.set("dob", new SimpleDateFormat("dd-MM-yyyy").parse("13-05-2015"));

		DefaultAttributeMetaData targetAttributeMetaData = new DefaultAttributeMetaData("bob");
		targetAttributeMetaData.setDataType(DATE);
		AttributeMapping attributeMapping = new AttributeMapping(targetAttributeMetaData);
		attributeMapping.setAlgorithm("$('dob').value()");
		Object result = algorithmService.apply(attributeMapping, source, entityMetaData);
		assertEquals(result.toString(), "Wed May 13 00:00:00 CEST 2015");
	}

	@Test
	public void testGetAgeScript() throws ParseException
	{
		DefaultEntityMetaData entityMetaData = new DefaultEntityMetaData("LL");
		entityMetaData.addAttribute("id").setDataType(INT).setIdAttribute(true);
		entityMetaData.addAttribute("dob").setDataType(DATE);
		Entity source = new MapEntity(entityMetaData);
		source.set("id", 1);
		source.set("dob", new SimpleDateFormat("dd-MM-yyyy").parse("28-08-1973"));

		DefaultAttributeMetaData targetAttributeMetaData = new DefaultAttributeMetaData("age");
		targetAttributeMetaData.setDataType(INT);
		AttributeMapping attributeMapping = new AttributeMapping(targetAttributeMetaData);
		attributeMapping.setAlgorithm(
				"Math.floor((new Date('02/12/2015') - $('dob').value())/(365.2425 * 24 * 60 * 60 * 1000))");
		Object result = algorithmService.apply(attributeMapping, source, entityMetaData);
		assertEquals(result, (long) 41);
	}

	@Test
	public void testGetXrefScript() throws ParseException
	{
		// xref entities
		DefaultEntityMetaData entityMetaDataXref = new DefaultEntityMetaData("xrefEntity1");
		entityMetaDataXref.addAttribute("id").setDataType(INT).setIdAttribute(true);
		entityMetaDataXref.addAttribute("field1").setDataType(STRING);
		Entity xref1a = new MapEntity(entityMetaDataXref);
		xref1a.set("id", "1");
		xref1a.set("field1", "Test");

		DefaultEntityMetaData entityMetaDataXref2 = new DefaultEntityMetaData("xrefEntity2");
		entityMetaDataXref2.addAttribute("id").setDataType(INT).setIdAttribute(true);
		entityMetaDataXref2.addAttribute("field1").setDataType(STRING);
		Entity xref2a = new MapEntity(entityMetaDataXref2);
		xref2a.set("id", "2");
		xref2a.set("field2", "Test");

		// source Entity
		DefaultEntityMetaData entityMetaDataSource = new DefaultEntityMetaData("Source");
		entityMetaDataSource.addAttribute("id").setDataType(INT).setIdAttribute(true);
		entityMetaDataSource.addAttribute("xref").setDataType(XREF);
		Entity source = new MapEntity(entityMetaDataSource);
		source.set("id", "1");
		source.set("xref", xref2a);

		DefaultAttributeMetaData targetAttributeMetaData = new DefaultAttributeMetaData("field1");
		targetAttributeMetaData.setDataType(XREF);
		targetAttributeMetaData.setRefEntity(entityMetaDataXref);
		AttributeMapping attributeMapping = new AttributeMapping(targetAttributeMetaData);
		attributeMapping.setAlgorithm("$('xref').map({'1':'2', '2':'1'}).value();");
		when(dataService.findOne("xrefEntity1", "1")).thenReturn(xref1a);
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
		DefaultEntityMetaData refEntityMeta = new DefaultEntityMetaData(refEntityName);
		refEntityMeta.addAttribute(refEntityIdAttrName).setDataType(STRING).setIdAttribute(true);
		refEntityMeta.addAttribute(refEntityLabelAttrName).setDataType(STRING).setLabelAttribute(true);

		Entity refEntity0 = new MapEntity(refEntityMeta);
		refEntity0.set(refEntityIdAttrName, refEntityId0);
		refEntity0.set(refEntityLabelAttrName, "label0");

		Entity refEntity1 = new MapEntity(refEntityMeta);
		refEntity1.set(refEntityIdAttrName, refEntityId1);
		refEntity1.set(refEntityLabelAttrName, "label1");

		// mapping
		DefaultAttributeMetaData targetAttributeMetaData = new DefaultAttributeMetaData(targetEntityAttrName);
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
		DefaultEntityMetaData entityMetaDataSource = new DefaultEntityMetaData(sourceEntityName);
		entityMetaDataSource.addAttribute(refEntityIdAttrName).setDataType(INT).setIdAttribute(true).setAuto(true);
		entityMetaDataSource.addAttribute(sourceEntityAttrName).setDataType(MREF).setNillable(false)
				.setRefEntity(refEntityMeta);
		Entity source = new MapEntity(entityMetaDataSource);
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
		DefaultEntityMetaData refEntityMeta = new DefaultEntityMetaData(refEntityName);
		refEntityMeta.addAttribute(refEntityIdAttrName).setDataType(STRING).setIdAttribute(true);
		refEntityMeta.addAttribute(refEntityLabelAttrName).setDataType(STRING).setLabelAttribute(true);

		// mapping
		DefaultAttributeMetaData targetAttributeMetaData = new DefaultAttributeMetaData(targetEntityAttrName);
		targetAttributeMetaData.setDataType(MREF).setNillable(true).setRefEntity(refEntityMeta);
		AttributeMapping attributeMapping = new AttributeMapping(targetAttributeMetaData);
		attributeMapping.setAlgorithm("$('" + sourceEntityAttrName + "').value()");

		// source Entity
		DefaultEntityMetaData entityMetaDataSource = new DefaultEntityMetaData(sourceEntityName);
		entityMetaDataSource.addAttribute(refEntityIdAttrName).setDataType(INT).setIdAttribute(true).setAuto(true);
		entityMetaDataSource.addAttribute(sourceEntityAttrName).setDataType(MREF).setNillable(true)
				.setRefEntity(refEntityMeta);
		Entity source = new MapEntity(entityMetaDataSource);
		source.set(sourceEntityAttrName, null);

		Object result = algorithmService.apply(attributeMapping, source, entityMetaDataSource);
		assertNull(result);
	}

	@Test
	public void testCreateAttributeMappingIfOnlyOneMatch()
	{
		DefaultEntityMetaData targetEntityMetaData = new DefaultEntityMetaData("target");
		DefaultAttributeMetaData targetAttribute = new DefaultAttributeMetaData("targetHeight");
		targetAttribute.setDescription("height");
		targetEntityMetaData.addAttributeMetaData(targetAttribute);

		DefaultEntityMetaData sourceEntityMetaData = new DefaultEntityMetaData("source");
		DefaultAttributeMetaData sourceAttribute = new DefaultAttributeMetaData("sourceHeight");
		sourceAttribute.setDescription("height");
		sourceEntityMetaData.addAttributeMetaData(sourceAttribute);

		MolgenisUser owner = new MolgenisUser();
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

		Map<AttributeMetaData, ExplainedAttributeMetaData> matches = ImmutableMap.of(sourceAttribute,
				ExplainedAttributeMetaData.create(sourceAttribute,
						Arrays.asList(ExplainedQueryString.create("height", "height", "height", 100)), true));

		LinkedHashMultimap<Relation, OntologyTerm> ontologyTermTags = LinkedHashMultimap.create();

		when(semanticSearchService.decisionTreeToFindRelevantAttributes(sourceEntityMetaData, targetAttribute,
				ontologyTermTags.values(), null)).thenReturn(matches);

		when(ontologyTagService.getTagsForAttribute(targetEntityMetaData, targetAttribute))
				.thenReturn(ontologyTermTags);

		algorithmService.autoGenerateAlgorithm(sourceEntityMetaData, targetEntityMetaData, mapping, targetAttribute);

		assertEquals(mapping.getAttributeMapping("targetHeight").getAlgorithm(), "$('sourceHeight').value();");
	}

	@Test
	public void testWhenSourceDoesNotMatchThenNoMappingGetsCreated()
	{
		DefaultEntityMetaData targetEntityMetaData = new DefaultEntityMetaData("target");
		DefaultAttributeMetaData targetAttribute = new DefaultAttributeMetaData("targetHeight");
		targetAttribute.setDescription("height");
		targetEntityMetaData.addAttributeMetaData(targetAttribute);

		DefaultEntityMetaData sourceEntityMetaData = new DefaultEntityMetaData("source");
		DefaultAttributeMetaData sourceAttribute = new DefaultAttributeMetaData("sourceHeight");
		sourceAttribute.setDescription("weight");
		sourceEntityMetaData.addAttributeMetaData(sourceAttribute);

		MolgenisUser owner = new MolgenisUser();
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

		Assert.assertNull(mapping.getAttributeMapping("targetHeight"));
	}

	@Test
	public void testWhenSourceHasMultipleMatchesThenFirstMappingGetsCreated()
	{
		DefaultEntityMetaData targetEntityMetaData = new DefaultEntityMetaData("target");
		DefaultAttributeMetaData targetAttribute = new DefaultAttributeMetaData("targetHeight");
		targetAttribute.setDescription("height");
		targetEntityMetaData.addAttributeMetaData(targetAttribute);

		DefaultEntityMetaData sourceEntityMetaData = new DefaultEntityMetaData("source");
		DefaultAttributeMetaData sourceAttribute1 = new DefaultAttributeMetaData("sourceHeight1");
		sourceAttribute1.setDescription("height");
		DefaultAttributeMetaData sourceAttribute2 = new DefaultAttributeMetaData("sourceHeight2");
		sourceAttribute2.setDescription("height");

		sourceEntityMetaData.addAllAttributeMetaData(Arrays.asList(sourceAttribute1, sourceAttribute2));

		MolgenisUser owner = new MolgenisUser();
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

		Map<AttributeMetaData, ExplainedAttributeMetaData> mappings = ImmutableMap
				.<AttributeMetaData, ExplainedAttributeMetaData> of(sourceAttribute1,
						ExplainedAttributeMetaData.create(sourceAttribute1), sourceAttribute2,
						ExplainedAttributeMetaData.create(sourceAttribute2));

		LinkedHashMultimap<Relation, OntologyTerm> ontologyTermTags = LinkedHashMultimap
				.<Relation, OntologyTerm> create();

		when(semanticSearchService.decisionTreeToFindRelevantAttributes(sourceEntityMetaData, targetAttribute,
				ontologyTermTags.values(), null)).thenReturn(mappings);

		when(ontologyTagService.getTagsForAttribute(targetEntityMetaData, targetAttribute))
				.thenReturn(ontologyTermTags);

		algorithmService.autoGenerateAlgorithm(sourceEntityMetaData, targetEntityMetaData, mapping, targetAttribute);

		Assert.assertEquals(mapping.getAttributeMapping("targetHeight").getSourceAttributeMetaDatas().get(0),
				sourceAttribute1);
	}

	@Configuration
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
			IdGenerator idGenerator = mock(IdGenerator.class);
			return idGenerator;
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

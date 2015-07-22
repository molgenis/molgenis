package org.molgenis.data.mapper.service.impl;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.MolgenisFieldTypes.DATE;
import static org.molgenis.MolgenisFieldTypes.INT;
import static org.molgenis.MolgenisFieldTypes.MREF;
import static org.molgenis.MolgenisFieldTypes.STRING;
import static org.molgenis.MolgenisFieldTypes.XREF;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.molgenis.auth.MolgenisUser;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.IdGenerator;
import org.molgenis.data.mapper.mapping.model.AttributeMapping;
import org.molgenis.data.mapper.mapping.model.EntityMapping;
import org.molgenis.data.mapper.mapping.model.MappingProject;
import org.molgenis.data.mapper.service.AlgorithmService;
import org.molgenis.data.semantic.Relation;
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
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

;

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
	public void testIsSingleMatchHighQuality()
	{
		AlgorithmServiceImpl algorithmServiceImpl = (AlgorithmServiceImpl) algorithmService;

		DefaultAttributeMetaData targetAttribute1 = new DefaultAttributeMetaData("height");
		targetAttribute1.setLabel("height");
		Multimap<Relation, OntologyTerm> tags1 = LinkedHashMultimap.<Relation, OntologyTerm> create();
		List<ExplainedQueryString> explanations1 = Arrays.asList(ExplainedQueryString.create("height", "height",
				"standing height", 50.0));
		assertFalse(algorithmServiceImpl.isSingleMatchHighQuality(targetAttribute1, tags1, explanations1));

		DefaultAttributeMetaData targetAttribute2 = new DefaultAttributeMetaData("height");
		targetAttribute2.setLabel("height");
		Multimap<Relation, OntologyTerm> tags2 = LinkedHashMultimap.<Relation, OntologyTerm> create();
		tags2.put(Relation.isAssociatedWith, OntologyTerm.create("http://www.molgenis.org/height", "length"));
		List<ExplainedQueryString> explanations2 = Arrays.asList(ExplainedQueryString.create("height", "height",
				"height", 100));
		assertTrue(algorithmServiceImpl.isSingleMatchHighQuality(targetAttribute2, tags2, explanations2));

		DefaultAttributeMetaData targetAttribute3 = new DefaultAttributeMetaData("fasting_glucose");
		targetAttribute3.setLabel("glucose fasting");
		Multimap<Relation, OntologyTerm> tags3 = LinkedHashMultimap.<Relation, OntologyTerm> create();
		tags3.put(Relation.isAssociatedWith, OntologyTerm.create(
				"http://www.molgenis.org/fasting,http://www.molgenis.org/glucose", "fasting,glucose"));
		List<ExplainedQueryString> explanations3 = Arrays.asList(
				ExplainedQueryString.create("fasting", "fasting", "fasting", 100),
				ExplainedQueryString.create("glucose", "glucose", "blood glucose", 50));
		assertFalse(algorithmServiceImpl.isSingleMatchHighQuality(targetAttribute3, tags3, explanations3));
	}

	@Test
	public void testIsGoodMatch()
	{
		AlgorithmServiceImpl algorithmServiceImpl = (AlgorithmServiceImpl) algorithmService;

		Map<String, Double> matchedTags = new HashMap<String, Double>();
		matchedTags.put("height", 100.0);
		matchedTags.put("weight", 50.0);
		assertFalse(algorithmServiceImpl.isGoodMatch(matchedTags, "blood"));
		assertFalse(algorithmServiceImpl.isGoodMatch(matchedTags, "weight"));
		assertTrue(algorithmServiceImpl.isGoodMatch(matchedTags, "height"));
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
		attributeMapping
				.setAlgorithm("Math.floor((new Date('02/12/2015') - $('dob').value())/(365.2425 * 24 * 60 * 60 * 1000))");
		Object result = algorithmService.apply(attributeMapping, source, entityMetaData);
		assertEquals(result, 41);
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
		when(dataService.findAll(refEntityName, Arrays.asList(refEntityId0, refEntityId1))).thenReturn(
				Arrays.asList(refEntity0, refEntity1));

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

		Map<AttributeMetaData, Iterable<ExplainedQueryString>> matches = ImmutableMap.of(sourceAttribute,
				Arrays.asList(ExplainedQueryString.create("height", "height", "height", 100)));

		when(semanticSearchService.explainAttributes(sourceEntityMetaData, targetEntityMetaData, targetAttribute))
				.thenReturn(matches);

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

		when(semanticSearchService.explainAttributes(sourceEntityMetaData, targetEntityMetaData, targetAttribute))
				.thenReturn(emptyMap());

		algorithmService.autoGenerateAlgorithm(sourceEntityMetaData, targetEntityMetaData, mapping, targetAttribute);

		Assert.assertNull(mapping.getAttributeMapping("targetHeight"));
	}

	@Test
	public void testWhenSourceHasMultipleMatchesThenNoMappingGetsCreated()
	{
		DefaultEntityMetaData targetEntityMetaData = new DefaultEntityMetaData("target");
		DefaultAttributeMetaData targetAttribute = new DefaultAttributeMetaData("targetHeight");
		targetAttribute.setDescription("height");
		targetEntityMetaData.addAttributeMetaData(targetAttribute);

		DefaultEntityMetaData sourceEntityMetaData = new DefaultEntityMetaData("source");
		DefaultAttributeMetaData sourceAttribute1 = new DefaultAttributeMetaData("sourceHeight1");
		sourceAttribute1.setDescription("height");
		DefaultAttributeMetaData sourceAttribute2 = new DefaultAttributeMetaData("sourceHeight2");
		sourceAttribute1.setDescription("height");

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

		Iterable<AttributeMetaData> mappings = asList(sourceAttribute1, sourceAttribute2);
		when(semanticSearchService.findAttributes(sourceEntityMetaData, targetEntityMetaData, targetAttribute))
				.thenReturn(mappings);

		algorithmService.autoGenerateAlgorithm(sourceEntityMetaData, targetEntityMetaData, mapping, targetAttribute);

		Assert.assertNull(mapping.getAttributeMapping("targetHeight"));
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
		public AlgorithmService algorithmService()
		{
			return new AlgorithmServiceImpl(dataService(), ontologyTagService(), semanticSearchService());
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
	}
}

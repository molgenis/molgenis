package org.molgenis.data.semanticsearch.service.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.QueryRule;
import org.molgenis.data.meta.AttributeMetaDataMetaData;
import org.molgenis.data.meta.EntityMetaDataMetaData;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.semantic.Relation;
import org.molgenis.data.semanticsearch.service.OntologyTagService;
import org.molgenis.data.semanticsearch.service.SemanticSearchService;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntity;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.ontology.core.model.OntologyTerm;
import org.molgenis.ontology.core.service.OntologyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

@ContextConfiguration(classes = SemanticSearchServiceHelperTest.Config.class)
public class SemanticSearchServiceHelperTest extends AbstractTestNGSpringContextTests
{
	@Autowired
	private OntologyService ontologyService;

	@Autowired
	private SemanticSearchServiceHelper semanticSearchServiceHelper;

	@Autowired
	private DataService dataService;

	@Autowired
	private OntologyTagService ontologyTagService;

	@Autowired
	private SemanticSearchService semanticSearchService;

	@Autowired
	private MetaDataService metaDataService;

	@Test
	public void testCreateDisMaxQueryRule()
	{
		EntityMetaData targetEntityMetaData = new DefaultEntityMetaData("targetEntityMetaData");
		DefaultAttributeMetaData targetAttribute = new DefaultAttributeMetaData("targetAttribute");
		targetAttribute.setDescription("Height");

		Multimap<Relation, OntologyTerm> tags = LinkedHashMultimap.<Relation, OntologyTerm> create();
		OntologyTerm ontologyTerm1 = OntologyTerm.create("http://onto/standingheight", "Standing height",
				"Description is not used", Arrays.<String> asList("body_length"));
		OntologyTerm ontologyTerm2 = OntologyTerm.create("http://onto/sittingheight", "Sitting height",
				"Description is not used", Arrays.<String> asList("sitting_length"));
		OntologyTerm ontologyTerm3 = OntologyTerm.create("http://onto/height", "Height", "Description is not used",
				Arrays.<String> asList("sature"));
		tags.put(Relation.isAssociatedWith, ontologyTerm1);
		tags.put(Relation.isAssociatedWith, ontologyTerm2);
		tags.put(Relation.isRealizationOf, ontologyTerm2);
		tags.put(Relation.isDefinedBy, ontologyTerm3);

		when(ontologyTagService.getTagsForAttribute(targetEntityMetaData, targetAttribute)).thenReturn(tags);

		QueryRule actualRule = semanticSearchServiceHelper.createDisMaxQueryRule(targetEntityMetaData, targetAttribute);
		String expectedRule = "(label FUZZY_MATCH 'Height'(label FUZZY_MATCH 'body_length'label FUZZY_MATCH 'Standing height')(label FUZZY_MATCH 'sitting_length'label FUZZY_MATCH 'Sitting height')(label FUZZY_MATCH 'sitting_length'label FUZZY_MATCH 'Sitting height')(label FUZZY_MATCH 'sature'label FUZZY_MATCH 'Height'))";
		assertEquals(actualRule.toString(), expectedRule);
	}

	@Test
	public void testGetAttributeIdentifiers()
	{
		EntityMetaData sourceEntityMetaData = new DefaultEntityMetaData("sourceEntityMetaData");
		Entity entityMetaDataEntity = mock(DefaultEntity.class);

		when(
				dataService.findOne(EntityMetaDataMetaData.ENTITY_NAME,
						new QueryImpl().eq(EntityMetaDataMetaData.FULL_NAME, sourceEntityMetaData.getName())))
				.thenReturn(entityMetaDataEntity);

		Entity attributeEntity1 = mock(DefaultEntity.class);
		when(attributeEntity1.getString(AttributeMetaDataMetaData.IDENTIFIER)).thenReturn("1");
		Entity attributeEntity2 = mock(DefaultEntity.class);
		when(attributeEntity2.getString(AttributeMetaDataMetaData.IDENTIFIER)).thenReturn("2");
		when(entityMetaDataEntity.getEntities(EntityMetaDataMetaData.ATTRIBUTES)).thenReturn(
				Arrays.<Entity> asList(attributeEntity1, attributeEntity2));

		List<String> expactedAttributeIdentifiers = Arrays.<String> asList("1", "2");
		assertEquals(semanticSearchServiceHelper.getAttributeIdentifiers(sourceEntityMetaData),
				expactedAttributeIdentifiers);
	}

	@Test
	public void testFindTagsSync()
	{
		String description = "Fall " + SemanticSearchServiceHelper.STOP_WORDS + " sleep";
		List<String> ontologyIds = Arrays.<String> asList("1");
		Set<String> searchTerms = Sets.newHashSet("fall", "sleep");
		semanticSearchServiceHelper.findTags(description, ontologyIds);
		verify(ontologyService).findOntologyTerms(ontologyIds, searchTerms, 100);
	}

	@Test
	public void testSearchIsoLatin() throws InterruptedException, ExecutionException
	{
		String description = "Standing height (Ångstrøm)";
		List<String> ontologyIds = Arrays.<String> asList("1");
		Set<String> searchTerms = Sets.newHashSet("standing", "height", "ångstrøm");
		semanticSearchServiceHelper.findTags(description, ontologyIds);
		verify(ontologyService).findOntologyTerms(ontologyIds, searchTerms, 100);
	}

	@Test
	public void testSearchUnicode() throws InterruptedException, ExecutionException
	{
		String description = "/əˈnædrəməs/";
		List<String> ontologyIds = Arrays.<String> asList("1");
		Set<String> searchTerms = Sets.newHashSet("əˈnædrəməs");
		semanticSearchServiceHelper.findTags(description, ontologyIds);
		verify(ontologyService).findOntologyTerms(ontologyIds, searchTerms, 100);
	}

	@Configuration
	public static class Config
	{
		@Bean
		OntologyService ontologyService()
		{
			return mock(OntologyService.class);
		}

		@Bean
		SemanticSearchService semanticSearchService()
		{
			return new SemanticSearchServiceImpl();
		}

		@Bean
		DataService dataService()
		{
			return mock(DataService.class);
		}

		@Bean
		MetaDataService metaDataService()
		{
			return mock(MetaDataService.class);
		}

		@Bean
		OntologyTagService ontologyTagService()
		{
			return mock(OntologyTagService.class);
		}

		@Bean
		SemanticSearchServiceHelper semanticSearchServiceHelper()
		{
			return new SemanticSearchServiceHelper(ontologyTagService(), dataService(), ontologyService());
		}
	}
}

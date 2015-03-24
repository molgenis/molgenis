package org.molgenis.data.semantic;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.ontology.OntologyService;
import org.molgenis.ontology.repository.model.OntologyTerm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableSet;

@ContextConfiguration(classes = SemanticSearchServiceImplTest.Config.class)
public class SemanticSearchServiceImplTest extends AbstractTestNGSpringContextTests
{
	@Configuration
	public static class Config
	{
		@Bean
		MetaDataService metaDataService()
		{
			return mock(MetaDataService.class);
		}

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
	}

	@Autowired
	private MetaDataService metaDataService;

	@Autowired
	private OntologyService ontologyService;

	@Autowired
	private SemanticSearchService semanticSearchService;

	@Test
	public void testSearchDescription()
	{
		DefaultAttributeMetaData attribute = new DefaultAttributeMetaData("attr1");
		attribute.setDescription("Standing height in meters.");

		List<String> ontologies = asList("1", "2");
		when(
				ontologyService.findOntologyTerms(ontologies, ImmutableSet.<String> of("standing", "height", "meters"),
						100)).thenReturn(asList(OntologyTerm.create("http://onto/height", "Standing height")));
		semanticSearchService.findTags(attribute, ontologies);
	}
}

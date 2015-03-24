package org.molgenis.ontology.repository;

import static java.util.Arrays.asList;
import static org.elasticsearch.common.collect.ImmutableSet.of;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.List;

import org.mockito.ArgumentCaptor;
import org.molgenis.data.DataService;
import org.molgenis.data.Query;
import org.molgenis.data.support.MapEntity;
import org.molgenis.ontology.model.OntologyTermMetaData;
import org.molgenis.ontology.repository.model.OntologyTerm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

@ContextConfiguration(classes = OntologyTermRepositoryTest.Config.class)
public class OntologyTermRepositoryTest extends AbstractTestNGSpringContextTests
{
	@Configuration
	public static class Config
	{
		@Bean
		public DataService dataService()
		{
			return mock(DataService.class);
		}

		@Bean
		public OntologyTermRepository ontologyTermRepository()
		{
			return new OntologyTermRepository();
		}
	}

	@Autowired
	OntologyTermRepository ontologyTermRepository;

	@Autowired
	DataService dataService;

	@Test
	public void testSearchOntologyTerm()
	{
		ArgumentCaptor<Query> queryCaptor = forClass(Query.class);
		MapEntity ontologyTermEntity = new MapEntity(OntologyTermMetaData.INSTANCE);
		ontologyTermEntity.set(OntologyTermMetaData.ID, "12");
		ontologyTermEntity.set(OntologyTermMetaData.ONTOLOGY, "34");
		ontologyTermEntity.set(OntologyTermMetaData.ONTOLOGY_TERM_IRI, "http://www.test.nl/iri");
		ontologyTermEntity.set(OntologyTermMetaData.ONTOLOGY_TERM_NAME, "Ontology term");

		when(dataService.findAll(eq("Ontology_OntologyTerm"), queryCaptor.capture())).thenReturn(
				asList(ontologyTermEntity));

		List<OntologyTerm> terms = ontologyTermRepository.findOntologyTerms(asList("1", "2"),
				of("term1", "term2", "term3"), 100);

		assertEquals(terms, asList(OntologyTerm.create("http://www.test.nl/iri", "Ontology term")));
		assertEquals(
				queryCaptor.getValue().toString(),
				"QueryImpl [rules=[ontology IN '[1, 2]',  AND , ( search 'term1' search 'term2' search 'term3')], pageSize=100, offset=0, sort=null]");

	}
}

package org.molgenis.ontology.core.repository;

import static org.mockito.Mockito.mock;
import org.molgenis.data.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = OntologyRepositoryTest.Config.class)
public class OntologyRepositoryTest
{
	@Autowired
	DataService dataService;

	@Autowired
	OntologyRepository ontologyRepository;

	@Test
	public void testGetOntologies()
	{
		//return Iterables.transform(dataService.findAll(ENTITY_NAME), OntologyRepository::toOntology);
	}

	@Test
	public void testGetOntology()
	{
		// return toOntology(dataService.findOne(ENTITY_NAME, QueryImpl.EQ(ONTOLOGY_IRI, IRI)));
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
		public OntologyRepository ontologyRepository()
		{
			return new OntologyRepository();
		}
	}
}

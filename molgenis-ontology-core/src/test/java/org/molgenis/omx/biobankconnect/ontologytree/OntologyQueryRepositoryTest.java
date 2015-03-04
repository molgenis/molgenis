package org.molgenis.omx.biobankconnect.ontologytree;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.ontology.OntologyService;
import org.molgenis.ontology.beans.OntologyEntity;
import org.molgenis.ontology.repository.OntologyIndexRepository;
import org.molgenis.ontology.repository.OntologyQueryRepository;
import org.molgenis.ontology.repository.OntologyTermQueryRepository;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class OntologyQueryRepositoryTest
{
	OntologyQueryRepository ontologyQueryRepository;

	@BeforeClass
	public void setUp() throws OWLOntologyCreationException
	{
		SearchService searchService = mock(SearchService.class);
		DataService dataService = mock(DataService.class);
		OntologyService ontologyService = mock(OntologyService.class);

		DefaultEntityMetaData entityMetaDataOntology = new DefaultEntityMetaData(
				OntologyQueryRepository.DEFAULT_ONTOLOGY_REPO);
		entityMetaDataOntology
				.addAttributeMetaData(new DefaultAttributeMetaData(OntologyQueryRepository.ONTOLOGY_NAME));
		entityMetaDataOntology.addAttributeMetaData(new DefaultAttributeMetaData(OntologyQueryRepository.ROOT,
				FieldTypeEnum.BOOL));
		entityMetaDataOntology.addAttributeMetaData(new DefaultAttributeMetaData(OntologyQueryRepository.ONTOLOGY_IRI,
				FieldTypeEnum.STRING));

		DefaultEntityMetaData entityMetaDataOntologyTerm = new DefaultEntityMetaData("test");
		entityMetaDataOntologyTerm.addAttributeMetaData(new DefaultAttributeMetaData(OntologyQueryRepository.ROOT,
				FieldTypeEnum.BOOL));
		entityMetaDataOntologyTerm.addAttributeMetaData(new DefaultAttributeMetaData(
				OntologyQueryRepository.ONTOLOGY_IRI, FieldTypeEnum.STRING));

		Entity entity_1 = new MapEntity();
		entity_1.set(OntologyQueryRepository.ROOT, true);
		entity_1.set(OntologyQueryRepository.ONTOLOGY_NAME, "another ontology");
		entity_1.set(OntologyQueryRepository.ONTOLOGY_IRI, "http://www.ontology.test");
		entity_1.set(OntologyQueryRepository.ENTITY_TYPE, OntologyQueryRepository.TYPE_ONTOLOGYTERM);
		entity_1.set(OntologyQueryRepository.ID, "ontology");

		Entity entity_2 = new MapEntity();
		entity_2.set(OntologyQueryRepository.ROOT, true);
		entity_2.set(OntologyQueryRepository.ONTOLOGY_NAME, "final ontology");
		entity_2.set(OntologyQueryRepository.ONTOLOGY_IRI, "http://www.ontology2.test");
		entity_2.set(OntologyQueryRepository.ENTITY_TYPE, OntologyQueryRepository.TYPE_ONTOLOGYTERM);
		entity_2.set(OntologyQueryRepository.ID, "ontology-2");

		Entity entity_3 = new MapEntity();
		entity_3.set(OntologyQueryRepository.ROOT, true);
		entity_3.set(OntologyQueryRepository.ONTOLOGY_NAME, "test3");
		entity_3.set(OntologyQueryRepository.ONTOLOGY_IRI, "http://www.ontology3.test");
		entity_3.set(OntologyQueryRepository.ENTITY_TYPE, OntologyQueryRepository.TYPE_ONTOLOGYTERM);
		entity_3.set(OntologyQueryRepository.ID, "ontology-3");

		when(
				searchService.search(
						new QueryImpl().eq(OntologyIndexRepository.ENTITY_TYPE, OntologyIndexRepository.TYPE_ONTOLOGY),
						entityMetaDataOntology)).thenReturn(
				Arrays.<Entity> asList(new OntologyEntity(entity_1, entityMetaDataOntology, dataService, searchService,
						ontologyService), new OntologyEntity(entity_2, entityMetaDataOntology, dataService,
						searchService, ontologyService), new OntologyEntity(entity_3, entityMetaDataOntology,
						dataService, searchService, ontologyService)));

		when(
				searchService.search(
						new QueryImpl().eq(OntologyIndexRepository.ONTOLOGY_IRI, "http://www.ontology3.test").and()
								.eq(OntologyIndexRepository.ENTITY_TYPE, OntologyIndexRepository.TYPE_ONTOLOGY),
						entityMetaDataOntology)).thenReturn(
				Arrays.<Entity> asList(new OntologyEntity(entity_3, entityMetaDataOntology, dataService, searchService,
						ontologyService)));

		when(
				searchService.count(
						new QueryImpl().eq(OntologyIndexRepository.ENTITY_TYPE, OntologyIndexRepository.TYPE_ONTOLOGY)
								.pageSize(Integer.MAX_VALUE).offset(Integer.MIN_VALUE), null)).thenReturn(new Long(3));

		when(
				searchService.count(
						new QueryImpl().eq(OntologyIndexRepository.ONTOLOGY_IRI, "http://www.final.ontology.test")
								.and().eq(OntologyIndexRepository.ENTITY_TYPE, OntologyIndexRepository.TYPE_ONTOLOGY)
								.pageSize(Integer.MAX_VALUE).offset(Integer.MIN_VALUE), null)).thenReturn(new Long(1));
		when(
				searchService.search(
						new QueryImpl().eq(OntologyQueryRepository.ENTITY_TYPE, OntologyIndexRepository.TYPE_ONTOLOGY),
						entityMetaDataOntology)).thenReturn(
				Arrays.<Entity> asList(new OntologyEntity(entity_1, entityMetaDataOntology, dataService, searchService,
						ontologyService)));

		when(
				searchService.search(new QueryImpl().eq(OntologyTermQueryRepository.ID, "ontology-2"),
						entityMetaDataOntology)).thenReturn(
				Arrays.<Entity> asList(new OntologyEntity(entity_1, entityMetaDataOntology, dataService, searchService,
						ontologyService)));

		when(
				searchService.search(
						new QueryImpl().eq(OntologyIndexRepository.ONTOLOGY_IRI, "http://www.final.ontology.test")
								.and().eq(OntologyQueryRepository.ENTITY_TYPE, OntologyIndexRepository.TYPE_ONTOLOGY),
						entityMetaDataOntology)).thenReturn(
				Arrays.<Entity> asList(new OntologyEntity(entity_2, entityMetaDataOntology, dataService, searchService,
						ontologyService)));

		ontologyQueryRepository = new OntologyQueryRepository("ontologyindex", ontologyService, searchService,
				dataService);
	}

	@Test
	public void count()
	{
		assertEquals(ontologyQueryRepository.count(new QueryImpl()), 3);
		assertEquals(ontologyQueryRepository.count(new QueryImpl().eq(OntologyIndexRepository.ONTOLOGY_IRI,
				"http://www.final.ontology.test")), 1);
	}

	@Test
	public void findAll()
	{
		List<String> validOntologyIris = Arrays.asList("http://www.ontology.test", "http://www.another.ontology.test",
				"http://www.final.ontology.test");

		for (Entity entity : ontologyQueryRepository.findAll(new QueryImpl()))
		{
			assertTrue(validOntologyIris.contains(entity.get(OntologyIndexRepository.ONTOLOGY_IRI).toString()));
		}
	}

	@Test
	public void findOneQuery()
	{
		Entity entity = ontologyQueryRepository.findOne(new QueryImpl().eq(OntologyIndexRepository.ONTOLOGY_IRI,
				"http://www.final.ontology.test"));
		assertEquals(entity.get(OntologyQueryRepository.ONTOLOGY_NAME).toString(), "final ontology");
	}

	@Test
	public void findOneObject()
	{
		Entity entity = ontologyQueryRepository.findOne("ontology-2");
		assertEquals(((OntologyEntity) entity).getEntity().get(OntologyQueryRepository.ONTOLOGY_NAME).toString(),
				"another ontology");
	}

}

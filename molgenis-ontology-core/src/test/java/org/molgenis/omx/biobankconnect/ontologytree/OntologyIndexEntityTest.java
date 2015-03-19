package org.molgenis.omx.biobankconnect.ontologytree;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.ontology.OntologyService;
import org.molgenis.ontology.beans.OntologyEntity;
import org.molgenis.ontology.repository.OntologyIndexRepository;
import org.molgenis.ontology.repository.OntologyQueryRepository;
import org.molgenis.ontology.repository.OntologyTermIndexRepository;
import org.molgenis.ontology.repository.OntologyTermQueryRepository;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class OntologyIndexEntityTest
{
	DefaultEntityMetaData entityMetaData;

	OntologyEntity ontologyIndexEntity;

	@BeforeClass
	public void setUp() throws OWLOntologyCreationException
	{
		SearchService searchService = mock(SearchService.class);
		DataService dataService = mock(DataService.class);
		OntologyService ontologyService = mock(OntologyService.class);

		entityMetaData = new DefaultEntityMetaData(OntologyQueryRepository.ENTITY_NAME);
		entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(OntologyTermIndexRepository.ONTOLOGY_NAME));
		entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(OntologyTermIndexRepository.ROOT,
				FieldTypeEnum.BOOL));
		entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(OntologyIndexRepository.ONTOLOGY_IRI,
				FieldTypeEnum.STRING));

		DefaultEntityMetaData entityMetaData_1 = new DefaultEntityMetaData("test");
		entityMetaData_1.addAttributeMetaData(new DefaultAttributeMetaData(OntologyTermIndexRepository.ROOT,
				FieldTypeEnum.BOOL));
		entityMetaData_1.addAttributeMetaData(new DefaultAttributeMetaData(OntologyIndexRepository.ONTOLOGY_IRI,
				FieldTypeEnum.STRING));

		when(dataService.getEntityMetaData(entityMetaData_1.getName())).thenReturn(entityMetaData_1);

		Entity entity_1 = mock(Entity.class);
		when(entity_1.get(OntologyTermIndexRepository.ROOT)).thenReturn(true);
		when(entity_1.getString(OntologyIndexRepository.ONTOLOGY_NAME)).thenReturn("test");
		when(entity_1.get(OntologyIndexRepository.ONTOLOGY_IRI)).thenReturn("http://www.ontology.test");
		when(entity_1.get(OntologyTermQueryRepository.ENTITY_TYPE)).thenReturn(
				OntologyTermQueryRepository.TYPE_ONTOLOGYTERM);
		when(entity_1.getIdValue()).thenReturn("forged-id");

		Entity entity_2 = mock(Entity.class);
		when(entity_2.get(OntologyTermIndexRepository.ROOT)).thenReturn(true);
		when(entity_2.getString(OntologyIndexRepository.ONTOLOGY_NAME)).thenReturn("test2");
		when(entity_2.get(OntologyIndexRepository.ONTOLOGY_IRI)).thenReturn("http://www.ontology2.test");
		when(entity_2.get(OntologyTermQueryRepository.ENTITY_TYPE)).thenReturn(
				OntologyTermQueryRepository.TYPE_ONTOLOGYTERM);
		when(entity_2.getIdValue()).thenReturn("forged-id-2");

		Entity entity_3 = mock(Entity.class);
		when(entity_3.get(OntologyTermIndexRepository.ROOT)).thenReturn(true);
		when(entity_3.get(OntologyIndexRepository.ONTOLOGY_NAME)).thenReturn("test3");
		when(entity_3.get(OntologyIndexRepository.ONTOLOGY_IRI)).thenReturn("http://www.ontology3.test");
		when(entity_3.get(OntologyTermQueryRepository.ENTITY_TYPE)).thenReturn(
				OntologyTermQueryRepository.TYPE_ONTOLOGYTERM);
		when(entity_3.getIdValue()).thenReturn("forged-id-3");

		when(searchService.search(new QueryImpl(), entityMetaData)).thenReturn(
				Arrays.asList(entity_1, entity_2, entity_3));

		when(
				searchService.search(
						new QueryImpl().eq(OntologyTermIndexRepository.ROOT, true).pageSize(Integer.MAX_VALUE),
						entityMetaData)).thenReturn(Arrays.asList(entity_1, entity_2, entity_3));

		when(ontologyService.getRootOntologyTermEntities("http://www.ontology.test")).thenReturn(
				Arrays.asList(entity_1));

		when(
				searchService.count(new QueryImpl().eq(OntologyTermQueryRepository.ENTITY_TYPE,
						OntologyTermQueryRepository.TYPE_ONTOLOGYTERM), entityMetaData_1)).thenReturn((long) 1);

		ontologyIndexEntity = new OntologyEntity(entity_1, entityMetaData, dataService, searchService, ontologyService);
	}

	@Test
	public void get()
	{
		assertEquals(ontologyIndexEntity.getIdValue().toString(), "forged-id");
		assertEquals(Boolean.parseBoolean(ontologyIndexEntity.get(OntologyTermIndexRepository.ROOT).toString()), true);
		assertEquals(ontologyIndexEntity.get(OntologyQueryRepository.FIELDTYPE).toString().toLowerCase(),
				MolgenisFieldTypes.COMPOUND.toString().toLowerCase());
		Object attribues = ontologyIndexEntity.get("attributes");
		if (attribues instanceof List<?>)
		{
			attribues = (List<?>) ontologyIndexEntity.get("attributes");
			assertEquals(((List<?>) attribues).size(), 3);
		}
	}
}

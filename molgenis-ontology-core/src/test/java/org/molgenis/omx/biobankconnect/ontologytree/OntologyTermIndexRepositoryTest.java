package org.molgenis.omx.biobankconnect.ontologytree;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.ontology.OntologyService;
import org.molgenis.ontology.repository.AbstractOntologyRepository;
import org.molgenis.ontology.repository.OntologyTermIndexRepository;
import org.molgenis.ontology.repository.OntologyTermQueryRepository;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class OntologyTermIndexRepositoryTest
{

	OntologyTermQueryRepository ontologyTermIndexRepository;
	String ontologyIRI = "http://www.ontology.test";

	@BeforeClass
	public void setUp() throws OWLOntologyCreationException
	{

		DefaultEntityMetaData entityMetaData = new DefaultEntityMetaData("test-ontology");
		DefaultAttributeMetaData attributeId = new DefaultAttributeMetaData(AbstractOntologyRepository.ID);
		attributeId.setIdAttribute(true);
		entityMetaData.addAttributeMetaData(attributeId);
		entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(AbstractOntologyRepository.ONTOLOGY_TERM_IRI,
				MolgenisFieldTypes.FieldTypeEnum.HYPERLINK));
		DefaultAttributeMetaData attributeMetaData = new DefaultAttributeMetaData(
				AbstractOntologyRepository.ONTOLOGY_TERM);
		attributeMetaData.setLabelAttribute(true);
		entityMetaData.addAttributeMetaData(attributeMetaData);
		entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(AbstractOntologyRepository.SYNONYMS));
		entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(AbstractOntologyRepository.ENTITY_TYPE));
		entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(AbstractOntologyRepository.NODE_PATH));
		entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(AbstractOntologyRepository.PARENT_NODE_PATH));
		entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(
				AbstractOntologyRepository.PARENT_ONTOLOGY_TERM_IRI));
		entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(AbstractOntologyRepository.FIELDTYPE));
		entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(AbstractOntologyRepository.LAST,
				MolgenisFieldTypes.FieldTypeEnum.BOOL));
		entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(AbstractOntologyRepository.ROOT,
				MolgenisFieldTypes.FieldTypeEnum.BOOL));
		entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(
				AbstractOntologyRepository.ONTOLOGY_TERM_DEFINITION));
		entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(AbstractOntologyRepository.ONTOLOGY_NAME));
		entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(AbstractOntologyRepository.ONTOLOGY_IRI,
				MolgenisFieldTypes.FieldTypeEnum.HYPERLINK));
		DefaultAttributeMetaData childrenAttributeMetaData = new DefaultAttributeMetaData("attributes",
				MolgenisFieldTypes.FieldTypeEnum.MREF);
		childrenAttributeMetaData.setRefEntity(entityMetaData);
		entityMetaData.addAttributeMetaData(childrenAttributeMetaData);

		MapEntity hit1 = new MapEntity();
		hit1.set(AbstractOntologyRepository.ENTITY_TYPE, AbstractOntologyRepository.TYPE_ONTOLOGYTERM);
		hit1.set(AbstractOntologyRepository.ONTOLOGY_IRI, ontologyIRI);
		hit1.set(AbstractOntologyRepository.ONTOLOGY_NAME, "test ontology");
		hit1.set(AbstractOntologyRepository.LAST, false);
		hit1.set(AbstractOntologyRepository.ROOT, true);
		hit1.set(AbstractOntologyRepository.NODE_PATH, "1.2");
		hit1.set(AbstractOntologyRepository.ONTOLOGY_TERM_IRI, ontologyIRI + "#term1");
		hit1.set(AbstractOntologyRepository.ONTOLOGY_TERM, "ontology term 1");
		hit1.set(AbstractOntologyRepository.ID, "ontology-1");

		MapEntity hit2 = new MapEntity();
		hit2.set(AbstractOntologyRepository.ENTITY_TYPE, AbstractOntologyRepository.TYPE_ONTOLOGYTERM);
		hit2.set(AbstractOntologyRepository.ONTOLOGY_IRI, ontologyIRI);
		hit2.set(AbstractOntologyRepository.ONTOLOGY_NAME, "test ontology");
		hit2.set(AbstractOntologyRepository.LAST, false);
		hit2.set(AbstractOntologyRepository.ROOT, false);
		hit2.set(AbstractOntologyRepository.NODE_PATH, "1.2.3");
		hit2.set(AbstractOntologyRepository.PARENT_NODE_PATH, "1.2");
		hit2.set(AbstractOntologyRepository.PARENT_ONTOLOGY_TERM_IRI, ontologyIRI + "#term1");
		hit2.set(AbstractOntologyRepository.ONTOLOGY_TERM_IRI, ontologyIRI + "#term2");
		hit2.set(AbstractOntologyRepository.ONTOLOGY_TERM, "ontology term 2");
		hit2.set(AbstractOntologyRepository.SYNONYMS, "OT-2");
		hit2.set(AbstractOntologyRepository.ID, "ontology-2");

		MapEntity hit3 = new MapEntity();
		hit3.set(AbstractOntologyRepository.ENTITY_TYPE, AbstractOntologyRepository.TYPE_ONTOLOGYTERM);
		hit3.set(AbstractOntologyRepository.ONTOLOGY_IRI, ontologyIRI);
		hit3.set(AbstractOntologyRepository.ONTOLOGY_NAME, "test ontology");
		hit3.set(AbstractOntologyRepository.LAST, false);
		hit3.set(AbstractOntologyRepository.ROOT, false);
		hit3.set(AbstractOntologyRepository.NODE_PATH, "1.2.4");
		hit3.set(AbstractOntologyRepository.PARENT_NODE_PATH, "1.2");
		hit3.set(AbstractOntologyRepository.PARENT_ONTOLOGY_TERM_IRI, ontologyIRI + "#term1");
		hit3.set(AbstractOntologyRepository.ONTOLOGY_TERM_IRI, ontologyIRI + "#term3");
		hit3.set(AbstractOntologyRepository.ONTOLOGY_TERM, "ontology term 3");
		hit3.set(AbstractOntologyRepository.SYNONYMS, "OT-3");
		hit3.set(AbstractOntologyRepository.ID, "ontology-3");

		SearchService searchService = mock(SearchService.class);
		when(
				searchService.search(
						new QueryImpl().eq(OntologyTermIndexRepository.ENTITY_TYPE,
								AbstractOntologyRepository.TYPE_ONTOLOGYTERM).pageSize(1), entityMetaData)).thenReturn(
				Arrays.<org.molgenis.data.Entity> asList(hit1));

		when(
				searchService.search(new QueryImpl().eq(AbstractOntologyRepository.ENTITY_TYPE,
						OntologyTermIndexRepository.TYPE_ONTOLOGYTERM), entityMetaData)).thenReturn(
				Arrays.<org.molgenis.data.Entity> asList(hit1, hit2, hit3));

		when(
				searchService.search(
						new QueryImpl()
								.eq(AbstractOntologyRepository.PARENT_NODE_PATH, "1.2")
								.and()
								.eq(AbstractOntologyRepository.ENTITY_TYPE,
										AbstractOntologyRepository.TYPE_ONTOLOGYTERM), entityMetaData)).thenReturn(
				Arrays.<org.molgenis.data.Entity> asList(hit2, hit3));

		when(
				searchService.search(
						new QueryImpl()
								.eq(AbstractOntologyRepository.NODE_PATH, "1.2.3")
								.and()
								.eq(AbstractOntologyRepository.ENTITY_TYPE,
										AbstractOntologyRepository.TYPE_ONTOLOGYTERM), entityMetaData)).thenReturn(
				Arrays.<org.molgenis.data.Entity> asList(hit2));
		when(searchService.search(new QueryImpl().eq(OntologyTermQueryRepository.ID, "ontology-3"), entityMetaData))
				.thenReturn(Arrays.<org.molgenis.data.Entity> asList(hit3));
		when(searchService.count(new QueryImpl().pageSize(Integer.MAX_VALUE).offset(Integer.MIN_VALUE), entityMetaData))
				.thenReturn(new Long(3));

		Entity hit = mock(Entity.class);
		when(hit.getAttributeNames()).thenReturn(Arrays.asList("OMIM", "HPO"));
		when(searchService.search(new QueryImpl().pageSize(1), entityMetaData)).thenReturn(Arrays.asList(hit));

		DataService dataService = mock(DataService.class);
		OntologyService ontologyService = mock(OntologyService.class);

		ontologyTermIndexRepository = new OntologyTermQueryRepository("test-ontology", searchService, dataService,
				ontologyService);
	}

	@Test
	public void count()
	{
		assertEquals(ontologyTermIndexRepository.count(new QueryImpl()), 3);
	}

	@Test
	public void findAll()
	{
		for (Entity entity : ontologyTermIndexRepository.findAll(new QueryImpl()))
		{
			Object rootObject = entity.get(OntologyTermIndexRepository.ROOT);
			if (rootObject != null && Boolean.parseBoolean(rootObject.toString()))
			{
				List<String> validOntologyTermIris = Arrays.asList(ontologyIRI + "#term2", ontologyIRI + "#term3");
				for (Entity subEntity : ontologyTermIndexRepository.findAll(new QueryImpl().eq(
						OntologyTermIndexRepository.PARENT_NODE_PATH, entity.get(OntologyTermIndexRepository.NODE_PATH)
								.toString())))
				{
					assertTrue(validOntologyTermIris.contains(subEntity.get(
							OntologyTermIndexRepository.ONTOLOGY_TERM_IRI).toString()));
				}
			}
		}
	}

	@Test
	public void findOneQuery()
	{
		Entity entity = ontologyTermIndexRepository.findOne(new QueryImpl().eq(OntologyTermIndexRepository.NODE_PATH,
				"1.2.3"));
		assertEquals(entity.get(OntologyTermIndexRepository.SYNONYMS).toString(), "OT-2");
	}

	@Test
	public void findOneObject()
	{
		Entity entity = ontologyTermIndexRepository.findOne("ontology-3");
		assertEquals(entity.get(OntologyTermIndexRepository.SYNONYMS).toString(), "OT-3");
	}

}

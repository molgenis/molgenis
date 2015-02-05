package org.molgenis.omx.biobankconnect.ontologytree;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.ontology.beans.OntologyTermEntity;
import org.molgenis.ontology.repository.AbstractOntologyRepository;
import org.molgenis.ontology.repository.OntologyIndexRepository;
import org.molgenis.ontology.repository.OntologyQueryRepository;
import org.molgenis.ontology.repository.OntologyTermIndexRepository;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class OntologyTermIndexEntityTest
{
	EntityMetaData entityMetaData;

	OntologyTermEntity ontologyTermIndexEntity;

	@BeforeClass
	public void setUp() throws OWLOntologyCreationException
	{
		MapEntity hit1 = new MapEntity();
		hit1.set(OntologyTermIndexRepository.ROOT, true);
		hit1.set(OntologyTermIndexRepository.LAST, false);
		hit1.set(OntologyTermIndexRepository.NODE_PATH, "1");
		hit1.set(OntologyIndexRepository.ONTOLOGY_IRI, "http://www.ontology.test");
		hit1.set(OntologyTermIndexRepository.ONTOLOGY_TERM_IRI, "http://www.ontology.test#term1");
		hit1.set(AbstractOntologyRepository.ID, "forged-id");

		MapEntity hit2 = new MapEntity();
		hit2.set(OntologyTermIndexRepository.ROOT, false);
		hit2.set(OntologyTermIndexRepository.LAST, true);
		hit2.set(OntologyTermIndexRepository.NODE_PATH, "1.2");
		hit2.set(OntologyTermIndexRepository.PARENT_NODE_PATH, "1");
		hit2.set(OntologyTermIndexRepository.PARENT_ONTOLOGY_TERM_IRI, "http://www.ontology.test");
		hit2.set(OntologyIndexRepository.ONTOLOGY_IRI, "http://www.ontology.test");
		hit2.set(OntologyTermIndexRepository.ONTOLOGY_TERM_IRI, "http://www.ontology.test#term2");
		hit2.set(AbstractOntologyRepository.ID, "forged-id-2");

		MapEntity hit3 = new MapEntity();
		hit3.set(OntologyTermIndexRepository.ROOT, false);
		hit3.set(OntologyTermIndexRepository.LAST, true);
		hit3.set(OntologyTermIndexRepository.NODE_PATH, "1.3");
		hit3.set(OntologyTermIndexRepository.PARENT_NODE_PATH, "1");
		hit3.set(OntologyTermIndexRepository.PARENT_ONTOLOGY_TERM_IRI, "http://www.ontology.test");
		hit3.set(OntologyIndexRepository.ONTOLOGY_IRI, "http://www.ontology.test");
		hit3.set(OntologyTermIndexRepository.ONTOLOGY_TERM_IRI, "http://www.ontology.test#term3");
		hit3.set(AbstractOntologyRepository.ID, "forged-id-3");

		SearchService searchService = mock(SearchService.class);
		when(
				searchService.search(new QueryImpl().pageSize(10000).eq(OntologyTermIndexRepository.ROOT, true),
						entityMetaData)).thenReturn(Arrays.<Entity> asList(hit1));

		when(
				searchService.search(
						new QueryImpl()
								.eq(OntologyTermIndexRepository.PARENT_NODE_PATH, "1")
								.and()
								.eq(OntologyTermIndexRepository.PARENT_ONTOLOGY_TERM_IRI,
										"http://www.ontology.test#term1").pageSize(Integer.MAX_VALUE), null))
				.thenReturn(Arrays.<Entity> asList(hit2, hit3));

		OntologyQueryRepository ontologyIndexRepository = mock(OntologyQueryRepository.class);
		ontologyTermIndexEntity = new OntologyTermEntity(hit1, ontologyIndexRepository.getEntityMetaData(),
				searchService);
	}

	@Test
	public void get()
	{
		assertEquals(ontologyTermIndexEntity.get(OntologyTermIndexRepository.ID).toString(), "forged-id");
		assertEquals(Boolean.parseBoolean(ontologyTermIndexEntity.get(OntologyTermIndexRepository.ROOT).toString()),
				true);
		Object attributes = ontologyTermIndexEntity.get("attributes");
		if (attributes instanceof List<?>)
		{
			assertEquals(((List<?>) attributes).size(), 2);
			List<String> ids = Arrays.asList("forged-id-2", "forged-id-3");
			for (Object object : (List<?>) attributes)
			{
				if (object instanceof OntologyTermEntity)
				{
					assertTrue(ids.contains(((OntologyTermEntity) object).getIdValue().toString()));
				}
			}
		}
	}
}
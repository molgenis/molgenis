package org.molgenis.omx.biobankconnect.ontologyservice;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.*;

import com.google.common.collect.Lists;
import org.molgenis.data.DataService;
import org.molgenis.data.QueryRule;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.ontology.Ontology;
import org.molgenis.ontology.OntologyService;
import org.molgenis.ontology.OntologyTerm;
import org.molgenis.ontology.repository.OntologyIndexRepository;
import org.molgenis.ontology.repository.OntologyTermIndexRepository;
import org.molgenis.ontology.repository.OntologyTermQueryRepository;
import org.molgenis.ontology.service.OntologyServiceImpl;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class OntologyServiceTest
{
	OntologyService ontologyService;
    private DataService dataService;
    private SearchService searchService;

    @BeforeMethod
	public void setUp() throws OWLOntologyCreationException
	{
        DefaultEntityMetaData meta = new DefaultEntityMetaData("ontology");
        meta.addAttributeMetaData(new DefaultAttributeMetaData(OntologyTermIndexRepository.ENTITY_TYPE));
        meta.addAttributeMetaData(new DefaultAttributeMetaData(OntologyTermIndexRepository.ONTOLOGY_IRI));
        meta.addAttributeMetaData(new DefaultAttributeMetaData(OntologyTermIndexRepository.ONTOLOGY_NAME));
        meta.addAttributeMetaData(new DefaultAttributeMetaData(OntologyTermIndexRepository.LAST));
        meta.addAttributeMetaData(new DefaultAttributeMetaData(OntologyTermIndexRepository.ROOT));
        meta.addAttributeMetaData(new DefaultAttributeMetaData(OntologyTermIndexRepository.NODE_PATH));
        meta.addAttributeMetaData(new DefaultAttributeMetaData(OntologyTermIndexRepository.ONTOLOGY_TERM_IRI));
        meta.addAttributeMetaData(new DefaultAttributeMetaData(OntologyTermIndexRepository.ONTOLOGY_TERM));
        meta.addAttributeMetaData(new DefaultAttributeMetaData(OntologyTermIndexRepository.SYNONYMS));
        meta.addAttributeMetaData(new DefaultAttributeMetaData(OntologyTermIndexRepository.ID));
        meta.setIdAttribute(OntologyIndexRepository.ID);

        MapEntity hit1 = new MapEntity();
        hit1.set(OntologyTermIndexRepository.ENTITY_TYPE, OntologyIndexRepository.TYPE_ONTOLOGY);
        hit1.set(OntologyTermIndexRepository.ONTOLOGY_IRI, "http://www.ontology.test");
        hit1.set(OntologyTermIndexRepository.ONTOLOGY_NAME, "test ontology");
        hit1.set(OntologyTermIndexRepository.ID, "ontology-1");

		MapEntity hit2 = new MapEntity();
        hit2.set(OntologyTermIndexRepository.ENTITY_TYPE, OntologyIndexRepository.TYPE_ONTOLOGY);
        hit2.set(OntologyTermIndexRepository.ONTOLOGY_IRI, "http://www.another.ontology.test");
        hit2.set(OntologyTermIndexRepository.ONTOLOGY_NAME, "another ontology");
        hit2.set(OntologyTermIndexRepository.ID, "ontology-2");

        MapEntity hit3 = new MapEntity();
        hit3.set(OntologyTermIndexRepository.ENTITY_TYPE, OntologyTermIndexRepository.TYPE_ONTOLOGYTERM);
        hit3.set(OntologyTermIndexRepository.ONTOLOGY_IRI, "http://www.ontology.test");
        hit3.set(OntologyTermIndexRepository.ONTOLOGY_NAME, "test ontology");
        hit3.set(OntologyTermIndexRepository.LAST, false);
        hit3.set(OntologyTermIndexRepository.ROOT, true);
        hit3.set(OntologyTermIndexRepository.NODE_PATH, "1.2");
        hit3.set(OntologyTermIndexRepository.ONTOLOGY_TERM_IRI, "http://www.ontology.test#term1");
        hit3.set(OntologyTermIndexRepository.ONTOLOGY_TERM, "ontology term 1");
        hit3.set(OntologyTermIndexRepository.SYNONYMS, "OT-1");
        hit3.set(OntologyTermIndexRepository.ID, "ontologyterm-1");

        MapEntity hit4 = new MapEntity();
        hit4.set(OntologyTermIndexRepository.ENTITY_TYPE, OntologyTermIndexRepository.TYPE_ONTOLOGYTERM);
        hit4.set(OntologyTermIndexRepository.ONTOLOGY_IRI, "http://www.ontology.test");
        hit4.set(OntologyTermIndexRepository.ONTOLOGY_NAME, "test ontology");
        hit4.set(OntologyTermIndexRepository.LAST, false);
        hit4.set(OntologyTermIndexRepository.ROOT, false);
        hit4.set(OntologyTermIndexRepository.NODE_PATH, "1.2.3");
        hit4.set(OntologyTermIndexRepository.PARENT_NODE_PATH, "1.2");
        hit4.set(OntologyTermIndexRepository.PARENT_ONTOLOGY_TERM_IRI, "http://www.ontology.test#term1");
        hit4.set(OntologyTermIndexRepository.ONTOLOGY_TERM_IRI, "http://www.ontology.test#term2");
        hit4.set(OntologyTermIndexRepository.ONTOLOGY_TERM, "ontology term 2");
        hit4.set(OntologyTermIndexRepository.SYNONYMS, "OntologyTerm two");
        hit4.set("score", 2.5);
        hit4.set(OntologyTermIndexRepository.ID, "ontologyterm-2");

        MapEntity hit5 = new MapEntity();
        hit5.set(OntologyTermIndexRepository.ENTITY_TYPE, OntologyTermIndexRepository.TYPE_ONTOLOGYTERM);
        hit5.set(OntologyTermIndexRepository.ONTOLOGY_IRI, "http://www.ontology.test");
        hit5.set(OntologyTermIndexRepository.ONTOLOGY_NAME, "test ontology");
        hit5.set(OntologyTermIndexRepository.LAST, false);
        hit5.set(OntologyTermIndexRepository.ROOT, false);
        hit5.set(OntologyTermIndexRepository.NODE_PATH, "1.2.4");
        hit5.set(OntologyTermIndexRepository.PARENT_NODE_PATH, "1.2");
        hit5.set(OntologyTermIndexRepository.PARENT_ONTOLOGY_TERM_IRI, "http://www.ontology.test#term1");
        hit5.set(OntologyTermIndexRepository.ONTOLOGY_TERM_IRI, "http://www.ontology.test#term3");
        hit5.set(OntologyTermIndexRepository.ONTOLOGY_TERM, "ontology term 3");
        hit5.set(OntologyTermIndexRepository.SYNONYMS, "OntologyTerm three");
        hit5.set("score", 4.5);
        hit5.set(OntologyTermIndexRepository.ID, "ontologyterm-3");

        searchService = mock(SearchService.class);
		when(
				searchService.search(new QueryImpl().eq(OntologyIndexRepository.ENTITY_TYPE,
						OntologyIndexRepository.TYPE_ONTOLOGY).pageSize(Integer.MAX_VALUE), meta)).thenReturn(
				Arrays.<org.molgenis.data.Entity> asList(hit1, hit2));

		when(
				searchService.search(new QueryImpl()
						.eq(OntologyIndexRepository.ENTITY_TYPE, OntologyIndexRepository.TYPE_ONTOLOGY).and()
						.eq(OntologyIndexRepository.ONTOLOGY_IRI, "http://www.ontology.test")
						.pageSize(Integer.MAX_VALUE), meta)).thenReturn(Arrays.<org.molgenis.data.Entity>asList(hit1));
		when(
				searchService.search(new QueryImpl()
						.eq(OntologyTermIndexRepository.NODE_PATH, "1.2").and()
						.eq(OntologyTermIndexRepository.ONTOLOGY_TERM_IRI, "http://www.ontology.test#term1")
						.pageSize(5000), meta)).thenReturn(Arrays.<org.molgenis.data.Entity>asList(hit3));

		when(
				searchService.search(new QueryImpl()
						.eq(OntologyIndexRepository.ENTITY_TYPE, OntologyTermQueryRepository.TYPE_ONTOLOGYTERM).and()
						.eq(OntologyTermQueryRepository.ONTOLOGY_TERM_IRI, "http://www.ontology.test#term1"), meta)).thenReturn(Arrays.<org.molgenis.data.Entity>asList(hit3));


        when(
                searchService.search(new QueryImpl()
                        .eq(OntologyTermQueryRepository.ENTITY_TYPE, OntologyTermQueryRepository.TYPE_ONTOLOGYTERM).and()
                        .eq(OntologyTermIndexRepository.PARENT_NODE_PATH, "1.2").and()
                        .eq(OntologyTermIndexRepository.PARENT_ONTOLOGY_TERM_IRI, "http://www.ontology.test#term1")
                        .pageSize(Integer.MAX_VALUE), meta)).thenReturn(Arrays.<org.molgenis.data.Entity>asList(hit4, hit5));

        when(
                searchService.search(new QueryImpl()
                .eq(OntologyTermQueryRepository.ENTITY_TYPE, OntologyTermQueryRepository.TYPE_ONTOLOGYTERM)
                .and().eq(OntologyTermIndexRepository.ROOT, true), meta)).thenReturn(Arrays.<org.molgenis.data.Entity>asList(hit3));

        when(
				searchService.search(new QueryImpl().eq(
						OntologyTermIndexRepository.ROOT, true).pageSize(Integer.MAX_VALUE), null)).thenReturn(Arrays.<org.molgenis.data.Entity>asList(hit3));

        dataService = mock(DataService.class);
        when(dataService.getEntityMetaData("test ontology")).thenReturn(meta);
        when(dataService.getEntityMetaData("ontologyindex")).thenReturn(meta);

		List<QueryRule> rules = new ArrayList<QueryRule>();
		rules.add(new QueryRule(OntologyTermIndexRepository.SYNONYMS, Operator.SEARCH, "ontologyterm~0.8"));
		new QueryRule(OntologyTermIndexRepository.SYNONYMS, Operator.SEARCH, "three~0.8");
		QueryRule finalQuery = new QueryRule(rules);
		finalQuery.setOperator(Operator.SHOULD);
		when(
                searchService.search(new QueryImpl(finalQuery)
                        .pageSize(100), null)
        ).thenReturn(Arrays.<org.molgenis.data.Entity>asList(hit4, hit5));

		ontologyService = new OntologyServiceImpl(searchService, dataService);
	}

    @AfterMethod
    public void resetMocks(){
        if(dataService!=null)
            reset(dataService);
       if(searchService!=null)
            reset(searchService);
    }

	@Test
	public void findOntologyTerm()
	{
        //FIXME: implement findOntologyTerms
		Iterable<OntologyTerm> hit = ontologyService.findOntologyTerms("1.2", "http://www.ontology.test#term1");
		//assertEquals(hit.iterator().next().getIRI(), "http://www.ontology.test");
	}

	@Test
	public void getAllOntologies()
	{
		List<String> validOntologyIris = Arrays.asList("http://www.ontology.test", "http://www.another.ontology.test");
		for (Ontology ontology : ontologyService.getAllOntologies())
		{
			assertTrue(validOntologyIris.contains(ontology.getIri()));
		}
	}

	@Test
	public void getChildren()
	{
        OntologyTerm parent = ontologyService.getOntologyTerm("http://www.ontology.test#term1", "http://www.ontology.test");
		String ontologyIri = parent.getOntology().getIri();
        String ontologyTermIri = parent.getIRI();
        Iterable<OntologyTerm> children = ontologyService.getChildOntologyTerms(ontologyIri, ontologyTermIri);
        assertEquals(Lists.newArrayList(children).size(), 2);
		List<String> validedOntologyTermIri = Arrays.asList("http://www.ontology.test#term2",
				"http://www.ontology.test#term3");
		for (OntologyTerm hit : children)
		{
			assertTrue(validedOntologyTermIri.contains(hit.getIRI()));
		}
	}

	@Test
	public void getOntologyByUrl()
	{
        Ontology hit = ontologyService.getOntology("http://www.ontology.test");
		assertEquals(hit.getLabel(), "test ontology");

	}

	@Test
	public void getRootOntologyTerms()
	{
        Iterable<OntologyTerm> rootHits = ontologyService.getRootOntologyTerms("http://www.ontology.test");
		List<OntologyTerm> list = Lists.newArrayList(rootHits);
        assertEquals(list.size(), 1);
		assertEquals(list.get(0).getLabel(), "ontology term 1");
	}

	 @Test
	 public void search()
	 {
     //FIXME: search is not implemented
	 /**OntologyServiceResult result = ontologyService.search("http://www.ontology.test", "OntologyTerm three");
     List<Map<String,Object>> searchHits = result.getOntologyTerms();
     assertEquals(searchHits.size(), 2);
	 assertEquals(((OntologyTerm)searchHits.get(0)).getIRI(), "ontologyterm-3");
	 assertEquals(((OntologyTerm)searchHits.get(1)).getIRI(), "ontologyterm-2");**/
	 }
}

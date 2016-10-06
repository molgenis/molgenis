package org.molgenis.ontology.core.repository;

import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.QueryRule;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.ontology.core.meta.*;
import org.molgenis.ontology.core.model.OntologyTerm;
import org.molgenis.test.data.AbstractMolgenisSpringTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableSet.of;
import static java.util.Arrays.asList;
import static java.util.Collections.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.QueryRule.Operator.*;
import static org.molgenis.ontology.core.meta.OntologyTermMetaData.*;
import static org.testng.Assert.assertEquals;

@ContextConfiguration(classes = OntologyTermRepositoryTest.Config.class)
public class OntologyTermRepositoryTest extends AbstractMolgenisSpringTest
{
	@Autowired
	DataService dataService;

	@Autowired
	OntologyTermRepository ontologyTermRepository;

	@Autowired
	private OntologyMetaData ontologyMetaData;

	@Autowired
	private OntologyTermMetaData ontologyTermMetaData;

	@Autowired
	private OntologyTermNodePathMetaData ontologyTermNodePathMetaData;

	private OntologyTermEntity ontologyTermEntity;

	@Captor
	ArgumentCaptor<Query<OntologyTermEntity>> argumentCaptor;

	// @Captor
	// ArgumentCaptor<String> argumentCaptor;
	//
	// @Captor
	// ArgumentCaptor<Query<OntologyTermEntity>> argumentCaptor;

	@BeforeTest
	public void beforeTest()
	{
		MockitoAnnotations.initMocks(this);

		OntologyEntity ontologyEntity = mock(OntologyEntity.class);
		when(ontologyEntity.getId()).thenReturn("34");

		OntologyTermSynonym ontologyTermSynonym = mock(OntologyTermSynonym.class);
		when(ontologyTermSynonym.getOntologyTermSynonym()).thenReturn("Ontology term synonym");

		ontologyTermEntity = mock(OntologyTermEntity.class);
		when(ontologyTermEntity.getId()).thenReturn("12");
		when(ontologyTermEntity.getOntology()).thenReturn(ontologyEntity);
		when(ontologyTermEntity.getOntologyTermIri()).thenReturn("http://www.test.nl/iri");
		when(ontologyTermEntity.getOntologyTermName()).thenReturn("Ontology term");
		when(ontologyTermEntity.getOntologyTermSynonyms()).thenReturn(singleton(ontologyTermSynonym));
	}

	@Test
	public void testFindExcatOntologyTerms()
	{
		OntologyEntity ontologyEntity = mock(OntologyEntity.class);
		when(ontologyEntity.getId()).thenReturn("34");
		when(ontologyEntity.getIRI()).thenReturn("http://www.test.nl/");
		when(ontologyEntity.getOntologyName()).thenReturn("test");

		OntologyTermSynonym synonymEntity1 = mock(OntologyTermSynonym.class);
		when(synonymEntity1.getId()).thenReturn("synonym-1");
		when(synonymEntity1.getOntologyTermSynonym()).thenReturn("Weight Reduction Diet");

		OntologyTermSynonym synonymEntity2 = mock(OntologyTermSynonym.class);
		when(synonymEntity2.getId()).thenReturn("synonym-2");
		when(synonymEntity2.getOntologyTermSynonym()).thenReturn("Weight loss Diet");

		OntologyTermSynonym synonymEntity3 = mock(OntologyTermSynonym.class);
		when(synonymEntity3.getId()).thenReturn("synonym-3");
		when(synonymEntity3.getOntologyTermSynonym()).thenReturn("Diet, Reducing");

		OntologyTermEntity ontologyTermEntity1 = mock(OntologyTermEntity.class);
		when(ontologyTermEntity1.getId()).thenReturn("1");
		when(ontologyTermEntity1.getOntology()).thenReturn(ontologyEntity);
		when(ontologyTermEntity1.getOntologyTermIri()).thenReturn("http://www.test.nl/iri/1");
		when(ontologyTermEntity1.getOntologyTermName()).thenReturn("Diet, Reducing");
		when(ontologyTermEntity1.getOntologyTermSynonyms())
				.thenReturn(asList(synonymEntity1, synonymEntity2, synonymEntity3));
		when(ontologyTermEntity1.getOntologyTermNodePaths()).thenReturn(emptyList());
		when(ontologyTermEntity1.getOntologyTermDynamicAnnotations()).thenReturn(emptyList());

		OntologyTermSynonym synonymEntity4 = mock(OntologyTermSynonym.class);
		when(synonymEntity4.getId()).thenReturn("synonym-4");
		when(synonymEntity4.getOntologyTermSynonym()).thenReturn("Weight");

		OntologyTermEntity ontologyTermEntity2 = mock(OntologyTermEntity.class);
		when(ontologyTermEntity2.getId()).thenReturn("12");
		when(ontologyTermEntity2.getOntology()).thenReturn(ontologyEntity);
		when(ontologyTermEntity2.getOntologyTermIri()).thenReturn("http://www.test.nl/iri/2");
		when(ontologyTermEntity2.getOntologyTermName()).thenReturn("Weight");
		when(ontologyTermEntity2.getOntologyTermSynonyms()).thenReturn(singleton(synonymEntity4));
		when(ontologyTermEntity2.getOntologyTermNodePaths()).thenReturn(emptyList());
		when(ontologyTermEntity2.getOntologyTermDynamicAnnotations()).thenReturn(emptyList());

		List<QueryRule> rules = Arrays
				.asList(new QueryRule(ONTOLOGY, Operator.IN, Arrays.asList("1", "2")), new QueryRule(Operator.AND),
						new QueryRule(Arrays.asList(
								new QueryRule(OntologyTermMetaData.ONTOLOGY_TERM_SYNONYM, Operator.FUZZY_MATCH,
										"weight"))));

		when(dataService.findAll(ONTOLOGY_TERM, new QueryImpl<OntologyTermEntity>(rules).pageSize(100),
				OntologyTermEntity.class)).thenReturn(Stream.of(ontologyTermEntity1, ontologyTermEntity2));

		List<OntologyTerm> exactOntologyTerms = ontologyTermRepository
				.findExcatOntologyTerms(asList("1", "2"), of("weight"), 100);

		assertEquals(exactOntologyTerms, singletonList(
				OntologyTerm.create("12", "http://www.test.nl/iri/2", "Weight", Arrays.asList("Weight"))));
	}

	@Test
	public void testFindOntologyTerms()
	{
		QueryRule innerQueryRule = new QueryRule(
				asList(new QueryRule(OntologyTermMetaData.ONTOLOGY_TERM_SYNONYM, FUZZY_MATCH, "term1"),
						new QueryRule(OR),
						new QueryRule(OntologyTermMetaData.ONTOLOGY_TERM_SYNONYM, FUZZY_MATCH, "term2"),
						new QueryRule(OR),
						new QueryRule(OntologyTermMetaData.ONTOLOGY_TERM_SYNONYM, FUZZY_MATCH, "term3")));

		List<QueryRule> rules = asList(new QueryRule(ONTOLOGY, IN, asList("1", "2")), new QueryRule(AND),
				innerQueryRule);

		when(dataService.findAll(eq(ONTOLOGY_TERM), argumentCaptor.capture(), eq(OntologyTermEntity.class)))
				.thenReturn(Stream.of(ontologyTermEntity));

		List<OntologyTerm> terms = ontologyTermRepository
				.findOntologyTerms(asList("1", "2"), of("term1", "term2", "term3"), 100);

		assertEquals(terms, singletonList(OntologyTerm
				.create("12", "http://www.test.nl/iri", "Ontology term", singletonList("Ontology term synonym"))));

		assertEquals(argumentCaptor.getValue().toString(),
				new QueryImpl<OntologyTermEntity>(rules).pageSize(100).toString());
	}

	@Test
	public void testGetChildOntologyTermsByNodePath()
	{
		Entity ontologyEntity = new DynamicEntity(ontologyMetaData);
		ontologyEntity.set(OntologyMetaData.ONTOLOGY_IRI, "http://www.molgenis.org");
		ontologyEntity.set(OntologyMetaData.ONTOLOGY_NAME, "molgenis");

		Entity nodePathEntity_1 = new DynamicEntity(ontologyTermNodePathMetaData);
		nodePathEntity_1.set(OntologyTermNodePathMetaData.NODE_PATH, "0[0].1[1]");
		Entity nodePathEntity_2 = new DynamicEntity(ontologyTermNodePathMetaData);
		nodePathEntity_2.set(OntologyTermNodePathMetaData.NODE_PATH, "0[0].1[1].0[2]");
		Entity nodePathEntity_3 = new DynamicEntity(ontologyTermNodePathMetaData);
		nodePathEntity_3.set(OntologyTermNodePathMetaData.NODE_PATH, "0[0].1[1].1[2]");

		Entity ontologyTerm_2 = new DynamicEntity(ontologyTermMetaData);
		ontologyTerm_2.set(ONTOLOGY, ontologyEntity);
		ontologyTerm_2.set(ONTOLOGY_TERM_IRI, "iri 2");
		ontologyTerm_2.set(ONTOLOGY_TERM_NAME, "name 2");
		ontologyTerm_2.set(OntologyTermMetaData.ONTOLOGY_TERM_NODE_PATH, asList(nodePathEntity_1, nodePathEntity_2));
		ontologyTerm_2.set(ONTOLOGY_TERM_SYNONYM, emptyList());

		Entity ontologyTerm_3 = new DynamicEntity(ontologyTermMetaData);
		ontologyTerm_3.set(ONTOLOGY, ontologyEntity);
		ontologyTerm_3.set(ONTOLOGY_TERM_IRI, "iri 3");
		ontologyTerm_3.set(ONTOLOGY_TERM_NAME, "name 3");
		ontologyTerm_3.set(OntologyTermMetaData.ONTOLOGY_TERM_NODE_PATH, singletonList(nodePathEntity_3));
		ontologyTerm_3.set(ONTOLOGY_TERM_SYNONYM, emptyList());

		when(dataService.findAll(ONTOLOGY_TERM, new QueryImpl<>(
				new QueryRule(OntologyTermMetaData.ONTOLOGY_TERM_NODE_PATH, QueryRule.Operator.FUZZY_MATCH,
						"\"0[0].1[1]\"")).and().eq(ONTOLOGY, ontologyEntity)))
				.thenReturn(Stream.of(ontologyTerm_2, ontologyTerm_3));

		// assertEquals(childOntologyTermsByNodePath.size(), 2);
		// assertEquals(childOntologyTermsByNodePath.get(0),
		// OntologyTermEntity.create("iri 2", "name 2", null, singletonList("name 2")));
		// assertEquals(childOntologyTermsByNodePath.get(1),
		// OntologyTermEntity.create("iri 3", "name 3", null, singletonList("name 3")));
	}

	@Test
	public void testCalculateNodePathDistance()
	{
		// Case 1
		assertEquals(ontologyTermRepository.calculateNodePathDistance("0[0].0[1]", "0[0].0[1].1[2]"), 1);

		// Case 2
		assertEquals(ontologyTermRepository.calculateNodePathDistance("0[0].0[1].1[2]", "0[0].0[1]"), 1);

		// Case 3
		assertEquals(ontologyTermRepository.calculateNodePathDistance("0[0].0[1].1[2].2[3]", "0[0].0[1].0[2].2[3]"), 4);

		// Case 4
		assertEquals(ontologyTermRepository.calculateNodePathDistance("0[0].0[1]", "0[0].0[1].0[2].1[3].2[4]"), 3);

		// Case 5
		assertEquals(ontologyTermRepository.calculateNodePathDistance("0[0].0[1]", "0[0].0[1]"), 0);
	}

	@Test
	public void testGetOntologyTerm()
	{
		when(dataService.findOne(ONTOLOGY_TERM,
				new QueryImpl<OntologyTermEntity>().eq(ONTOLOGY_TERM_IRI, "http://www.test.nl/iri"),
				OntologyTermEntity.class)).thenReturn(ontologyTermEntity);

		List<String> iris = Arrays.asList("http://www.test.nl/iri");

		List<OntologyTerm> ontologyTerm = ontologyTermRepository.getOntologyTerms(iris);
		assertEquals(ontologyTerm, Arrays.asList(OntologyTerm
				.create("12", "http://www.test.nl/iri", "Ontology term", singletonList("Ontology term synonym"))));
	}

	@Configuration
	@ComponentScan({ "org.molgenis.ontology.core.meta", "org.molgenis.ontology.core.model" })
	public static class Config
	{
		@Autowired
		OntologyTermMetaData ontologyTermMetaData;

		@Bean
		public DataService dataService()
		{
			return mock(DataService.class);
		}

		@Bean
		public OntologyTermRepository ontologyTermRepository()
		{
			return new OntologyTermRepository(dataService(), ontologyTermMetaData);
		}
	}
}
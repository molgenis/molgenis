package org.molgenis.ontology.sorta;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.ontology.sorta.meta.OntologyTermHitEntityMetaData.COMBINED_SCORE;
import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.QueryRule;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.ontology.core.meta.OntologyMetaData;
import org.molgenis.ontology.core.meta.OntologyTermDynamicAnnotationMetaData;
import org.molgenis.ontology.core.meta.OntologyTermMetaData;
import org.molgenis.ontology.core.meta.OntologyTermSynonymMetaData;
import org.molgenis.ontology.roc.InformationContentService;
import org.molgenis.ontology.sorta.service.SortaService;
import org.molgenis.ontology.sorta.service.impl.SortaServiceImpl;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;

public class SortaServiceImplTest
{
	private static final String ONTOLOGY_IRI = "http://www.molgenis.org/";

	private SortaService sortaService;

	private Entity firstInput;

	private Entity secondInput;

	private Entity thirdInput;

	@BeforeClass
	public void beforeClass()
	{
		DataService dataService = mock(DataService.class);

		firstInput = new MapEntity(ImmutableMap.<String, Object> of("Name", "hearing impairment"));

		sortaService = new SortaServiceImpl(dataService, new InformationContentService(dataService));

		// Mock ontology entity
		Entity ontologyEntity = new MapEntity();
		ontologyEntity.set(OntologyMetaData.ONTOLOGY_IRI, ONTOLOGY_IRI);
		// define dataService actions for test one
		when(dataService.findOne(OntologyMetaData.ENTITY_NAME,
				new QueryImpl().eq(OntologyMetaData.ONTOLOGY_IRI, ONTOLOGY_IRI))).thenReturn(ontologyEntity);

		when(dataService.count(OntologyTermMetaData.ENTITY_NAME,
				new QueryImpl().eq(OntologyTermMetaData.ONTOLOGY, ontologyEntity))).thenReturn((long) 100);

		QueryRule queryRule = new QueryRule(
				Arrays.asList(new QueryRule(OntologyTermMetaData.ONTOLOGY_TERM_SYNONYM, Operator.FUZZY_MATCH, "hear")));
		queryRule.setOperator(Operator.DIS_MAX);
		when(dataService.count(OntologyTermMetaData.ENTITY_NAME, new QueryImpl(queryRule))).thenReturn((long) 50);

		QueryRule queryRule2 = new QueryRule(Arrays
				.asList(new QueryRule(OntologyTermMetaData.ONTOLOGY_TERM_SYNONYM, Operator.FUZZY_MATCH, "impair")));
		queryRule2.setOperator(Operator.DIS_MAX);
		when(dataService.count(OntologyTermMetaData.ENTITY_NAME, new QueryImpl(queryRule2))).thenReturn((long) 50);

		when(dataService.findAll(OntologyMetaData.ENTITY_NAME)).thenReturn(Arrays.asList(ontologyEntity).stream());

		// ########################### TEST ONE ###########################
		// Mock the first ontologyterm entity only with name
		Entity ontologyTermSynonym_1 = new MapEntity();
		ontologyTermSynonym_1.set(OntologyTermSynonymMetaData.ONTOLOGY_TERM_SYNONYM, "hearing impairment");
		MapEntity ontologyTermEntity_1 = new MapEntity(OntologyTermMetaData.INSTANCE);
		ontologyTermEntity_1.set(OntologyTermMetaData.ID, 1);
		ontologyTermEntity_1.set(OntologyTermMetaData.ONTOLOGY, ontologyEntity);
		ontologyTermEntity_1.set(OntologyTermMetaData.ONTOLOGY_TERM_NAME, "hearing impairment");
		ontologyTermEntity_1.set(OntologyTermMetaData.ONTOLOGY_TERM_IRI, ONTOLOGY_IRI + "1");
		ontologyTermEntity_1.set(OntologyTermMetaData.ONTOLOGY_TERM_SYNONYM, Arrays.asList(ontologyTermSynonym_1));
		ontologyTermEntity_1.set(OntologyTermMetaData.ONTOLOGY_TERM_DYNAMIC_ANNOTATION, Collections.emptyList());

		// Mock the second ontologyterm entity only with name
		Entity ontologyTermSynonym_2 = new MapEntity();
		ontologyTermSynonym_2.set(OntologyTermSynonymMetaData.ONTOLOGY_TERM_SYNONYM, "mixed hearing impairment");
		Entity ontologyTermEntity_2 = new MapEntity(OntologyTermMetaData.INSTANCE);
		ontologyTermEntity_2.set(OntologyTermMetaData.ID, 2);
		ontologyTermEntity_2.set(OntologyTermMetaData.ONTOLOGY, ontologyEntity);
		ontologyTermEntity_2.set(OntologyTermMetaData.ONTOLOGY_TERM_NAME, "mixed hearing impairment");
		ontologyTermEntity_2.set(OntologyTermMetaData.ONTOLOGY_TERM_IRI, ONTOLOGY_IRI + "2");
		ontologyTermEntity_2.set(OntologyTermMetaData.ONTOLOGY_TERM_SYNONYM, Arrays.asList(ontologyTermSynonym_2));
		ontologyTermEntity_2.set(OntologyTermMetaData.ONTOLOGY_TERM_DYNAMIC_ANNOTATION, Collections.emptyList());

		// DataService action for regular matching ontologyterm synonyms
		QueryRule disMaxRegularQueryRule = new QueryRule(Arrays.asList(new QueryRule(
				OntologyTermMetaData.ONTOLOGY_TERM_SYNONYM, Operator.FUZZY_MATCH, "hear~0.8 impair~0.8")));
		disMaxRegularQueryRule.setOperator(Operator.DIS_MAX);

		List<QueryRule> finalQueryRules = Arrays.asList(
				new QueryRule(OntologyTermMetaData.ONTOLOGY, Operator.EQUALS, ontologyEntity),
				new QueryRule(Operator.AND), disMaxRegularQueryRule);

		when(dataService.findAll(OntologyTermMetaData.ENTITY_NAME, new QueryImpl(finalQueryRules).pageSize(50)))
				.thenReturn(Arrays.asList(ontologyTermEntity_1, ontologyTermEntity_2).stream());

		// DataService action for n-gram matching ontologyterm synonyms
		QueryRule disMaxNGramQueryRule = new QueryRule(Arrays.asList(
				new QueryRule(OntologyTermMetaData.ONTOLOGY_TERM_SYNONYM, Operator.FUZZY_MATCH_NGRAM, "hear impair")));
		disMaxNGramQueryRule.setOperator(Operator.DIS_MAX);
		when(dataService
				.findAll(OntologyTermMetaData.ENTITY_NAME,
						new QueryImpl(Arrays.asList(
								new QueryRule(OntologyTermMetaData.ONTOLOGY, Operator.EQUALS, ontologyEntity),
								new QueryRule(Operator.AND), disMaxNGramQueryRule)).pageSize(10)))
										.thenReturn(Arrays.asList(ontologyTermEntity_1, ontologyTermEntity_2).stream());

		// DataService action for querying specific ontologyterm based on ontologyIRI and ontologyTermIRI
		when(dataService.findOne(OntologyTermMetaData.ENTITY_NAME,
				new QueryImpl().eq(OntologyTermMetaData.ONTOLOGY_TERM_IRI, ONTOLOGY_IRI + "1").and()
						.eq(OntologyTermMetaData.ONTOLOGY, ontologyEntity))).thenReturn(ontologyTermEntity_1);

		when(dataService.findOne(OntologyTermMetaData.ENTITY_NAME,
				new QueryImpl().eq(OntologyTermMetaData.ONTOLOGY_TERM_IRI, ONTOLOGY_IRI + "2").and()
						.eq(OntologyTermMetaData.ONTOLOGY, ontologyEntity))).thenReturn(ontologyTermEntity_2);

		// ########################### TEST TWO ###########################
		secondInput = new MapEntity(ImmutableMap.of("Name", "input", "OMIM", "123456"));

		Entity ontologyTermSynonym_3 = new MapEntity();
		ontologyTermSynonym_3.set(OntologyTermSynonymMetaData.ONTOLOGY_TERM_SYNONYM, "ot_3");

		// Mock ontologyTermDynamicAnnotation entities
		Entity ontologyTermDynamicAnnotation_3_1 = new MapEntity();
		ontologyTermDynamicAnnotation_3_1.set(OntologyTermDynamicAnnotationMetaData.NAME, "OMIM");
		ontologyTermDynamicAnnotation_3_1.set(OntologyTermDynamicAnnotationMetaData.VALUE, "123456");
		ontologyTermDynamicAnnotation_3_1.set(OntologyTermDynamicAnnotationMetaData.LABEL, "OMIM:123456");

		// Mock ontologyTerm entity based on the previous entities defined
		Entity ontologyTermEntity_3 = new MapEntity(OntologyTermMetaData.INSTANCE);
		ontologyTermEntity_3.set(OntologyTermMetaData.ID, 3);
		ontologyTermEntity_3.set(OntologyTermMetaData.ONTOLOGY, ontologyEntity);
		ontologyTermEntity_3.set(OntologyTermMetaData.ONTOLOGY_TERM_NAME, "ot_3");
		ontologyTermEntity_3.set(OntologyTermMetaData.ONTOLOGY_TERM_IRI, ONTOLOGY_IRI + "3");
		ontologyTermEntity_3.set(OntologyTermMetaData.ONTOLOGY_TERM_SYNONYM, Arrays.asList(ontologyTermEntity_3));
		ontologyTermEntity_3.set(OntologyTermMetaData.ONTOLOGY_TERM_DYNAMIC_ANNOTATION,
				Arrays.asList(ontologyTermDynamicAnnotation_3_1));

		// DataService action for matching ontologyterm annotation
		QueryRule annotationQueryRule = new QueryRule(
				Arrays.asList(new QueryRule(OntologyTermDynamicAnnotationMetaData.NAME, Operator.EQUALS, "OMIM"),
						new QueryRule(Operator.AND),
						new QueryRule(OntologyTermDynamicAnnotationMetaData.VALUE, Operator.EQUALS, "123456")));

		when(dataService.findAll(OntologyTermDynamicAnnotationMetaData.ENTITY_NAME,
				new QueryImpl(Arrays.asList(annotationQueryRule)).pageSize(Integer.MAX_VALUE)))
						.thenReturn(Arrays.asList(ontologyTermDynamicAnnotation_3_1).stream());

		when(dataService
				.findAll(OntologyTermMetaData.ENTITY_NAME,
						new QueryImpl(Arrays.asList(
								new QueryRule(OntologyTermMetaData.ONTOLOGY, Operator.EQUALS, ontologyEntity),
								new QueryRule(Operator.AND),
								new QueryRule(OntologyTermMetaData.ONTOLOGY_TERM_DYNAMIC_ANNOTATION, Operator.IN,
										Arrays.asList(ontologyTermDynamicAnnotation_3_1))))
												.pageSize(Integer.MAX_VALUE)))
														.thenReturn(Arrays.asList(ontologyTermEntity_3).stream());

		// DataService action for elasticsearch regular matching ontologyterm synonyms
		QueryRule disMaxRegularQueryRule_2 = new QueryRule(Arrays
				.asList(new QueryRule(OntologyTermMetaData.ONTOLOGY_TERM_SYNONYM, Operator.FUZZY_MATCH, "input~0.8")));
		disMaxRegularQueryRule_2.setOperator(Operator.DIS_MAX);
		when(dataService
				.findAll(OntologyTermMetaData.ENTITY_NAME,
						new QueryImpl(Arrays.asList(
								new QueryRule(OntologyTermMetaData.ONTOLOGY, Operator.EQUALS, ontologyEntity),
								new QueryRule(Operator.AND), disMaxRegularQueryRule_2)).pageSize(49)))
										.thenReturn(Stream.empty());

		// DataService action for n-gram matching ontologyterm synonyms
		QueryRule disMaxNGramQueryRule_2 = new QueryRule(Arrays.asList(
				new QueryRule(OntologyTermMetaData.ONTOLOGY_TERM_SYNONYM, Operator.FUZZY_MATCH_NGRAM, "input")));
		disMaxNGramQueryRule_2.setOperator(Operator.DIS_MAX);
		when(dataService
				.findAll(OntologyTermMetaData.ENTITY_NAME,
						new QueryImpl(Arrays.asList(
								new QueryRule(OntologyTermMetaData.ONTOLOGY, Operator.EQUALS, ontologyEntity),
								new QueryRule(Operator.AND), disMaxNGramQueryRule_2)).pageSize(10)))
										.thenReturn(Stream.empty());

		// ########################### TEST THREE ###########################
		// Define the input for test three
		thirdInput = new MapEntity(ImmutableMap.of("Name", "proptosis, protruding eye, Exophthalmos "));

		Entity ontologyTermSynonym_4_1 = new MapEntity();
		ontologyTermSynonym_4_1.set(OntologyTermSynonymMetaData.ONTOLOGY_TERM_SYNONYM, "protruding eye");

		Entity ontologyTermSynonym_4_2 = new MapEntity();
		ontologyTermSynonym_4_2.set(OntologyTermSynonymMetaData.ONTOLOGY_TERM_SYNONYM, "proptosis");

		Entity ontologyTermSynonym_4_3 = new MapEntity();
		ontologyTermSynonym_4_3.set(OntologyTermSynonymMetaData.ONTOLOGY_TERM_SYNONYM, "Exophthalmos");

		// Mock ontologyTerm entity based on the previous entities defined
		Entity ontologyTermEntity_4 = new MapEntity(OntologyTermMetaData.INSTANCE);
		ontologyTermEntity_4.set(OntologyTermMetaData.ID, 4);
		ontologyTermEntity_4.set(OntologyTermMetaData.ONTOLOGY, ontologyEntity);
		ontologyTermEntity_4.set(OntologyTermMetaData.ONTOLOGY_TERM_NAME, "protruding eye");
		ontologyTermEntity_4.set(OntologyTermMetaData.ONTOLOGY_TERM_IRI, ONTOLOGY_IRI + "4");
		ontologyTermEntity_4.set(OntologyTermMetaData.ONTOLOGY_TERM_SYNONYM,
				Arrays.asList(ontologyTermSynonym_4_1, ontologyTermSynonym_4_2, ontologyTermSynonym_4_3));
		ontologyTermEntity_4.set(OntologyTermMetaData.ONTOLOGY_TERM_DYNAMIC_ANNOTATION, Collections.emptyList());

		// DataService action for elasticsearch regular matching ontologyterm synonyms
		QueryRule disMaxRegularQueryRule_3 = new QueryRule(
				Arrays.asList(new QueryRule(OntologyTermMetaData.ONTOLOGY_TERM_SYNONYM, Operator.FUZZY_MATCH,
						"proptosi~0.8 protrud~0.8 ey~0.8 exophthalmo~0.8")));
		disMaxRegularQueryRule_3.setOperator(Operator.DIS_MAX);

		when(dataService
				.findAll(OntologyTermMetaData.ENTITY_NAME,
						new QueryImpl(Arrays.asList(
								new QueryRule(OntologyTermMetaData.ONTOLOGY, Operator.EQUALS, ontologyEntity),
								new QueryRule(Operator.AND), disMaxRegularQueryRule_3)).pageSize(50)))
										.thenReturn(Arrays.asList(ontologyTermEntity_4).stream());

		// DataService action for elasticsearch ngram matching ontologyterm synonyms
		QueryRule disMaxNGramQueryRule_3 = new QueryRule(
				Arrays.asList(new QueryRule(OntologyTermMetaData.ONTOLOGY_TERM_SYNONYM, Operator.FUZZY_MATCH_NGRAM,
						"proptosi protrud ey exophthalmo")));
		disMaxNGramQueryRule_3.setOperator(Operator.DIS_MAX);

		when(dataService
				.findAll(OntologyTermMetaData.ENTITY_NAME,
						new QueryImpl(Arrays.asList(
								new QueryRule(OntologyTermMetaData.ONTOLOGY, Operator.EQUALS, ontologyEntity),
								new QueryRule(Operator.AND), disMaxNGramQueryRule_3)).pageSize(10)))
										.thenReturn(Arrays.asList(ontologyTermEntity_4).stream());
	}

	@Test
	public void findOntologyTermEntities()
	{
		// Test one: match only the name of input with ontologyterms
		Iterable<Entity> ontologyTerms_test1 = sortaService.findOntologyTermEntities(ONTOLOGY_IRI, firstInput);
		Iterator<Entity> iterator_test1 = ontologyTerms_test1.iterator();

		assertEquals(iterator_test1.hasNext(), true);
		Entity firstMatch_test1 = iterator_test1.next();
		assertEquals(firstMatch_test1.getDouble(COMBINED_SCORE).intValue(), 100);

		assertEquals(iterator_test1.hasNext(), true);
		Entity secondMatch_test1 = iterator_test1.next();
		assertEquals(secondMatch_test1.getDouble(COMBINED_SCORE).intValue(), new Double(85).intValue());

		assertEquals(iterator_test1.hasNext(), false);

		// Test two: match the database annotation of input with ontologyterms
		Iterable<Entity> ontologyTerms_test2 = sortaService.findOntologyTermEntities(ONTOLOGY_IRI, secondInput);
		Iterator<Entity> iterator_test2 = ontologyTerms_test2.iterator();

		assertEquals(iterator_test2.hasNext(), true);
		Entity firstMatch_test2 = iterator_test2.next();
		assertEquals(firstMatch_test2.getDouble(COMBINED_SCORE).intValue(), 100);

		assertEquals(iterator_test2.hasNext(), false);

		// Test three: match only the name of input with ontologyterms, since the name contains multiple synonyms
		// therefore add up all the scores from synonyms
		Iterable<Entity> ontologyTerms_test3 = sortaService.findOntologyTermEntities(ONTOLOGY_IRI, thirdInput);
		Iterator<Entity> iterator_test3 = ontologyTerms_test3.iterator();

		assertEquals(iterator_test3.hasNext(), true);
		Entity firstMatch_test3 = iterator_test3.next();
		assertEquals(firstMatch_test3.getDouble(COMBINED_SCORE).intValue(), 100);

		assertEquals(iterator_test3.hasNext(), false);
	}

	@Test
	public void getAllOntologyEntities()
	{
		Iterable<Entity> allOntologyEntities = sortaService.getAllOntologyEntities();

		Iterator<Entity> iterator = allOntologyEntities.iterator();

		assertEquals(iterator.hasNext(), true);

		Entity ontologyEntity = iterator.next();

		assertEquals(ontologyEntity.getString(OntologyMetaData.ONTOLOGY_IRI), ONTOLOGY_IRI);

		assertEquals(iterator.hasNext(), false);

	}

	@Test
	public void getOntologyEntity()
	{
		Entity ontologyEntity = sortaService.getOntologyEntity(ONTOLOGY_IRI);
		assertEquals(ONTOLOGY_IRI, ontologyEntity.getString(OntologyMetaData.ONTOLOGY_IRI));
	}

	@Test
	public void getOntologyTermEntity()
	{
		Entity firstOntologyTermEntity = sortaService.getOntologyTermEntity(ONTOLOGY_IRI + 1, ONTOLOGY_IRI);
		assertEquals(firstOntologyTermEntity.getString(OntologyTermMetaData.ONTOLOGY_TERM_NAME), "hearing impairment");

		Entity secondOntologyTermEntity = sortaService.getOntologyTermEntity(ONTOLOGY_IRI + 2, ONTOLOGY_IRI);
		assertEquals(secondOntologyTermEntity.getString(OntologyTermMetaData.ONTOLOGY_TERM_NAME),
				"mixed hearing impairment");
	}
}

package org.molgenis.ontology.roc;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import org.mockito.Mockito;
import org.molgenis.data.DataService;
import org.molgenis.data.QueryRule;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.ontology.core.meta.OntologyMetaData;
import org.molgenis.ontology.core.meta.OntologyTermMetaData;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

public class InformationContentServiceTest
{
	DataService dataService = Mockito.mock(DataService.class);
	InformationContentService informationContentService = new InformationContentService(dataService);

	@Test
	public void createStemmedWordSet()
	{
		Set<String> actualStemmedWordSet = informationContentService
				.createStemmedWordSet("hearing-impairment_eye ball");
		Set<String> expectedStemmedWordSet = Sets.newHashSet("hear", "impair", "ey", "ball");

		Assert.assertEquals(actualStemmedWordSet.size(), expectedStemmedWordSet.size());
		Assert.assertTrue(expectedStemmedWordSet.containsAll(actualStemmedWordSet));
	}

	@Test
	public void createWordIDF()
	{
		String ontologyIri = "http://www.molgenis.org";

		MapEntity ontologyEntity = new MapEntity(ImmutableMap.of(OntologyMetaData.ONTOLOGY_IRI, ontologyIri));

		Mockito.when(dataService.findOne(OntologyMetaData.ENTITY_NAME,
				new QueryImpl().eq(OntologyMetaData.ONTOLOGY_IRI, ontologyIri))).thenReturn(ontologyEntity);

		Mockito.when(dataService.count(OntologyTermMetaData.ENTITY_NAME,
				new QueryImpl().eq(OntologyTermMetaData.ONTOLOGY, ontologyEntity))).thenReturn((long) 100);

		QueryRule queryRule = new QueryRule(
				Arrays.asList(new QueryRule(OntologyTermMetaData.ONTOLOGY_TERM_SYNONYM, Operator.FUZZY_MATCH, "hear")));
		queryRule.setOperator(Operator.DIS_MAX);
		QueryRule finalQuery = new QueryRule(
				Arrays.asList(new QueryRule(OntologyTermMetaData.ONTOLOGY, Operator.EQUALS, ontologyEntity),
						new QueryRule(Operator.AND), queryRule));
		Mockito.when(dataService.count(OntologyTermMetaData.ENTITY_NAME, new QueryImpl(finalQuery)))
				.thenReturn((long) 30);

		QueryRule queryRule2 = new QueryRule(Arrays
				.asList(new QueryRule(OntologyTermMetaData.ONTOLOGY_TERM_SYNONYM, Operator.FUZZY_MATCH, "impair")));
		queryRule2.setOperator(Operator.DIS_MAX);
		QueryRule finalQuery2 = new QueryRule(
				Arrays.asList(new QueryRule(OntologyTermMetaData.ONTOLOGY, Operator.EQUALS, ontologyEntity),
						new QueryRule(Operator.AND), queryRule2));
		Mockito.when(dataService.count(OntologyTermMetaData.ENTITY_NAME, new QueryImpl(finalQuery2)))
				.thenReturn((long) 10);

		Map<String, Double> expectedWordIDF = informationContentService.createWordIDF("hearing impairment",
				ontologyIri);

		Assert.assertEquals(expectedWordIDF.get("hear").intValue(), 2);
		Assert.assertEquals(expectedWordIDF.get("impair").intValue(), 3);
	}

	@Test
	public void redistributedNGramScore()
	{
		String ontologyIri = "http://www.molgenis.org";

		MapEntity ontologyEntity = new MapEntity(ImmutableMap.of(OntologyMetaData.ONTOLOGY_IRI, ontologyIri));

		Mockito.when(dataService.findOne(OntologyMetaData.ENTITY_NAME,
				new QueryImpl().eq(OntologyMetaData.ONTOLOGY_IRI, ontologyIri))).thenReturn(ontologyEntity);

		Mockito.when(dataService.count(OntologyTermMetaData.ENTITY_NAME,
				new QueryImpl().eq(OntologyTermMetaData.ONTOLOGY, ontologyEntity))).thenReturn((long) 100);

		QueryRule queryRule = new QueryRule(
				Arrays.asList(new QueryRule(OntologyTermMetaData.ONTOLOGY_TERM_SYNONYM, Operator.FUZZY_MATCH, "hear")));
		queryRule.setOperator(Operator.DIS_MAX);
		QueryRule finalQuery = new QueryRule(
				Arrays.asList(new QueryRule(OntologyTermMetaData.ONTOLOGY, Operator.EQUALS, ontologyEntity),
						new QueryRule(Operator.AND), queryRule));
		Mockito.when(dataService.count(OntologyTermMetaData.ENTITY_NAME, new QueryImpl(finalQuery)))
				.thenReturn((long) 30);

		QueryRule queryRule2 = new QueryRule(Arrays
				.asList(new QueryRule(OntologyTermMetaData.ONTOLOGY_TERM_SYNONYM, Operator.FUZZY_MATCH, "impair")));
		queryRule2.setOperator(Operator.DIS_MAX);
		QueryRule finalQuery2 = new QueryRule(
				Arrays.asList(new QueryRule(OntologyTermMetaData.ONTOLOGY, Operator.EQUALS, ontologyEntity),
						new QueryRule(Operator.AND), queryRule2));
		Mockito.when(dataService.count(OntologyTermMetaData.ENTITY_NAME, new QueryImpl(finalQuery2)))
				.thenReturn((long) 10);

		Map<String, Double> redistributedNGramScore = informationContentService
				.redistributedNGramScore("hearing impairment", ontologyIri);
		Assert.assertEquals(redistributedNGramScore.get("hear").intValue(), -7);
		Assert.assertEquals(redistributedNGramScore.get("impair").intValue(), 7);
	}
}

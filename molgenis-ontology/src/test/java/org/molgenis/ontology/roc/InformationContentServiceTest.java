package org.molgenis.ontology.roc;

import com.google.common.collect.Sets;
import org.mockito.Mockito;
import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.QueryRule;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.ontology.core.config.OntologyTestConfig;
import org.molgenis.ontology.core.meta.Ontology;
import org.molgenis.ontology.core.meta.OntologyFactory;
import org.molgenis.ontology.core.meta.OntologyMetaData;
import org.molgenis.ontology.core.meta.OntologyTermMetaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.when;
import static org.molgenis.ontology.core.meta.OntologyMetaData.ONTOLOGY;
import static org.molgenis.ontology.core.meta.OntologyTermMetaData.ONTOLOGY_TERM;

@ContextConfiguration(classes = { InformationContentServiceTest.Config.class })
public class InformationContentServiceTest extends AbstractMolgenisSpringTest
{
	@Autowired
	private OntologyFactory ontologyFactory;

	private DataService dataService = Mockito.mock(DataService.class);
	private InformationContentService informationContentService = new InformationContentService(dataService);

	@Test
	public void createStemmedWordSet()
	{
		Set<String> actualStemmedWordSet = informationContentService.createStemmedWordSet(
				"hearing-impairment_eye ball");
		Set<String> expectedStemmedWordSet = Sets.newHashSet("hear", "impair", "ey", "ball");

		Assert.assertEquals(actualStemmedWordSet.size(), expectedStemmedWordSet.size());
		Assert.assertTrue(expectedStemmedWordSet.containsAll(actualStemmedWordSet));
	}

	@Test
	public void createWordIDF()
	{
		String ontologyIri = "http://www.molgenis.org";

		Ontology ontology = ontologyFactory.create();
		ontology.setOntologyIri(ontologyIri);
		when(dataService.findOne(ONTOLOGY,
				new QueryImpl<>().eq(OntologyMetaData.ONTOLOGY_IRI, ontologyIri))).thenReturn(ontology);

		when(dataService.count(ONTOLOGY_TERM,
				new QueryImpl<>().eq(OntologyTermMetaData.ONTOLOGY, ontology))).thenReturn((long) 100);

		QueryRule queryRule = new QueryRule(
				singletonList(new QueryRule(OntologyTermMetaData.ONTOLOGY_TERM_SYNONYM, Operator.FUZZY_MATCH, "hear")));
		queryRule.setOperator(Operator.DIS_MAX);
		QueryRule finalQuery = new QueryRule(
				asList(new QueryRule(OntologyTermMetaData.ONTOLOGY, Operator.EQUALS, ontology),
						new QueryRule(Operator.AND), queryRule));
		when(dataService.count(ONTOLOGY_TERM, new QueryImpl<>(finalQuery))).thenReturn((long) 30);

		QueryRule queryRule2 = new QueryRule(singletonList(
				new QueryRule(OntologyTermMetaData.ONTOLOGY_TERM_SYNONYM, Operator.FUZZY_MATCH, "impair")));
		queryRule2.setOperator(Operator.DIS_MAX);
		QueryRule finalQuery2 = new QueryRule(
				asList(new QueryRule(OntologyTermMetaData.ONTOLOGY, Operator.EQUALS, ontology),
						new QueryRule(Operator.AND), queryRule2));
		when(dataService.count(ONTOLOGY_TERM, new QueryImpl<>(finalQuery2))).thenReturn((long) 10);

		Map<String, Double> expectedWordIDF = informationContentService.createWordIDF("hearing impairment",
				ontologyIri);

		Assert.assertEquals(expectedWordIDF.get("hear").intValue(), 2);
		Assert.assertEquals(expectedWordIDF.get("impair").intValue(), 3);
	}

	@Test
	public void redistributedNGramScore()
	{
		String ontologyIri = "http://www.molgenis.org";

		Entity ontologyEntity = ontologyFactory.create();
		ontologyEntity.set(OntologyMetaData.ONTOLOGY_IRI, ontologyIri);

		when(dataService.findOne(ONTOLOGY,
				new QueryImpl<>().eq(OntologyMetaData.ONTOLOGY_IRI, ontologyIri))).thenReturn(ontologyEntity);

		when(dataService.count(ONTOLOGY_TERM,
				new QueryImpl<>().eq(OntologyTermMetaData.ONTOLOGY, ontologyEntity))).thenReturn((long) 100);

		QueryRule queryRule = new QueryRule(
				singletonList(new QueryRule(OntologyTermMetaData.ONTOLOGY_TERM_SYNONYM, Operator.FUZZY_MATCH, "hear")));
		queryRule.setOperator(Operator.DIS_MAX);
		QueryRule finalQuery = new QueryRule(
				asList(new QueryRule(OntologyTermMetaData.ONTOLOGY, Operator.EQUALS, ontologyEntity),
						new QueryRule(Operator.AND), queryRule));
		when(dataService.count(ONTOLOGY_TERM, new QueryImpl<>(finalQuery))).thenReturn((long) 30);

		QueryRule queryRule2 = new QueryRule(singletonList(
				new QueryRule(OntologyTermMetaData.ONTOLOGY_TERM_SYNONYM, Operator.FUZZY_MATCH, "impair")));
		queryRule2.setOperator(Operator.DIS_MAX);
		QueryRule finalQuery2 = new QueryRule(
				asList(new QueryRule(OntologyTermMetaData.ONTOLOGY, Operator.EQUALS, ontologyEntity),
						new QueryRule(Operator.AND), queryRule2));
		when(dataService.count(ONTOLOGY_TERM, new QueryImpl<>(finalQuery2))).thenReturn((long) 10);

		Map<String, Double> redistributedNGramScore = informationContentService.redistributedNGramScore(
				"hearing impairment", ontologyIri);
		Assert.assertEquals(redistributedNGramScore.get("hear").intValue(), -7);
		Assert.assertEquals(redistributedNGramScore.get("impair").intValue(), 7);
	}

	@Configuration
	@Import(OntologyTestConfig.class)
	public static class Config
	{

	}
}

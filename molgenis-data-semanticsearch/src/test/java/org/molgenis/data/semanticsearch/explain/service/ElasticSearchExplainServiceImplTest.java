package org.molgenis.data.semanticsearch.explain.service;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;

import org.apache.lucene.search.Explanation;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.collect.Sets;
import org.molgenis.data.QueryRule;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.meta.AttributeMetaDataMetaData;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;

public class ElasticSearchExplainServiceImplTest
{
	private ElasticSearchExplainService elasticSearchExplainService;
	private ExplainServiceHelper explainServiceHelper;

	@BeforeClass
	public void setup()
	{
		Client client = mock(Client.class);
		String indexName = "molgenis";
		explainServiceHelper = new ExplainServiceHelper();
		elasticSearchExplainService = new ElasticSearchExplainServiceImpl(client, indexName, explainServiceHelper);
	}

	@Test
	public void testRegExp()
	{
		String description = "weight(label:high in 328) [PerFieldSimilarity], result of:";
		String actual = explainServiceHelper.getMatchedWord(description);
		assertEquals(actual, "high");

		String description2 = "weight(label:length^0.5 in 328) [PerFieldSimilarity], result of:";
		String actual2 = explainServiceHelper.getMatchedWord(description2);
		assertEquals(actual2, "length");
	}

	@Test
	public void testRemoveBoostFromQuery()
	{
		String description = "Measurement^0.5 glucose^0.25 fasting^0.5";
		assertEquals(explainServiceHelper.removeBoostFromQuery(description), "Measurement glucose fasting");

		String description2 = "Measurement^5 glucose^5.0 fasting^00.52";
		assertEquals(explainServiceHelper.removeBoostFromQuery(description2), "Measurement glucose fasting");

		String description3 = "Measurement glucose 5 fasting43";
		assertEquals(explainServiceHelper.removeBoostFromQuery(description3), "Measurement glucose 5 fasting43");

		String description4 = "glycemia^0.03125";
		assertEquals(explainServiceHelper.removeBoostFromQuery(description4), "glycemia");
	}

	@Test
	public void testIsSingleQuery()
	{
		assertTrue(explainServiceHelper.termsConsistOfSingleWord(Sets.newHashSet("test", "test2", "test3")));
		assertFalse(explainServiceHelper.termsConsistOfSingleWord(Sets.newHashSet("test", "test2 test3")));
	}

	@Test
	public void testDiscoverMatchedQueries()
	{
		Explanation explanation_1 = new Explanation(Float.valueOf("5.381672"), "sum of:");
		Explanation explanation_2 = new Explanation(Float.valueOf("3.6267629"), "sum of:");
		explanation_2.addDetail(new Explanation(Float.valueOf("2.0587344"),
				"weight(label:high in 328) [PerFieldSimilarity], result of:"));
		explanation_2.addDetail(new Explanation(Float.valueOf("1.5680285"),
				"weight(label:blood in 328) [PerFieldSimilarity], result of:"));
		Explanation explanation_3 = new Explanation(Float.valueOf("1.754909"),
				"weight(label:medic in 328) [PerFieldSimilarity], result of:");
		explanation_1.addDetail(explanation_2);
		explanation_1.addDetail(explanation_3);

		String actual = explainServiceHelper.discoverMatchedQueries(explanation_1);
		assertEquals(actual, "high blood|medic");
	}

	@Test
	public void testRecursivelyFindQuery()
	{
		QueryRule queryRule_1 = new QueryRule(AttributeMetaDataMetaData.LABEL, Operator.FUZZY_MATCH, "hypertension");
		QueryRule queryRule_2 = new QueryRule(AttributeMetaDataMetaData.LABEL, Operator.FUZZY_MATCH,
				"hypertensive disorder");
		QueryRule queryRule_3 = new QueryRule(AttributeMetaDataMetaData.LABEL, Operator.FUZZY_MATCH,
				"high blood pressure");

		QueryRule queryRule_4 = new QueryRule(AttributeMetaDataMetaData.LABEL, Operator.FUZZY_MATCH, "drug");
		QueryRule queryRule_5 = new QueryRule(AttributeMetaDataMetaData.LABEL, Operator.FUZZY_MATCH, "medication");
		QueryRule queryRule_6 = new QueryRule(AttributeMetaDataMetaData.LABEL, Operator.FUZZY_MATCH, "pill");

		QueryRule disMaxQueryRule_1 = new QueryRule(Arrays.asList(queryRule_1, queryRule_2, queryRule_3));
		disMaxQueryRule_1.setOperator(Operator.DIS_MAX);

		QueryRule disMaxQueryRule_2 = new QueryRule(Arrays.asList(queryRule_4, queryRule_5, queryRule_6));
		disMaxQueryRule_2.setOperator(Operator.DIS_MAX);

		QueryRule shouldQueryRule = new QueryRule(Arrays.asList(disMaxQueryRule_1, disMaxQueryRule_2));
		shouldQueryRule.setOperator(Operator.SHOULD);

		QueryRule finalDisMaxQueryRule = new QueryRule(Arrays.asList(shouldQueryRule));
		finalDisMaxQueryRule.setOperator(Operator.DIS_MAX);

		assertEquals(explainServiceHelper.recursivelyFindQuery("high blood", finalDisMaxQueryRule.getNestedRules())
				.toString(), ImmutableMap.of("high blood pressure", 73.333).toString());

		assertEquals(explainServiceHelper.recursivelyFindQuery("medication", finalDisMaxQueryRule.getNestedRules())
				.toString(), ImmutableMap.of("medication", 100.0).toString());
	}

	@Test
	public void testReverseSearchQueryStrings()
	{
		Explanation explanation_1 = new Explanation(Float.valueOf("5.381672"), "sum of:");
		Explanation explanation_2 = new Explanation(Float.valueOf("3.6267629"), "sum of:");
		explanation_2.addDetail(new Explanation(Float.valueOf("2.0587344"),
				"weight(label:high in 328) [PerFieldSimilarity], result of:"));
		explanation_2.addDetail(new Explanation(Float.valueOf("1.5680285"),
				"weight(label:blood in 328) [PerFieldSimilarity], result of:"));
		Explanation explanation_3 = new Explanation(Float.valueOf("1.754909"),
				"weight(label:medication in 328) [PerFieldSimilarity], result of:");
		explanation_1.addDetail(explanation_2);
		explanation_1.addDetail(explanation_3);

		QueryRule queryRule_1 = new QueryRule(AttributeMetaDataMetaData.LABEL, Operator.FUZZY_MATCH, "hypertension");
		QueryRule queryRule_2 = new QueryRule(AttributeMetaDataMetaData.LABEL, Operator.FUZZY_MATCH,
				"hypertensive disorder");
		QueryRule queryRule_3 = new QueryRule(AttributeMetaDataMetaData.LABEL, Operator.FUZZY_MATCH,
				"high blood pressure");

		QueryRule queryRule_4 = new QueryRule(AttributeMetaDataMetaData.LABEL, Operator.FUZZY_MATCH, "drug");
		QueryRule queryRule_5 = new QueryRule(AttributeMetaDataMetaData.LABEL, Operator.FUZZY_MATCH, "medication");
		QueryRule queryRule_6 = new QueryRule(AttributeMetaDataMetaData.LABEL, Operator.FUZZY_MATCH, "pill");

		QueryRule disMaxQueryRule_1 = new QueryRule(Arrays.asList(queryRule_1, queryRule_2, queryRule_3));
		disMaxQueryRule_1.setOperator(Operator.DIS_MAX);

		QueryRule disMaxQueryRule_2 = new QueryRule(Arrays.asList(queryRule_4, queryRule_5, queryRule_6));
		disMaxQueryRule_2.setOperator(Operator.DIS_MAX);

		QueryRule shouldQueryRule = new QueryRule(Arrays.asList(disMaxQueryRule_1, disMaxQueryRule_2));
		shouldQueryRule.setOperator(Operator.SHOULD);

		QueryRule finalDisMaxQueryRule = new QueryRule(Arrays.asList(shouldQueryRule));
		finalDisMaxQueryRule.setOperator(Operator.DIS_MAX);

		assertEquals(elasticSearchExplainService.reverseSearchQueryStrings(finalDisMaxQueryRule, explanation_1)
				.toString(), "[high blood pressure=73.333, medication=100.0]");
	}
}

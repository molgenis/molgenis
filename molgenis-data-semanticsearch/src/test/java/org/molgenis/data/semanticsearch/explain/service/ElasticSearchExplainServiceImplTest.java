package org.molgenis.data.semanticsearch.explain.service;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.search.Explanation;
import org.elasticsearch.client.Client;
import org.molgenis.data.semanticsearch.explain.bean.ExplainedQueryString;
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
		explainServiceHelper = new ExplainServiceHelper();
		Client client = mock(Client.class);
		elasticSearchExplainService = new ElasticSearchExplainServiceImpl(client, explainServiceHelper);
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
	public void testDiscoverMatchedQueries()
	{
		Explanation explanation_1 = new Explanation(Float.valueOf("5.381672"), "sum of:");
		Explanation explanation_2 = new Explanation(Float.valueOf("3.6267629"), "sum of:");

		explanation_2.addDetail(new Explanation(Float.valueOf("2.0587344"),
				"weight(label:high in 328) [PerFieldSimilarity], result of:"));
		explanation_2.addDetail(new Explanation(Float.valueOf("1.5680285"),
				"weight(label:blood in 328) [PerFieldSimilarity], result of:"));

		Explanation explanation_3 = new Explanation(Float.valueOf("1.754909"), "max of:");
		explanation_3.addDetail(new Explanation(Float.valueOf("1.754909"),
				"weight(label:medication in 328) [PerFieldSimilarity], result of:"));
		explanation_1.addDetail(explanation_2);
		explanation_1.addDetail(explanation_3);

		System.out.println(explanation_1);

		Set<String> actual = explainServiceHelper.findMatchedWords(explanation_1);
		assertEquals(actual.size(), 2);
		assertTrue(actual.contains("high blood"));
		assertTrue(actual.contains("medication"));
	}

	@Test
	public void testRecursivelyFindQuery()
	{
		Map<String, String> expanedQueryMap = new HashMap<String, String>();
		expanedQueryMap.put("hypertension", "hypertension");
		expanedQueryMap.put("hypertensive disorder", "hypertension");
		expanedQueryMap.put("high blood pressure", "hypertension");
		expanedQueryMap.put("medication", "medication");
		expanedQueryMap.put("drug", "medication");
		expanedQueryMap.put("pill", "medication");

		assertEquals(explainServiceHelper.findMatchQueries("high blood", expanedQueryMap).toString(),
				ImmutableMap.of("high blood pressure", 73.333).toString());

		assertEquals(explainServiceHelper.findMatchQueries("medication", expanedQueryMap).toString(),
				ImmutableMap.of("medication", 100.0).toString());
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
		Explanation explanation_3 = new Explanation(Float.valueOf("1.754909"), "max of:");
		explanation_3.addDetail(new Explanation(Float.valueOf("1.754909"),
				"weight(label:medication in 328) [PerFieldSimilarity], result of:"));
		explanation_1.addDetail(explanation_2);
		explanation_1.addDetail(explanation_3);

		Map<String, String> expanedQueryMap = new HashMap<String, String>();
		expanedQueryMap.put("hypertension", "hypertension");
		expanedQueryMap.put("hypertensive disorder", "hypertension");
		expanedQueryMap.put("high blood pressure", "hypertension");
		expanedQueryMap.put("medication", "medication");
		expanedQueryMap.put("drug", "medication");
		expanedQueryMap.put("pill", "medication");

		Set<ExplainedQueryString> reverseSearchQueryStrings = elasticSearchExplainService.findQueriesFromExplanation(
				expanedQueryMap, explanation_1);

		Iterator<ExplainedQueryString> iterator = reverseSearchQueryStrings.iterator();

		ExplainedQueryString first = iterator.next();

		assertEquals(first.getMatchedWords(), "high blood");
		assertEquals(first.getQueryString(), "high blood pressure");
		assertEquals(first.getTagName(), "hypertension");
		assertEquals((int) first.getScore(), 73);

		ExplainedQueryString second = iterator.next();
		assertEquals(second.getMatchedWords(), "medication");
		assertEquals(second.getQueryString(), "medication");
		assertEquals(second.getTagName(), "medication");
		assertEquals((int) second.getScore(), 100);

	}
}

package org.molgenis.semanticsearch.explain.service;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.apache.lucene.search.Explanation;
import org.molgenis.data.QueryRule;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.meta.model.AttributeMetadata;
import org.molgenis.util.ResourceUtils;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class ExplainServiceHelperTest
{

	ExplainServiceHelper explainServiceHelper = new ExplainServiceHelper();

	@Test
	public void discoverMatchedQueries() throws JsonSyntaxException, IOException
	{
		Explanation explanation = new Gson().fromJson(ResourceUtils.getString("explain_api_example.json"),
				Explanation.class);
		Set<String> discoverMatchedQueries = explainServiceHelper.findMatchedWords(explanation);
		assertEquals(discoverMatchedQueries.size(), 2);
		assertTrue(discoverMatchedQueries.contains("high blood"));
		assertTrue(discoverMatchedQueries.contains("medic"));
	}

	@Test
	public void getMatchedWord()
	{
		assertEquals(
				explainServiceHelper.getMatchedWord("weight(label:blood in 20697) [PerFieldSimilarity], result of:"),
				"blood");
		assertEquals(explainServiceHelper.getMatchedWord(
				"weight(label:blood^0.5 in 20697) [PerFieldSimilarity], result of:"), "blood");
	}

	@Test
	public void testFindQuery()
	{
		QueryRule queryRule_1 = new QueryRule(AttributeMetadata.LABEL, Operator.FUZZY_MATCH, "hypertension");
		QueryRule queryRule_2 = new QueryRule(AttributeMetadata.LABEL, Operator.FUZZY_MATCH, "hypertensive disorder");
		QueryRule queryRule_3 = new QueryRule(AttributeMetadata.LABEL, Operator.FUZZY_MATCH, "high blood pressure");

		QueryRule queryRule_4 = new QueryRule(AttributeMetadata.LABEL, Operator.FUZZY_MATCH, "drug");
		QueryRule queryRule_5 = new QueryRule(AttributeMetadata.LABEL, Operator.FUZZY_MATCH, "medication");
		QueryRule queryRule_6 = new QueryRule(AttributeMetadata.LABEL, Operator.FUZZY_MATCH, "pill");

		QueryRule disMaxQueryRule_1 = new QueryRule(Arrays.asList(queryRule_1, queryRule_2, queryRule_3));
		disMaxQueryRule_1.setOperator(Operator.DIS_MAX);

		QueryRule disMaxQueryRule_2 = new QueryRule(Arrays.asList(queryRule_4, queryRule_5, queryRule_6));
		disMaxQueryRule_2.setOperator(Operator.DIS_MAX);

		QueryRule shouldQueryRule = new QueryRule(Arrays.asList(disMaxQueryRule_1, disMaxQueryRule_2));
		shouldQueryRule.setOperator(Operator.SHOULD);

		QueryRule finalDisMaxQueryRule = new QueryRule(Arrays.asList(shouldQueryRule));
		finalDisMaxQueryRule.setOperator(Operator.DIS_MAX);

		Map<String, String> expanedQueryMap = new HashMap<>();
		expanedQueryMap.put("hypertension", "hypertension");
		expanedQueryMap.put("hypertensive disorder", "hypertension");
		expanedQueryMap.put("high blood pressure", "hypertension");
		expanedQueryMap.put("medication", "medication");
		expanedQueryMap.put("drug", "medication");
		expanedQueryMap.put("pill", "medication");

		Map<String, Double> findMatchQueries = explainServiceHelper.findMatchQueries("blood high", expanedQueryMap);
		assertEquals(findMatchQueries.get("high blood pressure"), 73.333, 0.001);
	}
}

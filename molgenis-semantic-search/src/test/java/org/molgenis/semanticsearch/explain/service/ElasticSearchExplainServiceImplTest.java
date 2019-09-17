package org.molgenis.semanticsearch.explain.service;

import static java.util.Collections.emptySet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.apache.lucene.search.Explanation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.molgenis.data.elasticsearch.ElasticsearchService;
import org.molgenis.semanticsearch.explain.bean.ExplainedQueryString;

class ElasticSearchExplainServiceImplTest {
  private ElasticSearchExplainService elasticSearchExplainService;
  private ExplainServiceHelper explainServiceHelper;

  @BeforeEach
  void setup() {
    explainServiceHelper = new ExplainServiceHelper();
    ElasticsearchService elasticsearchService = mock(ElasticsearchService.class);
    elasticSearchExplainService =
        new ElasticSearchExplainServiceImpl(elasticsearchService, explainServiceHelper);
  }

  @Test
  void testRegExp() {
    String description = "weight(label:high in 328) [PerFieldSimilarity], result of:";
    String actual = explainServiceHelper.getMatchedWord(description);
    assertEquals("high", actual);

    String description2 = "weight(label:length^0.5 in 328) [PerFieldSimilarity], result of:";
    String actual2 = explainServiceHelper.getMatchedWord(description2);
    assertEquals("length", actual2);
  }

  @Test
  void testRemoveBoostFromQuery() {
    String description = "Measurement^0.5 glucose^0.25 fasting^0.5";
    assertEquals(
        "Measurement glucose fasting", explainServiceHelper.removeBoostFromQuery(description));

    String description2 = "Measurement^5 glucose^5.0 fasting^00.52";
    assertEquals(
        "Measurement glucose fasting", explainServiceHelper.removeBoostFromQuery(description2));

    String description3 = "Measurement glucose 5 fasting43";
    assertEquals(
        "Measurement glucose 5 fasting43", explainServiceHelper.removeBoostFromQuery(description3));

    String description4 = "glycemia^0.03125";
    assertEquals("glycemia", explainServiceHelper.removeBoostFromQuery(description4));
  }

  @Test
  void testDiscoverMatchedQueries() {
    Explanation explanation_2 =
        Explanation.match(
            Float.valueOf("3.6267629"),
            "sum of:",
            Explanation.match(
                Float.valueOf("2.0587344"),
                "weight(label:high in 328) [PerFieldSimilarity], result of:"),
            Explanation.match(
                Float.valueOf("1.5680285"),
                "weight(label:blood in 328) [PerFieldSimilarity], result of:"));

    Explanation explanation_3 =
        Explanation.match(
            Float.valueOf("1.754909"),
            "max of:",
            Explanation.match(
                Float.valueOf("1.754909"),
                "weight(label:medication in 328) [PerFieldSimilarity], result of:"));

    Explanation explanation_1 =
        Explanation.match(Float.valueOf("5.381672"), "sum of:", explanation_2, explanation_3);

    Set<String> actual = explainServiceHelper.findMatchedWords(explanation_1);
    assertEquals(2, actual.size());
    assertTrue(actual.contains("high blood"));
    assertTrue(actual.contains("medication"));
  }

  @Test
  void testRecursivelyFindQuery() {
    Map<String, String> expanedQueryMap = new HashMap<>();
    expanedQueryMap.put("hypertension", "hypertension");
    expanedQueryMap.put("hypertensive disorder", "hypertension");
    expanedQueryMap.put("high blood pressure", "hypertension");
    expanedQueryMap.put("medication", "medication");
    expanedQueryMap.put("drug", "medication");
    expanedQueryMap.put("pill", "medication");

    Map<String, Double> findMatchQueries =
        explainServiceHelper.findMatchQueries("high blood", expanedQueryMap);
    assertEquals(findMatchQueries.get("high blood pressure"), 73.333, 0.001);

    Map<String, Double> findMatchQueries2 =
        explainServiceHelper.findMatchQueries("medication", expanedQueryMap);
    assertEquals(findMatchQueries2.get("medication"), 100.0, 0.001);
  }

  @Test
  void testReverseSearchQueryStrings() {
    Explanation explanation_2 =
        Explanation.match(
            Float.valueOf("3.6267629"),
            "sum of:",
            Explanation.match(
                Float.valueOf("2.0587344"),
                "weight(label:high in 328) [PerFieldSimilarity], result of:"),
            Explanation.match(
                Float.valueOf("1.5680285"),
                "weight(label:blood in 328) [PerFieldSimilarity], result of:"));
    Explanation explanation_3 =
        Explanation.match(
            Float.valueOf("1.754909"),
            "max of:",
            Explanation.match(
                Float.valueOf("1.754909"),
                "weight(label:medication in 328) [PerFieldSimilarity], result of:"));
    Explanation explanation_1 =
        Explanation.match(Float.valueOf("5.381672"), "sum of:", explanation_2, explanation_3);

    Map<String, String> expanedQueryMap = new HashMap<>();
    expanedQueryMap.put("hypertension", "hypertension");
    expanedQueryMap.put("hypertensive disorder", "hypertension");
    expanedQueryMap.put("high blood pressure", "hypertension");
    expanedQueryMap.put("medication", "medication");
    expanedQueryMap.put("drug", "medication");
    expanedQueryMap.put("pill", "medication");

    Set<ExplainedQueryString> reverseSearchQueryStrings =
        elasticSearchExplainService.findQueriesFromExplanation(expanedQueryMap, explanation_1);

    Iterator<ExplainedQueryString> iterator = reverseSearchQueryStrings.iterator();

    ExplainedQueryString first = iterator.next();

    assertEquals("high blood", first.getMatchedWords());
    assertEquals("high blood pressure", first.getQueryString());
    assertEquals("hypertension", first.getTagName());
    assertEquals(73, (int) first.getScore());

    ExplainedQueryString second = iterator.next();
    assertEquals("medication", second.getMatchedWords());
    assertEquals("medication", second.getQueryString());
    assertEquals("medication", second.getTagName());
    assertEquals(100, (int) second.getScore());
  }

  @Test
  void testReverseSearchQueryStringsWithoutExpandedQueryMap() {
    Explanation explanation_2 =
        Explanation.match(
            Float.valueOf("3.6267629"),
            "sum of:",
            Explanation.match(
                Float.valueOf("2.0587344"),
                "weight(label:high in 328) [PerFieldSimilarity], result of:"),
            Explanation.match(
                Float.valueOf("1.5680285"),
                "weight(label:blood in 328) [PerFieldSimilarity], result of:"));
    Explanation explanation_3 =
        Explanation.match(
            Float.valueOf("1.754909"),
            "max of:",
            Explanation.match(
                Float.valueOf("1.754909"),
                "weight(label:medication in 328) [PerFieldSimilarity], result of:"));
    Explanation explanation_1 =
        Explanation.match(Float.valueOf("5.381672"), "sum of:", explanation_2, explanation_3);

    Map<String, String> expandedQueryMap = new HashMap<>();

    Set<ExplainedQueryString> reverseSearchQueryStrings =
        elasticSearchExplainService.findQueriesFromExplanation(expandedQueryMap, explanation_1);

    assertEquals(emptySet(), reverseSearchQueryStrings);
  }
}

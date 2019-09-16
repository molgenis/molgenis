package org.molgenis.semanticsearch.service.impl;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.model.AttributeMetadata.ATTRIBUTE_META_DATA;
import static org.molgenis.semanticsearch.semantic.Hit.create;

import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.apache.lucene.search.Explanation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.quality.Strictness;
import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.QueryRule;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.AttributeMetadata;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.ontology.core.model.OntologyTerm;
import org.molgenis.ontology.core.service.OntologyService;
import org.molgenis.semanticsearch.explain.bean.ExplainedAttribute;
import org.molgenis.semanticsearch.explain.bean.ExplainedQueryString;
import org.molgenis.semanticsearch.explain.service.ElasticSearchExplainService;
import org.molgenis.semanticsearch.semantic.Hit;
import org.molgenis.semanticsearch.semantic.Hits;
import org.molgenis.semanticsearch.service.OntologyTagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = SemanticSearchServiceImplTest.Config.class)
class SemanticSearchServiceImplTest extends AbstractMolgenisSpringTest {
  @Autowired private EntityTypeFactory entityTypeFactory;

  @Autowired private AttributeFactory attrMetaDataFactory;

  @Autowired private OntologyService ontologyService;

  @Autowired private SemanticSearchServiceHelper semanticSearchServiceHelper;

  @Autowired private DataService dataService;

  @Autowired private SemanticSearchServiceImpl semanticSearchService;

  @Autowired AttributeMetadata attributeMetadata;

  @Autowired private ElasticSearchExplainService elasticSearchExplainService;

  @Mock private Map<String, String> collectExpandedQueryMap;

  @Mock private Query<Entity> query;

  @Mock private java.util.Set<ExplainedQueryString> explainedQueryStrings;

  private List<String> ontologies;

  private OntologyTerm standingHeight;

  private List<OntologyTerm> ontologyTerms;

  private Attribute attribute;

  SemanticSearchServiceImplTest() {
    super(Strictness.WARN);
  }

  @BeforeEach
  void init() {
    ontologies = asList("1", "2");
    standingHeight =
        OntologyTerm.create(
            "http://onto/height", "Standing height", asList("Standing height", "length"));
    OntologyTerm bodyWeight =
        OntologyTerm.create(
            "http://onto/bmi", "Body weight", asList("Body weight", "Mass in kilograms"));

    OntologyTerm hypertension = OntologyTerm.create("http://onto/hyp", "Hypertension");
    OntologyTerm maternalHypertension =
        OntologyTerm.create("http://onto/mhyp", "Maternal hypertension");
    ontologyTerms = asList(standingHeight, bodyWeight, hypertension, maternalHypertension);
    attribute = attrMetaDataFactory.create("attrID").setName("attr1");
    reset(elasticSearchExplainService, collectExpandedQueryMap, query);

    when(semanticSearchServiceHelper.getOtLabelAndSynonyms(standingHeight))
        .thenReturn(newHashSet("Standing height", "Standing height", "length"));

    when(semanticSearchServiceHelper.getOtLabelAndSynonyms(bodyWeight))
        .thenReturn(newHashSet("Body weight", "Body weight", "Mass in kilograms"));

    when(semanticSearchServiceHelper.getOtLabelAndSynonyms(hypertension))
        .thenReturn(newHashSet("Hypertension"));

    when(semanticSearchServiceHelper.getOtLabelAndSynonyms(maternalHypertension))
        .thenReturn(newHashSet("Maternal hypertension"));
  }

  @Test
  void testSearchHypertension() {
    Mockito.reset(ontologyService);
    attribute.setDescription("History of Hypertension");
    when(ontologyService.findOntologyTerms(
            ontologies, ImmutableSet.of("history", "hypertens"), 100))
        .thenReturn(ontologyTerms);
    Hit<OntologyTerm> result = semanticSearchService.findTags(attribute, ontologies);
    assertNull(result);
  }

  @Test
  void testDistanceFrom() {
    assertEquals(
        semanticSearchService.distanceFrom("Hypertension", ImmutableSet.of("history", "hypertens")),
        .6923,
        0.0001,
        "String distance should be equal");
    assertEquals(
        semanticSearchService.distanceFrom(
            "Maternal Hypertension", ImmutableSet.of("history", "hypertens")),
        .5454,
        0.0001,
        "String distance should be equal");
  }

  @Test
  void testSearchDescription() {
    Mockito.reset(ontologyService);
    attribute.setDescription("Standing height in meters.");
    when(ontologyService.findOntologyTerms(
            ontologies, ImmutableSet.of("standing", "height", "meters"), 100))
        .thenReturn(ontologyTerms);
    Hit<OntologyTerm> result = semanticSearchService.findTags(attribute, ontologies);
    assertEquals(create(standingHeight, 0.81250f), result);
  }

  @Test
  void testSearchLabel() {
    Mockito.reset(ontologyService);
    attribute.setDescription("Standing height (m.)");

    when(ontologyService.findOntologyTerms(
            ontologies, ImmutableSet.of("standing", "height", "m"), 100))
        .thenReturn(ontologyTerms);
    Hit<OntologyTerm> result = semanticSearchService.findTags(attribute, ontologies);
    assertEquals(create(standingHeight, 0.92857f), result);
  }

  @Test
  void testIsSingleMatchHighQuality() {
    List<ExplainedQueryString> explanations1 =
        singletonList(ExplainedQueryString.create("height", "height", "standing height", 50.0));
    assertFalse(
        semanticSearchService.isSingleMatchHighQuality(
            newHashSet("height"), newHashSet("height"), explanations1));

    List<ExplainedQueryString> explanations2 =
        singletonList(ExplainedQueryString.create("body length", "body length", "height", 100));

    assertTrue(
        semanticSearchService.isSingleMatchHighQuality(
            newHashSet("height in meter"), newHashSet("height in meter", "height"), explanations2));

    List<ExplainedQueryString> explanations3 =
        asList(
            ExplainedQueryString.create("fasting", "fasting", "fasting", 100),
            ExplainedQueryString.create("glucose", "blood glucose", "blood glucose", 50));

    assertFalse(
        semanticSearchService.isSingleMatchHighQuality(
            newHashSet("fasting glucose"),
            newHashSet("fasting glucose", "fasting", "blood glucose"),
            explanations3));

    List<ExplainedQueryString> explanations4 =
        singletonList(ExplainedQueryString.create("number of", "number of", "number", 100));

    assertFalse(
        semanticSearchService.isSingleMatchHighQuality(
            newHashSet("number of cigarette smoked"),
            newHashSet("number of cigarette smoked", "number of"),
            explanations4));
  }

  @Test
  void testIsGoodMatch() {
    Map<String, Double> matchedTags = newHashMap();
    matchedTags.put("height", 100.0);
    matchedTags.put("weight", 50.0);
    assertFalse(semanticSearchService.isGoodMatch(matchedTags, "blood"));
    assertFalse(semanticSearchService.isGoodMatch(matchedTags, "weight"));
    assertTrue(semanticSearchService.isGoodMatch(matchedTags, "height"));

    Map<String, Double> matchedTags2 = newHashMap();
    matchedTags2.put("fasting", 100.0);
    matchedTags2.put("glucose", 100.0);

    assertTrue(semanticSearchService.isGoodMatch(matchedTags2, "fasting glucose"));
  }

  @Test
  void testFindAttributes() {
    EntityType sourceEntityType = entityTypeFactory.create("sourceEntityType");

    // Mock the id's of the attribute entities that should be searched
    List<String> attributeIdentifiers = asList("1", "2");
    when(semanticSearchServiceHelper.getAttributeIdentifiers(sourceEntityType))
        .thenReturn(attributeIdentifiers);

    // Mock the createDisMaxQueryRule method
    List<QueryRule> rules = newArrayList();
    QueryRule targetQueryRuleLabel =
        new QueryRule(AttributeMetadata.LABEL, QueryRule.Operator.FUZZY_MATCH, "height");
    rules.add(targetQueryRuleLabel);
    QueryRule targetQueryRuleOntologyTermTag =
        new QueryRule(AttributeMetadata.LABEL, QueryRule.Operator.FUZZY_MATCH, "standing height");
    rules.add(targetQueryRuleOntologyTermTag);
    QueryRule targetQueryRuleOntologyTermTagSyn =
        new QueryRule(AttributeMetadata.LABEL, QueryRule.Operator.FUZZY_MATCH, "length");
    rules.add(targetQueryRuleOntologyTermTagSyn);
    QueryRule disMaxQueryRule = new QueryRule(rules);
    disMaxQueryRule.setOperator(QueryRule.Operator.DIS_MAX);

    when(semanticSearchServiceHelper.createDisMaxQueryRuleForAttribute(
            newHashSet("targetAttribute"), emptyList()))
        .thenReturn(disMaxQueryRule);

    Entity entity1 = mock(Entity.class);
    when(entity1.getString(AttributeMetadata.NAME)).thenReturn("height_0");
    when(entity1.getString(AttributeMetadata.LABEL)).thenReturn("height");
    when(entity1.getString(AttributeMetadata.DESCRIPTION))
        .thenReturn("this is a height measurement in m!");

    List<QueryRule> disMaxQueryRules =
        newArrayList(
            new QueryRule(AttributeMetadata.ID, QueryRule.Operator.IN, attributeIdentifiers),
            new QueryRule(QueryRule.Operator.AND),
            disMaxQueryRule);

    Attribute attributeHeight = attrMetaDataFactory.create().setName("height_0");
    Attribute attributeWeight = attrMetaDataFactory.create().setName("weight_0");
    sourceEntityType.addAttribute(attributeHeight);
    sourceEntityType.addAttribute(attributeWeight);

    // Case 1
    when(dataService.findAll(ATTRIBUTE_META_DATA, new QueryImpl<>(disMaxQueryRules)))
        .thenReturn(Stream.of(entity1));

    Hits<ExplainedAttribute> termsActual1 =
        semanticSearchService.findAttributes(
            sourceEntityType, newHashSet("targetAttribute"), emptyList());

    Hits<ExplainedAttribute> termsExpected1 =
        Hits.create(Hit.create(ExplainedAttribute.create(attributeHeight, emptySet(), false), 1f));

    assertEquals(termsExpected1.toString(), termsActual1.toString());

    // Case 2
    when(dataService.findAll(ATTRIBUTE_META_DATA, new QueryImpl<>(disMaxQueryRules)))
        .thenReturn(Stream.empty());

    Hits<ExplainedAttribute> termsActual2 =
        semanticSearchService.findAttributes(
            sourceEntityType, newHashSet("targetAttribute"), emptyList());

    Hits<ExplainedAttribute> termsExpected2 = Hits.create();

    assertEquals(termsExpected2, termsActual2);

    Mockito.reset(ontologyService);
    attribute.setDescription("Standing height (Ångstrøm)");

    when(ontologyService.findOntologyTerms(
            ontologies, ImmutableSet.of("standing", "height", "ångstrøm"), 100))
        .thenReturn(ontologyTerms);
    Hit<OntologyTerm> result = semanticSearchService.findTags(attribute, ontologies);
    assertEquals(create(standingHeight, 0.76471f), result);
  }

  @Test
  void testSearchUnicode() {
    Mockito.reset(ontologyService);
    attribute.setDescription("/əˈnædrəməs/");

    when(ontologyService.findOntologyTerms(ontologies, ImmutableSet.of("əˈnædrəməs"), 100))
        .thenReturn(ontologyTerms);
    Hit<OntologyTerm> result = semanticSearchService.findTags(attribute, ontologies);
    assertNull(result);
  }

  @Test
  void testSearchMultipleTags() {
    Mockito.reset(ontologyService);
    attribute.setDescription("Body mass index");

    when(ontologyService.findOntologyTerms(
            ontologies, ImmutableSet.of("body", "mass", "index"), 100))
        .thenReturn(ontologyTerms);
    Hit<OntologyTerm> result = semanticSearchService.findTags(attribute, ontologies);
    assertNull(result);
  }

  @Test
  void testConvertAttribute() {
    when(dataService.getEntityType(ATTRIBUTE_META_DATA)).thenReturn(attributeMetadata);
    Explanation explanation = Explanation.match(0.3f, "match");
    when(elasticSearchExplainService.explain(query, attributeMetadata, "attrID"))
        .thenReturn(explanation);
    when(elasticSearchExplainService.findQueriesFromExplanation(
            collectExpandedQueryMap, explanation))
        .thenReturn(explainedQueryStrings);

    assertEquals(
        explainedQueryStrings,
        semanticSearchService.convertAttributeToExplainedAttribute(
            attribute, collectExpandedQueryMap, query));
  }

  @Configuration
  public static class Config {
    @Bean
    MetaDataService metaDataService() {
      return mock(MetaDataService.class);
    }

    @Bean
    OntologyService ontologyService() {
      return mock(OntologyService.class);
    }

    @Bean
    SemanticSearchServiceImpl semanticSearchService() {
      return new SemanticSearchServiceImpl(
          dataService(),
          ontologyService(),
          semanticSearchServiceHelper(),
          elasticSearchExplainService(),
          ontologyTagService());
    }

    @Bean
    OntologyTagService ontologyTagService() {
      return mock(OntologyTagService.class);
    }

    @Bean
    DataService dataService() {
      return mock(DataService.class);
    }

    @Bean
    ElasticSearchExplainService elasticSearchExplainService() {
      return mock(ElasticSearchExplainService.class);
    }

    @Bean
    SemanticSearchServiceHelper semanticSearchServiceHelper() {
      return mock(SemanticSearchServiceHelper.class);
    }
  }
}

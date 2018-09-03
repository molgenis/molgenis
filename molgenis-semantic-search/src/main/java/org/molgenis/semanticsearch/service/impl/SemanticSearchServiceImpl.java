package org.molgenis.semanticsearch.service.impl;

import static java.util.Collections.emptySet;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;
import static org.molgenis.data.meta.model.AttributeMetadata.ATTRIBUTE_META_DATA;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.spell.StringDistance;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.QueryRule;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeMetadata;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.semantic.Relation;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.ontology.core.model.Ontology;
import org.molgenis.ontology.core.model.OntologyTerm;
import org.molgenis.ontology.core.service.OntologyService;
import org.molgenis.semanticsearch.explain.bean.AttributeSearchResults;
import org.molgenis.semanticsearch.explain.bean.EntityTypeSearchResults;
import org.molgenis.semanticsearch.explain.bean.ExplainedAttribute;
import org.molgenis.semanticsearch.explain.bean.ExplainedQueryString;
import org.molgenis.semanticsearch.explain.service.ElasticSearchExplainService;
import org.molgenis.semanticsearch.semantic.Hit;
import org.molgenis.semanticsearch.semantic.Hits;
import org.molgenis.semanticsearch.service.OntologyTagService;
import org.molgenis.semanticsearch.service.SemanticSearchService;
import org.molgenis.semanticsearch.string.NGramDistanceAlgorithm;
import org.molgenis.semanticsearch.string.Stemmer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SemanticSearchServiceImpl implements SemanticSearchService {
  private static final Logger LOG = LoggerFactory.getLogger(SemanticSearchServiceImpl.class);

  private final DataService dataService;
  private final OntologyService ontologyService;
  private final SemanticSearchServiceHelper semanticSearchServiceHelper;
  private final ElasticSearchExplainService elasticSearchExplainService;
  private final OntologyTagService ontologyTagService;

  private static final int MAX_NUM_TAGS = 100;
  private static final float CUTOFF = 0.4f;
  private Splitter termSplitter = Splitter.onPattern("[^\\p{IsAlphabetic}]+");
  private Joiner termJoiner = Joiner.on(' ');
  private static final String UNIT_ONTOLOGY_IRI = "http://purl.obolibrary.org/obo/uo.owl";

  // We only explain the top 10 suggested attributes because beyond that the attributes are not high
  // quliaty anymore
  private static final int MAX_NUMBER_EXPLAINED_ATTRIBUTES = 10;

  public SemanticSearchServiceImpl(
      DataService dataService,
      OntologyService ontologyService,
      SemanticSearchServiceHelper semanticSearchServiceHelper,
      ElasticSearchExplainService elasticSearchExplainService,
      OntologyTagService ontologyTagService) {
    this.dataService = requireNonNull(dataService);
    this.ontologyService = requireNonNull(ontologyService);
    this.semanticSearchServiceHelper = requireNonNull(semanticSearchServiceHelper);
    this.elasticSearchExplainService = requireNonNull(elasticSearchExplainService);
    this.ontologyTagService = requireNonNull(ontologyTagService);
  }

  /** public for testability */
  public Hits<ExplainedAttribute> findAttributes(
      EntityType sourceEntityType, Set<String> queryTerms, Collection<OntologyTerm> ontologyTerms) {
    Iterable<String> attributeIdentifiers =
        semanticSearchServiceHelper.getAttributeIdentifiers(sourceEntityType);

    QueryRule disMaxQueryRule =
        semanticSearchServiceHelper.createDisMaxQueryRuleForAttribute(queryTerms, ontologyTerms);

    List<QueryRule> finalQueryRules =
        Lists.newArrayList(new QueryRule(AttributeMetadata.ID, Operator.IN, attributeIdentifiers));

    if (disMaxQueryRule.getNestedRules().size() > 0) {
      finalQueryRules.addAll(Arrays.asList(new QueryRule(Operator.AND), disMaxQueryRule));
    }

    Stream<Entity> attributeEntities =
        dataService.findAll(ATTRIBUTE_META_DATA, new QueryImpl<>(finalQueryRules));

    Map<String, String> collectExpanedQueryMap =
        semanticSearchServiceHelper.collectExpandedQueryMap(queryTerms, ontologyTerms);

    // Because the explain-API can be computationally expensive we limit the explanation to the top
    // 10 attributes
    List<ExplainedAttribute> attributeSearchHits = new ArrayList<>();
    AtomicInteger count = new AtomicInteger(0);
    attributeEntities.forEach(
        attributeEntity -> {
          Attribute attribute =
              sourceEntityType.getAttribute(attributeEntity.getString(AttributeMetadata.NAME));
          Set<ExplainedQueryString> explainedQueryStrings;
          boolean isHighQuality;
          if (count.get() < MAX_NUMBER_EXPLAINED_ATTRIBUTES) {
            explainedQueryStrings =
                convertAttributeToExplainedAttribute(
                    attribute, collectExpanedQueryMap, new QueryImpl<>(finalQueryRules));

            isHighQuality =
                isSingleMatchHighQuality(
                    queryTerms,
                    Sets.newHashSet(collectExpanedQueryMap.values()),
                    explainedQueryStrings);
          } else {
            explainedQueryStrings = emptySet();
            isHighQuality = false;
          }
          attributeSearchHits.add(
              ExplainedAttribute.create(attribute, explainedQueryStrings, isHighQuality));
          count.incrementAndGet();
        });

    return Hits.create(
        attributeSearchHits
            .stream()
            .map(explainedAttribute -> Hit.create(explainedAttribute, 1f))
            .collect(toList()));
  }

  boolean isSingleMatchHighQuality(
      Collection<String> queryTerms,
      Collection<String> ontologyTermQueries,
      Iterable<ExplainedQueryString> explanations) {
    Map<String, Double> matchedTags = new HashMap<>();

    for (ExplainedQueryString explanation : explanations) {
      matchedTags.put(explanation.getTagName().toLowerCase(), explanation.getScore());
    }

    ontologyTermQueries.removeAll(queryTerms);

    if (!queryTerms.isEmpty()
        && queryTerms.stream().anyMatch(token -> isGoodMatch(matchedTags, token))) return true;

    if (!ontologyTermQueries.isEmpty()
        && ontologyTermQueries.stream().allMatch(token -> isGoodMatch(matchedTags, token)))
      return true;

    return false;
  }

  boolean isGoodMatch(Map<String, Double> matchedTags, String label) {
    label = label.toLowerCase();
    return matchedTags.containsKey(label) && matchedTags.get(label).intValue() == 100
        || Sets.newHashSet(label.split(" "))
            .stream()
            .allMatch(
                word -> matchedTags.containsKey(word) && matchedTags.get(word).intValue() == 100);
  }

  @Override
  public EntityTypeSearchResults findAttributes(
      EntityType sourceEntityType, EntityType targetEntityType, Set<String> searchTerms) {
    List<AttributeSearchResults> attributeSearchResults =
        stream(targetEntityType.getAtomicAttributes().spliterator(), false)
            .filter(targetAttribute -> targetAttribute.getExpression() == null)
            .map(
                targetAttribute ->
                    findAttributes(
                        sourceEntityType, targetEntityType,
                        targetAttribute, searchTerms))
            .collect(toList());
    return EntityTypeSearchResults.create(targetEntityType, attributeSearchResults);
  }

  @Override
  public AttributeSearchResults findAttributes(
      EntityType sourceEntityType,
      EntityType targetEntityType,
      Attribute targetAttribute,
      Set<String> searchTerms) {
    // Find relevant attributes base on tags
    Multimap<Relation, OntologyTerm> tagsForAttribute =
        ontologyTagService.getTagsForAttribute(targetEntityType, targetAttribute);
    Collection<OntologyTerm> ontologyTermsFromTags = tagsForAttribute.values();

    Set<String> queryTerms = createLexicalSearchQueryTerms(targetAttribute, searchTerms);

    Collection<OntologyTerm> ontologyTerms = ontologyTermsFromTags;

    if (null != searchTerms && !searchTerms.isEmpty()) {
      Set<String> escapedSearchTerms =
          searchTerms
              .stream()
              .filter(StringUtils::isNotBlank)
              .map(QueryParser::escape)
              .collect(Collectors.toSet());
      ontologyTerms =
          ontologyService.findExcatOntologyTerms(
              ontologyService.getAllOntologiesIds(), escapedSearchTerms, MAX_NUM_TAGS);
    } else if (null == ontologyTerms || ontologyTerms.isEmpty()) {
      List<String> allOntologiesIds = ontologyService.getAllOntologiesIds();
      Ontology unitOntology = ontologyService.getOntology(UNIT_ONTOLOGY_IRI);
      if (unitOntology != null) {
        allOntologiesIds.remove(unitOntology.getId());
      }
      Hit<OntologyTerm> ontologyTermHit = findTags(targetAttribute, allOntologiesIds);
      ontologyTerms =
          ontologyTermHit != null
              ? Arrays.asList(ontologyTermHit.getResult())
              : Collections.emptyList();
    }

    Hits<ExplainedAttribute> hits = findAttributes(sourceEntityType, queryTerms, ontologyTerms);
    return AttributeSearchResults.create(targetAttribute, hits);
  }

  /**
   * A helper function to create a list of queryTerms based on the information from the
   * targetAttribute as well as user defined searchTerms. If the user defined searchTerms exist, the
   * targetAttribute information will not be used.
   *
   * @return list of queryTerms
   */
  public Set<String> createLexicalSearchQueryTerms(
      Attribute targetAttribute, Set<String> searchTerms) {
    Set<String> queryTerms = new HashSet<>();

    if (searchTerms != null && !searchTerms.isEmpty()) {
      queryTerms.addAll(searchTerms);
    }

    if (queryTerms.isEmpty()) {
      if (StringUtils.isNotBlank(targetAttribute.getLabel())) {
        queryTerms.add(targetAttribute.getLabel());
      }

      if (StringUtils.isNotBlank(targetAttribute.getDescription())) {
        queryTerms.add(targetAttribute.getDescription());
      }
    }

    return queryTerms;
  }

  /**
   * A helper function to explain each of the matched attributes returned by the explain-API
   *
   * @param attribute The attribute found
   * @param collectExpandedQueryMap ?
   * @param query the query used to find the attribute
   * @return Set of explained query strings
   */
  public Set<ExplainedQueryString> convertAttributeToExplainedAttribute(
      Attribute attribute, Map<String, String> collectExpandedQueryMap, Query<Entity> query) {
    EntityType attributeMetaData = dataService.getEntityType(ATTRIBUTE_META_DATA);
    String attributeID = attribute.getIdentifier();
    Explanation explanation =
        elasticSearchExplainService.explain(query, attributeMetaData, attributeID);
    return elasticSearchExplainService.findQueriesFromExplanation(
        collectExpandedQueryMap, explanation);
  }

  @Override
  public Hits<OntologyTerm> findOntologyTerms(
      Attribute attribute, Collection<Ontology> ontologies) {
    List<String> ontologyIds = ontologies.stream().map(Ontology::getId).collect(toList());
    Hit<OntologyTerm> ontologyTermHit = findTags(attribute, ontologyIds);
    return ontologyTermHit != null ? Hits.create(ontologyTermHit) : Hits.create();
  }

  /** package-private for testability */
  Hit<OntologyTerm> findTags(Attribute attribute, List<String> ontologyIds) {
    String description =
        attribute.getDescription() == null ? attribute.getLabel() : attribute.getDescription();
    Set<String> searchTerms = splitIntoTerms(description);

    if (LOG.isDebugEnabled()) {
      LOG.debug("findAttributeOntologyTerms({},{},{})", ontologyIds, searchTerms, MAX_NUM_TAGS);
    }

    List<OntologyTerm> candidates =
        ontologyService.findOntologyTerms(ontologyIds, searchTerms, MAX_NUM_TAGS);

    if (LOG.isDebugEnabled()) {
      LOG.debug("Candidates: {}", candidates);
    }

    List<Hit<OntologyTerm>> hits =
        candidates
            .stream()
            .filter(
                ontologyTerm ->
                    filterOntologyTerm(
                        splitIntoTerms(Stemmer.stemAndJoin(searchTerms)), ontologyTerm))
            .map(
                ontolgoyTerm ->
                    Hit.create(
                        ontolgoyTerm, bestMatchingSynonym(ontolgoyTerm, searchTerms).getScore()))
            .sorted(Ordering.natural().reverse())
            .collect(toList());

    if (LOG.isDebugEnabled()) {
      LOG.debug("Hits: {}", hits);
    }

    Hit<OntologyTerm> result = null;
    String bestMatchingSynonym = null;
    for (Hit<OntologyTerm> hit : hits) {
      String bestMatchingSynonymForHit =
          bestMatchingSynonym(hit.getResult(), searchTerms).getResult();
      if (result == null) {
        result = hit;
        bestMatchingSynonym = bestMatchingSynonymForHit;
      } else {
        Set<String> jointTerms =
            Sets.union(
                splitIntoTerms(bestMatchingSynonym), splitIntoTerms(bestMatchingSynonymForHit));
        String joinedSynonyms = termJoiner.join(jointTerms);
        Hit<OntologyTerm> joinedHit =
            Hit.create(
                OntologyTerm.and(result.getResult(), hit.getResult()),
                distanceFrom(joinedSynonyms, searchTerms));
        if (joinedHit.compareTo(result) > 0) {
          result = joinedHit;
          bestMatchingSynonym = bestMatchingSynonym + " " + bestMatchingSynonymForHit;
        }
      }

      if (LOG.isDebugEnabled()) {
        LOG.debug("result: {}", result);
      }
    }
    if (result != null && result.getScore() >= CUTOFF) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Tag {} with {}", attribute, result);
      }
      return result;
    }
    return null;
  }

  private boolean filterOntologyTerm(Set<String> keywordsFromAttribute, OntologyTerm ontologyTerm) {
    Set<String> ontologyTermSynonyms =
        semanticSearchServiceHelper.getOtLabelAndSynonyms(ontologyTerm);

    for (String synonym : ontologyTermSynonyms) {
      Set<String> splitIntoTerms = splitIntoTerms(Stemmer.stemAndJoin(splitIntoTerms(synonym)));
      if (!splitIntoTerms.isEmpty() && keywordsFromAttribute.containsAll(splitIntoTerms))
        return true;
    }

    return false;
  }

  /**
   * Computes the best matching synonym which is closest to a set of search terms.<br>
   * Will stem the {@link OntologyTerm} 's synonyms and the search terms, and then compute the
   * maximum {@link StringDistance} between them. 0 means disjunct, 1 means identical
   *
   * @param ontologyTerm the {@link OntologyTerm}
   * @param searchTerms the search terms
   * @return the maximum {@link StringDistance} between the ontologyterm and the search terms
   */
  public Hit<String> bestMatchingSynonym(OntologyTerm ontologyTerm, Set<String> searchTerms) {
    Optional<Hit<String>> bestSynonym =
        ontologyTerm
            .getSynonyms()
            .stream()
            .map(synonym -> Hit.create(synonym, distanceFrom(synonym, searchTerms)))
            .max(Comparator.naturalOrder());
    return bestSynonym.get();
  }

  float distanceFrom(String synonym, Set<String> searchTerms) {
    String s1 = Stemmer.stemAndJoin(splitIntoTerms(synonym));
    String s2 = Stemmer.stemAndJoin(searchTerms);
    float distance = (float) NGramDistanceAlgorithm.stringMatching(s1, s2) / 100;
    LOG.debug("Similarity between: {} and {} is {}", s1, s2, distance);
    return distance;
  }

  private Set<String> splitIntoTerms(String description) {
    return FluentIterable.from(termSplitter.split(description))
        .transform(String::toLowerCase)
        .filter(w -> !NGramDistanceAlgorithm.STOPWORDSLIST.contains(w))
        .filter(StringUtils::isNotEmpty)
        .toSet();
  }
}

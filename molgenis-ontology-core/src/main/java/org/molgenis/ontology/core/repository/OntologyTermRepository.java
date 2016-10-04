package org.molgenis.ontology.core.repository;

import static com.google.common.collect.Iterators.filter;
import static java.lang.Integer.MAX_VALUE;
import static java.util.Collections.emptyList;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.molgenis.data.QueryRule.Operator.AND;
import static org.molgenis.data.QueryRule.Operator.FUZZY_MATCH;
import static org.molgenis.data.QueryRule.Operator.IN;
import static org.molgenis.data.QueryRule.Operator.OR;
import static org.molgenis.ontology.core.meta.OntologyTermMetaData.ONTOLOGY;
import static org.molgenis.ontology.core.meta.OntologyTermMetaData.ONTOLOGY_TERM;
import static org.molgenis.ontology.core.meta.OntologyTermMetaData.ONTOLOGY_TERM_IRI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.common.collect.Lists;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Fetch;
import org.molgenis.data.Query;
import org.molgenis.data.QueryRule;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.ontology.core.meta.OntologyEntity;
import org.molgenis.ontology.core.meta.OntologyMetaData;
import org.molgenis.ontology.core.meta.OntologyTermDynamicAnnotation;
import org.molgenis.ontology.core.meta.OntologyTermEntity;
import org.molgenis.ontology.core.meta.OntologyTermMetaData;
import org.molgenis.ontology.core.meta.OntologyTermNodePath;
import org.molgenis.ontology.core.meta.OntologyTermNodePathMetaData;
import org.molgenis.ontology.core.meta.OntologyTermSynonym;
import org.molgenis.ontology.core.meta.SemanticTypeEntity;
import org.molgenis.ontology.core.meta.SemanticTypeMetaData;
import org.molgenis.ontology.core.model.OntologyTermAnnotation;
import org.molgenis.ontology.core.model.OntologyTermImpl;
import org.molgenis.ontology.core.model.SemanticType;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

/**
 * Maps {@link OntologyTermMetaData} {@link Entity} <-> {@link OntologyTermImpl}
 */
public class OntologyTermRepository
{
	public static final int DEFAULT_EXPANSION_LEVEL = 3;

	private static final String ESCAPED_NODEPATH_SEPARATOR = "\\.";
	private static final String NODEPATH_SEPARATOR = ".";

	private final DataService dataService;
	private final OntologyTermMetaData ontologyTermMetaData;

	@Autowired
	public OntologyTermRepository(DataService dataService, OntologyTermMetaData ontologyTermMetaData)
	{
		this.dataService = requireNonNull(dataService);
		this.ontologyTermMetaData = requireNonNull(ontologyTermMetaData);
	}

	/**
	 * FIXME write docs
	 *
	 * @param term
	 * @param pageSize
	 * @return
	 */
	public List<OntologyTermImpl> findOntologyTerms(String term, int pageSize)
	{
		Iterable<OntologyTermEntity> ontologyTermEntities;

		// #1 find exact match
		Query<OntologyTermEntity> termNameQuery = new QueryImpl<OntologyTermEntity>()
				.eq(OntologyTermMetaData.ONTOLOGY_TERM_NAME, term).pageSize(pageSize);

		ontologyTermEntities = new Iterable<OntologyTermEntity>()
		{
			@Override
			public Iterator<OntologyTermEntity> iterator()
			{
				return dataService.findAll(ONTOLOGY_TERM, termNameQuery, OntologyTermEntity.class).iterator();
			}
		};

		if (!ontologyTermEntities.iterator().hasNext())
		{
			Query<OntologyTermEntity> termsQuery = new QueryImpl<OntologyTermEntity>().search(term).pageSize(pageSize);
			ontologyTermEntities = new Iterable<OntologyTermEntity>()
			{
				@Override
				public Iterator<OntologyTermEntity> iterator()
				{
					return dataService.findAll(ONTOLOGY_TERM, termsQuery, OntologyTermEntity.class).iterator();
				}
			};
		}

		return stream(ontologyTermEntities.spliterator(), false).map(OntologyTermRepository::toOntologyTerm)
				.collect(toList());
	}

	/**
	 * Finds exact {@link OntologyTermImpl}s within {@link OntologyEntity}s.
	 *
	 * @param ontologyIds
	 *            IDs of the {@link OntologyEntity}s to search in
	 * @param terms
	 *            {@link List} of search terms. the {@link OntologyTermImpl} must match at least one of these terms
	 * @param pageSize
	 *            max number of results
	 * @return {@link List} of {@link OntologyTermImpl}s
	 */
	public List<OntologyTermImpl> findExcatOntologyTerms(List<String> ontologyIds, Set<String> terms, int pageSize)
	{
		List<OntologyTermImpl> findOntologyTerms = findOntologyTerms(ontologyIds, terms, pageSize);
		return findOntologyTerms.stream().filter(ontologyTerm -> isOntologyTermExactMatch(terms, ontologyTerm))
				.collect(Collectors.toList());
	}

	private boolean isOntologyTermExactMatch(Set<String> terms, OntologyTermImpl ontologyTermImpl)
	{
		Set<String> lowerCaseSearchTerms = terms.stream().map(StringUtils::lowerCase).collect(Collectors.toSet());
		for (String synonym : ontologyTermImpl.getSynonyms())
		{
			if (lowerCaseSearchTerms.contains(synonym.toLowerCase()))
			{
				return true;
			}
		}
		if (lowerCaseSearchTerms.contains(ontologyTermImpl.getLabel().toLowerCase()))
		{
			return true;
		}
		return false;
	}

	/**
	 * Finds {@link OntologyTermImpl}s within {@link OntologyEntity}s.
	 *
	 * @param ontologyIds
	 *            IDs of the {@link OntologyEntity}s to search in
	 * @param terms
	 *            {@link List} of search terms. the {@link OntologyTermImpl} must match at least one of these terms
	 * @param pageSize
	 *            max number of results
	 * @return {@link List} of {@link OntologyTermImpl}s
	 */
	public List<OntologyTermImpl> findOntologyTerms(List<String> ontologyIds, Set<String> terms, int pageSize)
	{
		List<QueryRule> rules = new ArrayList<QueryRule>();
		for (String term : terms)
		{
			if (rules.size() > 0)
			{
				rules.add(new QueryRule(Operator.OR));
			}
			rules.add(new QueryRule(OntologyTermMetaData.ONTOLOGY_TERM_SYNONYM, Operator.FUZZY_MATCH, term));
		}
		rules = Arrays.asList(new QueryRule(ONTOLOGY, Operator.IN, ontologyIds), new QueryRule(Operator.AND),
				new QueryRule(rules));

		List<OntologyTermImpl> ontologyTerms = dataService
				.findAll(ONTOLOGY_TERM, new QueryImpl<OntologyTermEntity>(rules).pageSize(pageSize),
						OntologyTermEntity.class)
				.map(OntologyTermRepository::toOntologyTerm).collect(Collectors.toList());

		return ontologyTerms;
	}

	public List<OntologyTermImpl> findOntologyTerms(List<String> ontologyIds, Set<String> terms, int pageSize,
			List<OntologyTermImpl> ontologyTermScope)
	{
		Fetch fetch = new Fetch();
		ontologyTermMetaData.getAtomicAttributes().forEach(attribute -> fetch.field(attribute.getName()));
		List<QueryRule> rules = new ArrayList<QueryRule>();
		for (String term : terms)
		{
			if (rules.size() > 0)
			{
				rules.add(new QueryRule(OR));
			}
			rules.add(new QueryRule(OntologyTermMetaData.ONTOLOGY_TERM_SYNONYM, FUZZY_MATCH, term));
		}
		rules = Arrays.asList(new QueryRule(ONTOLOGY, IN, ontologyIds), new QueryRule(AND), new QueryRule(rules));

		List<String> filteredOntologyTermIris = ontologyTermScope.stream().map(OntologyTermImpl::getIRI)
				.collect(toList());

		rules = Arrays.asList(new QueryRule(ONTOLOGY_TERM_IRI, IN, filteredOntologyTermIris), new QueryRule(AND),
				new QueryRule(rules));

		return dataService.findAll(OntologyTermMetaData.ONTOLOGY_TERM,
				new QueryImpl<OntologyTermEntity>(rules).pageSize(pageSize).fetch(fetch), OntologyTermEntity.class)
				.map(OntologyTermRepository::toOntologyTerm).collect(toList());
	}

	public List<OntologyTermImpl> getAllOntologyTerms(String ontologyId)
	{
		OntologyEntity ontology = dataService.findOne(OntologyMetaData.ONTOLOGY,
				new QueryImpl<OntologyEntity>().eq(OntologyMetaData.ONTOLOGY_IRI, ontologyId), OntologyEntity.class);

		if (ontology != null)
		{
			Query<OntologyTermEntity> query = new QueryImpl<OntologyTermEntity>()
					.eq(OntologyTermMetaData.ONTOLOGY, ontology).pageSize(MAX_VALUE);
			List<OntologyTermImpl> collect = dataService
					.findAll(OntologyTermMetaData.ONTOLOGY_TERM, query, OntologyTermEntity.class)
					.map(OntologyTermRepository::toOntologyTerm).collect(toList());
			return collect;
		}

		return Collections.emptyList();
	}

	/**
	 * Retrieves an {@link OntologyTermImpl} for one or more IRIs
	 *
	 * @param iris
	 *            Array of {@link OntologyTermImpl} IRIs
	 * @return combined {@link OntologyTermImpl} for the iris.
	 */
	public OntologyTermImpl getOntologyTerm(String iri)
	{
		OntologyTermEntity ontologyTermEntity = dataService.findOne(ONTOLOGY_TERM,
				new QueryImpl<OntologyTermEntity>().eq(ONTOLOGY_TERM_IRI, iri), OntologyTermEntity.class);

		return toOntologyTerm(ontologyTermEntity);
	}

	public List<OntologyTermImpl> getOntologyTerms(List<String> iris)
	{
		List<OntologyTermImpl> ontologyTermImpls = Lists.newArrayList();
		for (String iri : iris)
		{
			OntologyTermEntity ontologyTermEntity = dataService.findOne(ONTOLOGY_TERM,
					new QueryImpl<OntologyTermEntity>().eq(ONTOLOGY_TERM_IRI, iri), OntologyTermEntity.class);

			if (nonNull(ontologyTermEntity))
			{
				ontologyTermImpls.add(toOntologyTerm(ontologyTermEntity));
			}
		}
		return ontologyTermImpls;
	}

	/**
	 * Calculate the distance between any two ontology terms in the ontology tree structure by calculating the
	 * difference in nodePaths.
	 *
	 * @param ontologyTermImpl1
	 * @param ontologyTermImpl2
	 *
	 * @return the distance between two ontology terms
	 */
	public Integer getOntologyTermDistance(OntologyTermImpl ontologyTermImpl1, OntologyTermImpl ontologyTermImpl2)
	{
		if (ontologyTermImpl1.getNodePaths().isEmpty() || ontologyTermImpl2.getNodePaths().isEmpty()) return 0;

		OptionalInt min = ontologyTermImpl1.getNodePaths().stream()
				.flatMap(nodePath1 -> ontologyTermImpl2.getNodePaths().stream()
						.map(nodePath2 -> calculateNodePathDistance(nodePath1, nodePath2)))
				.mapToInt(Integer::valueOf).min();

		return min.isPresent() ? min.getAsInt() : 0;
	}

	/**
	 * Calculate the semantic relatedness between any two ontology terms in the ontology tree
	 *
	 * @param ontologyTermImpl1
	 * @param ontologyTermImpl2
	 *
	 * @return the distance between two ontology terms
	 */
	public double getOntologyTermSemanticRelatedness(OntologyTermImpl ontologyTermImpl1,
			OntologyTermImpl ontologyTermImpl2)
	{
		if (ontologyTermImpl1.getIRI().equals(ontologyTermImpl2.getIRI())) return 1;

		if (ontologyTermImpl1.getNodePaths().isEmpty() || ontologyTermImpl2.getNodePaths().isEmpty()) return 0;

		OptionalDouble max = ontologyTermImpl1.getNodePaths().stream()
				.flatMap(nodePath1 -> ontologyTermImpl2.getNodePaths().stream()
						.map(nodePath2 -> calculateRelatedness(nodePath1, nodePath2)))
				.mapToDouble(Double::valueOf).max();

		return max.isPresent() ? max.getAsDouble() : 0;
	}

	/**
	 * Calculate the distance between nodePaths, e.g. 0[0].1[1].2[2], 0[0].2[1].2[2]. The distance is the non-overlap
	 * part of the strings
	 *
	 * @param nodePath1
	 * @param nodePath2
	 * @return distance
	 */
	public int calculateNodePathDistance(String nodePath1, String nodePath2)
	{
		String[] nodePathFragment1 = isBlank(nodePath1) ? new String[0] : nodePath1.split(ESCAPED_NODEPATH_SEPARATOR);
		String[] nodePathFragment2 = isBlank(nodePath2) ? new String[0] : nodePath2.split(ESCAPED_NODEPATH_SEPARATOR);

		int overlapBlock = calculateOverlapBlock(nodePathFragment1, nodePathFragment2);
		return nodePathFragment1.length + nodePathFragment2.length - overlapBlock * 2;
	}

	double calculateRelatedness(String nodePath1, String nodePath2)
	{
		String[] nodePathFragment1 = isBlank(nodePath1) ? new String[0] : nodePath1.split(ESCAPED_NODEPATH_SEPARATOR);
		String[] nodePathFragment2 = isBlank(nodePath2) ? new String[0] : nodePath2.split(ESCAPED_NODEPATH_SEPARATOR);

		int overlapBlock = calculateOverlapBlock(nodePathFragment1, nodePathFragment2);

		if (nodePathFragment1.length == 0 || nodePathFragment2.length == 0) return 0;

		return (double) 2 * overlapBlock / (nodePathFragment1.length + nodePathFragment2.length);
	}

	int calculateOverlapBlock(String[] nodePathFragment1, String[] nodePathFragment2)
	{
		int overlapBlock = 0;
		while (overlapBlock < nodePathFragment1.length && overlapBlock < nodePathFragment2.length
				&& nodePathFragment1[overlapBlock].equals(nodePathFragment2[overlapBlock]))
		{
			overlapBlock++;
		}
		return overlapBlock;
	}

	public Iterable<OntologyTermImpl> getParents(OntologyTermImpl ontologyTermImpl, int maxLevel)
	{
		List<OntologyTermImpl> parentOntologyTerms = new ArrayList<>();

		List<String> nodePaths = ontologyTermImpl.getNodePaths();

		for (int i = 0; i < maxLevel; i++)
		{
			nodePaths = nodePaths.stream().map(this::getParentNodePath).filter(StringUtils::isNotBlank)
					.collect(toList());

			if (nodePaths.size() > 0)
			{
				// TODO: my teammates will take look
				List<String> nodePathEntityIdentifiers = nodePaths.stream()
						.map(nodePath -> dataService.findOne(OntologyTermNodePathMetaData.ONTOLOGY_TERM_NODE_PATH,
								new QueryImpl<OntologyTermNodePath>().eq(OntologyTermNodePathMetaData.NODE_PATH,
										nodePath),
								OntologyTermNodePath.class))
						.filter(Objects::nonNull).map(Entity::getIdValue).map(Object::toString).collect(toList());

				if (nodePathEntityIdentifiers.size() > 0)
				{
					List<OntologyTermImpl> ontologyTerms = dataService
							.findAll(OntologyTermMetaData.ONTOLOGY_TERM,
									new QueryImpl<OntologyTermEntity>().in(OntologyTermMetaData.ONTOLOGY_TERM_NODE_PATH,
											nodePathEntityIdentifiers),
									OntologyTermEntity.class)
							.map(OntologyTermRepository::toOntologyTerm).collect(toList());

					nodePaths = ontologyTerms.stream().flatMap(ot -> ot.getNodePaths().stream())
							.collect(Collectors.toList());

					parentOntologyTerms.addAll(ontologyTerms);
				}
			}
			else break;
		}

		return parentOntologyTerms;
	}

	/**
	 * Get the {@link OntologyTermImpl} children at the specified level
	 * 
	 * @param ontologyTermImpl
	 * @param maxLevel
	 * @return
	 */
	public Iterable<OntologyTermImpl> getChildren(OntologyTermImpl ontologyTermImpl, int maxLevel)
	{
		BiPredicate<String, String> ontologyTermChildrenPredicate = new BiPredicate<String, String>()
		{
			public boolean test(String parentNodePath, String childNodePath)
			{
				return calculateNodePathDistance(parentNodePath, childNodePath) <= maxLevel;
			}
		};
		return getChildren(ontologyTermImpl, ontologyTermChildrenPredicate);
	}

	/**
	 * Retrieve all {@link OntologyTermImpl} children that satisfy the children predicate containing the instruction to
	 * stop retrieving children at the given max level
	 * 
	 * @param ontologyTermImpl
	 * @param ontologyTermChildrenPredicate
	 * @return
	 */
	private Iterable<OntologyTermImpl> getChildren(OntologyTermImpl ontologyTermImpl,
			BiPredicate<String, String> ontologyTermChildrenPredicate)
	{
		Fetch fetch = new Fetch();
		ontologyTermMetaData.getAtomicAttributes().forEach(attribute -> fetch.field(attribute.getName()));

		OntologyTermEntity ontologyTermEntity = dataService.findOne(OntologyTermMetaData.ONTOLOGY_TERM,
				new QueryImpl<OntologyTermEntity>().eq(ONTOLOGY_TERM_IRI, ontologyTermImpl.getIRI()).fetch(fetch),
				OntologyTermEntity.class);

		Iterable<OntologyTermImpl> iterable = null;

		if (ontologyTermEntity != null)
		{
			OntologyEntity ontologyEntity = ontologyTermEntity.getOntology();

			List<String> nodePaths = StreamSupport
					.stream(ontologyTermEntity.getOntologyTermNodePaths().spliterator(), false)
					.map(OntologyTermNodePath::getNodePath).collect(Collectors.toList());

			if (nodePaths.size() > 0)
			{
				// The nodePaths that start with the same starting point have the same children in UMLS
				Multimap<String, String> uniqueSubTrees = LinkedHashMultimap.create();
				for (String nodePath : nodePaths)
				{
					String startNodePath = nodePath.split(ESCAPED_NODEPATH_SEPARATOR)[0];
					uniqueSubTrees.put(startNodePath, nodePath);
				}

				for (Entry<String, Collection<String>> entrySet : uniqueSubTrees.asMap().entrySet())
				{
					String nodePath = entrySet.getValue().iterator().next();
					Iterable<OntologyTermImpl> childOntologyTermStream = childOntologyTermStream(ontologyTermImpl,
							ontologyEntity, nodePath, ontologyTermChildrenPredicate);
					iterable = iterable == null ? childOntologyTermStream
							: Iterables.concat(iterable, childOntologyTermStream);
				}
			}
		}

		return iterable == null ? emptyList() : iterable;
	}

	// FIXME: this is a work around for getting the children of the currentNodePathEntity. The essential problem is
	// that ElasticSearch doesn't support the startsWith type of search. Therefore we first of all need to get all the
	// nodePaths that look similar to the currentNodePath of interest. Then we filter out all the nodePaths that are not
	// actually the children of the currentNodePath of interest. As the size of ontology gets bigger, it can be a very
	// expensive operation, luckily all the similar nodePaths are sorted based on the relevance, so we can stop looking
	// when we encounter the first nodePath (mismatch) that is not a child of the currentNodePath because we know the
	// rest of the nodePaths cannot be more similar than the first mismatch.
	Iterable<OntologyTermImpl> childOntologyTermStream(OntologyTermImpl ontologyTermImpl, Entity ontologyEntity,
			final String parentNodePath, BiPredicate<String, String> childrenPredicate)
	{
		Query<OntologyTermNodePath> ontologyTermNodePathQuery = new QueryImpl<OntologyTermNodePath>(
				new QueryRule(OntologyTermNodePathMetaData.NODE_PATH, FUZZY_MATCH, "\"" + parentNodePath + "\""));

		Iterable<OntologyTermNodePath> ontologyTermNodePathEntities = new Iterable<OntologyTermNodePath>()
		{
			public Iterator<OntologyTermNodePath> iterator()
			{
				Iterator<OntologyTermNodePath> ontologyTermIterator = new Iterator<OntologyTermNodePath>()
				{
					private OntologyTermNodePath prevEntity = null;
					private final Iterator<OntologyTermNodePath> ontologyTermNodePathIterator = dataService
							.findAll(OntologyTermNodePathMetaData.ONTOLOGY_TERM_NODE_PATH, ontologyTermNodePathQuery,
									OntologyTermNodePath.class)
							.iterator();

					public boolean hasNext()
					{
						boolean continueIteration = true;
						if (prevEntity != null)
						{
							continueIteration = childrenPredicate.test(parentNodePath, prevEntity.getNodePath());
						}
						return ontologyTermNodePathIterator.hasNext() && continueIteration;
					}

					public OntologyTermNodePath next()
					{
						prevEntity = ontologyTermNodePathIterator.next();
						return prevEntity;
					}
				};
				return filter(ontologyTermIterator, entity -> !entity.getNodePath().equals(parentNodePath)
						&& entity.getNodePath().startsWith(parentNodePath));
			}
		};

		if (Iterables.isEmpty(ontologyTermNodePathEntities))
		{
			return Collections.emptyList();
		}

		Query<OntologyTermEntity> ontologyTermQuery = new QueryImpl<OntologyTermEntity>(
				new QueryRule(OntologyTermMetaData.ONTOLOGY_TERM_NODE_PATH, IN, ontologyTermNodePathEntities)).and()
						.eq(OntologyTermMetaData.ONTOLOGY, ontologyEntity);

		return new Iterable<OntologyTermImpl>()
		{
			@Override
			public Iterator<OntologyTermImpl> iterator()
			{
				return dataService
						.findAll(OntologyTermMetaData.ONTOLOGY_TERM, ontologyTermQuery, OntologyTermEntity.class)
						.map(OntologyTermRepository::toOntologyTerm).iterator();
			}
		};
	}

	public List<SemanticType> getAllSemanticType()
	{
		return dataService.findAll(SemanticTypeMetaData.SEMANTIC_TYPE, SemanticTypeEntity.class)
				.map(entity -> SemanticType.create(entity.getIdentifier(), entity.getSemanticTypeName(),
						entity.getSemanticTypeGroup(), entity.isGlobalKeyConcept()))
				.collect(Collectors.toList());
	}

	public boolean related(OntologyTermImpl ontologyTermImpl1, OntologyTermImpl ontologyTermImpl2, int stopLevel)
	{
		if (ontologyTermImpl1.getIRI().equals(ontologyTermImpl2.getIRI())) return true;

		List<String> targetNodePaths = ontologyTermImpl1.getNodePaths().stream()
				.filter(nodePath -> nodePath.split(ESCAPED_NODEPATH_SEPARATOR).length > stopLevel)
				.collect(Collectors.toList());

		List<String> sourceNodePaths = ontologyTermImpl2.getNodePaths().stream()
				.filter(nodePath -> nodePath.split(ESCAPED_NODEPATH_SEPARATOR).length > stopLevel)
				.collect(Collectors.toList());

		if (targetNodePaths.isEmpty() || sourceNodePaths.isEmpty())
		{
			return false;
		}

		return targetNodePaths.stream().anyMatch(targetNodePath -> sourceNodePaths.stream().anyMatch(
				sourceNodePath -> targetNodePath.contains(sourceNodePath) || sourceNodePath.contains(targetNodePath)));
	}

	/**
	 * If any of the nodePaths of both of {@link OntologyTermImpl}s are within (less and equal) the max distance.
	 * 
	 * @param ontologyTermImpl1
	 * @param ontologyTermImpl2
	 * @param maxDistance
	 * @return
	 */
	public boolean areWithinDistance(OntologyTermImpl ontologyTermImpl1, OntologyTermImpl ontologyTermImpl2,
			int maxDistance)
	{
		if (ontologyTermImpl1.getIRI().equals(ontologyTermImpl2.getIRI())) return true;

		if (ontologyTermImpl1.getNodePaths().isEmpty() || ontologyTermImpl2.getNodePaths().isEmpty()) return false;

		boolean anyMatch = ontologyTermImpl1.getNodePaths().stream()
				.anyMatch(nodePath1 -> ontologyTermImpl2.getNodePaths().stream()
						.anyMatch(nodePath2 -> calculateNodePathDistance(nodePath1, nodePath2) <= maxDistance));

		return anyMatch;
	}

	private String getParentNodePath(String currentNodePath)
	{
		String[] split = currentNodePath.split(ESCAPED_NODEPATH_SEPARATOR);
		if (split.length > 0)
		{
			return StringUtils.join(Stream.of(split).limit(split.length - 1).collect(toList()), NODEPATH_SEPARATOR);
		}
		return StringUtils.EMPTY;
	}

	public static OntologyTermImpl toOntologyTerm(OntologyTermEntity ontologyTermEntity)
	{
		if (Objects.isNull(ontologyTermEntity))
		{
			return null;
		}

		// Collect synonyms if there are any
		List<String> synonyms = new ArrayList<>();

		Iterable<OntologyTermSynonym> ontologyTermSynonymEntities = ontologyTermEntity.getOntologyTermSynonyms();

		if (ontologyTermSynonymEntities != null)
		{
			synonyms.addAll(stream(ontologyTermSynonymEntities.spliterator(), false)
					.map(OntologyTermSynonym::getOntologyTermSynonym).collect(Collectors.toSet()));
		}

		// Collection nodePaths is there are any
		List<String> nodePaths = new ArrayList<>();
		Iterable<OntologyTermNodePath> ontologyTermNodePathEntities = ontologyTermEntity.getOntologyTermNodePaths();
		if (ontologyTermNodePathEntities != null)
		{
			nodePaths.addAll(stream(ontologyTermNodePathEntities.spliterator(), false)
					.map(OntologyTermNodePath::getNodePath).collect(toList()));
		}

		// Collect annotations if there are any
		List<OntologyTermAnnotation> annotations = new ArrayList<>();
		Iterable<OntologyTermDynamicAnnotation> ontologyTermAnnotationEntities = ontologyTermEntity
				.getOntologyTermDynamicAnnotations();
		if (ontologyTermAnnotationEntities != null)
		{
			annotations.addAll(stream(ontologyTermAnnotationEntities.spliterator(), false)
					.map(annotation -> OntologyTermAnnotation.create(annotation.getName(), annotation.getValue()))
					.collect(Collectors.toList()));
		}

		// Collect semantic types if there are any
		List<SemanticType> semanticTypes = new ArrayList<>();
		Iterable<SemanticTypeEntity> ontologyTermSemanticTypeEntities = ontologyTermEntity.getSemanticTypes();
		if (ontologyTermSemanticTypeEntities != null)
		{
			semanticTypes.addAll(stream(ontologyTermSemanticTypeEntities.spliterator(), false)
					.map(semanticType -> SemanticType.create(semanticType.getIdentifier(),
							semanticType.getSemanticTypeName(), semanticType.getSemanticTypeGroup(),
							semanticType.isGlobalKeyConcept()))
					.collect(Collectors.toList()));
		}

		return OntologyTermImpl.create(ontologyTermEntity.getId(), ontologyTermEntity.getOntologyTermIri(),
				ontologyTermEntity.getOntologyTermName(), null, synonyms, nodePaths, annotations, semanticTypes);
	}
}
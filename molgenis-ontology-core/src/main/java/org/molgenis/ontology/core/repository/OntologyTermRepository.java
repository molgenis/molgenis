package org.molgenis.ontology.core.repository;

import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.common.collect.Lists;
import org.molgenis.data.*;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.ontology.core.meta.*;
import org.molgenis.ontology.core.model.OntologyTerm;
import org.molgenis.ontology.core.model.OntologyTermAnnotation;
import org.molgenis.ontology.core.model.SemanticType;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.google.common.collect.Iterators.filter;
import static java.lang.Integer.MAX_VALUE;
import static java.util.Collections.emptyList;
import static java.util.Comparator.naturalOrder;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.molgenis.data.QueryRule.Operator.*;
import static org.molgenis.ontology.core.meta.OntologyTermMetaData.*;

/**
 * Maps {@link OntologyTermMetaData} {@link Entity} <-> {@link OntologyTerm}
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
	public List<OntologyTerm> findOntologyTerms(String term, int pageSize)
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
	 * Finds exact {@link OntologyTerm}s within {@link OntologyEntity}s.
	 *
	 * @param ontologyIds IDs of the {@link OntologyEntity}s to search in
	 * @param terms       {@link List} of search terms. the {@link OntologyTerm} must match at least one of these terms
	 * @param pageSize    max number of results
	 * @return {@link List} of {@link OntologyTerm}s
	 */
	public List<OntologyTerm> findExcatOntologyTerms(List<String> ontologyIds, Set<String> terms, int pageSize)
	{
		List<OntologyTerm> findOntologyTerms = findOntologyTerms(ontologyIds, terms, pageSize);
		return findOntologyTerms.stream().filter(ontologyTerm -> isOntologyTermExactMatch(terms, ontologyTerm))
				.collect(Collectors.toList());
	}

	private boolean isOntologyTermExactMatch(Set<String> terms, OntologyTerm ontologyTerm)
	{
		Set<String> lowerCaseSearchTerms = terms.stream().map(StringUtils::lowerCase).collect(Collectors.toSet());
		for (String synonym : ontologyTerm.getSynonyms())
		{
			if (lowerCaseSearchTerms.contains(synonym.toLowerCase()))
			{
				return true;
			}
		}
		if (lowerCaseSearchTerms.contains(ontologyTerm.getLabel().toLowerCase()))
		{
			return true;
		}
		return false;
	}

	/**
	 * Finds {@link OntologyTerm}s within {@link OntologyEntity}s.
	 *
	 * @param ontologyIds IDs of the {@link OntologyEntity}s to search in
	 * @param terms       {@link List} of search terms. the {@link OntologyTerm} must match at least one of these terms
	 * @param pageSize    max number of results
	 * @return {@link List} of {@link OntologyTerm}s
	 */
	public List<OntologyTerm> findOntologyTerms(List<String> ontologyIds, Set<String> terms, int pageSize)
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

		List<OntologyTerm> ontologyTerms = dataService
				.findAll(ONTOLOGY_TERM, new QueryImpl<OntologyTermEntity>(rules).pageSize(pageSize),
						OntologyTermEntity.class).map(OntologyTermRepository::toOntologyTerm)
				.collect(Collectors.toList());

		return ontologyTerms;
	}

	public List<OntologyTerm> findOntologyTerms(List<String> ontologyIds, Set<String> terms, int pageSize,
			List<OntologyTerm> ontologyTermScope)
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

		List<String> filteredOntologyTermIris = ontologyTermScope.stream().map(OntologyTerm::getIRI)
				.collect(toList());

		rules = Arrays.asList(new QueryRule(ONTOLOGY_TERM_IRI, IN, filteredOntologyTermIris), new QueryRule(AND),
				new QueryRule(rules));

		return dataService.findAll(OntologyTermMetaData.ONTOLOGY_TERM,
				new QueryImpl<OntologyTermEntity>(rules).pageSize(pageSize).fetch(fetch), OntologyTermEntity.class)
				.map(OntologyTermRepository::toOntologyTerm).collect(toList());
	}

	public List<OntologyTerm> getAllOntologyTerms(String ontologyId)
	{
		OntologyEntity ontology = dataService.findOne(OntologyMetaData.ONTOLOGY,
				new QueryImpl<OntologyEntity>().eq(OntologyMetaData.ONTOLOGY_IRI, ontologyId), OntologyEntity.class);

		if (ontology != null)
		{
			Query<OntologyTermEntity> query = new QueryImpl<OntologyTermEntity>()
					.eq(OntologyTermMetaData.ONTOLOGY, ontology).pageSize(MAX_VALUE);
			List<OntologyTerm> collect = dataService
					.findAll(OntologyTermMetaData.ONTOLOGY_TERM, query, OntologyTermEntity.class)
					.map(OntologyTermRepository::toOntologyTerm).collect(toList());
			return collect;
		}

		return Collections.emptyList();
	}

	/**
	 * Retrieves an {@link OntologyTerm} for an IRI
	 *
	 * @param iri The IRI
	 * @return {@link OntologyTerm} for the iri
	 */
	public OntologyTerm getOntologyTerm(String iri)
	{
		OntologyTermEntity ontologyTermEntity = dataService
				.findOne(ONTOLOGY_TERM, new QueryImpl<OntologyTermEntity>().eq(ONTOLOGY_TERM_IRI, iri),
						OntologyTermEntity.class);

		return toOntologyTerm(ontologyTermEntity);
	}

	public List<OntologyTerm> getOntologyTerms(List<String> iris)
	{
		List<OntologyTerm> ontologyTerms = Lists.newArrayList();
		for (String iri : iris)
		{
			OntologyTermEntity ontologyTermEntity = dataService
					.findOne(ONTOLOGY_TERM, new QueryImpl<OntologyTermEntity>().eq(ONTOLOGY_TERM_IRI, iri),
							OntologyTermEntity.class);

			if (nonNull(ontologyTermEntity))
			{
				ontologyTerms.add(toOntologyTerm(ontologyTermEntity));
			}
		}
		return ontologyTerms;
	}

	/**
	 * Calculate the distance between any two ontology terms in the ontology tree structure by calculating the
	 * difference in nodePaths.
	 *
	 * @param ontologyTerm1 the first {@link OntologyTerm}
	 * @param ontologyTerm2 the second {@link OntologyTerm}
	 * @return the distance between two ontology terms
	 */
	public Integer getOntologyTermDistance(OntologyTerm ontologyTerm1, OntologyTerm ontologyTerm2)
	{
		if (ontologyTerm1.getNodePaths().isEmpty() || ontologyTerm2.getNodePaths().isEmpty()) return 0;

		return ontologyTerm1.getNodePaths().stream().flatMap(
				nodePath1 -> ontologyTerm2.getNodePaths().stream()
						.map(nodePath2 -> calculateNodePathDistance(nodePath1, nodePath2)))
				.min(naturalOrder()).orElse(0);
	}

	/**
	 * Calculate the semantic relatedness between any two ontology terms in the same ontology tree
	 *
	 * @param ontologyTerm1 the first ontology term
	 * @param ontologyTerm2 the second ontology term
	 * @return the distance between two ontology terms, 1 if they're equal, 0 if they're unrelated
	 */
	public double getOntologyTermSemanticRelatedness(OntologyTerm ontologyTerm1,
			OntologyTerm ontologyTerm2)
	{
		if (ontologyTerm1.getIRI().equals(ontologyTerm2.getIRI())) return 1;

		if (ontologyTerm1.getNodePaths().isEmpty() || ontologyTerm2.getNodePaths().isEmpty()) return 0;

		return ontologyTerm1.getNodePaths().stream().flatMap(
				nodePath1 -> ontologyTerm2.getNodePaths().stream()
						.map(nodePath2 -> calculateRelatedness(nodePath1, nodePath2)))
				.max(naturalOrder()).orElse(0.0);
	}

	/**
	 * Calculate the distance between nodePaths, e.g. 0[0].1[1].2[2], 0[0].2[1].2[2]. The distance is the non-overlap
	 * part of the strings
	 *
	 * @param nodePath1 the first node path to compare
	 * @param nodePath2 the second node path to compare
	 * @return distance the distance between the two node paths.
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

	public Iterable<OntologyTerm> getParents(OntologyTerm ontologyTerm, int maxLevel)
	{
		List<OntologyTerm> parentOntologyTerms = new ArrayList<>();

		List<String> nodePaths = ontologyTerm.getNodePaths();

		for (int i = 0; i < maxLevel; i++)
		{
			nodePaths = nodePaths.stream().map(this::getParentNodePath).filter(StringUtils::isNotBlank)
					.collect(toList());

			if (nodePaths.size() > 0)
			{
				// TODO: my teammates will take look
				List<String> nodePathEntityIdentifiers = nodePaths.stream().map(nodePath -> dataService
						.findOne(OntologyTermNodePathMetaData.ONTOLOGY_TERM_NODE_PATH,
								new QueryImpl<OntologyTermNodePath>()
										.eq(OntologyTermNodePathMetaData.NODE_PATH, nodePath),
								OntologyTermNodePath.class)).filter(Objects::nonNull).map(Entity::getIdValue)
						.map(Object::toString).collect(toList());

				if (nodePathEntityIdentifiers.size() > 0)
				{
					List<OntologyTerm> ontologyTerms = dataService.findAll(OntologyTermMetaData.ONTOLOGY_TERM,
							new QueryImpl<OntologyTermEntity>()
									.in(OntologyTermMetaData.ONTOLOGY_TERM_NODE_PATH, nodePathEntityIdentifiers),
							OntologyTermEntity.class).map(OntologyTermRepository::toOntologyTerm).collect(toList());

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
	 * Get the {@link OntologyTerm} children at the specified level
	 *
	 * @param ontologyTerm 
	 * @param maxLevel
	 * @return
	 */
	public Iterable<OntologyTerm> getChildren(OntologyTerm ontologyTerm, int maxLevel)
	{
		BiPredicate<String, String> ontologyTermChildrenPredicate = new BiPredicate<String, String>()
		{
			public boolean test(String parentNodePath, String childNodePath)
			{
				return calculateNodePathDistance(parentNodePath, childNodePath) <= maxLevel;
			}
		};
		return getChildren(ontologyTerm, ontologyTermChildrenPredicate);
	}

	/**
	 * Retrieve all {@link OntologyTerm} children that satisfy the children predicate containing the instruction to
	 * stop retrieving children at the given max level
	 *
	 * @param ontologyTerm
	 * @param ontologyTermChildrenPredicate
	 * @return
	 */
	private Iterable<OntologyTerm> getChildren(OntologyTerm ontologyTerm,
			BiPredicate<String, String> ontologyTermChildrenPredicate)
	{
		Fetch fetch = new Fetch();
		ontologyTermMetaData.getAtomicAttributes().forEach(attribute -> fetch.field(attribute.getName()));

		OntologyTermEntity ontologyTermEntity = dataService.findOne(OntologyTermMetaData.ONTOLOGY_TERM,
				new QueryImpl<OntologyTermEntity>().eq(ONTOLOGY_TERM_IRI, ontologyTerm.getIRI()).fetch(fetch),
				OntologyTermEntity.class);

		Iterable<OntologyTerm> iterable = null;

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
					Iterable<OntologyTerm> childOntologyTermStream = childOntologyTermStream(ontologyTerm,
							ontologyEntity, nodePath, ontologyTermChildrenPredicate);
					iterable = iterable == null ? childOntologyTermStream : Iterables
							.concat(iterable, childOntologyTermStream);
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
	Iterable<OntologyTerm> childOntologyTermStream(OntologyTerm ontologyTerm, Entity ontologyEntity,
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
									OntologyTermNodePath.class).iterator();

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
				return filter(ontologyTermIterator,
						entity -> !entity.getNodePath().equals(parentNodePath) && entity.getNodePath()
								.startsWith(parentNodePath));
			}
		};

		if (Iterables.isEmpty(ontologyTermNodePathEntities))
		{
			return Collections.emptyList();
		}

		Query<OntologyTermEntity> ontologyTermQuery = new QueryImpl<OntologyTermEntity>(
				new QueryRule(OntologyTermMetaData.ONTOLOGY_TERM_NODE_PATH, IN, ontologyTermNodePathEntities)).and()
				.eq(OntologyTermMetaData.ONTOLOGY, ontologyEntity);

		return new Iterable<OntologyTerm>()
		{
			@Override
			public Iterator<OntologyTerm> iterator()
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
				.map(entity -> SemanticType
						.create(entity.getIdentifier(), entity.getSemanticTypeName(), entity.getSemanticTypeGroup(),
								entity.isGlobalKeyConcept())).collect(Collectors.toList());
	}

	public boolean related(OntologyTerm ontologyTerm1, OntologyTerm ontologyTerm2, int stopLevel)
	{
		if (ontologyTerm1.getIRI().equals(ontologyTerm2.getIRI())) return true;

		List<String> targetNodePaths = ontologyTerm1.getNodePaths().stream()
				.filter(nodePath -> nodePath.split(ESCAPED_NODEPATH_SEPARATOR).length > stopLevel)
				.collect(Collectors.toList());

		List<String> sourceNodePaths = ontologyTerm2.getNodePaths().stream()
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
	 * If any of the nodePaths of both of {@link OntologyTerm}s are within (less and equal) the max distance.
	 *
	 * @param ontologyTerm1
	 * @param ontologyTerm2
	 * @param maxDistance
	 * @return
	 */
	public boolean areWithinDistance(OntologyTerm ontologyTerm1, OntologyTerm ontologyTerm2,
			int maxDistance)
	{
		if (ontologyTerm1.getIRI().equals(ontologyTerm2.getIRI())) return true;

		if (ontologyTerm1.getNodePaths().isEmpty() || ontologyTerm2.getNodePaths().isEmpty()) return false;

		boolean anyMatch = ontologyTerm1.getNodePaths().stream().anyMatch(
				nodePath1 -> ontologyTerm2.getNodePaths().stream()
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

	public static OntologyTerm toOntologyTerm(OntologyTermEntity ontologyTermEntity)
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
					.map(semanticType -> SemanticType
							.create(semanticType.getIdentifier(), semanticType.getSemanticTypeName(),
									semanticType.getSemanticTypeGroup(), semanticType.isGlobalKeyConcept()))
					.collect(Collectors.toList()));
		}

		return OntologyTerm.create(ontologyTermEntity.getId(), ontologyTermEntity.getOntologyTermIri(),
				ontologyTermEntity.getOntologyTermName(), null, synonyms, nodePaths, annotations, semanticTypes);
	}
}
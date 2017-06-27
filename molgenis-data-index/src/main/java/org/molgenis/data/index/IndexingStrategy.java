package org.molgenis.data.index;

import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableSet;
import org.molgenis.data.index.meta.IndexAction;
import org.molgenis.data.index.meta.IndexActionFactory;
import org.molgenis.data.index.meta.IndexActionGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.google.common.collect.ImmutableSet.copyOf;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.google.common.collect.Sets.union;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.partitioningBy;
import static org.molgenis.data.index.meta.IndexActionMetaData.IndexStatus.PENDING;

/**
 * Determines which IndexActions need to be taken to bring the indices up to date.
 */
@Component
public class IndexingStrategy
{
	private static final Logger LOG = LoggerFactory.getLogger(IndexingStrategy.class);
	private final IndexActionFactory indexActionFactory;

	IndexingStrategy(IndexActionFactory indexActionFactory)
	{
		this.indexActionFactory = Objects.requireNonNull(indexActionFactory);
	}

	/**
	 * Determines which IndexActions are necessary to bring the index up to date with the current transaction.
	 *
	 * @param registeredIndexActions The IndexActions that were registered during a transaction.
	 * @return List<IndexAction> List of IndexActions that are necessary
	 */
	Set<IndexAction> determineNecessaryActions(Collection<IndexAction> registeredIndexActions,
			IndexDependencyModel dependencyModel)
	{
		ImmutableSet<IndexAction> indexActions = copyOf(registeredIndexActions);
		if (indexActions.isEmpty())
		{
			return emptySet();
		}
		Stopwatch sw = Stopwatch.createStarted();
		IndexActionGroup indexActionGroup = indexActions.iterator().next().getIndexActionGroup();
		Set<IndexAction> result = determineNecessaryActionsInternal(indexActions, indexActionGroup, dependencyModel);
		if (LOG.isDebugEnabled())
		{
			LOG.debug("Determined {} necessary actions in {}", result.size(), sw);
		}
		return result;
	}

	/**
	 * Determines the necessary index actions.
	 *
	 * @param indexActions     The index actions stored for the current transaction, deduplicated
	 * @param indexActionGroup The IndexActionGroup that the created IndexActions will belong to
	 * @param dependencies     {@link IndexDependencyModel} to determine which entities depend on which entities
	 * @return List of {@link IndexAction}s
	 */
	private Set<IndexAction> determineNecessaryActionsInternal(ImmutableSet<IndexAction> indexActions,
			IndexActionGroup indexActionGroup, IndexDependencyModel dependencies)
	{
		Map<Boolean, List<IndexAction>> split = indexActions.stream()
				.collect(partitioningBy(IndexAction::isWholeRepository));
		ImmutableSet<String> allEntityTypeIds = indexActions.stream().map(IndexAction::getEntityTypeId)
				.collect(toImmutableSet());
		Set<String> dependentEntities = allEntityTypeIds.stream().flatMap(dependencies::getEntityTypesDependentOn)
				.collect(toImmutableSet());
		return collectResult(indexActionGroup, split.get(false), split.get(true), dependentEntities);
	}

	/**
	 * Collects the results into a List.
	 *
	 * @param indexActionGroup    the IndexGroup that all of these IndexActions will belong to
	 * @param singleEntityActions IndexActions for specific Entity instances. These will only be indexed if their repo will not be reindexed fully
	 * @param wholeRepoActions    IndexActions for the whole Repo
	 * @param dependentEntityIds  Set containing IDs of dependent EntityTypes, we will add IndexActions for the whole of these EntityTypes
	 * @return ImmutableList with the {@link IndexAction}s
	 */
	private Set<IndexAction> collectResult(IndexActionGroup indexActionGroup, List<IndexAction> singleEntityActions,
			List<IndexAction> wholeRepoActions, Set<String> dependentEntityIds)
	{
		Set<String> wholeRepoIds = union(
				wholeRepoActions.stream().map(IndexAction::getEntityTypeId).collect(toImmutableSet()),
				dependentEntityIds);

		ImmutableSet.Builder<IndexAction> result = ImmutableSet.builder();
		result.addAll(wholeRepoActions);
		dependentEntityIds.stream().map(id -> createIndexAction(id, indexActionGroup)).forEach(result::add);
		singleEntityActions.stream().filter(action -> !wholeRepoIds.contains(action.getEntityTypeId()))
				.forEach(result::add);
		return result.build();
	}

	private IndexAction createIndexAction(String referencingEntity, IndexActionGroup indexActionGroup)
	{
		return indexActionFactory.create().setEntityTypeId(referencingEntity).setIndexActionGroup(indexActionGroup)
				.setIndexStatus(PENDING);
	}
}

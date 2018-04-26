package org.molgenis.data.index;

import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.google.common.collect.Sets.union;
import static java.util.stream.Collectors.partitioningBy;

/**
 * Determines the impact of changes.
 */
@Component
public class IndexingStrategy
{
	private static final Logger LOG = LoggerFactory.getLogger(IndexingStrategy.class);

	/**
	 * Determines which {@link Impact}s follow from a set of changes.
	 *
	 * @param changes         The {@link Impact}s of which the impact needs to be determined
	 * @param dependencyModel {@link IndexDependencyModel} to determine which entities depend on which entities
	 * @return Set<Impact> containing the impact of the changes
	 */
	Set<Impact> determineImpact(Set<Impact> changes, IndexDependencyModel dependencyModel)
	{
		Stopwatch sw = Stopwatch.createStarted();
		Map<Boolean, List<Impact>> split = changes.stream().collect(partitioningBy(Impact::isWholeRepository));
		ImmutableSet<String> allEntityTypeIds = changes.stream().map(Impact::getEntityTypeId).collect(toImmutableSet());
		Set<String> dependentEntities = allEntityTypeIds.stream()
														.flatMap(dependencyModel::getEntityTypesDependentOn)
														.collect(toImmutableSet());
		Set<Impact> result = collectResult(split.get(false), split.get(true), dependentEntities);
		if (LOG.isDebugEnabled())
		{
			LOG.debug("Determined {} necessary actions in {}", result.size(), sw);
		}
		return result;
	}

	/**
	 * Combines the results.
	 *
	 * @param singleEntityChanges {@link Impact}s for changes made to specific Entity instances
	 * @param wholeRepoActions    {@link Impact}s for changes made to entire repositories
	 * @param dependentEntityIds  {@link Impact}s for entitytypes that are dependent on one or more of the changes
	 * @return Set with the {@link Impact}s
	 */
	private Set<Impact> collectResult(List<Impact> singleEntityChanges, List<Impact> wholeRepoActions,
			Set<String> dependentEntityIds)
	{
		Set<String> wholeRepoIds = union(
				wholeRepoActions.stream().map(Impact::getEntityTypeId).collect(toImmutableSet()), dependentEntityIds);

		ImmutableSet.Builder<Impact> result = ImmutableSet.builder();
		result.addAll(wholeRepoActions);
		dependentEntityIds.stream().map(Impact::createWholeRepositoryImpact).forEach(result::add);
		singleEntityChanges.stream()
						   .filter(action -> !wholeRepoIds.contains(action.getEntityTypeId()))
						   .forEach(result::add);
		return result.build();
	}
}

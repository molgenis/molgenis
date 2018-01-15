package org.molgenis.data.util;

import org.molgenis.data.MolgenisDataException;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.google.common.collect.ImmutableSet.copyOf;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.difference;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Collections.singleton;
import static java.util.stream.Collectors.toSet;

@Component
public class GenericDependencyResolver
{
	public <A> List<A> resolve(Collection<A> items, Function<A, Set<A>> getDependencies)
	{
		List<A> result = newArrayList();
		Set<A> alreadyResolved = newHashSet();
		Set<A> stillToResolve = newHashSet(items);

		while (!stillToResolve.isEmpty())
		{
			List<A> newlyResolved = stillToResolve.stream()
												  .filter(item -> alreadyResolved.containsAll(
														  getDependencies.apply(item)))
												  .collect(Collectors.toList());
			if (newlyResolved.isEmpty())
			{
				throw new MolgenisDataException("Could not resolve dependencies of items " + stillToResolve
						+ ". Are there circular dependencies?");
			}
			alreadyResolved.addAll(newlyResolved);
			stillToResolve.removeAll(newlyResolved);
			result.addAll(newlyResolved);
		}
		return result;
	}

	/**
	 * Retrieves all items that depend on a given item.
	 *
	 * @param item          the item that the other items depend on
	 * @param getDepth      function that returns the depth up to which a specific item's dependencies are resolved
	 * @param getDependants function that returns the items that depend on a specific item
	 * @param <A>           the type of the item
	 * @return Set of items that directly or indirectly depend on the given item
	 */
	public <A> Set<A> getAllDependants(A item, Function<A, Integer> getDepth, Function<A, Set<A>> getDependants)
	{
		Set<A> currentGeneration = singleton(item);
		Set<A> result = newHashSet();
		Set<A> visited = newHashSet();

		for (int depth = 0; !currentGeneration.isEmpty(); depth++)
		{
			currentGeneration = copyOf(difference(getDirectDependants(currentGeneration, getDependants), visited));
			result.addAll(currentGeneration.stream().filter(getDepthFilter(depth, getDepth)).collect(toSet()));
			visited.addAll(currentGeneration);
		}

		return result;
	}

	private <A> Set<A> getDirectDependants(Set<A> items, Function<A, Set<A>> getDependants)
	{
		return items.stream().flatMap(item -> getDependants.apply(item).stream()).collect(toSet());
	}

	private <A> Predicate<A> getDepthFilter(int depth, Function<A, Integer> getDepth)
	{
		return item -> getDepth.apply(item) > depth;
	}
}

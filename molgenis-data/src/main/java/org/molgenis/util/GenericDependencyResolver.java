package org.molgenis.util;

import org.molgenis.data.MolgenisDataException;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;

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
}

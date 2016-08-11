package org.molgenis.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.molgenis.data.MolgenisDataException;
import org.testng.annotations.Test;

import java.util.Set;

import static java.util.Objects.requireNonNull;
import static org.testng.Assert.assertEquals;

public class GenericDependencyResolverTest
{
	private GenericDependencyResolver genericDependencyResolver = new GenericDependencyResolver();

	private class DependentOn
	{
		private final Set<DependentOn> dependencies = Sets.newHashSet();
		private final String label;

		DependentOn(String label)
		{
			this.label = requireNonNull(label);
		}

		Set<DependentOn> getDependencies()
		{
			return dependencies;
		}

		void addDependency(DependentOn dependentOn)
		{
			dependencies.add(dependentOn);
		}

		@Override
		public String toString()
		{
			return label;
		}
	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void testCyclicDepencencies()
	{
		DependentOn d1 = new DependentOn("1");
		DependentOn d2 = new DependentOn("2");
		DependentOn d3 = new DependentOn("3");

		d1.addDependency(d2);
		d2.addDependency(d3);
		d3.addDependency(d1);

		genericDependencyResolver.resolve(Sets.newHashSet(d1, d2, d3), DependentOn::getDependencies);
	}

	@Test
	public void testResolveTransientDependency()
	{
		DependentOn d1 = new DependentOn("1");
		DependentOn d2 = new DependentOn("2");
		DependentOn d3 = new DependentOn("3");

		d1.addDependency(d2);
		d2.addDependency(d3);

		assertEquals(Lists.newArrayList(d3, d2, d1),
				genericDependencyResolver.resolve(Sets.newHashSet(d1, d2, d3), DependentOn::getDependencies));
	}
}

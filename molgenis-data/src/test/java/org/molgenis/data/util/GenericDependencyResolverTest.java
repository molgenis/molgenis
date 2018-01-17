package org.molgenis.data.util;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.mockito.Mock;
import org.molgenis.data.MolgenisDataException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Set;
import java.util.function.Function;

import static java.util.Collections.emptySet;
import static java.util.Objects.requireNonNull;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.testng.Assert.assertEquals;

public class GenericDependencyResolverTest
{
	private GenericDependencyResolver genericDependencyResolver = new GenericDependencyResolver();

	@Mock
	private Function<Integer, Integer> getDepth;

	@Mock
	private Function<Integer, Set<Integer>> getDependants;

	@BeforeMethod
	public void beforeMethod()
	{
		initMocks(this);
	}

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
	public void testCyclicDependencies()
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

	@Test
	public void testGetAllDependants()
	{
		when(getDepth.apply(1)).thenReturn(1);
		when(getDepth.apply(2)).thenReturn(1);
		when(getDepth.apply(3)).thenReturn(3);
		when(getDepth.apply(4)).thenReturn(1);

		when(getDependants.apply(0)).thenReturn(ImmutableSet.of(1, 4));
		when(getDependants.apply(1)).thenReturn(ImmutableSet.of(2));
		when(getDependants.apply(2)).thenReturn(ImmutableSet.of(3));
		when(getDependants.apply(3)).thenReturn(ImmutableSet.of(2));
		when(getDependants.apply(4)).thenReturn(emptySet());

		assertEquals(genericDependencyResolver.getAllDependants(0, getDepth, getDependants), ImmutableSet.of(1, 4, 3));
		verify(getDepth, never()).apply(0);
	}

	@Test
	public void testGetAllDependantsCircular()
	{
		when(getDepth.apply(0)).thenReturn(1);
		when(getDependants.apply(0)).thenReturn(ImmutableSet.of(0));

		assertEquals(genericDependencyResolver.getAllDependants(0, getDepth, getDependants), ImmutableSet.of(0));
	}

	@Test
	public void testGetAllDependantsCircularZeroDepth()
	{
		when(getDepth.apply(0)).thenReturn(0);
		when(getDependants.apply(0)).thenReturn(ImmutableSet.of(0));

		assertEquals(genericDependencyResolver.getAllDependants(0, getDepth, getDependants), emptySet());
	}
}

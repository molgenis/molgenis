package org.molgenis.data.util;

import static com.google.common.collect.ImmutableSet.of;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Collections.emptySet;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.Set;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.data.MolgenisDataException;

class GenericDependencyResolverTest {
  private GenericDependencyResolver genericDependencyResolver = new GenericDependencyResolver();

  @Mock private Function<Integer, Integer> getDepth;

  @Mock private Function<Integer, Set<Integer>> getDependants;

  @BeforeEach
  void beforeMethod() {
    initMocks(this);
  }

  private class DependentOn {
    private final Set<DependentOn> dependencies = Sets.newHashSet();
    private final String label;

    DependentOn(String label) {
      this.label = requireNonNull(label);
    }

    Set<DependentOn> getDependencies() {
      return dependencies;
    }

    void addDependency(DependentOn dependentOn) {
      dependencies.add(dependentOn);
    }

    @Override
    public String toString() {
      return label;
    }
  }

  @SuppressWarnings("deprecation")
  @Test
  void testCyclicDependencies() {
    DependentOn d1 = new DependentOn("1");
    DependentOn d2 = new DependentOn("2");
    DependentOn d3 = new DependentOn("3");

    d1.addDependency(d2);
    d2.addDependency(d3);
    d3.addDependency(d1);

    assertThrows(
        MolgenisDataException.class,
        () ->
            genericDependencyResolver.resolve(
                Sets.newHashSet(d1, d2, d3), DependentOn::getDependencies));
  }

  @Test
  void testResolveTransientDependency() {
    DependentOn d1 = new DependentOn("1");
    DependentOn d2 = new DependentOn("2");
    DependentOn d3 = new DependentOn("3");

    d1.addDependency(d2);
    d2.addDependency(d3);

    assertEquals(
        genericDependencyResolver.resolve(newHashSet(d1, d2, d3), DependentOn::getDependencies),
        newArrayList(d3, d2, d1));
  }

  @Test
  void testGetAllDependants() {
    when(getDepth.apply(1)).thenReturn(1);
    when(getDepth.apply(2)).thenReturn(1);
    when(getDepth.apply(3)).thenReturn(3);
    when(getDepth.apply(4)).thenReturn(1);

    when(getDependants.apply(0)).thenReturn(ImmutableSet.of(1, 4));
    when(getDependants.apply(1)).thenReturn(ImmutableSet.of(2));
    when(getDependants.apply(2)).thenReturn(ImmutableSet.of(3));
    when(getDependants.apply(3)).thenReturn(ImmutableSet.of(2));
    when(getDependants.apply(4)).thenReturn(emptySet());

    assertEquals(
        of(1, 4, 3), genericDependencyResolver.getAllDependants(0, getDepth, getDependants));
    verify(getDepth, never()).apply(0);
  }

  @Test
  void testGetAllDependantsCircular() {
    when(getDepth.apply(0)).thenReturn(1);
    when(getDependants.apply(0)).thenReturn(ImmutableSet.of(0));

    assertEquals(of(0), genericDependencyResolver.getAllDependants(0, getDepth, getDependants));
  }

  @Test
  void testGetAllDependantsCircularZeroDepth() {
    when(getDepth.apply(0)).thenReturn(0);
    when(getDependants.apply(0)).thenReturn(ImmutableSet.of(0));

    assertEquals(
        emptySet(), genericDependencyResolver.getAllDependants(0, getDepth, getDependants));
  }
}

package org.molgenis.data.index;

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.molgenis.data.index.Impact.createSingleEntityImpact;
import static org.molgenis.data.index.Impact.createWholeRepositoryImpact;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.molgenis.data.index.config.IndexTestConfig;
import org.springframework.test.context.ContextConfiguration;

@ExtendWith(MockitoExtension.class)
@ContextConfiguration(classes = {IndexTestConfig.class})
class IndexingStrategyTest {
  @Mock private IndexDependencyModel dependencyModel;

  IndexingStrategy indexingStrategy;

  @BeforeEach
  private void beforeMethod() {
    indexingStrategy = new IndexingStrategy();
    reset(dependencyModel);
  }

  @Test
  void testDetermineNecessaryActionsEmptySet() {
    assertEquals(indexingStrategy.determineImpact(emptySet(), dependencyModel), emptySet());
  }

  @Test
  void testDetermineImpact() {
    Impact indexAction = createSingleEntityImpact("A", "id");
    Set<Impact> registeredIndexActions = singleton(indexAction);
    when(dependencyModel.getEntityTypesDependentOn("A")).thenReturn(Stream.of("A", "B", "C"));
    Set<Impact> actual = indexingStrategy.determineImpact(registeredIndexActions, dependencyModel);
    assertEquals(
        actual,
        ImmutableSet.of(
            createWholeRepositoryImpact("A"),
            createWholeRepositoryImpact("B"),
            createWholeRepositoryImpact("C")));
  }

  @Test
  void testDetermineImpact2() {
    Impact change = createSingleEntityImpact("A", null);
    Set<Impact> registeredIndexActions = ImmutableSet.of(change);

    when(dependencyModel.getEntityTypesDependentOn("A")).thenReturn(Stream.of("A", "B", "C"));

    Set<Impact> actual = indexingStrategy.determineImpact(registeredIndexActions, dependencyModel);
    assertEquals(
        actual,
        ImmutableSet.of(
            createWholeRepositoryImpact("A"),
            createWholeRepositoryImpact("B"),
            createWholeRepositoryImpact("C")));
  }

  @Test
  void testDetermineImpactNoDependencies() {
    Impact change = createSingleEntityImpact("A", "6");
    Set<Impact> registeredIndexActions = ImmutableSet.of(change);

    when(dependencyModel.getEntityTypesDependentOn("A")).thenReturn(Stream.empty());

    Set<Impact> actual = indexingStrategy.determineImpact(registeredIndexActions, dependencyModel);
    assertEquals(actual, ImmutableSet.of(createSingleEntityImpact("A", "6")));
  }
}

// package org.molgenis.data.index;
//
// import static com.google.common.collect.ImmutableSet.of;
// import static java.util.Collections.emptySet;
// import static java.util.Collections.singleton;
// import static org.junit.jupiter.api.Assertions.assertEquals;
// import static org.mockito.Mockito.reset;
// import static org.mockito.Mockito.when;
// import static org.molgenis.data.index.Impact.createSingleEntityImpact;
// import static org.molgenis.data.index.Impact.createWholeRepositoryImpact;
//
// import com.google.common.collect.ImmutableSet;
// import java.util.Set;
// import java.util.stream.Stream;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.mockito.Mock;
// import org.molgenis.data.index.config.IndexTestConfig;
// import org.molgenis.test.AbstractMockitoTest;
// import org.springframework.test.context.ContextConfiguration;
//
// @ContextConfiguration(classes = {IndexTestConfig.class})
// class IndexingStrategyTest extends AbstractMockitoTest {
//  @Mock private IndexDependencyModel dependencyModel;
//
//  IndexingStrategy indexingStrategy;
//
//  @BeforeEach
//  private void beforeMethod() {
//    indexingStrategy = new IndexingStrategy();
//    reset(dependencyModel);
//  }
//
//  @Test
//  void testDetermineNecessaryActionsEmptySet() {
//    assertEquals(emptySet(), indexingStrategy.determineImpact(emptySet(), dependencyModel));
//  }
//
//  @Test
//  void testDetermineImpact() {
//    Impact indexAction = createSingleEntityImpact("A", "id");
//    Set<Impact> registeredIndexActions = singleton(indexAction);
//    when(dependencyModel.getEntityTypesDependentOn("A")).thenReturn(Stream.of("A", "B", "C"));
//    Set<Impact> actual = indexingStrategy.determineImpact(registeredIndexActions,
// dependencyModel);
//    assertEquals(
//        of(
//            createWholeRepositoryImpact("A"),
//            createWholeRepositoryImpact("B"),
//            createWholeRepositoryImpact("C")),
//        actual);
//  }
//
//  @Test
//  void testDetermineImpact2() {
//    Impact change = createSingleEntityImpact("A", null);
//    Set<Impact> registeredIndexActions = ImmutableSet.of(change);
//
//    when(dependencyModel.getEntityTypesDependentOn("A")).thenReturn(Stream.of("A", "B", "C"));
//
//    Set<Impact> actual = indexingStrategy.determineImpact(registeredIndexActions,
// dependencyModel);
//    assertEquals(
//        of(
//            createWholeRepositoryImpact("A"),
//            createWholeRepositoryImpact("B"),
//            createWholeRepositoryImpact("C")),
//        actual);
//  }
//
//  @Test
//  void testDetermineImpactNoDependencies() {
//    Impact change = createSingleEntityImpact("A", "6");
//    Set<Impact> registeredIndexActions = ImmutableSet.of(change);
//
//    when(dependencyModel.getEntityTypesDependentOn("A")).thenReturn(Stream.empty());
//
//    Set<Impact> actual = indexingStrategy.determineImpact(registeredIndexActions,
// dependencyModel);
//    assertEquals(of(createSingleEntityImpact("A", "6")), actual);
//  }
// }

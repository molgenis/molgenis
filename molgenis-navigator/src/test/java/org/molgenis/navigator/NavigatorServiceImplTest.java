package org.molgenis.navigator;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toSet;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ENTITY_TYPE_META_DATA;
import static org.molgenis.data.meta.model.PackageMetadata.PACKAGE;
import static org.testng.Assert.assertEquals;

import java.util.HashSet;
import java.util.stream.Stream;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.Package;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class NavigatorServiceImplTest extends AbstractMockitoTest {

  @Mock private DataService dataService;

  private NavigatorServiceImpl navigatorServiceImpl;

  @BeforeMethod
  public void setUpBeforeMethod() {
    navigatorServiceImpl = new NavigatorServiceImpl(dataService);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void testNavigatorServiceImpl() {
    new NavigatorServiceImpl(null);
  }

  @Test
  public void testDeleteItemsPackagesAndEntityTypes() {
    EntityType entityTypeA0 = when(mock(EntityType.class).getId()).thenReturn("eA0").getMock();
    EntityType entityTypeA1 = when(mock(EntityType.class).getId()).thenReturn("eA1").getMock();
    EntityType entityTypeB0 = when(mock(EntityType.class).getId()).thenReturn("eB0").getMock();
    EntityType entityTypeB1 = when(mock(EntityType.class).getId()).thenReturn("eB1").getMock();
    EntityType entityTypeC0 = when(mock(EntityType.class).getId()).thenReturn("eC0").getMock();

    Package packageA = when(mock(Package.class).getId()).thenReturn("pA").getMock();
    Package packageB = when(mock(Package.class).getId()).thenReturn("pB").getMock();
    Package packageC = when(mock(Package.class).getId()).thenReturn("pC").getMock();
    Package packageD = when(mock(Package.class).getId()).thenReturn("pD").getMock();
    when(packageA.getEntityTypes()).thenReturn(asList(entityTypeA0, entityTypeA1));
    when(packageB.getEntityTypes()).thenReturn(asList(entityTypeB0, entityTypeB1));
    when(packageC.getEntityTypes()).thenReturn(singletonList(entityTypeC0));
    when(packageA.getChildren()).thenReturn(asList(packageC, packageD));

    doReturn(Stream.of(packageA, packageB))
        .when(dataService)
        .findAll(eq(PACKAGE), any(Stream.class), eq(Package.class));

    navigatorServiceImpl.deleteItems(asList("pA", "pB"), asList("e0", "e1"));

    @SuppressWarnings("unchecked")
    ArgumentCaptor<Stream<Object>> entityTypeIdCaptor = ArgumentCaptor.forClass(Stream.class);
    verify(dataService).deleteAll(eq(ENTITY_TYPE_META_DATA), entityTypeIdCaptor.capture());
    assertEquals(
        entityTypeIdCaptor.getValue().collect(toSet()),
        new HashSet<>(asList("e0", "e1", "eA0", "eA1", "eB0", "eB1", "eC0")));

    @SuppressWarnings("unchecked")
    ArgumentCaptor<Stream<Object>> packageIdCaptor = ArgumentCaptor.forClass(Stream.class);
    verify(dataService).deleteAll(eq(PACKAGE), packageIdCaptor.capture());
    assertEquals(
        packageIdCaptor.getValue().collect(toSet()), new HashSet<>(asList("pA", "pB", "pC", "pD")));

    verifyNoMoreInteractions(dataService);
  }

  @Test
  public void testDeleteItemsPackages() {
    EntityType entityTypeA0 = when(mock(EntityType.class).getId()).thenReturn("eA0").getMock();
    EntityType entityTypeA1 = when(mock(EntityType.class).getId()).thenReturn("eA1").getMock();

    Package packageA = when(mock(Package.class).getId()).thenReturn("pA").getMock();
    when(packageA.getEntityTypes()).thenReturn(asList(entityTypeA0, entityTypeA1));

    doReturn(Stream.of(packageA))
        .when(dataService)
        .findAll(eq(PACKAGE), any(Stream.class), eq(Package.class));

    navigatorServiceImpl.deleteItems(singletonList("pA"), emptyList());

    @SuppressWarnings("unchecked")
    ArgumentCaptor<Stream<Object>> entityTypeIdCaptor = ArgumentCaptor.forClass(Stream.class);
    verify(dataService).deleteAll(eq(ENTITY_TYPE_META_DATA), entityTypeIdCaptor.capture());
    assertEquals(
        entityTypeIdCaptor.getValue().collect(toSet()), new HashSet<>(asList("eA0", "eA1")));

    @SuppressWarnings("unchecked")
    ArgumentCaptor<Stream<Object>> packageIdCaptor = ArgumentCaptor.forClass(Stream.class);
    verify(dataService).deleteAll(eq(PACKAGE), packageIdCaptor.capture());
    assertEquals(packageIdCaptor.getValue().collect(toSet()), new HashSet<>(singletonList("pA")));

    verifyNoMoreInteractions(dataService);
  }

  @Test
  public void testDeleteItemsEntityTypes() {
    navigatorServiceImpl.deleteItems(emptyList(), asList("e0", "e1"));

    @SuppressWarnings("unchecked")
    ArgumentCaptor<Stream<Object>> entityTypeIdCaptor = ArgumentCaptor.forClass(Stream.class);
    verify(dataService).deleteAll(eq(ENTITY_TYPE_META_DATA), entityTypeIdCaptor.capture());
    assertEquals(entityTypeIdCaptor.getValue().collect(toSet()), new HashSet<>(asList("e0", "e1")));

    verifyNoMoreInteractions(dataService);
  }

  @Test
  public void testDeleteItemsNothing() {
    navigatorServiceImpl.deleteItems(emptyList(), emptyList());
    verifyZeroInteractions(dataService);
  }
}

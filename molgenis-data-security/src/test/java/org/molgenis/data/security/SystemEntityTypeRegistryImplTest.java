package org.molgenis.data.security;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.SystemEntityType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.security.exception.EntityTypePermissionDeniedException;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.molgenis.test.AbstractMockitoTest;

class SystemEntityTypeRegistryImplTest extends AbstractMockitoTest {
  @Mock private UserPermissionEvaluator permissionService;

  private SystemEntityTypeRegistryImpl systemEntityTypeRegistry;

  @BeforeEach
  void setUpBeforeMethod() {
    systemEntityTypeRegistry = new SystemEntityTypeRegistryImpl(permissionService);
  }

  @Test
  void testSystemEntityTypeRegistryImpl() {
    assertThrows(NullPointerException.class, () -> new SystemEntityTypeRegistryImpl(null));
  }

  @Test
  void testGetSystemEntityTypeNotExists() {
    assertNull(systemEntityTypeRegistry.getSystemEntityType("unknownEntityTypeId"));
  }

  @Test
  void testGetSystemEntityTypePermitted() {
    String entityTypeId = "entityType";
    SystemEntityType systemEntityType = mock(SystemEntityType.class);
    when(systemEntityType.getId()).thenReturn(entityTypeId);
    when(permissionService.hasPermission(
            new EntityTypeIdentity(entityTypeId), EntityTypePermission.READ_METADATA))
        .thenReturn(true);

    systemEntityTypeRegistry.addSystemEntityType(systemEntityType);
    assertEquals(systemEntityTypeRegistry.getSystemEntityType(entityTypeId), systemEntityType);
  }

  @Test
  void testGetSystemEntityTypeNotPermitted() {
    String entityTypeId = "entityType";
    SystemEntityType systemEntityType = mock(SystemEntityType.class);
    when(systemEntityType.getId()).thenReturn(entityTypeId);
    when(permissionService.hasPermission(
            new EntityTypeIdentity(entityTypeId), EntityTypePermission.READ_METADATA))
        .thenReturn(false);

    systemEntityTypeRegistry.addSystemEntityType(systemEntityType);
    Exception exception =
        assertThrows(
            EntityTypePermissionDeniedException.class,
            () -> systemEntityTypeRegistry.getSystemEntityType(entityTypeId));
    assertThat(exception.getMessage())
        .containsPattern("permission:READ_METADATA entityTypeId:entityType");
  }

  @Test
  void testGetSystemEntityTypesPermitted() {
    String entityTypeId = "entityType";
    SystemEntityType systemEntityType = mock(SystemEntityType.class);
    when(systemEntityType.getId()).thenReturn(entityTypeId);
    when(permissionService.hasPermission(
            new EntityTypeIdentity(entityTypeId), EntityTypePermission.READ_METADATA))
        .thenReturn(true);

    systemEntityTypeRegistry.addSystemEntityType(systemEntityType);
    assertEquals(
        systemEntityTypeRegistry.getSystemEntityTypes().collect(toList()),
        singletonList(systemEntityType));
  }

  @Test
  void testGetSystemEntityTypesNotPermitted() {
    String entityTypeId = "entityType";
    SystemEntityType systemEntityType = mock(SystemEntityType.class);
    when(systemEntityType.getId()).thenReturn(entityTypeId);
    when(permissionService.hasPermission(
            new EntityTypeIdentity(entityTypeId), EntityTypePermission.READ_METADATA))
        .thenReturn(false);

    systemEntityTypeRegistry.addSystemEntityType(systemEntityType);
    assertEquals(systemEntityTypeRegistry.getSystemEntityTypes().count(), 0);
  }

  @Test
  void testHasSystemEntityTypeExists() {
    String entityTypeId = "entityType";
    SystemEntityType systemEntityType = mock(SystemEntityType.class);
    when(systemEntityType.getId()).thenReturn(entityTypeId);
    systemEntityTypeRegistry.addSystemEntityType(systemEntityType);
    assertTrue(systemEntityTypeRegistry.hasSystemEntityType(entityTypeId));
  }

  @Test
  void testHasSystemEntityTypeNotExists() {
    String entityTypeId = "unknownEntityType";
    assertFalse(systemEntityTypeRegistry.hasSystemEntityType(entityTypeId));
  }

  @Test
  void testGetSystemAttributePermittedExists() {
    String entityTypeId = "entityType";
    SystemEntityType systemEntityType = mock(SystemEntityType.class);
    when(systemEntityType.getId()).thenReturn(entityTypeId);
    Attribute attr = when(mock(Attribute.class).getIdentifier()).thenReturn("attr").getMock();
    when(systemEntityType.getAllAttributes()).thenReturn(singletonList(attr));
    when(permissionService.hasPermission(
            new EntityTypeIdentity(entityTypeId), EntityTypePermission.READ_METADATA))
        .thenReturn(true);

    systemEntityTypeRegistry.addSystemEntityType(systemEntityType);
    assertEquals(systemEntityTypeRegistry.getSystemAttribute("attr"), attr);
  }

  @Test
  void testGetSystemAttributePermittedNotExists() {
    String entityTypeId = "entityType";
    SystemEntityType systemEntityType = mock(SystemEntityType.class);
    when(systemEntityType.getId()).thenReturn(entityTypeId);
    when(systemEntityType.getAllAttributes()).thenReturn(emptyList());

    systemEntityTypeRegistry.addSystemEntityType(systemEntityType);
    assertNull(systemEntityTypeRegistry.getSystemAttribute("attr"));
  }

  @Test
  void testGetSystemAttributeNotPermitted() {
    String entityTypeId = "entityType";
    SystemEntityType systemEntityType = mock(SystemEntityType.class);
    when(systemEntityType.getId()).thenReturn(entityTypeId);
    Attribute attr = when(mock(Attribute.class).getIdentifier()).thenReturn("attr").getMock();
    when(systemEntityType.getAllAttributes()).thenReturn(singletonList(attr));
    when(permissionService.hasPermission(
            new EntityTypeIdentity(entityTypeId), EntityTypePermission.READ_METADATA))
        .thenReturn(false);

    systemEntityTypeRegistry.addSystemEntityType(systemEntityType);
    Exception exception =
        assertThrows(
            EntityTypePermissionDeniedException.class,
            () -> systemEntityTypeRegistry.getSystemAttribute("attr"));
    assertThat(exception.getMessage())
        .containsPattern("permission:READ_METADATA entityTypeId:entityType");
  }

  @Test
  void testGetSystemAttributeCompound() {
    String entityTypeId = "entityType";
    SystemEntityType systemEntityType = mock(SystemEntityType.class);
    when(systemEntityType.getId()).thenReturn(entityTypeId);
    Attribute attr = when(mock(Attribute.class).getIdentifier()).thenReturn("attr").getMock();
    Attribute compoundAttr =
        when(mock(Attribute.class).getIdentifier()).thenReturn("compoundAttr").getMock();
    when(compoundAttr.getDataType()).thenReturn(AttributeType.COMPOUND);
    when(compoundAttr.getChildren()).thenReturn(singletonList(attr));
    when(systemEntityType.getAllAttributes()).thenReturn(singletonList(compoundAttr));
    when(permissionService.hasPermission(
            new EntityTypeIdentity(entityTypeId), EntityTypePermission.READ_METADATA))
        .thenReturn(true);

    systemEntityTypeRegistry.addSystemEntityType(systemEntityType);
    assertEquals(systemEntityTypeRegistry.getSystemAttribute("attr"), attr);
  }

  @Test
  void testGetSystemAttributeCompoundNotExists() {
    String entityTypeId = "entityType";
    SystemEntityType systemEntityType = mock(SystemEntityType.class);
    when(systemEntityType.getId()).thenReturn(entityTypeId);
    Attribute attr = when(mock(Attribute.class).getIdentifier()).thenReturn("attr").getMock();
    when(attr.getDataType()).thenReturn(AttributeType.STRING);
    Attribute compoundAttr =
        when(mock(Attribute.class).getIdentifier()).thenReturn("compoundAttr").getMock();
    when(compoundAttr.getDataType()).thenReturn(AttributeType.COMPOUND);
    when(compoundAttr.getChildren()).thenReturn(singletonList(attr));
    when(systemEntityType.getAllAttributes()).thenReturn(singletonList(compoundAttr));

    systemEntityTypeRegistry.addSystemEntityType(systemEntityType);
    assertNull(systemEntityTypeRegistry.getSystemAttribute("unknownAttr"));
  }

  @Test
  void testHasSystemAttributePermittedExists() {
    String entityTypeId = "entityType";
    SystemEntityType systemEntityType = mock(SystemEntityType.class);
    when(systemEntityType.getId()).thenReturn(entityTypeId);
    Attribute attr = when(mock(Attribute.class).getIdentifier()).thenReturn("attr").getMock();
    when(systemEntityType.getAllAttributes()).thenReturn(singletonList(attr));

    systemEntityTypeRegistry.addSystemEntityType(systemEntityType);
    assertTrue(systemEntityTypeRegistry.hasSystemAttribute("attr"));
  }

  @Test
  void testHasSystemAttributePermittedNotExists() {
    String entityTypeId = "entityType";
    SystemEntityType systemEntityType = mock(SystemEntityType.class);
    when(systemEntityType.getId()).thenReturn(entityTypeId);
    when(systemEntityType.getAllAttributes()).thenReturn(emptyList());

    systemEntityTypeRegistry.addSystemEntityType(systemEntityType);
    assertFalse(systemEntityTypeRegistry.hasSystemAttribute("attr"));
  }

  @Test
  void testHasSystemAttributeNotPermitted() {
    String entityTypeId = "entityType";
    SystemEntityType systemEntityType = mock(SystemEntityType.class);
    when(systemEntityType.getId()).thenReturn(entityTypeId);
    Attribute attr = when(mock(Attribute.class).getIdentifier()).thenReturn("attr").getMock();
    when(systemEntityType.getAllAttributes()).thenReturn(singletonList(attr));

    systemEntityTypeRegistry.addSystemEntityType(systemEntityType);
    assertTrue(systemEntityTypeRegistry.hasSystemAttribute("attr"));
  }
}

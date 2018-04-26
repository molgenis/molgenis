package org.molgenis.data.security;

import org.mockito.Mock;
import org.molgenis.data.MolgenisDataAccessException;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.SystemEntityType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.security.core.Permission;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;

/**
 * See {@link Permission#COUNT} for an explanation why we are not using {@link Permission#READ} in the test cases.
 */
public class SystemEntityTypeRegistryImplTest extends AbstractMockitoTest
{
	@Mock
	private UserPermissionEvaluator permissionService;

	private SystemEntityTypeRegistryImpl systemEntityTypeRegistry;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		systemEntityTypeRegistry = new SystemEntityTypeRegistryImpl(permissionService);
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void testSystemEntityTypeRegistryImpl()
	{
		new SystemEntityTypeRegistryImpl(null);
	}

	@Test
	public void testGetSystemEntityTypeNotExists()
	{
		assertNull(systemEntityTypeRegistry.getSystemEntityType("unknownEntityTypeId"));
	}

	@Test
	public void testGetSystemEntityTypePermitted()
	{
		String entityTypeId = "entityType";
		SystemEntityType systemEntityType = mock(SystemEntityType.class);
		when(systemEntityType.getId()).thenReturn(entityTypeId);
		when(permissionService.hasPermission(new EntityTypeIdentity(entityTypeId),
				EntityTypePermission.COUNT)).thenReturn(true);

		systemEntityTypeRegistry.addSystemEntityType(systemEntityType);
		assertEquals(systemEntityTypeRegistry.getSystemEntityType(entityTypeId), systemEntityType);
	}

	@SuppressWarnings("deprecation")
	@Test(expectedExceptions = MolgenisDataAccessException.class)
	public void testGetSystemEntityTypeNotPermitted()
	{
		String entityTypeId = "entityType";
		SystemEntityType systemEntityType = mock(SystemEntityType.class);
		when(systemEntityType.getId()).thenReturn(entityTypeId);
		when(permissionService.hasPermission(new EntityTypeIdentity(entityTypeId), EntityTypePermission.COUNT)).thenReturn(false);

		systemEntityTypeRegistry.addSystemEntityType(systemEntityType);
		systemEntityTypeRegistry.getSystemEntityType(entityTypeId);
	}

	@Test
	public void testGetSystemEntityTypesPermitted()
	{
		String entityTypeId = "entityType";
		SystemEntityType systemEntityType = mock(SystemEntityType.class);
		when(systemEntityType.getId()).thenReturn(entityTypeId);
		when(permissionService.hasPermission(new EntityTypeIdentity(entityTypeId),
				EntityTypePermission.COUNT)).thenReturn(true);

		systemEntityTypeRegistry.addSystemEntityType(systemEntityType);
		assertEquals(systemEntityTypeRegistry.getSystemEntityTypes().collect(toList()),
				singletonList(systemEntityType));
	}

	@Test
	public void testGetSystemEntityTypesNotPermitted()
	{
		String entityTypeId = "entityType";
		SystemEntityType systemEntityType = mock(SystemEntityType.class);
		when(systemEntityType.getId()).thenReturn(entityTypeId);
		when(permissionService.hasPermission(new EntityTypeIdentity(entityTypeId),
				EntityTypePermission.COUNT)).thenReturn(false);

		systemEntityTypeRegistry.addSystemEntityType(systemEntityType);
		assertEquals(systemEntityTypeRegistry.getSystemEntityTypes().count(), 0);
	}

	@Test
	public void testHasSystemEntityTypeExists()
	{
		String entityTypeId = "entityType";
		SystemEntityType systemEntityType = mock(SystemEntityType.class);
		when(systemEntityType.getId()).thenReturn(entityTypeId);
		systemEntityTypeRegistry.addSystemEntityType(systemEntityType);
		assertTrue(systemEntityTypeRegistry.hasSystemEntityType(entityTypeId));
	}

	@Test
	public void testHasSystemEntityTypeNotExists()
	{
		String entityTypeId = "unknownEntityType";
		assertFalse(systemEntityTypeRegistry.hasSystemEntityType(entityTypeId));
	}

	@Test
	public void testGetSystemAttributePermittedExists()
	{
		String entityTypeId = "entityType";
		SystemEntityType systemEntityType = mock(SystemEntityType.class);
		when(systemEntityType.getId()).thenReturn(entityTypeId);
		Attribute attr = when(mock(Attribute.class).getIdentifier()).thenReturn("attr").getMock();
		when(systemEntityType.getAllAttributes()).thenReturn(singletonList(attr));
		when(permissionService.hasPermission(new EntityTypeIdentity(entityTypeId),
				EntityTypePermission.COUNT)).thenReturn(true);

		systemEntityTypeRegistry.addSystemEntityType(systemEntityType);
		assertEquals(systemEntityTypeRegistry.getSystemAttribute("attr"), attr);
	}

	@Test
	public void testGetSystemAttributePermittedNotExists()
	{
		String entityTypeId = "entityType";
		SystemEntityType systemEntityType = mock(SystemEntityType.class);
		when(systemEntityType.getId()).thenReturn(entityTypeId);
		when(systemEntityType.getAllAttributes()).thenReturn(emptyList());

		systemEntityTypeRegistry.addSystemEntityType(systemEntityType);
		assertNull(systemEntityTypeRegistry.getSystemAttribute("attr"));
	}

	@SuppressWarnings("deprecation")
	@Test(expectedExceptions = MolgenisDataAccessException.class)
	public void testGetSystemAttributeNotPermitted()
	{
		String entityTypeId = "entityType";
		SystemEntityType systemEntityType = mock(SystemEntityType.class);
		when(systemEntityType.getId()).thenReturn(entityTypeId);
		Attribute attr = when(mock(Attribute.class).getIdentifier()).thenReturn("attr").getMock();
		when(systemEntityType.getAllAttributes()).thenReturn(singletonList(attr));
		when(permissionService.hasPermission(new EntityTypeIdentity(entityTypeId),
				EntityTypePermission.COUNT)).thenReturn(false);

		systemEntityTypeRegistry.addSystemEntityType(systemEntityType);
		assertEquals(systemEntityTypeRegistry.getSystemAttribute("attr"), attr);
	}

	@Test
	public void testGetSystemAttributeCompound()
	{
		String entityTypeId = "entityType";
		SystemEntityType systemEntityType = mock(SystemEntityType.class);
		when(systemEntityType.getId()).thenReturn(entityTypeId);
		Attribute attr = when(mock(Attribute.class).getIdentifier()).thenReturn("attr").getMock();
		Attribute compoundAttr = when(mock(Attribute.class).getIdentifier()).thenReturn("compoundAttr").getMock();
		when(compoundAttr.getDataType()).thenReturn(AttributeType.COMPOUND);
		when(compoundAttr.getChildren()).thenReturn(singletonList(attr));
		when(systemEntityType.getAllAttributes()).thenReturn(singletonList(compoundAttr));
		when(permissionService.hasPermission(new EntityTypeIdentity(entityTypeId),
				EntityTypePermission.COUNT)).thenReturn(true);

		systemEntityTypeRegistry.addSystemEntityType(systemEntityType);
		assertEquals(systemEntityTypeRegistry.getSystemAttribute("attr"), attr);
	}

	@Test
	public void testGetSystemAttributeCompoundNotExists()
	{
		String entityTypeId = "entityType";
		SystemEntityType systemEntityType = mock(SystemEntityType.class);
		when(systemEntityType.getId()).thenReturn(entityTypeId);
		Attribute attr = when(mock(Attribute.class).getIdentifier()).thenReturn("attr").getMock();
		when(attr.getDataType()).thenReturn(AttributeType.STRING);
		Attribute compoundAttr = when(mock(Attribute.class).getIdentifier()).thenReturn("compoundAttr").getMock();
		when(compoundAttr.getDataType()).thenReturn(AttributeType.COMPOUND);
		when(compoundAttr.getChildren()).thenReturn(singletonList(attr));
		when(systemEntityType.getAllAttributes()).thenReturn(singletonList(compoundAttr));

		systemEntityTypeRegistry.addSystemEntityType(systemEntityType);
		assertNull(systemEntityTypeRegistry.getSystemAttribute("unknownAttr"));
	}

	@Test
	public void testHasSystemAttributePermittedExists()
	{
		String entityTypeId = "entityType";
		SystemEntityType systemEntityType = mock(SystemEntityType.class);
		when(systemEntityType.getId()).thenReturn(entityTypeId);
		Attribute attr = when(mock(Attribute.class).getIdentifier()).thenReturn("attr").getMock();
		when(systemEntityType.getAllAttributes()).thenReturn(singletonList(attr));

		systemEntityTypeRegistry.addSystemEntityType(systemEntityType);
		assertTrue(systemEntityTypeRegistry.hasSystemAttribute("attr"));
	}

	@Test
	public void testHasSystemAttributePermittedNotExists()
	{
		String entityTypeId = "entityType";
		SystemEntityType systemEntityType = mock(SystemEntityType.class);
		when(systemEntityType.getId()).thenReturn(entityTypeId);
		when(systemEntityType.getAllAttributes()).thenReturn(emptyList());

		systemEntityTypeRegistry.addSystemEntityType(systemEntityType);
		assertFalse(systemEntityTypeRegistry.hasSystemAttribute("attr"));
	}

	@Test
	public void testHasSystemAttributeNotPermitted()
	{
		String entityTypeId = "entityType";
		SystemEntityType systemEntityType = mock(SystemEntityType.class);
		when(systemEntityType.getId()).thenReturn(entityTypeId);
		Attribute attr = when(mock(Attribute.class).getIdentifier()).thenReturn("attr").getMock();
		when(systemEntityType.getAllAttributes()).thenReturn(singletonList(attr));

		systemEntityTypeRegistry.addSystemEntityType(systemEntityType);
		assertTrue(systemEntityTypeRegistry.hasSystemAttribute("attr"));
	}
}
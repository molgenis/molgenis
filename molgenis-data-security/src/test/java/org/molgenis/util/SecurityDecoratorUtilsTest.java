package org.molgenis.util;

import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.security.EntityTypePermissionDeniedException;
import org.molgenis.security.core.Permission;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = { SecurityDecoratorUtilsTest.Config.class })
@TestExecutionListeners(listeners = WithSecurityContextTestExecutionListener.class)
public class SecurityDecoratorUtilsTest extends AbstractTestNGSpringContextTests
{
	@WithMockUser(username = "USER", authorities = { "ROLE_SU" })
	@Test
	public void testValidatePermissionSuperuser()
	{
		SecurityDecoratorUtils.validatePermission(getMockEntityType(), Permission.WRITEMETA);
	}

	@WithMockUser(username = "USER", authorities = { "ROLE_SYSTEM" })
	@Test
	public void testValidatePermissionSystemUser()
	{
		SecurityDecoratorUtils.validatePermission(getMockEntityType(), Permission.WRITE);
	}

	@WithMockUser(username = "USER", authorities = { "ROLE_ENTITY_READ_entityTypeId" })
	@Test
	public void testValidatePermissionUser()
	{
		SecurityDecoratorUtils.validatePermission(getMockEntityType(), Permission.READ);
	}

	@WithMockUser(username = "USER", authorities = { "ROLE_ENTITY_COUNT_entityTypeId" })
	@Test(expectedExceptions = EntityTypePermissionDeniedException.class, expectedExceptionsMessageRegExp = "id:entityTypeId permission:READ")
	public void testValidatePermissionUserDifferentPermission()
	{
		SecurityDecoratorUtils.validatePermission(getMockEntityType(), Permission.READ);
	}

	@WithMockUser(username = "USER", authorities = { "ROLE_ENTITY_COUNT_otherEntityTypeId" })
	@Test(expectedExceptions = EntityTypePermissionDeniedException.class, expectedExceptionsMessageRegExp = "id:entityTypeId permission:COUNT")
	public void testValidatePermissionUserDifferentEntityType()
	{
		SecurityDecoratorUtils.validatePermission(getMockEntityType(), Permission.COUNT);
	}

	private EntityType getMockEntityType()
	{
		EntityType entityType = mock(EntityType.class);
		when(entityType.getId()).thenReturn("entityTypeId");
		when(entityType.getLabel()).thenReturn("entityTypeLabel");
		return entityType;
	}

	static class Config
	{

	}
}
package org.molgenis.security.permission;

import org.mockito.Mock;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.security.EntityTypeIdentity;
import org.molgenis.security.core.PermissionSet;
import org.molgenis.test.AbstractMockitoTestNGSpringContextTests;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;

@ContextConfiguration(classes = { PermissionSystemServiceImplTest.Config.class })
@TestExecutionListeners(listeners = WithSecurityContextTestExecutionListener.class)
public class PermissionSystemServiceImplTest extends AbstractMockitoTestNGSpringContextTests
{
	@Mock
	private MutableAclService mutableAclService;

	private PermissionSystemServiceImpl permissionSystemServiceImpl;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		permissionSystemServiceImpl = new PermissionSystemServiceImpl(mutableAclService);
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void testPermissionSystemService()
	{
		new PermissionSystemServiceImpl(null);
	}

	@Test
	@WithMockUser(username = "user")
	public void giveUserEntityPermissions()
	{
		String entityTypeId = "entityTypeId";
		EntityType entityType = when(mock(EntityType.class).getId()).thenReturn(entityTypeId).getMock();

		MutableAcl acl = mock(MutableAcl.class);

		when(mutableAclService.readAclById(new EntityTypeIdentity(entityTypeId))).thenReturn(acl);

		permissionSystemServiceImpl.giveUserWriteMetaPermissions(entityType);
		verify(mutableAclService).updateAcl(acl);
		verify(acl).insertAce(0, PermissionSet.WRITEMETA, new PrincipalSid("user"),
				true);
	}

	@Configuration
	public static class Config
	{
	}
}
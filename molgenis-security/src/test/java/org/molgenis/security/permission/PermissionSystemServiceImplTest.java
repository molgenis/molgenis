package org.molgenis.security.permission;

import org.mockito.Mock;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.security.EntityTypeIdentity;
import org.molgenis.data.security.EntityTypePermission;
import org.molgenis.data.security.permission.PermissionSystemService;
import org.molgenis.test.AbstractMockitoTestNGSpringContextTests;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.acls.domain.AclImpl;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.acls.model.Sid;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;

@ContextConfiguration(classes = { PermissionSystemServiceImplTest.Config.class })
@TestExecutionListeners(listeners = WithSecurityContextTestExecutionListener.class)
public class PermissionSystemServiceImplTest extends AbstractMockitoTestNGSpringContextTests
{
	@Mock
	private MutableAclService mutableAclService;
	@Mock
	private EntityType entityType0;
	@Mock
	private EntityType entityType1;
	@Mock
	private AclImpl acl0;
	@Mock
	private AclImpl acl1;

	private PermissionSystemService permissionSystemService;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		permissionSystemService = new PermissionSystemServiceImpl(mutableAclService);
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void testPermissionSystemService()
	{
		new PermissionSystemServiceImpl(null);
	}

	@Test
	@WithMockUser(username = "user", authorities = { "existingAuthority" })
	public void giveUserEntityPermissions()
	{
		when(entityType0.getId()).thenReturn("entityTypeId0");
		when(entityType1.getId()).thenReturn("entityTypeId1");

		PrincipalSid userSid = new PrincipalSid("user");
		List<Sid> userSidList = Collections.singletonList(userSid);
		doReturn(acl0).when(mutableAclService).readAclById(new EntityTypeIdentity("entityTypeId0"), userSidList);
		doReturn(acl1).when(mutableAclService).readAclById(new EntityTypeIdentity("entityTypeId1"), userSidList);

		permissionSystemService.giveUserWriteMetaPermissions(asList(entityType0, entityType1));

		verify(acl0).insertAce(0, EntityTypePermission.WRITEMETA, userSid, true);
		verify(acl1).insertAce(0, EntityTypePermission.WRITEMETA, userSid, true);

		verify(mutableAclService).updateAcl(acl0);
		verify(mutableAclService).updateAcl(acl1);
	}

	@Test
	@WithMockUser(username = "SYSTEM", authorities = { "ROLE_SYSTEM" })
	public void giveUserEntityPermissionsSystemUser()
	{
		permissionSystemService.giveUserWriteMetaPermissions(Collections.singleton(entityType0));
		verifyZeroInteractions(mutableAclService);
	}

	@Test
	@WithMockUser(username = "user", authorities = { "ROLE_SU" })
	public void giveUserEntityPermissionsSuperuser()
	{
		permissionSystemService.giveUserWriteMetaPermissions(Collections.singleton(entityType0));
		verifyZeroInteractions(mutableAclService);
	}

	@Configuration
	public static class Config
	{
	}
}
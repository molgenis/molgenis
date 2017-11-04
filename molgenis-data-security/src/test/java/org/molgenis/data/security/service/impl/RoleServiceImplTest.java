package org.molgenis.data.security.service.impl;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoSession;
import org.molgenis.data.DataService;
import org.molgenis.data.security.model.RoleEntity;
import org.molgenis.data.security.model.RoleFactory;
import org.molgenis.security.core.model.Role;
import org.molgenis.security.core.service.RoleService;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockitoSession;
import static org.mockito.Mockito.when;
import static org.mockito.quality.Strictness.STRICT_STUBS;
import static org.testng.Assert.assertEquals;

public class RoleServiceImplTest
{

	@Mock
	private DataService dataService;
	@Mock
	private RoleFactory roleFactory;

	@InjectMocks
	private RoleServiceImpl roleService;

	private MockitoSession mockitoSession;

	@BeforeMethod
	public void beforeMethod()
	{
		roleService = null;
		mockitoSession = mockitoSession().strictness(STRICT_STUBS).initMocks(this).startMocking();
	}

	@AfterMethod
	public void afterMethod()
	{
		mockitoSession.finishMocking();
	}

	@Test
	public void testCreateRolesForGroup() {
		String label = "BBMRI-NL";

		RoleEntity roleEntity = mock(RoleEntity.class);
		when(roleFactory.create()).thenReturn(roleEntity);

		List<Role> roles = roleService.createRolesForGroup(label);
		assertEquals(roles.size(), 7);
	}

}

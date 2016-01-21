package org.molgenis.security.permission;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.molgenis.auth.GroupAuthority;
import org.molgenis.auth.MolgenisGroup;
import org.molgenis.auth.MolgenisUser;
import org.molgenis.data.DataService;
import org.molgenis.data.MolgenisDataAccessException;
import org.molgenis.data.importer.ImportResultsWizardPage;
import org.molgenis.data.importer.ImportWizardController;
import org.molgenis.data.importer.OptionsWizardPage;
import org.molgenis.data.importer.PackageWizardPage;
import org.molgenis.data.importer.UploadWizardPage;
import org.molgenis.data.importer.ValidationResultWizardPage;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.framework.ui.MolgenisPluginRegistry;
import org.molgenis.security.core.utils.SecurityUtils;
import org.molgenis.security.permission.ImportWizardControllerTest.Config;
import org.molgenis.security.user.UserAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.web.context.request.WebRequest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Created by charbonb on 17/03/15.
 */
@ContextConfiguration(classes =
{ Config.class })
public class ImportWizardControllerTest extends AbstractTestNGSpringContextTests
{
	private ImportWizardController controller;
	private WebRequest webRequest;

	@Autowired
	DataService dataService;

	@Autowired
	GrantedAuthoritiesMapper grantedAuthoritiesMapper;

	@Autowired
	UserAccountService userAccountService;
	private Authentication authentication;
	private UserDetails userDetails;

	@Configuration
	static class Config
	{
		@Bean
		public PermissionManagerServiceImpl pluginPermissionManagerServiceImpl()
		{
			return new PermissionManagerServiceImpl(dataService(), molgenisPluginRegistry(),
					grantedAuthoritiesMapper());
		}

		@Bean
		public DataService dataService()
		{
			return mock(DataService.class);
		}

		@Bean
		public MolgenisPluginRegistry molgenisPluginRegistry()
		{
			return mock(MolgenisPluginRegistry.class);
		}

		@Bean
		public GrantedAuthoritiesMapper grantedAuthoritiesMapper()
		{
			return mock(GrantedAuthoritiesMapper.class);
		}

		@Bean
		public UserAccountService userAccountService()
		{

			UserAccountService userAccountService = mock(UserAccountService.class);
			MolgenisGroup group1 = new MolgenisGroup();
			group1.setId("ID");
			group1.setActive(true);
			group1.setName("TestGroup");
			when(userAccountService.getCurrentUserGroups()).thenReturn(Collections.singletonList(group1));
			return userAccountService;
		}
	}

	@BeforeMethod
	public void setUp()
	{
		reset(dataService);
		UploadWizardPage uploadWizardPage = mock(UploadWizardPage.class);
		OptionsWizardPage optionsWizardPage = mock(OptionsWizardPage.class);
		ValidationResultWizardPage validationResultWizardPage = mock(ValidationResultWizardPage.class);
		ImportResultsWizardPage importResultsWizardPage = mock(ImportResultsWizardPage.class);
		PackageWizardPage packageWizardPage = mock(PackageWizardPage.class);

		controller = new ImportWizardController(uploadWizardPage, optionsWizardPage, packageWizardPage,
				validationResultWizardPage, importResultsWizardPage, dataService, grantedAuthoritiesMapper,
				userAccountService);

		List<GroupAuthority> authorities = new ArrayList<>();

		MolgenisGroup group1 = new MolgenisGroup();
		group1.setId("ID");
		group1.setActive(true);
		group1.setName("TestGroup");

		MapEntity entity1 = new MapEntity("Entity1");
		entity1.set(GroupAuthority.ROLE, SecurityUtils.AUTHORITY_ENTITY_WRITEMETA_PREFIX + "ENTITY1");
		entity1.set(GroupAuthority.MOLGENISGROUP, group1);
		GroupAuthority authority1 = new GroupAuthority();
		authority1.set(entity1);

		MapEntity entity2 = new MapEntity("Entity2");
		entity2.set(GroupAuthority.ROLE, SecurityUtils.AUTHORITY_ENTITY_WRITEMETA_PREFIX + "ENTITY2");
		entity2.set(GroupAuthority.MOLGENISGROUP, group1);
		GroupAuthority authority2 = new GroupAuthority();
		authority2.set(entity2);

		MapEntity entity3 = new MapEntity("Entity2");
		entity3.set(GroupAuthority.ROLE, SecurityUtils.AUTHORITY_ENTITY_WRITEMETA_PREFIX + "ENTITY3");
		entity3.set(GroupAuthority.MOLGENISGROUP, group1);
		GroupAuthority authority3 = new GroupAuthority();
		authority3.set(entity3);

		MapEntity entity4 = new MapEntity("Entity2");
		entity4.set(GroupAuthority.ROLE, SecurityUtils.AUTHORITY_ENTITY_WRITEMETA_PREFIX + "ENTITY4");
		entity4.set(GroupAuthority.MOLGENISGROUP, group1);
		GroupAuthority authority4 = new GroupAuthority();
		authority4.set(entity4);

		authorities.add(authority1);
		authorities.add(authority2);
		authorities.add(authority3);
		authorities.add(authority4);

		webRequest = mock(WebRequest.class);
		when(webRequest.getParameter("entityIds")).thenReturn("entity1,entity2");
		when(dataService.findOne(MolgenisGroup.ENTITY_NAME, "ID", MolgenisGroup.class)).thenReturn(group1);
		when(dataService.findAll(GroupAuthority.ENTITY_NAME, new QueryImpl().eq(GroupAuthority.MOLGENISGROUP, group1),
				GroupAuthority.class)).thenAnswer(new Answer<Stream<GroupAuthority>>()
				{
					@Override
					public Stream<GroupAuthority> answer(InvocationOnMock invocation) throws Throwable
					{
						return Stream.of(authority1, authority2, authority3, authority4);
					}
				});
		when(dataService.findAll(GroupAuthority.ENTITY_NAME, new QueryImpl().eq(GroupAuthority.MOLGENISGROUP, "ID"),
				GroupAuthority.class)).thenAnswer(new Answer<Stream<GroupAuthority>>()
				{
					@Override
					public Stream<GroupAuthority> answer(InvocationOnMock invocation) throws Throwable
					{
						return Stream.of(authority1, authority2, authority3, authority4);
					}
				});
		when(dataService.getEntityNames()).thenReturn(Stream.of("entity1", "entity2", "entity3", "entity4", "entity5"));

		authentication = mock(Authentication.class);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		GrantedAuthority grantedAuthority1 = new SimpleGrantedAuthority(authority1.getRole().toString());
		GrantedAuthority grantedAuthority2 = new SimpleGrantedAuthority(authority2.getRole().toString());
		GrantedAuthority grantedAuthority3 = new SimpleGrantedAuthority(authority3.getRole().toString());
		GrantedAuthority grantedAuthority4 = new SimpleGrantedAuthority(authority4.getRole().toString());
		userDetails = mock(UserDetails.class);
		when(userDetails.getUsername()).thenReturn("username");
		when(userDetails.getPassword()).thenReturn("encoded-password");
		when((Collection<GrantedAuthority>) userDetails.getAuthorities()).thenReturn(Arrays
				.<GrantedAuthority> asList(grantedAuthority1, grantedAuthority2, grantedAuthority3, grantedAuthority4));
		when(authentication.getPrincipal()).thenReturn(userDetails);
		when((Collection<GrantedAuthority>) authentication.getAuthorities()).thenReturn(Arrays
				.<GrantedAuthority> asList(grantedAuthority1, grantedAuthority2, grantedAuthority3, grantedAuthority4));
	}

	@Test
	public void getGroupEntityClassPermissionsTest()
	{
		Permissions permissions = controller.getGroupEntityClassPermissions("ID", webRequest);
		Map<String, List<Permission>> groupPermissions = permissions.getGroupPermissions();

		Permission permission = new Permission();
		permission.setType("writemeta");
		permission.setGroup("TestGroup");
		assertEquals(groupPermissions.get("entity1"), Arrays.asList(permission));
		assertEquals(groupPermissions.get("entity2"), Arrays.asList(permission));
		assertEquals(groupPermissions.get("entity3"), Arrays.asList(permission));
		assertEquals(groupPermissions.get("entity4"), Arrays.asList(permission));

		assertEquals(groupPermissions.size(), 4);
	}

	@Test
	public void addGroupEntityClassPermissionsTest()
	{
		MolgenisUser user = mock(MolgenisUser.class);
		when(user.isSuperuser()).thenReturn(false);
		when(userAccountService.getCurrentUser()).thenReturn(user);

		webRequest = mock(WebRequest.class);
		when(webRequest.getParameter("entityIds")).thenReturn("entity3,entity4");
		when(webRequest.getParameter("radio-entity3"))
				.thenReturn(org.molgenis.security.core.Permission.COUNT.toString());
		when(webRequest.getParameter("radio-entity4"))
				.thenReturn(org.molgenis.security.core.Permission.WRITE.toString());

		GroupAuthority authority = new GroupAuthority();
		authority.setMolgenisGroup(dataService.findOne(MolgenisGroup.ENTITY_NAME, "ID", MolgenisGroup.class));
		authority.setRole(SecurityUtils.AUTHORITY_ENTITY_PREFIX
				+ org.molgenis.security.core.Permission.COUNT.toString().toUpperCase() + "_" + "entity3".toUpperCase());

		controller.addGroupEntityClassPermissions("ID", webRequest);

		verify(dataService, times(2)).add(GroupAuthority.ENTITY_NAME, authority);
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class)
	public void addGroupEntityClassPermissionsTestNoPermission()
	{
		MolgenisUser user = mock(MolgenisUser.class);
		when(user.isSuperuser()).thenReturn(false);
		when(userAccountService.getCurrentUser()).thenReturn(user);

		webRequest = mock(WebRequest.class);
		when(webRequest.getParameter("entityIds")).thenReturn("entity3,entity5");
		when(webRequest.getParameter("radio-entity3"))
				.thenReturn(org.molgenis.security.core.Permission.COUNT.toString());
		when(webRequest.getParameter("radio-entity5"))
				.thenReturn(org.molgenis.security.core.Permission.WRITE.toString());
		controller.addGroupEntityClassPermissions("ID", webRequest);

	}

	@Test()
	public void addGroupEntityClassPermissionsTestNoPermissionSU()
	{
		MolgenisUser user = mock(MolgenisUser.class);
		when(user.isSuperuser()).thenReturn(true);
		when(userAccountService.getCurrentUser()).thenReturn(user);

		webRequest = mock(WebRequest.class);
		when(webRequest.getParameter("entityIds")).thenReturn("entity3,entity5");
		when(webRequest.getParameter("radio-entity3"))
				.thenReturn(org.molgenis.security.core.Permission.COUNT.toString());
		when(webRequest.getParameter("radio-entity5"))
				.thenReturn(org.molgenis.security.core.Permission.WRITE.toString());

		GroupAuthority authority = new GroupAuthority();
		authority.setMolgenisGroup(dataService.findOne(MolgenisGroup.ENTITY_NAME, "ID", MolgenisGroup.class));
		authority.setRole(SecurityUtils.AUTHORITY_ENTITY_PREFIX
				+ org.molgenis.security.core.Permission.COUNT.toString().toUpperCase() + "_" + "entity3".toUpperCase());

		controller.addGroupEntityClassPermissions("ID", webRequest);

		verify(dataService, times(2)).add(GroupAuthority.ENTITY_NAME, authority);

	}

}

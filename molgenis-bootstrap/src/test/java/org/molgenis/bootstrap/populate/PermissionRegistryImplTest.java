package org.molgenis.bootstrap.populate;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.meta.UploadPackage;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.plugin.model.PluginIdentity;
import org.molgenis.data.security.EntityTypeIdentity;
import org.molgenis.data.security.PackageIdentity;
import org.molgenis.data.security.auth.Role;
import org.molgenis.security.core.PermissionSet;
import org.molgenis.test.AbstractMockitoTest;
import org.molgenis.util.Pair;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Sid;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.molgenis.data.decorator.meta.DecoratorConfigurationMetadata.DECORATOR_CONFIGURATION;
import static org.molgenis.data.file.model.FileMetaMetaData.FILE_META;
import static org.molgenis.data.i18n.model.L10nStringMetaData.L10N_STRING;
import static org.molgenis.data.i18n.model.LanguageMetadata.LANGUAGE;
import static org.molgenis.data.meta.model.AttributeMetadata.ATTRIBUTE_META_DATA;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ENTITY_TYPE_META_DATA;
import static org.molgenis.data.meta.model.PackageMetadata.PACKAGE;
import static org.molgenis.data.meta.model.TagMetadata.TAG;
import static org.molgenis.data.security.auth.RoleMetadata.NAME;
import static org.molgenis.data.security.auth.RoleMetadata.ROLE;
import static org.molgenis.security.account.AccountService.ROLE_USER;
import static org.molgenis.security.core.PermissionSet.READ;
import static org.molgenis.security.core.PermissionSet.WRITEMETA;
import static org.testng.Assert.assertEquals;

public class PermissionRegistryImplTest extends AbstractMockitoTest
{
	@Mock(answer = RETURNS_DEEP_STUBS)
	private DataService dataService;

	@Mock
	private Role userRole;

	@Mock
	private Package uploadPackage;

	@Mock
	private EntityType entityTypeEntityType;

	@Mock
	private EntityType attributeEntityType;

	@Captor
	private ArgumentCaptor<Stream<Object>> entityTypeIdCaptor;

	@Captor
	private ArgumentCaptor<Stream<Object>> packageIdCaptor;

	private PermissionRegistryImpl permissionRegistryImpl;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		permissionRegistryImpl = new PermissionRegistryImpl(dataService);
	}

	@Test
	public void testGetPermissions()
	{
		when(dataService.query(ROLE, Role.class).eq(NAME, ROLE_USER).findOne()).thenReturn(userRole);
		when(userRole.getName()).thenReturn(ROLE_USER);

		when(entityTypeEntityType.getId()).thenReturn(ENTITY_TYPE_META_DATA);
		when(attributeEntityType.getId()).thenReturn(ATTRIBUTE_META_DATA);

		when(uploadPackage.getId()).thenReturn(UploadPackage.UPLOAD);
		doReturn(Stream.of(entityTypeEntityType, attributeEntityType)).when(dataService)
																	  .findAll(eq(ENTITY_TYPE_META_DATA),
																			  entityTypeIdCaptor.capture(),
																			  eq(EntityType.class));
		doReturn(Stream.of(uploadPackage)).when(dataService)
										  .findAll(eq(PACKAGE), packageIdCaptor.capture(), eq(Package.class));

		GrantedAuthoritySid userSid = new GrantedAuthoritySid("ROLE_USER");
		Multimap<ObjectIdentity, Pair<PermissionSet, Sid>> expectedPermissions = ImmutableListMultimap.of(
				new PluginIdentity("useraccount"), new Pair<>(READ, userSid),
				new EntityTypeIdentity(ENTITY_TYPE_META_DATA), new Pair<>(READ, userSid),
				new EntityTypeIdentity(ATTRIBUTE_META_DATA), new Pair<>(READ, userSid),
				new PackageIdentity(UploadPackage.UPLOAD), new Pair<>(WRITEMETA, userSid));
		assertEquals(permissionRegistryImpl.getPermissions(), expectedPermissions);

		assertEquals(entityTypeIdCaptor.getValue().collect(Collectors.toSet()),
				ImmutableSet.of(ENTITY_TYPE_META_DATA, ATTRIBUTE_META_DATA, PACKAGE, TAG, LANGUAGE, L10N_STRING,
						FILE_META, DECORATOR_CONFIGURATION));
		assertEquals(packageIdCaptor.getValue().collect(toList()), singletonList(UploadPackage.UPLOAD));
	}
}
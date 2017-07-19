package org.molgenis.app;

import org.molgenis.app.controller.HomeController;
import org.molgenis.bootstrap.populate.PermissionPopulator;
import org.molgenis.data.security.acl.*;
import org.molgenis.ui.admin.user.UserAccountController;
import org.springframework.stereotype.Component;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static org.molgenis.bootstrap.populate.UsersGroupsAuthoritiesPopulatorImpl.ROLE_USER_ID;
import static org.molgenis.data.i18n.model.L10nStringMetaData.L10N_STRING;
import static org.molgenis.data.i18n.model.LanguageMetadata.LANGUAGE;
import static org.molgenis.data.meta.model.AttributeMetadata.ATTRIBUTE_META_DATA;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ENTITY_TYPE_META_DATA;
import static org.molgenis.data.meta.model.PackageMetadata.PACKAGE;
import static org.molgenis.data.meta.model.TagMetadata.TAG;
import static org.molgenis.file.model.FileMetaMetaData.FILE_META;
import static org.molgenis.security.core.Permission.READ;
import static org.molgenis.security.core.Permission.WRITE;
import static org.molgenis.security.core.utils.SecurityUtils.ANONYMOUS_USERNAME;
import static org.molgenis.ui.PluginMetadata.PLUGIN;

@Component
public class PermissionPopulatorImpl implements PermissionPopulator
{
	private final EntityAclManager entityAclManager;

	public PermissionPopulatorImpl(EntityAclManager entityAclManager)
	{
		this.entityAclManager = requireNonNull(entityAclManager);
	}

	@Override
	public void populate()
	{
		SecurityId roleUserSecurityId = SecurityId.createForAuthority(ROLE_USER_ID);
		EntityAce roleUserReadAce = EntityAce.create(newHashSet(READ), roleUserSecurityId, true);

		// allow anonymous user and user role to see the home plugin
		EntityAcl homePluginAcl = entityAclManager.readAcl(EntityIdentity.create(PLUGIN, HomeController.ID));
		EntityAce homePluginAnonymousAce = EntityAce.create(newHashSet(READ),
				SecurityId.createForUsername(ANONYMOUS_USERNAME), true);
		homePluginAcl = homePluginAcl.toBuilder().setEntries(asList(homePluginAnonymousAce, roleUserReadAce)).build();
		entityAclManager.updateAcl(homePluginAcl);

		// allow user role to update profile
		EntityAcl userAccountPluginAcl = entityAclManager.readAcl(
				EntityIdentity.create(PLUGIN, UserAccountController.ID));
		EntityAce userAccountPluginUserAce = EntityAce.create(newHashSet(WRITE), roleUserSecurityId, true);
		userAccountPluginAcl = userAccountPluginAcl.toBuilder()
												   .setEntries(singletonList(userAccountPluginUserAce))
												   .build();
		entityAclManager.updateAcl(userAccountPluginAcl);

		asList(ENTITY_TYPE_META_DATA, ATTRIBUTE_META_DATA, PACKAGE, TAG, LANGUAGE, L10N_STRING, FILE_META).forEach(
				entityTypeId ->
				{
					EntityAcl entityTypeAcl = entityAclManager.readAcl(
							EntityIdentity.create(ENTITY_TYPE_META_DATA, entityTypeId));
					entityTypeAcl = entityTypeAcl.toBuilder().setEntries(singletonList(roleUserReadAce)).build();
					entityAclManager.updateAcl(entityTypeAcl);
				});
	}
}

package org.molgenis.bootstrap.populate;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import org.molgenis.core.ui.admin.user.UserAccountController;
import org.molgenis.data.DataService;
import org.molgenis.data.decorator.meta.DecoratorConfigurationMetadata;
import org.molgenis.data.file.model.FileMetaMetaData;
import org.molgenis.data.i18n.model.L10nStringMetaData;
import org.molgenis.data.i18n.model.LanguageMetadata;
import org.molgenis.data.meta.model.*;
import org.molgenis.data.plugin.model.PluginIdentity;
import org.molgenis.data.plugin.model.PluginPermission;
import org.molgenis.data.plugin.model.PluginPermissionUtils;
import org.molgenis.data.security.EntityTypeIdentity;
import org.molgenis.data.security.EntityTypePermission;
import org.molgenis.data.security.EntityTypePermissionUtils;
import org.molgenis.data.security.auth.Group;
import org.molgenis.data.security.owned.OwnedEntityType;
import org.molgenis.util.Pair;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.acls.model.Sid;
import org.springframework.stereotype.Component;

import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ENTITY_TYPE_META_DATA;
import static org.molgenis.data.security.auth.GroupMetaData.GROUP;
import static org.molgenis.data.security.auth.GroupMetaData.NAME;
import static org.molgenis.security.account.AccountService.ALL_USER_GROUP;
import static org.molgenis.security.acl.SidUtils.createSid;

@Component
public class PermissionRegistryImpl implements PermissionRegistry
{
	private final DataService dataService;

	public PermissionRegistryImpl(DataService dataService)
	{
		this.dataService = requireNonNull(dataService);
	}

	@Override
	public Multimap<ObjectIdentity, Pair<Permission, Sid>> getPermissions()
	{
		ImmutableMultimap.Builder<ObjectIdentity, Pair<Permission, Sid>> mapBuilder = new ImmutableMultimap.Builder<>();

		Group allUsersGroup = dataService.query(GROUP, Group.class).eq(NAME, ALL_USER_GROUP).findOne();
		Sid allUsersGroupSid = createSid(allUsersGroup);

		ObjectIdentity pluginIdentity = new PluginIdentity(UserAccountController.ID);
		Permission pluginPermissions = PluginPermissionUtils.getCumulativePermission(PluginPermission.WRITE);
		mapBuilder.putAll(pluginIdentity, new Pair<>(pluginPermissions, allUsersGroupSid));

		dataService.findAll(ENTITY_TYPE_META_DATA,
				Stream.of(EntityTypeMetadata.ENTITY_TYPE_META_DATA, AttributeMetadata.ATTRIBUTE_META_DATA,
						PackageMetadata.PACKAGE, TagMetadata.TAG, LanguageMetadata.LANGUAGE,
						L10nStringMetaData.L10N_STRING, FileMetaMetaData.FILE_META, OwnedEntityType.OWNED,
						DecoratorConfigurationMetadata.DECORATOR_CONFIGURATION), EntityType.class).forEach(entityType ->
		{
			ObjectIdentity entityTypeIdentity = new EntityTypeIdentity(entityType);
			Permission entityTypePermissions = EntityTypePermissionUtils.getCumulativePermission(
					EntityTypePermission.READ);
			mapBuilder.putAll(entityTypeIdentity, new Pair<>(entityTypePermissions, allUsersGroupSid));
		});

		return mapBuilder.build();
	}
}

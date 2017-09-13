package org.molgenis.dataexplorer.directory;

import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeMetadata;
import org.molgenis.data.settings.DefaultSettingsEntity;
import org.molgenis.data.settings.DefaultSettingsEntityType;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;

@Component
public class DirectorySettings extends DefaultSettingsEntity
{
	private static final long serialVersionUID = 1L;
	public static final String COLLECTION_ENTITY = "collection_entity";
	public static final String NEGOTIATOR_URL = "negotiator_url";
	public static final String USERNAME = "username";
	public static final String PASSWORD = "password";
	private static final String NEGOTIATOR_URL_DEFAULT = "https://bbmri-dev.mitro.dkfz.de/api/directory/create_query";
	private static final String ID = "directory";

	public DirectorySettings()
	{
		super(ID);
	}

	@Component
	public static class Meta extends DefaultSettingsEntityType
	{
		private EntityTypeMetadata entityTypeMetadata;

		public Meta(EntityTypeMetadata entityTypeMetadata)
		{
			super(ID);
			this.entityTypeMetadata = entityTypeMetadata;
		}

		@Override
		public void init()
		{
			super.init();
			setLabel("Directory settings");
			setDescription("Settings for the Directory - Negotiator interaction.");
			addAttribute(NEGOTIATOR_URL).setLabel("Negotiator endpoint url")
										.setDefaultValue(NEGOTIATOR_URL_DEFAULT)
										.setDescription("URL to post negotiator queries to.");
			addAttribute(USERNAME).setLabel("Username")
								  .setDescription(
										  "Username to use in the basic authentication with the negotiator endbpoint.");
			addAttribute(PASSWORD).setLabel("Password")
								  .setDescription(
										  "Password to use in the basic authentication with the negotiator endpoint.");
			addAttribute(COLLECTION_ENTITY).setLabel("Collection entity")
										   .setDescription("Entity containing biobank collections.")
										   .setDataType(AttributeType.XREF)
										   .setRefEntity(entityTypeMetadata);
		}
	}

	@Nullable
	public String getUsername()
	{
		return getString(USERNAME);
	}

	@Nullable
	public String getPassword()
	{
		return getString(PASSWORD);
	}

	@Nullable
	public String getNegotiatorURL()
	{
		return getString(NEGOTIATOR_URL);
	}

	@Nullable
	public EntityType getCollectionEntityType()
	{
		return getEntity(COLLECTION_ENTITY, EntityType.class);
	}
}
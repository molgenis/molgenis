package org.molgenis.rdconnect;

import static org.molgenis.MolgenisFieldTypes.STRING;

import org.molgenis.data.settings.DefaultSettingsEntity;
import org.molgenis.data.settings.DefaultSettingsEntityMetaData;
import org.springframework.stereotype.Component;

@Component
public class IdCardBiobankIndexerDbSettings extends DefaultSettingsEntity implements IdCardBiobankIndexerSettings
{

	private static final long serialVersionUID = 1L;

	private static final String ID = IdCardBiobankIndexerController.ID;

	public IdCardBiobankIndexerDbSettings()
	{
		super(ID);
	}

	@Component
	private static class Meta extends DefaultSettingsEntityMetaData
	{
		private static final String API_BASE_URI = "apiBaseUri";
		private static final String BIOBANK_RESOURCE = "biobankResource";
		private static final String BIOBANK_COLLECTIONS_RESOURCE = "biobankCollResource";
		private static final String BIOBANK_COLLECTIONS_SELECTION_RESOURCE = "biobankCollSelResource";

		private static final String DEFAULT_API_BASE_URI = "http://catalogue.rd-connect.eu/api/jsonws/BiBBoxCommonServices-portlet.logapi";
		private static final String DEFAULT_BIOBANK_RESOURCE = "regbb/organization-id";
		private static final String DEFAULT_BIOBANK_COLLECTIONS_RESOURCE = "regbbs";
		private static final String DEFAULT_BIOBANK_COLLECTIONS_SELECTION_RESOURCE = DEFAULT_BIOBANK_COLLECTIONS_RESOURCE
				+ "/data";

		public Meta()
		{
			super(ID);
			setLabel("ID-Card biobank indexer settings");
			addAttribute(API_BASE_URI).setDataType(STRING).setLabel("API base URI")
					.setDefaultValue(DEFAULT_API_BASE_URI);
			addAttribute(BIOBANK_RESOURCE).setDataType(STRING).setLabel("Biobank resource")
					.setDefaultValue(DEFAULT_BIOBANK_RESOURCE);
			addAttribute(BIOBANK_COLLECTIONS_RESOURCE).setDataType(STRING).setLabel("Biobank collection resource")
					.setDefaultValue(DEFAULT_BIOBANK_COLLECTIONS_RESOURCE);
			addAttribute(BIOBANK_COLLECTIONS_SELECTION_RESOURCE).setDataType(STRING)
					.setLabel("Biobank collection filtered resource")
					.setDefaultValue(DEFAULT_BIOBANK_COLLECTIONS_SELECTION_RESOURCE);
		}
	}

	@Override
	public String getApiBaseUri()
	{
		return getString(Meta.API_BASE_URI);
	}

	@Override
	public void setApiBaseUri(String apiBaseUri)
	{
		set(Meta.API_BASE_URI, apiBaseUri);
	}

	@Override
	public String getBiobankResource()
	{
		return getString(Meta.BIOBANK_RESOURCE);
	}

	@Override
	public void setBiobankResource(String biobankResource)
	{
		set(Meta.BIOBANK_RESOURCE, biobankResource);
	}

	@Override
	public String getBiobankCollectionResource()
	{
		return getString(Meta.BIOBANK_COLLECTIONS_RESOURCE);
	}

	@Override
	public void setBiobankCollectionResource(String biobankCollectionResource)
	{
		set(Meta.BIOBANK_COLLECTIONS_RESOURCE, biobankCollectionResource);
	}

	@Override
	public String getBiobankCollectionSelectionResource()
	{
		return getString(Meta.BIOBANK_COLLECTIONS_SELECTION_RESOURCE);
	}

	@Override
	public void setBiobankCollectionSelectionResource(String biobankCollectionSelectionResource)
	{
		set(Meta.BIOBANK_COLLECTIONS_SELECTION_RESOURCE, biobankCollectionSelectionResource);
	}
}

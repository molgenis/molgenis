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
		private static final String ID_CARD_API_BASE_URI = "idCardApiBaseUri";
		private static final String DEFAULT_ID_CARD_API_BASE_URI = "http://catalogue.rd-connect.eu/api/jsonws/BiBBoxCommonServices-portlet.logapi";

		public Meta()
		{
			super(ID);
			setLabel("ID-Card biobank indexer settings");
			addAttribute(ID_CARD_API_BASE_URI).setDataType(STRING).setLabel("ID-Card API base URI")
					.setDefaultValue(DEFAULT_ID_CARD_API_BASE_URI);
		}
	}

	@Override
	public String getIdCardApiBaseUri()
	{
		return getString(Meta.ID_CARD_API_BASE_URI);
	}

	@Override
	public void setIdCardApiBaseUri(String idCardApiBaseUri)
	{
		set(Meta.ID_CARD_API_BASE_URI, idCardApiBaseUri);
	}
}

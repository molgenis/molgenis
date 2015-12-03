package org.molgenis.data.annotator.websettings;

import org.molgenis.data.annotation.entity.impl.HPOAnnotator;
import org.molgenis.data.settings.DefaultSettingsEntity;
import org.molgenis.data.settings.DefaultSettingsEntityMetaData;
import org.springframework.stereotype.Component;

@Component
public class HPOAnnotatorSettings extends DefaultSettingsEntity
{
	private static final long serialVersionUID = 1L;
	private static final String ID = HPOAnnotator.NAME;

	public HPOAnnotatorSettings()
	{
		super(ID);
	}

	@Component
	public static class Meta extends DefaultSettingsEntityMetaData
	{
		public static final String HPO_LOCATION = "hpoLocation";

		public Meta()
		{
			super(ID);
			setLabel("HPO annotator settings");
			addAttribute(HPO_LOCATION).setLabel("HPO file location");
		}
	}
}

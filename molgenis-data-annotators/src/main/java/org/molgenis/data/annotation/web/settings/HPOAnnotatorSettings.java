package org.molgenis.data.annotation.web.settings;

import org.molgenis.data.annotation.core.entity.impl.hpo.HPOAnnotator;
import org.molgenis.settings.DefaultSettingsEntity;
import org.molgenis.settings.DefaultSettingsEntityType;
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
	public static class Meta extends DefaultSettingsEntityType
	{
		public static final String HPO_LOCATION = "hpoLocation";

		public Meta()
		{
			super(ID);
		}

		@Override
		public void init()
		{
			super.init();
			setLabel("HPO annotator settings");
			addAttribute(HPO_LOCATION).setLabel("HPO file location");
		}
	}
}

package org.molgenis.data.annotation.web.settings;

import org.molgenis.data.annotation.core.entity.impl.DannAnnotator;
import org.molgenis.data.annotation.core.utils.AnnotatorUtils;
import org.molgenis.settings.DefaultSettingsEntity;
import org.molgenis.settings.DefaultSettingsEntityType;
import org.springframework.stereotype.Component;

@Component
public class DannAnnotatorSettings extends DefaultSettingsEntity
{
	private static final long serialVersionUID = 1L;
	public static final String ID = DannAnnotator.NAME;

	public DannAnnotatorSettings()
	{
		super(ID);
	}

	@Component
	public static class Meta extends DefaultSettingsEntityType
	{
		public static final String DANN_LOCATION = "dannLocation";

		public Meta()
		{
			super(ID);
		}

		@Override
		public void init()
		{
			super.init();
			setLabel("Dann annotator settings");
			String defaultLocation = AnnotatorUtils.getAnnotatorResourceDir() + "/dann/dann.tsv.bgz";
			addAttribute(DANN_LOCATION).setLabel("Dann file location").setDefaultValue(defaultLocation);
		}
	}
}

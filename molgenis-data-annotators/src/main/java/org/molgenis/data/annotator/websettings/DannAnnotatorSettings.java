package org.molgenis.data.annotator.websettings;

import org.molgenis.data.annotation.entity.impl.DannAnnotator;
import org.molgenis.data.annotation.utils.AnnotatorUtils;
import org.molgenis.data.settings.DefaultSettingsEntity;
import org.molgenis.data.settings.DefaultSettingsEntityMetaData;
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
	public static class Meta extends DefaultSettingsEntityMetaData
	{
		public static final String DANN_LOCATION = "dannLocation";

		public Meta()
		{
			super(ID);
			setLabel("Dann annotator settings");
			String defaultLocation = AnnotatorUtils.getAnnotatorResourceDir() + "/dann/dann.tsv.bgz";
			addAttribute(DANN_LOCATION).setLabel("Dann file location").setDefaultValue(defaultLocation);
		}
	}
}

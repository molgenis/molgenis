package org.molgenis.data.annotator.websettings;

import org.molgenis.data.annotation.entity.impl.gavin.GavinAnnotator;
import org.molgenis.data.annotation.utils.AnnotatorUtils;
import org.molgenis.data.settings.DefaultSettingsEntity;
import org.molgenis.data.settings.DefaultSettingsEntityMetaData;
import org.springframework.stereotype.Component;

@Component
public class GavinAnnotatorSettings extends DefaultSettingsEntity
{
	private static final long serialVersionUID = 1L;
	private static final String ID = GavinAnnotator.NAME;

	public GavinAnnotatorSettings()
	{
		super(ID);
	}

	@Component
	public static class Meta extends DefaultSettingsEntityMetaData
	{
		public static final String VARIANT_FILE_LOCATION = "variantFileLocation";

		public Meta()
		{
			super(ID);
			setLabel(GavinAnnotator.NAME + " annotator settings");

			String defaultLocation = AnnotatorUtils.getAnnotatorResourceDir() + "/variant/variantinterpretation_emx.xlsx";
			addAttribute(VARIANT_FILE_LOCATION).setLabel("Variant classification file location").setDefaultValue(defaultLocation);
		}
	}
}

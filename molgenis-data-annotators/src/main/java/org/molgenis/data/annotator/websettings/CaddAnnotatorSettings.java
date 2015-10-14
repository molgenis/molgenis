package org.molgenis.data.annotator.websettings;

import org.molgenis.data.annotation.entity.impl.CaddAnnotator;
import org.molgenis.data.annotation.utils.AnnotatorUtils;
import org.molgenis.data.settings.DefaultSettingsEntity;
import org.molgenis.data.settings.DefaultSettingsEntityMetaData;
import org.springframework.stereotype.Component;

@Component
public class CaddAnnotatorSettings extends DefaultSettingsEntity
{
	private static final long serialVersionUID = 1L;
	private static final String ID = CaddAnnotator.NAME;

	public CaddAnnotatorSettings()
	{
		super(ID);
	}

	@Component
	public static class Meta extends DefaultSettingsEntityMetaData
	{
		public static final String CADD_LOCATION = "caddLocation";

		public Meta()
		{
			super(ID);
			setLabel("Cadd annotator settings");

			String defaultLocation = AnnotatorUtils.getAnnotatorResourceDir() + "/CADD/1000G.vcf.gz";
			addAttribute(CADD_LOCATION).setLabel("Cadd file location").setDefaultValue(defaultLocation);
		}
	}
}

package org.molgenis.data.annotation.web.settings;

import org.molgenis.data.annotation.core.entity.impl.ClinvarAnnotator;
import org.molgenis.data.annotation.core.utils.AnnotatorUtils;
import org.molgenis.settings.DefaultSettingsEntity;
import org.molgenis.settings.DefaultSettingsEntityType;
import org.springframework.stereotype.Component;

@Component
public class ClinvarAnnotatorSettings extends DefaultSettingsEntity
{
	private static final long serialVersionUID = 1L;
	public static final String ID = ClinvarAnnotator.NAME;

	public ClinvarAnnotatorSettings()
	{
		super(ID);
	}

	@Component
	public static class Meta extends DefaultSettingsEntityType
	{
		public static final String CLINVAR_LOCATION = "clinvarLocation";

		public Meta()
		{
			super(ID);
		}

		@Override
		public void init()
		{
			super.init();
			setLabel("Clinvar annotator settings");

			String defaultLocation = AnnotatorUtils.getAnnotatorResourceDir() + "/Clinvar/clinvar.vcf.gz";
			addAttribute(CLINVAR_LOCATION).setLabel("Clinvar file location").setDefaultValue(defaultLocation);
		}
	}
}

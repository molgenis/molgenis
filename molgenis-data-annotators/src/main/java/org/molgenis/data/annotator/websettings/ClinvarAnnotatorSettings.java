package org.molgenis.data.annotator.websettings;

import org.molgenis.data.annotation.entity.impl.ClinvarAnnotator;
import org.molgenis.data.annotation.utils.AnnotatorUtils;
import org.molgenis.data.settings.DefaultSettingsEntity;
import org.molgenis.data.settings.DefaultSettingsEntityMetaData;
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
	public static class Meta extends DefaultSettingsEntityMetaData
	{
		public static final String CLINVAR_LOCATION = "clinvarLocation";

		public Meta()
		{
			super(ID);
			setLabel("Clinvar annotator settings");

			String defaultLocation = AnnotatorUtils.getAnnotatorResourceDir() + "/Clinvar/clinvar.vcf.gz";
			addAttribute(CLINVAR_LOCATION).setLabel("Clinvar file location").setDefaultValue(defaultLocation);
		}
	}

}

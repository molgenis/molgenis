package org.molgenis.data.annotation.web.settings;

import org.molgenis.data.annotation.core.entity.impl.ThousandGenomesAnnotator;
import org.molgenis.settings.DefaultSettingsEntity;
import org.molgenis.settings.DefaultSettingsEntityType;
import org.springframework.stereotype.Component;

@Component
public class ThousendGenomesAnnotatorSettings extends DefaultSettingsEntity
{
	private static final long serialVersionUID = 1L;
	private static final String ID = ThousandGenomesAnnotator.NAME;

	public ThousendGenomesAnnotatorSettings()
	{
		super(ID);
	}

	@Component
	public static class Meta extends DefaultSettingsEntityType
	{
		public static final String CHROMOSOMES = "chromosomes";
		public static final String FILEPATTERN = "filepattern";
		public static final String ROOT_DIRECTORY = "rootDirectory";
		public static final String OVERRIDE_CHROMOSOME_FILES = "overrideChromosomeFile";

		public Meta()
		{
			super(ID);
		}

		@Override
		public void init()
		{
			super.init();
			setLabel("1000 Genomes annotator settings");
			addAttribute(CHROMOSOMES).setLabel("Chromosomes")
									 .setDefaultValue("1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22");
			addAttribute(FILEPATTERN).setLabel("Filepattern")
									 .setDefaultValue(
											 "ALL.chr%s.phase3_shapeit2_mvncall_integrated_v5.20130502.genotypes.vcf.gz");
			addAttribute(ROOT_DIRECTORY).setLabel("Root directory");
			addAttribute(OVERRIDE_CHROMOSOME_FILES).setLabel("Override chromosome file");
		}
	}
}

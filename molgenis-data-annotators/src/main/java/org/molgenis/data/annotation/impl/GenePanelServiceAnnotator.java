package org.molgenis.data.annotation.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.annotation.AnnotationService;
import org.molgenis.data.annotation.VariantAnnotator;
import org.molgenis.data.support.AnnotationServiceImpl;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.vcf.VcfRepository;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.framework.server.MolgenisSimpleSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component("genePanelService")
public class GenePanelServiceAnnotator extends VariantAnnotator
{
	private static final Logger LOG = LoggerFactory.getLogger(GenePanelServiceAnnotator.class);

	private final MolgenisSettings molgenisSettings;
	private final AnnotationService annotatorService;

	// 5GPM list
	List<String> severeLateOnsetGenes5GPM = Arrays.asList(new String[]
	{ "AIP", "ALK", "APC", "AXIN2", "BAP1", "BMPR1A", "BRCA1", "CDH1", "CDK4", "CDKN2A", "CEBPA", "CHEK2", "CTHRC1",
			"CTNNA1", "DICER1", "EGFR", "FH", "FLCN", "GATA2", "KIT", "MAX", "MLH1", "MLH3", "MSH2", "MSH3", "MSH6",
			"MUTYH", "NF2", "PAX5", "PDGFRA", "PMS2", "PRKAR1A", "RAD51D", "STK11", "TMEM127", "TP53" });

	// http://www.ncbi.nlm.nih.gov/clinvar/docs/acmg/
	List<String> acmgRecomIncendentals = Arrays.asList(new String[]
	{ "APC", "MYH11", "ACTA2", "MYLK", "TMEM43", "DSP", "PKP2", "DSG2", "DSC2", "BRCA1", "BRCA2", "SCN5A", "RYR2",
			"LMNA", "MYBPC3", "COL3A1", "GLA", "APOB", "LDLR", "MYH7", "TPM1", "MYBPC3", "PRKAG2", "TNNI3", "MYL3",
			"MYL2", "ACTC1", "RET", "PCSK9", "TNNT2", "TP53", "TGFBR1", "TGFBR2", "TGFBR1", "TGFBR2", "SMAD3", "KCNQ1",
			"KCNH2", "SCN5A", "MLH1", "MSH2", "MSH6", "PMS2", "RYR1", "CACNA1S", "FBN1", "TGFBR1", "MEN1", "RET",
			"MUTYH", "NF2", "SDHD", "SDHAF2", "SDHC", "SDHB", "STK11", "MUTYH", "PTEN", "RB1", "TSC1", "TSC2", "VHL",
			"WT1" });

	// http://www.genedx.com/test-catalog/disorders/charge-syndrome/
	List<String> chargeSyndrome = Arrays.asList(new String[]
	{ "CHD7", "FGF8", "FGFR1", "GNRH1", "GNRHR", "KAL1", "KISS1", "KISS1R", "NR0B1", "NSMF", "NELF", "PROK2", "PROKR2",
			"TAC3", "TACR3" });

	private static final String NAME = "GenePanels";

	public final static String PANEL_SEVERELATEONSET = "GenePanel_5GPM";
	public final static String PANEL_ACMG = "GenePanel_ACMG";
	public final static String PANEL_CHARGE = "GenePanel_CHARGE";

	public static final String CUSTOMPANEL_FILE_LOCATION = "custompanel_location";

	@Autowired
	public GenePanelServiceAnnotator(MolgenisSettings molgenisSettings, AnnotationService annotatorService)
			throws IOException
	{
		this.molgenisSettings = molgenisSettings;
		this.annotatorService = annotatorService;
	}

	public GenePanelServiceAnnotator(File cgdFileLocation, File inputVcfFile, File outputVCFFile) throws IOException
	{
		this.molgenisSettings = new MolgenisSimpleSettings();
		molgenisSettings.setProperty(CUSTOMPANEL_FILE_LOCATION, cgdFileLocation.getAbsolutePath());
		this.annotatorService = new AnnotationServiceImpl();
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event)
	{
		annotatorService.addAnnotator(this);
	}

	@Override
	public String getSimpleName()
	{
		return NAME;
	}

	@Override
	protected boolean annotationDataExists()
	{
		return true;
	}

	@Override
	public List<Entity> annotateEntity(Entity entity) throws IOException, InterruptedException
	{
		List<Entity> results = new ArrayList<Entity>();

		Long position = entity.getLong(VcfRepository.POS);
		String chromosome = entity.getString(VcfRepository.CHROM);

		String geneSymbol = null;

		if (entity.getString(SnpEffServiceAnnotator.GENE_NAME) != null)
		{
			geneSymbol = entity.getString(SnpEffServiceAnnotator.GENE_NAME);
		}
		if (geneSymbol == null)
		{
			String annField = entity.getString(VcfRepository.getInfoPrefix() + "ANN");
			if (annField != null)
			{
				// if the entity is annotated with the snpEff annotator the split is already done
				String[] split = annField.split("\\|", -1);
				// TODO: ask Joeri to explain this line
				if (split.length > 10)
				{
					// 3 is 'gene name'
					// TODO check if it should not be index 4 -> 'gene id'
					if (split[3].length() != 0)
					{
						geneSymbol = split[3];
						// LOG.info("Gene symbol '" + geneSymbol + "' found for " + entity.toString());
					}
					else
					{
						// will happen a lot for whole genome sequencing data
						LOG.info("No gene symbol in ANN field for " + entity.toString());
					}

				}
			}
		}

		HashMap<String, Object> resultMap = new HashMap<String, Object>();

		if (severeLateOnsetGenes5GPM.contains(geneSymbol))
		{
			resultMap.put(PANEL_SEVERELATEONSET, "TRUE");
		}

		if (acmgRecomIncendentals.contains(geneSymbol))
		{
			resultMap.put(PANEL_ACMG, "TRUE");
		}

		if (chargeSyndrome.contains(geneSymbol))
		{
			resultMap.put(PANEL_CHARGE, "TRUE");
		}

		return results;

	}

	@Override
	public EntityMetaData getOutputMetaData()
	{
		DefaultEntityMetaData metadata = new DefaultEntityMetaData(this.getClass().getName(), MapEntity.class);

		metadata.addAttributeMetaData(new DefaultAttributeMetaData(PANEL_SEVERELATEONSET, FieldTypeEnum.STRING));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(PANEL_ACMG, FieldTypeEnum.STRING));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(PANEL_CHARGE, FieldTypeEnum.STRING));
		return metadata;
	}

}

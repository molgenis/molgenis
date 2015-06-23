package org.molgenis.data.annotation.entity.impl;

import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.DECIMAL;
import static org.molgenis.data.vcf.VcfRepository.ALT_META;
import static org.molgenis.data.vcf.VcfRepository.CHROM_META;
import static org.molgenis.data.vcf.VcfRepository.POS_META;
import static org.molgenis.data.vcf.VcfRepository.REF_META;

import java.util.ArrayList;
import java.util.List;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.RepositoryAnnotator;
import org.molgenis.data.annotation.entity.AnnotatorInfo;
import org.molgenis.data.annotation.entity.EntityAnnotator;
import org.molgenis.data.annotation.entity.AnnotatorInfo.Status;
import org.molgenis.data.annotation.resources.Resource;
import org.molgenis.data.annotation.resources.Resources;
import org.molgenis.data.annotation.resources.impl.ResourceImpl;
import org.molgenis.data.annotation.resources.impl.SingleResourceConfig;
import org.molgenis.data.annotation.resources.impl.TabixRepositoryFactory;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.framework.server.MolgenisSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

public class ExacAnnotator
{

	public static final String EXAC_AC = "EXAC_ALLELE_COUNT";
	public static final String EXAC_AC_LABEL = "EXACALLELECOUNT";
	public static final String EXAC_GMAF = "EXAC_GMAF";
	public static final String EXAC_GMAF_LABEL = "EXACGMAF";
	public static final String EXAC_AFR_MAF = "EXAC_AFR_MAF";
	public static final String EXAC_AFR_MAF_LABEL = "EXACAFRMAF";
	public static final String EXAC_AMR_MAF = "EXAC_AMR_MAF";
	public static final String EXAC_AMR_MAF_LABEL = "EXACAMRMAF";
	public static final String EXAC_ASN_MAF = "EXAC_ASN_MAF";
	public static final String EXAC_ASN_MAF_LABEL = "EXACASNMAF";
	public static final String EXAC_EUR_MAF = "EXAC_EUR_MAF";
	public static final String EXAC_EUR_MAF_LABEL = "EXACEURMAF";
	public static final String EXAC_AA_MAF = "EXAC_AA_MAF";
	public static final String EXAC_AA_MAF_LABEL = "EXACAAMAF";
	public static final String EXAC_EA_MAF = "EXAC_EA_MAF";
	public static final String EXAC_EA_MAF_LABEL = "EXACEAMAF";

	public static final String EXAC_FILE_LOCATION_PROPERTY = "exac_location";
	public static final String EXAC_TABIX_RESOURCE = "EXACTabixResource";
	private static final Logger LOG = LoggerFactory.getLogger(CaddAnnotator.class);

	@Autowired
	private MolgenisSettings molgenisSettings;
	@Autowired
	private DataService dataService;
	@Autowired
	private Resources resources;

	@Bean
	public RepositoryAnnotator exac()
	{
		List<AttributeMetaData> attributes = new ArrayList<>();

		DefaultAttributeMetaData exac_ac = new DefaultAttributeMetaData(EXAC_AC, FieldTypeEnum.DECIMAL)
		.setDescription("Allele count in genotypes derived straight from the ExAC AC field").setLabel(EXAC_AC_LABEL);
		
		DefaultAttributeMetaData exac_gmaf = new DefaultAttributeMetaData(EXAC_GMAF, FieldTypeEnum.DECIMAL)
				.setDescription("Global minor allele frequency derived straight from the ExAC GMAF field").setLabel(EXAC_GMAF_LABEL);

		DefaultAttributeMetaData exac_aa_maf = new DefaultAttributeMetaData(EXAC_AA_MAF, FieldTypeEnum.DECIMAL)
				.setDescription("African/African latino minor allele frequency derived straight from the ExAC AMR_MAF field").setLabel(EXAC_AA_MAF_LABEL);

		DefaultAttributeMetaData exac_amr_maf = new DefaultAttributeMetaData(EXAC_AMR_MAF, FieldTypeEnum.DECIMAL)
				.setDescription("American latino minor allele frequency derived straight from the ExAC AMR_MAF field").setLabel(EXAC_AMR_MAF_LABEL);

		DefaultAttributeMetaData exac_afr_maf = new DefaultAttributeMetaData(EXAC_AFR_MAF, FieldTypeEnum.DECIMAL)
				.setDescription("African minor allele frequency derived straight from the ExAC AFR_MAF field").setLabel(EXAC_AFR_MAF_LABEL);

		DefaultAttributeMetaData exac_asn_maf = new DefaultAttributeMetaData(EXAC_ASN_MAF, FieldTypeEnum.DECIMAL)
				.setDescription("Asia minor allele frequency derived straight from the ExAC ASN_MAF field").setLabel(EXAC_ASN_MAF_LABEL);

		DefaultAttributeMetaData exac_eur_maf = new DefaultAttributeMetaData(EXAC_EUR_MAF, FieldTypeEnum.DECIMAL)
				.setDescription("European minor allele frequency derived straight from the ExAC EUR_MAF field").setLabel(EXAC_EUR_MAF_LABEL);

		DefaultAttributeMetaData exac_ea_maf = new DefaultAttributeMetaData(EXAC_EA_MAF, FieldTypeEnum.DECIMAL)
				.setDescription("European American minor allele frequency derived straight from the ExAC AE_MAF field").setLabel(EXAC_EA_MAF_LABEL);

		attributes.add(exac_gmaf);
		attributes.add(exac_afr_maf);
		attributes.add(exac_amr_maf);
		attributes.add(exac_asn_maf);
		attributes.add(exac_ea_maf);
		attributes.add(exac_eur_maf);
		attributes.add(exac_aa_maf);

		AnnotatorInfo exacInfo = AnnotatorInfo
				.create(Status.BETA,
						AnnotatorInfo.Type.POPULATION_REFERENCE,
						"exac",
						" The Exome Aggregation Consortium (ExAC) is a coalition of investigators seeking to aggregate"
						+ " and harmonize exome sequencing data from a wide variety of large-scale sequencing projects"
						+ ", and to make summary data available for the wider scientific community.The data set provided"
						+ " on this website spans 60,706 unrelated individuals sequenced as part of various "
						+ "disease-specific and population genetic studies. ",
						attributes);
		EntityAnnotator entityAnnotator = new AnnotatorImpl(EXAC_TABIX_RESOURCE, exacInfo, new LocusQueryCreator(),
				new VariantResultFilter(), dataService, resources);

		return new RepositoryAnnotatorImpl(entityAnnotator);
	}

	@Bean
	Resource exacResource()
	{
		Resource exacTabixResource = null;

		DefaultEntityMetaData repoMetaData = new DefaultEntityMetaData(EXAC_TABIX_RESOURCE);
		repoMetaData.addAttributeMetaData(CHROM_META);
		repoMetaData.addAttributeMetaData(POS_META);
		repoMetaData.addAttributeMetaData(REF_META);
		repoMetaData.addAttributeMetaData(ALT_META);
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("AC", DECIMAL));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("GMAF", DECIMAL));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("AFR_MAF", DECIMAL));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("AMR_MAF", DECIMAL));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("ASN_MAF", DECIMAL));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("EUR_MAF", DECIMAL));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("AA_MAF", DECIMAL));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("EA_MAF", DECIMAL));

		repoMetaData.addAttribute("id").setIdAttribute(true).setVisible(false);

		exacTabixResource = new ResourceImpl(EXAC_TABIX_RESOURCE, new SingleResourceConfig(EXAC_FILE_LOCATION_PROPERTY,
				molgenisSettings), new TabixRepositoryFactory(repoMetaData));

		return exacTabixResource;
	}
	
	public void filterResults(Iterable<Entity> results, Entity annotatedEntity)
	{
	
	}
}

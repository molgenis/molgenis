package org.molgenis.data.annotation.entity.impl;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.RepositoryAnnotator;
import org.molgenis.data.annotation.cmd.cmdlineannotatorsettingsconfigurer.SingleFileLocationCmdLineAnnotatorSettingsConfigurer;
import org.molgenis.data.annotation.entity.AnnotatorConfig;
import org.molgenis.data.annotation.entity.AnnotatorInfo;
import org.molgenis.data.annotation.entity.EntityAnnotator;
import org.molgenis.data.annotation.filter.MultiAllelicResultFilter;
import org.molgenis.data.annotation.query.LocusQueryCreator;
import org.molgenis.data.annotation.resources.Resource;
import org.molgenis.data.annotation.resources.Resources;
import org.molgenis.data.annotation.resources.impl.RepositoryFactory;
import org.molgenis.data.annotation.resources.impl.ResourceImpl;
import org.molgenis.data.annotation.resources.impl.SingleResourceConfig;
import org.molgenis.data.annotation.resources.impl.TabixRepositoryFactory;
import org.molgenis.data.annotator.websettings.CaddAnnotatorSettings;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.AttributeMetaDataFactory;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.meta.model.EntityMetaDataFactory;
import org.molgenis.data.vcf.model.VcfAttributes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

import static org.molgenis.MolgenisFieldTypes.AttributeType.STRING;

@Configuration
public class CaddAnnotator implements AnnotatorConfig
{
	public static final String NAME = "cadd";
	// FIXME: nomenclature: http://cadd.gs.washington.edu/info
	public static final String CADD_SCALED = "CADD_SCALED";
	public static final String CADD_ABS = "CADD";
	public static final String CADD_SCALED_LABEL = "CADDSCALED";
	public static final String CADD_ABS_LABEL = "CADDABS";
	public static final String CADD_TABIX_RESOURCE = "CADDTabixResource";

	@Autowired
	private Entity caddAnnotatorSettings;

	@Autowired
	private Resources resources;

	@Autowired
	private DataService dataService;

	@Autowired
	private VcfAttributes vcfAttributes;

	@Autowired
	private EntityMetaDataFactory entityMetaDataFactory;

	@Autowired
	private AttributeMetaDataFactory attributeMetaDataFactory;
	private RepositoryAnnotatorImpl annotator;

	@Bean
	public RepositoryAnnotator cadd()
	{
		annotator = new RepositoryAnnotatorImpl(NAME);
		return annotator;
	}

	@Override
	public void init()
	{
		List<AttributeMetaData> attributes = new ArrayList<>();
		AttributeMetaData cadd_abs = getCaddAbsAttr();
		AttributeMetaData cadd_scaled = getCaddScaledAttr();

		attributes.add(cadd_abs);
		attributes.add(cadd_scaled);

		AnnotatorInfo caddInfo = AnnotatorInfo
				.create(AnnotatorInfo.Status.READY, AnnotatorInfo.Type.PATHOGENICITY_ESTIMATE, NAME,
						"CADD is a tool for scoring the deleteriousness of single nucleotide variants as well as insertion/deletions variants in the human genome.\n"
								+ "While many variant annotation and scoring utils are around, most annotations tend to exploit a single information type (e.g. conservation) "
								+ "and/or are restricted in scope (e.g. to missense changes). "
								+ "Thus, a broadly applicable metric that objectively weights and integrates diverse information is needed. "
								+ "Combined Annotation Dependent Depletion (CADD) is a framework that integrates multiple "
								+ "annotations into one metric by contrasting variants that survived natural selection with simulated mutations.\n"
								+ "C-scores strongly correlate with allelic diversity, pathogenicity of both coding and non-coding variants, and experimentally measured "
								+ "regulatory effects, and also highly rank causal variants within "
								+ "individual genome sequences. Finally, C-scores of complex trait-associated variants from genome-wide association studies (GWAS) are "
								+ "significantly higher than matched controls and correlate with study sample size, likely reflecting the increased accuracy of larger GWAS.\n"
								+ "CADD can quantitatively prioritize functional, deleterious, and disease causal variants across a wide range of functional categories, "
								+ "effect sizes and genetic architectures and can be used prioritize "
								+ "causal variation in both research and clinical settings. (source: http://cadd.gs.washington.edu/info)",
						attributes);
		EntityAnnotator entityAnnotator = new AnnotatorImpl(CADD_TABIX_RESOURCE, caddInfo,
				new LocusQueryCreator(vcfAttributes), new MultiAllelicResultFilter(attributes, true), dataService,
				resources,
				new SingleFileLocationCmdLineAnnotatorSettingsConfigurer(CaddAnnotatorSettings.Meta.CADD_LOCATION,
						caddAnnotatorSettings));
		annotator.init(entityAnnotator);
	}

	public AttributeMetaData getCaddScaledAttr()
	{
		return attributeMetaDataFactory.create().setName(CADD_SCALED).setDataType(STRING)
					.setDescription(
							"Since the raw scores do have relative meaning, one can take a specific group of variants, define the rank for each variant within that group, and then use "
									+ "that value as a \"normalized\" and now externally comparable unit of analysis. In our case, we scored and ranked all ~8.6 billion SNVs of the "
									+ "GRCh37/hg19 reference and then \"PHRED-scaled\" those values by expressing the rank in order of magnitude terms rather than the precise rank itself. "
									+ "For example, reference genome single nucleotide variants at the 10th-% of CADD scores are assigned to CADD-10, top 1% to CADD-20, top 0.1% to CADD-30, etc. "
									+ "The results of this transformation are the \"scaled\" CADD scores.(source: http://cadd.gs.washington.edu/info)")
					.setLabel(CADD_SCALED_LABEL);
	}

	public AttributeMetaData getCaddAbsAttr()
	{
		return attributeMetaDataFactory.create().setName(CADD_ABS).setDataType(STRING)
					.setDescription(
							"\"Raw\" CADD scores come straight from the model, and are interpretable as the extent to which the annotation profile for a given variant suggests that "
									+ "that variant is likely to be \"observed\" (negative values) vs \"simulated\" (positive values). These values have no absolute unit of meaning and are "
									+ "incomparable across distinct annotation combinations, training sets, or model parameters. However, raw values do have relative meaning, with higher values "
									+ "indicating that a variant is more likely to be simulated (or \"not observed\") and therefore more likely to have deleterious effects."
									+ "(source: http://cadd.gs.washington.edu/info)").setLabel(CADD_ABS_LABEL);
	}

	@Bean
	Resource caddResource()
	{
		return new ResourceImpl(CADD_TABIX_RESOURCE,
				new SingleResourceConfig(CaddAnnotatorSettings.Meta.CADD_LOCATION, caddAnnotatorSettings))
		{
			@Override
			public RepositoryFactory getRepositoryFactory()
			{
				String idAttrName = "id";
				EntityMetaData repoMetaData = entityMetaDataFactory.create().setName(CADD_TABIX_RESOURCE);
				repoMetaData.addAttribute(vcfAttributes.getChromAttribute());
				repoMetaData.addAttribute(vcfAttributes.getPosAttribute());
				repoMetaData.addAttribute(vcfAttributes.getRefAttribute());
				repoMetaData.addAttribute(vcfAttributes.getAltAttribute());
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName(CADD_ABS).setDataType(STRING));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName(CADD_SCALED).setDataType(STRING));
				AttributeMetaData idAttributeMetaData = attributeMetaDataFactory.create().setName(idAttrName)
						.setVisible(false);
				repoMetaData.addAttribute(idAttributeMetaData);
				repoMetaData.setIdAttribute(idAttributeMetaData);
				return new TabixRepositoryFactory(repoMetaData);
			}
		};
	}
}

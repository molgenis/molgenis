package org.molgenis.data.annotation.mini.impl;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.annotation.RepositoryAnnotator;
import org.molgenis.data.annotation.mini.AnnotatorInfo;
import org.molgenis.data.annotation.mini.AnnotatorInfo.Status;
import org.molgenis.data.annotation.mini.EntityAnnotator;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CaddAnnotator
{
	public static final String CADD_SCALED = "CADDSCALED";
	public static final String CADD_ABS = "CADDABS";

	@Bean
	public RepositoryAnnotator cadd()
	{
		AnnotatorInfo caddInfo = AnnotatorInfo
				.create(Status.BETA,
						AnnotatorInfo.Type.PATHOGENICITY_ESTIMATE,
						"cadd",
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
								+ "causal variation in both research and clinical settings. (source: http://cadd.gs.washington.edu/info)");
		EntityAnnotator entityAnnotator = new AnnotatorImpl("CADDTabixRepository", caddInfo, new LocusQueryCreator(),
				new VariantResultFilter());
		DefaultEntityMetaData metadata = new DefaultEntityMetaData(this.getClass().getName(), MapEntity.class);
		DefaultAttributeMetaData cadd_abs = new DefaultAttributeMetaData(CADD_ABS, FieldTypeEnum.DECIMAL)
				.setDescription("\"Raw\" CADD scores come straight from the model, and are interpretable as the extent to which the annotation profile for a given variant suggests that "
						+ "that variant is likely to be \"observed\" (negative values) vs \"simulated\" (positive values). These values have no absolute unit of meaning and are "
						+ "incomparable across distinct annotation combinations, training sets, or model parameters. However, raw values do have relative meaning, with higher values "
						+ "indicating that a variant is more likely to be simulated (or \"not observed\") and therefore more likely to have deleterious effects."
						+ "(source: http://cadd.gs.washington.edu/info)");
		DefaultAttributeMetaData cadd_scaled = new DefaultAttributeMetaData(CADD_SCALED, FieldTypeEnum.DECIMAL)
				.setDescription("Since the raw scores do have relative meaning, one can take a specific group of variants, define the rank for each variant within that group, and then use "
						+ "that value as a \"normalized\" and now externally comparable unit of analysis. In our case, we scored and ranked all ~8.6 billion SNVs of the "
						+ "GRCh37/hg19 reference and then \"PHRED-scaled\" those values by expressing the rank in order of magnitude terms rather than the precise rank itself. "
						+ "For example, reference genome single nucleotide variants at the 10th-% of CADD scores are assigned to CADD-10, top 1% to CADD-20, top 0.1% to CADD-30, etc. "
						+ "The results of this transformation are the \"scaled\" CADD scores.(source: http://cadd.gs.washington.edu/info)");

		metadata.addAttributeMetaData(cadd_abs);
		metadata.addAttributeMetaData(cadd_scaled);

		return new RepositoryAnnotatorImpl(entityAnnotator, metadata);
	}
}

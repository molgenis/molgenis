package org.molgenis.data.annotation.core.entity.impl;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.core.RepositoryAnnotator;
import org.molgenis.data.annotation.core.entity.AnnotatorConfig;
import org.molgenis.data.annotation.core.entity.AnnotatorInfo;
import org.molgenis.data.annotation.core.entity.EntityAnnotator;
import org.molgenis.data.annotation.core.entity.impl.framework.AbstractAnnotator;
import org.molgenis.data.annotation.core.entity.impl.framework.RepositoryAnnotatorImpl;
import org.molgenis.data.annotation.core.filter.MultiAllelicResultFilter;
import org.molgenis.data.annotation.core.query.LocusQueryCreator;
import org.molgenis.data.annotation.core.resources.Resource;
import org.molgenis.data.annotation.core.resources.Resources;
import org.molgenis.data.annotation.core.resources.impl.RepositoryFactory;
import org.molgenis.data.annotation.core.resources.impl.ResourceImpl;
import org.molgenis.data.annotation.core.resources.impl.SingleResourceConfig;
import org.molgenis.data.annotation.core.resources.impl.tabix.TabixRepositoryFactory;
import org.molgenis.data.annotation.web.settings.CaddAnnotatorSettings;
import org.molgenis.data.annotation.web.settings.SingleFileLocationCmdLineAnnotatorSettingsConfigurer;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.vcf.model.VcfAttributes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

import static org.molgenis.data.meta.AttributeType.STRING;

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
	private EntityTypeFactory entityTypeFactory;

	@Autowired
	private AttributeFactory attributeFactory;

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
		List<Attribute> attributes = createCaddAnnotatorAttributes();

		AnnotatorInfo caddInfo = AnnotatorInfo.create(AnnotatorInfo.Status.READY,
				AnnotatorInfo.Type.PATHOGENICITY_ESTIMATE, NAME,
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
		EntityAnnotator entityAnnotator = new AbstractAnnotator(CADD_TABIX_RESOURCE, caddInfo,
				new LocusQueryCreator(vcfAttributes), new MultiAllelicResultFilter(attributes, true, vcfAttributes),
				dataService, resources,
				new SingleFileLocationCmdLineAnnotatorSettingsConfigurer(CaddAnnotatorSettings.Meta.CADD_LOCATION,
						caddAnnotatorSettings))
		{
			@Override
			public List<Attribute> createAnnotatorAttributes(AttributeFactory attributeFactory)
			{
				return createCaddAnnotatorAttributes();
			}
		};
		annotator.init(entityAnnotator);
	}

	private List<Attribute> createCaddAnnotatorAttributes()
	{
		List<Attribute> attributes = new ArrayList<>();
		Attribute cadd_abs = createCaddAbsAttr(attributeFactory);
		Attribute cadd_scaled = createCaddScaledAttr(attributeFactory);

		attributes.add(cadd_abs);
		attributes.add(cadd_scaled);
		return attributes;
	}

	public static Attribute createCaddScaledAttr(AttributeFactory attributeFactory)
	{
		return attributeFactory.create()
							   .setName(CADD_SCALED)
							   .setDataType(STRING)
							   .setDescription(
									   "Since the raw scores do have relative meaning, one can take a specific group of variants, define the rank for each variant within that group, and then use "
											   + "that value as a \"normalized\" and now externally comparable unit of analysis. In our case, we scored and ranked all ~8.6 billion SNVs of the "
											   + "GRCh37/hg19 reference and then \"PHRED-scaled\" those values by expressing the rank in order of magnitude terms rather than the precise rank itself. "
											   + "For example, reference genome single nucleotide variants at the 10th-% of CADD scores are assigned to CADD-10, top 1% to CADD-20, top 0.1% to CADD-30, etc. "
											   + "The results of this transformation are the \"scaled\" CADD scores.(source: http://cadd.gs.washington.edu/info)")
							   .setLabel(CADD_SCALED_LABEL);
	}

	static Attribute createCaddAbsAttr(AttributeFactory attributeFactory)
	{
		return attributeFactory.create()
							   .setName(CADD_ABS)
							   .setDataType(STRING)
							   .setDescription(
									   "\"Raw\" CADD scores come straight from the model, and are interpretable as the extent to which the annotation profile for a given variant suggests that "
											   + "that variant is likely to be \"observed\" (negative values) vs \"simulated\" (positive values). These values have no absolute unit of meaning and are "
											   + "incomparable across distinct annotation combinations, training sets, or model parameters. However, raw values do have relative meaning, with higher values "
											   + "indicating that a variant is more likely to be simulated (or \"not observed\") and therefore more likely to have deleterious effects."
											   + "(source: http://cadd.gs.washington.edu/info)")
							   .setLabel(CADD_ABS_LABEL);
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
				EntityType repoMetaData = entityTypeFactory.create(CADD_TABIX_RESOURCE);
				repoMetaData.addAttribute(vcfAttributes.getChromAttribute());
				repoMetaData.addAttribute(vcfAttributes.getPosAttribute());
				repoMetaData.addAttribute(vcfAttributes.getRefAttribute());
				repoMetaData.addAttribute(vcfAttributes.getAltAttribute());
				repoMetaData.addAttribute(attributeFactory.create().setName(CADD_ABS).setDataType(STRING));
				repoMetaData.addAttribute(attributeFactory.create().setName(CADD_SCALED).setDataType(STRING));
				Attribute idAttribute = attributeFactory.create()
														.setName(idAttrName)
														.setVisible(false)
														.setIdAttribute(true);
				repoMetaData.addAttribute(idAttribute);
				return new TabixRepositoryFactory(repoMetaData);
			}
		};
	}
}

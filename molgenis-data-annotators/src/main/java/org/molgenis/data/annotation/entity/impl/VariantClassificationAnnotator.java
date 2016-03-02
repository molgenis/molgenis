package org.molgenis.data.annotation.entity.impl;

import autovalue.shaded.com.google.common.common.collect.Iterables;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.annotation.RepositoryAnnotator;
import org.molgenis.data.annotation.entity.AnnotatorInfo;
import org.molgenis.data.annotation.entity.EntityAnnotator;
import org.molgenis.data.annotation.entity.impl.SnpEffAnnotator.Impact;
import org.molgenis.data.annotation.impl.datastructures.Judgment;
import org.molgenis.data.annotation.impl.datastructures.Judgment.Classification;
import org.molgenis.data.annotation.impl.datastructures.Judgment.Method;
import org.molgenis.data.annotation.query.GeneNameQueryCreator;
import org.molgenis.data.annotation.resources.Resource;
import org.molgenis.data.annotation.resources.Resources;
import org.molgenis.data.annotation.resources.impl.InMemoryRepositoryFactory;
import org.molgenis.data.annotation.resources.impl.SingleResourceConfig;
import org.molgenis.data.annotator.websettings.VariantClassificationAnnotatorSettings;
import org.molgenis.data.importer.EmxMetaDataParser;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Configuration
public class VariantClassificationAnnotator
{
	public static final double MAF_THRESHOLD = 0.00474;
	public static final int CADD_MINIMUM_THRESHOLD = 5;
	public static final int CADD_MAXIMUM_THRESHOLD = 25;

	public enum Category
	{
		N1, N2, T1, T2, I1, I2, I3, C1, C2, C3, C4, C5
	}

	public static final String NAME = "GavinAnnotator";
	public static final String RESOURCE = "variantClassification";
	public static final String RESOURCE_ENTITY_NAME = "ccgg";

	public static final String PATHOMAFTHRESHOLD = "PathoMAFThreshold";
	public static final String MEANPATHOGENICCADDSCORE = "MeanPathogenicCADDScore";
	public static final String SPEC95THPERCADDTHRESHOLD = "Spec95thPerCADDThreshold";
	private static final String CATEGORY = "Category";

	public static final String CLASSIFICATION = "Classification";
	public static final String CONFIDENCE = "Confidence";
	public static final String REASON = "Reason";

	@Autowired
	private Entity variantClassificationAnnotatorSettings;

	@Autowired
	private DataService dataService;

	@Autowired
	private Resources resources;

	@Bean
	public RepositoryAnnotator variantClassification()
	{
		List<AttributeMetaData> attributes = new ArrayList<>();
		DefaultAttributeMetaData classification = new DefaultAttributeMetaData(CLASSIFICATION,
				MolgenisFieldTypes.FieldTypeEnum.STRING).setDescription(CLASSIFICATION).setLabel(CLASSIFICATION);
		DefaultAttributeMetaData confidence = new DefaultAttributeMetaData(CONFIDENCE,
				MolgenisFieldTypes.FieldTypeEnum.STRING).setDescription(CONFIDENCE).setLabel(CONFIDENCE);
		DefaultAttributeMetaData reason = new DefaultAttributeMetaData(REASON, MolgenisFieldTypes.FieldTypeEnum.STRING)
				.setDescription(REASON).setLabel(REASON);

		attributes.add(classification);
		attributes.add(confidence);
		attributes.add(reason);

		AnnotatorInfo classificationInfo = AnnotatorInfo.create(AnnotatorInfo.Status.READY,
				AnnotatorInfo.Type.PATHOGENICITY_ESTIMATE, NAME, "", attributes);
		EntityAnnotator entityAnnotator = new QueryAnnotatorImpl(RESOURCE, classificationInfo,
				new GeneNameQueryCreator(), dataService, resources, (annotationSourceFileName) -> {
					variantClassificationAnnotatorSettings.set(NAME, "");
				})
		{
			@Override
			public List<AttributeMetaData> getRequiredAttributes()
			{
				List<AttributeMetaData> requiredAttributes = new ArrayList<>();
				requiredAttributes.addAll(Arrays.asList(ExacAnnotator.EXAC_AF_ATTR, SnpEffAnnotator.GENE_NAME_ATTR,
						SnpEffAnnotator.IMPACT_ATTR, CaddAnnotator.CADD_SCALED_ATTR));
				return requiredAttributes;
			}

			@Override
			protected void processQueryResults(Entity inputEntity, Iterable<Entity> annotationSourceEntities,
					Entity resultEntity)
			{
				int sourceEntitiesSize = Iterables.size(annotationSourceEntities);

				if (sourceEntitiesSize == 1)
				{
					Entity annotationSourceEntity = annotationSourceEntities.iterator().next();
					// FIXME: multiallelic
					Judgment judgment = classifyVariant(inputEntity, annotationSourceEntity);
					resultEntity.set(CLASSIFICATION, judgment.getClassification().toString());
					resultEntity.set(CONFIDENCE, judgment.getConfidence().toString());
					resultEntity.set(REASON, judgment.getReason());
				}
				else if (sourceEntitiesSize == 0)
				{
					// if we have no data for this gene, immediately fall back to the naive method
					// FIXME: multiallelic
					Judgment judgment = genomewideClassifyVariant(inputEntity);
					resultEntity.set(CLASSIFICATION, judgment.getClassification().toString());
					resultEntity.set(CONFIDENCE, judgment.getConfidence().toString());
					resultEntity.set(REASON, judgment.getReason());
				}
				else
				{
					String message = "invalid number [" + sourceEntitiesSize
							+ "] of results for this gene in annotation resource";
					resultEntity.set(REASON, message);
					throw new MolgenisDataException(message);
				}
			}
		};
		return new RepositoryAnnotatorImpl(entityAnnotator);
	}

	@Bean
	Resource variantClassificationResource()
	{
		Resource variantClassificationResource = new EmxResourceImpl(RESOURCE,
				new SingleResourceConfig(VariantClassificationAnnotatorSettings.Meta.VARIANT_FILE_LOCATION,
						variantClassificationAnnotatorSettings),
				new InMemoryRepositoryFactory(RESOURCE_ENTITY_NAME, new EmxMetaDataParser(dataService)),
				RESOURCE_ENTITY_NAME, Collections.EMPTY_LIST);

		return variantClassificationResource;
	}

	private Judgment classifyVariant(Entity inputEntity, Entity annotationSourceEntity)
	{
		Double minorAlleleFrequency = inputEntity.getDouble(ExacAnnotator.EXAC_AF) != null
				? inputEntity.getDouble(ExacAnnotator.EXAC_AF) : 0;
		Impact impact = Impact.valueOf(inputEntity.getString(SnpEffAnnotator.PUTATIVE_IMPACT));
		Double CADDscore = inputEntity.getDouble(CaddAnnotator.CADD_SCALED);
		Category category = Category.valueOf(annotationSourceEntity.getString(CATEGORY));

		Double pathoMAFThreshold = annotationSourceEntity.getDouble(PATHOMAFTHRESHOLD);
		Double meanPathogenicCADDScore = annotationSourceEntity.getDouble(MEANPATHOGENICCADDSCORE);
		Double spec95thPerCADDThreshold = annotationSourceEntity.getDouble(SPEC95THPERCADDTHRESHOLD);

		// MAF based classification, calibrated
		if (minorAlleleFrequency > pathoMAFThreshold)
		{
			return new Judgment(Classification.Benign, Method.calibrated, "Variant MAF of " + minorAlleleFrequency
					+ " is greater than the pathogenic 95th percentile MAF of " + pathoMAFThreshold + ".");
		}

		String mafReason = "the variant MAF of " + minorAlleleFrequency
				+ " is lesser than the pathogenic 95th percentile MAF of " + pathoMAFThreshold + ".";

		// Impact based classification, calibrated
		if (impact != null)
		{
			if (category.equals(Category.I1) && impact.equals(Impact.HIGH))
			{
				return new Judgment(Judgment.Classification.Pathogn, Method.calibrated,
						"Variant is of high impact, while there are no known high impact variants in the population. Also, "
								+ mafReason);
			}
			else if (category.equals(Category.I2) && (impact.equals(Impact.MODERATE) || impact.equals(Impact.HIGH)))
			{
				return new Judgment(Judgment.Classification.Pathogn, Method.calibrated,
						"Variant is of high/moderate impact, while there are no known high/moderate impact variants in the population. Also, "
								+ mafReason);
			}
			else if (category.equals(Category.I3)
					&& (impact.equals(Impact.LOW) || impact.equals(Impact.MODERATE) || impact.equals(Impact.HIGH)))
			{
				return new Judgment(Judgment.Classification.Pathogn, Method.calibrated,
						"Variant is of high/moderate/low impact, while there are no known high/moderate/low impact variants in the population. Also, "
								+ mafReason);
			}
			else if (impact.equals(Impact.MODIFIER))
			{
				return new Judgment(Judgment.Classification.Benign, Method.calibrated,
						"Variant is of 'modifier' impact, and therefore unlikely to be pathogenic. However, "
								+ mafReason);
			}
		}

		// CADD score based classification, calibrated
		if (CADDscore != null)
		{
			if ((category.equals(Category.C1) || category.equals(Category.C2)))
			{
				if (CADDscore > meanPathogenicCADDScore)
				{
					return new Judgment(Judgment.Classification.Pathogn, Method.calibrated,
							"Variant CADD score of " + CADDscore + " is greater than the mean pathogenic score of "
									+ meanPathogenicCADDScore
									+ " in a gene for which CADD scores are informative. Also, " + mafReason);
				}
				else if (CADDscore < meanPathogenicCADDScore)
				{
					return new Judgment(Judgment.Classification.Benign, Method.calibrated,
							"Variant CADD score of " + CADDscore + " is lesser than the mean population score of "
									+ meanPathogenicCADDScore
									+ " in a gene for which CADD scores are informative, although " + mafReason);
				}
			}
			else if ((category.equals(Category.C3) || category.equals(Category.C4) || category.equals(Category.C5)))
			{
				if (CADDscore > spec95thPerCADDThreshold)
				{
					return new Judgment(Judgment.Classification.Pathogn, Method.calibrated,
							"Variant CADD score of " + CADDscore + " is greater than the 95% specificity threhold of "
									+ spec95thPerCADDThreshold + " for this gene. Also, " + mafReason);
				}
				else if (CADDscore < spec95thPerCADDThreshold)
				{
					return new Judgment(Judgment.Classification.Benign, Method.calibrated,
							"Variant CADD score of " + CADDscore + " is lesser than the 95% sensitivity threhold of "
									+ spec95thPerCADDThreshold + " for this gene, although " + mafReason);
				}
			}
		}

		// if everything so far has failed, we can still fall back to the naive method
		return genomewideClassifyVariant(inputEntity);
	}

	public Judgment genomewideClassifyVariant(Entity entity)
	{

		Double minorAlleleFrequency = entity.getDouble(ExacAnnotator.EXAC_AF) != null
				? entity.getDouble(ExacAnnotator.EXAC_AF) : 0;
		Impact impact = Impact.valueOf(entity.getString(SnpEffAnnotator.PUTATIVE_IMPACT));
		Double CADDscore = entity.getDouble(CaddAnnotator.CADD_SCALED);
		String gene = entity.getString(SnpEffAnnotator.GENE_NAME);

		if (minorAlleleFrequency != null && minorAlleleFrequency > MAF_THRESHOLD)
		{
			return new Judgment(Judgment.Classification.Benign, Method.genomewide, "MAF > " + MAF_THRESHOLD);
		}
		if (impact.equals(Impact.MODIFIER))
		{
			return new Judgment(Judgment.Classification.Benign, Method.genomewide, "Impact is MODIFIER");
		}
		else
		{
			if (CADDscore != null && CADDscore > CADD_MAXIMUM_THRESHOLD)
			{
				return new Judgment(Judgment.Classification.Pathogn, Method.genomewide,
						"CADDscore > " + CADD_MAXIMUM_THRESHOLD);
			}
			else if (CADDscore != null && CADDscore < CADD_MINIMUM_THRESHOLD)
			{
				return new Judgment(Judgment.Classification.Benign, Method.genomewide,
						"CADDscore < " + CADD_MINIMUM_THRESHOLD);
			}
			else
			{
				return new Judgment(Judgment.Classification.VOUS, Method.genomewide,
						"Unable to classify variant as benign or pathogenic. The combination of " + impact
								+ " impact, a CADD score " + (CADDscore != null ? CADDscore : "[missing]")
								+ " and MAF of " + (minorAlleleFrequency != null ? minorAlleleFrequency : "[missing]")
								+ " in " + gene + " is inconclusive.");
			}
		}
	}
}
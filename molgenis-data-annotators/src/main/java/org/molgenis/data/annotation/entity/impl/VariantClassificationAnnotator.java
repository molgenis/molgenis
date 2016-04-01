package org.molgenis.data.annotation.entity.impl;

import autovalue.shaded.com.google.common.common.collect.Iterables;
import org.apache.commons.lang.StringUtils;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.annotation.cmd.EffectsAnnotator;
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
import org.molgenis.data.importer.EmxFileOnlyMetaDataParser;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.EffectsMetaData;
import org.molgenis.data.vcf.VcfRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Configuration
public class VariantClassificationAnnotator
{
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
	public static final String VARIANT_ENTITY = "Variant";

	public static final int CADD_MAXIMUM_THRESHOLD = 25;
	public static final int CADD_MINIMUM_THRESHOLD = 5;
	public static final double MAF_THRESHOLD = 0.00474;

	public enum Category
	{
		N1, N2, T1, T2, I1, I2, I3, C1, C2, C3, C4, C5

	}

	@Autowired
	private Entity variantClassificationAnnotatorSettings;

	@Autowired
	private DataService dataService;

	@Autowired
	private Resources resources;

	@Bean
	public EffectsAnnotator variantClassification()
	{
		LinkedList<AttributeMetaData> attributes = new LinkedList<>();
		DefaultAttributeMetaData classification = new DefaultAttributeMetaData(CLASSIFICATION,
				MolgenisFieldTypes.FieldTypeEnum.STRING).setDescription(CLASSIFICATION).setLabel(CLASSIFICATION);
		DefaultAttributeMetaData confidence = new DefaultAttributeMetaData(CONFIDENCE,
				MolgenisFieldTypes.FieldTypeEnum.STRING).setDescription(CONFIDENCE).setLabel(CONFIDENCE);
		DefaultAttributeMetaData reason = new DefaultAttributeMetaData(REASON, MolgenisFieldTypes.FieldTypeEnum.STRING)
				.setDescription(REASON).setLabel(REASON);

		attributes.add(classification);
		attributes.add(confidence);
		attributes.add(reason);

		String description = "Please note that this annotator processes the results from a SnpEff annotation\nTherefor it should be used on the result entity rather than the variant entity itself.\nThe corresponding variant entity should also be annotated with CADD and EXaC";

		AnnotatorInfo classificationInfo = AnnotatorInfo.create(AnnotatorInfo.Status.READY,
				AnnotatorInfo.Type.PATHOGENICITY_ESTIMATE, NAME, description, attributes);
		EntityAnnotator entityAnnotator = new QueryAnnotatorImpl(RESOURCE, classificationInfo,
				new GeneNameQueryCreator(), dataService, resources, (annotationSourceFileName) -> {
					variantClassificationAnnotatorSettings.set(
							VariantClassificationAnnotatorSettings.Meta.VARIANT_FILE_LOCATION,
							annotationSourceFileName);
				})
		{
			@Override
			public List<AttributeMetaData> getRequiredAttributes()
			{
				List<AttributeMetaData> requiredAttributes = new ArrayList<>();
				DefaultEntityMetaData entityMetaData = new DefaultEntityMetaData(VARIANT_ENTITY);
				List<AttributeMetaData> refAttributesList = Arrays.asList(CaddAnnotator.CADD_SCALED_ATTR,
						ExacAnnotator.EXAC_AF_ATTR, VcfRepository.ALT_META);
				entityMetaData.addAllAttributeMetaData(refAttributesList);
				AttributeMetaData refAttr = new DefaultAttributeMetaData(VARIANT_ENTITY,
						MolgenisFieldTypes.FieldTypeEnum.XREF)
								.setRefEntity(entityMetaData)
								.setDescription("This annotator needs a references to an entity containing: "
										+ StreamSupport.stream(refAttributesList.spliterator(), false)
												.map(AttributeMetaData::getName).collect(Collectors.joining(", ")));

				requiredAttributes.addAll(Arrays.asList(EffectsMetaData.GENE_NAME_ATTR,
						EffectsMetaData.PUTATIVE_IMPACT_ATTR, refAttr, VcfRepository.ALT_META));
				return requiredAttributes;
			}

			@Override
			protected void processQueryResults(Entity inputEntity, Iterable<Entity> annotationSourceEntities,
					Entity resultEntity)
			{
				String alt = inputEntity.getString(EffectsMetaData.ALT);
				if (alt.contains(","))
				{
					throw new MolgenisDataException(
							"The variant prediction annotator only accepts single allele entities.");
				}
				int sourceEntitiesSize = Iterables.size(annotationSourceEntities);

				Entity variantEntity = inputEntity.getEntity(VARIANT_ENTITY);

				Map<String, Double> caddMap = toMap(variantEntity.getString(VcfRepository.ALT),
						variantEntity.getString(CaddAnnotator.CADD_SCALED));
				Map<String, Double> exacMap = toMap(variantEntity.getString(VcfRepository.ALT),
						variantEntity.getString(ExacAnnotator.EXAC_AF));

				Impact impact = Impact.valueOf(inputEntity.getString(EffectsMetaData.PUTATIVE_IMPACT));
				Double exacMAF = exacMap.get(alt);
				Double caddScaled = caddMap.get(alt);
				String gene = inputEntity.getString(EffectsMetaData.GENE_NAME);
				if (exacMAF == null)
				{
					exacMAF = 0.0;
				}

				if (sourceEntitiesSize == 1)
				{
					Entity annotationSourceEntity = annotationSourceEntities.iterator().next();

					Category category = Category.valueOf(annotationSourceEntity.getString(CATEGORY));

					Judgment judgment = classifyVariant(impact, caddScaled, exacMAF, category, gene,
							annotationSourceEntity);
					resultEntity.set(CLASSIFICATION, judgment.getClassification().toString());
					resultEntity.set(CONFIDENCE, judgment.getConfidence().toString());
					resultEntity.set(REASON, judgment.getReason());
				}
				else if (sourceEntitiesSize == 0)
				{
					// if we have no data for this gene, immediately fall back to the naive method
					Judgment judgment = genomewideClassifyVariant(impact, caddScaled, exacMAF, gene);
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
		return new EffectsAnnotator(entityAnnotator);
	}

	@Bean
	Resource variantClassificationResource()
	{
		Resource variantClassificationResource = new EmxResourceImpl(RESOURCE,
				new SingleResourceConfig(VariantClassificationAnnotatorSettings.Meta.VARIANT_FILE_LOCATION,
						variantClassificationAnnotatorSettings),
				new InMemoryRepositoryFactory(RESOURCE_ENTITY_NAME, new EmxFileOnlyMetaDataParser()),
				RESOURCE_ENTITY_NAME, Collections.EMPTY_LIST);

		return variantClassificationResource;
	}

	private Judgment classifyVariant(Impact impact, Double caddScaled, Double exacMAF, Category category, String gene,
			Entity annotationSourceEntity)
	{

		Double pathoMAFThreshold = annotationSourceEntity.getDouble(PATHOMAFTHRESHOLD);
		Double meanPathogenicCADDScore = annotationSourceEntity.getDouble(MEANPATHOGENICCADDSCORE);
		Double spec95thPerCADDThreshold = annotationSourceEntity.getDouble(SPEC95THPERCADDTHRESHOLD);

		// MAF based classification, calibrated
		if (exacMAF > pathoMAFThreshold)
		{
			return new Judgment(Classification.Benign, Method.calibrated, "Variant MAF of " + exacMAF
					+ " is greater than the pathogenic 95th percentile MAF of " + pathoMAFThreshold + ".");
		}

		String mafReason = "the variant MAF of " + exacMAF + " is lesser than the pathogenic 95th percentile MAF of "
				+ pathoMAFThreshold + ".";

		// Impact based classification, calibrated
		if (impact != null)
		{
			if (category.equals(Category.I1) && impact.equals(Impact.HIGH))
			{
				return new Judgment(Judgment.Classification.Pathognic, Method.calibrated,
						"Variant is of high impact, while there are no known high impact variants in the population. Also, "
								+ mafReason);
			}
			else if (category.equals(Category.I2) && (impact.equals(Impact.MODERATE) || impact.equals(Impact.HIGH)))
			{
				return new Judgment(Judgment.Classification.Pathognic, Method.calibrated,
						"Variant is of high/moderate impact, while there are no known high/moderate impact variants in the population. Also, "
								+ mafReason);
			}
			else if (category.equals(Category.I3)
					&& (impact.equals(Impact.LOW) || impact.equals(Impact.MODERATE) || impact.equals(Impact.HIGH)))
			{
				return new Judgment(Judgment.Classification.Pathognic, Method.calibrated,
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
		if (caddScaled != null)
		{
			if ((category.equals(Category.C1) || category.equals(Category.C2)))
			{
				if (caddScaled > meanPathogenicCADDScore)
				{
					return new Judgment(Judgment.Classification.Pathognic, Method.calibrated,
							"Variant CADD score of " + caddScaled + " is greater than the mean pathogenic score of "
									+ meanPathogenicCADDScore
									+ " in a gene for which CADD scores are informative. Also, " + mafReason);
				}
				else if (caddScaled < meanPathogenicCADDScore)
				{
					return new Judgment(Judgment.Classification.Benign, Method.calibrated,
							"Variant CADD score of " + caddScaled + " is lesser than the mean population score of "
									+ meanPathogenicCADDScore
									+ " in a gene for which CADD scores are informative, although " + mafReason);
				}
			}
			else if ((category.equals(Category.C3) || category.equals(Category.C4) || category.equals(Category.C5)))
			{
				if (caddScaled > spec95thPerCADDThreshold)
				{
					return new Judgment(Judgment.Classification.Pathognic, Method.calibrated,
							"Variant CADD score of " + caddScaled + " is greater than the 95% specificity threhold of "
									+ spec95thPerCADDThreshold + " for this gene. Also, " + mafReason);
				}
				else if (caddScaled < spec95thPerCADDThreshold)
				{
					return new Judgment(Judgment.Classification.Benign, Method.calibrated,
							"Variant CADD score of " + caddScaled + " is lesser than the 95% sensitivity threhold of "
									+ spec95thPerCADDThreshold + " for this gene, although " + mafReason);
				}
			}
		}

		// if everything so far has failed, we can still fall back to the naive method
		return genomewideClassifyVariant(impact, caddScaled, exacMAF, gene);
	}

	private Map<String, Double> toMap(String alternatives, String annotations)
	{
		if (annotations == null) annotations = "";
		String[] altArray = alternatives.split(",");
		String[] annotationsArray = annotations.split(",");

		Map<String, Double> result = new HashMap<>();
		if (altArray.length == annotationsArray.length)
		{
			for (int i = 0; i < altArray.length; i++)
			{
				Double value = null;
				if (StringUtils.isNotEmpty(annotationsArray[i]))
				{
					value = Double.parseDouble(annotationsArray[i]);
				}
				result.put(altArray[i], value);
			}
		}
		else if (StringUtils.isEmpty(annotations))
		{
			for (int i = 0; i < altArray.length; i++)
			{
				result.put(altArray[i], null);
			}
		}
		else
		{
			throw new MolgenisDataException(VcfRepository.ALT + " differs in length from the provided annotations.");
		}
		return result;
	}

	public Judgment genomewideClassifyVariant(Impact impact, Double caddScaled, Double exacMAF, String gene)
	{

		exacMAF = exacMAF != null ? exacMAF : 0;

		if (exacMAF != null && exacMAF > MAF_THRESHOLD)
		{
			return new Judgment(Judgment.Classification.Benign, Method.genomewide, "MAF > " + MAF_THRESHOLD);
		}
		if (impact.equals(Impact.MODIFIER))
		{
			return new Judgment(Judgment.Classification.Benign, Method.genomewide, "Impact is MODIFIER");
		}
		else
		{
			if (caddScaled != null && caddScaled > CADD_MAXIMUM_THRESHOLD)
			{
				return new Judgment(Judgment.Classification.Pathognic, Method.genomewide,
						"CADDscore > " + CADD_MAXIMUM_THRESHOLD);
			}
			else if (caddScaled != null && caddScaled < CADD_MINIMUM_THRESHOLD)
			{
				return new Judgment(Judgment.Classification.Benign, Method.genomewide,
						"CADDscore < " + CADD_MINIMUM_THRESHOLD);
			}
			else
			{
				return new Judgment(Judgment.Classification.VOUS, Method.genomewide,
						"Unable to classify variant as benign or pathogenic. The combination of " + impact
								+ " impact, a CADD score " + (caddScaled != null ? caddScaled : "[missing]")
								+ " and MAF of " + (exacMAF != null ? exacMAF : "[missing]") + " in " + gene
								+ " is inconclusive.");
			}
		}
	}
}
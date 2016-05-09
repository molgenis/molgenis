package org.molgenis.data.annotation.entity.impl.gavin;

import autovalue.shaded.com.google.common.common.collect.Iterables;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.annotation.EffectsAnnotator;
import org.molgenis.data.annotation.entity.AnnotatorInfo;
import org.molgenis.data.annotation.entity.EntityAnnotator;
import org.molgenis.data.annotation.entity.impl.CaddAnnotator;
import org.molgenis.data.annotation.entity.impl.EmxResourceImpl;
import org.molgenis.data.annotation.entity.impl.ExacAnnotator;
import org.molgenis.data.annotation.entity.impl.QueryAnnotatorImpl;
import org.molgenis.data.annotation.entity.impl.snpEff.Impact;
import org.molgenis.data.annotation.query.GeneNameQueryCreator;
import org.molgenis.data.annotation.resources.Resource;
import org.molgenis.data.annotation.resources.Resources;
import org.molgenis.data.annotation.resources.impl.InMemoryRepositoryFactory;
import org.molgenis.data.annotation.resources.impl.SingleResourceConfig;
import org.molgenis.data.annotation.utils.AnnotatorUtils;
import org.molgenis.data.annotator.websettings.GavinAnnotatorSettings;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Configuration
public class GavinAnnotator
{
	public static final String NAME = "Gavin";
	public static final String RESOURCE = "gavin";
	public static final String RESOURCE_ENTITY_NAME = "ccgg";
	private static final String CATEGORY = "Category";

	public static final String CLASSIFICATION = "Classification";
	public static final String CONFIDENCE = "Confidence";
	public static final String REASON = "Reason";
	public static final String VARIANT_ENTITY = "Variant";

	public static final int CADD_MAXIMUM_THRESHOLD = 25;
	public static final int CADD_MINIMUM_THRESHOLD = 5;
	public static final double MAF_THRESHOLD = 0.00474;
	private final GavinAlgorithm gavinAlgorithm = new GavinAlgorithm();

	@Autowired
	private Entity gavinAnnotatorSettings;

	@Autowired
	private DataService dataService;

	@Autowired
	private Resources resources;

	@Bean
	Resource GavinResource()
	{
		Resource gavinResource = new EmxResourceImpl(RESOURCE,
				new SingleResourceConfig(GavinAnnotatorSettings.Meta.VARIANT_FILE_LOCATION, gavinAnnotatorSettings),
				new InMemoryRepositoryFactory(RESOURCE_ENTITY_NAME, new EmxFileOnlyMetaDataParser()),
				RESOURCE_ENTITY_NAME, Collections.EMPTY_LIST);

		return gavinResource;
	}

	@Bean
	public EffectsAnnotator gavin()
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

		AnnotatorInfo gavinInfo = AnnotatorInfo.create(AnnotatorInfo.Status.READY,
				AnnotatorInfo.Type.PATHOGENICITY_ESTIMATE, NAME, description, attributes);
		EntityAnnotator entityAnnotator = new QueryAnnotatorImpl(RESOURCE, gavinInfo, new GeneNameQueryCreator(),
				dataService, resources, (annotationSourceFileName) -> {
					gavinAnnotatorSettings.set(GavinAnnotatorSettings.Meta.VARIANT_FILE_LOCATION,
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
						EffectsMetaData.PUTATIVE_IMPACT_ATTR, refAttr, EffectsMetaData.ALT_ATTR));
				return requiredAttributes;
			}

			@Override
			protected void processQueryResults(Entity inputEntity, Iterable<Entity> annotationSourceEntities,
					Entity resultEntity, boolean updateMode)
			{
				if (updateMode == true)
				{
					throw new MolgenisDataException("This annotator/filter does not support updating of values");
				}
				String alt = inputEntity.getString(EffectsMetaData.ALT);
				if (alt == null)
				{
					resultEntity.set(CLASSIFICATION, "");
					resultEntity.set(CONFIDENCE, "");
					resultEntity.set(REASON, "Missing ALT allele no judgment could be determined.");
					return;
				}
				if (alt.contains(","))
				{
					throw new MolgenisDataException(
							"The gavin annotator only accepts single allele input ('effect entities').");
				}
				int sourceEntitiesSize = Iterables.size(annotationSourceEntities);

				Entity variantEntity = inputEntity.getEntity(VARIANT_ENTITY);

				Map<String, Double> caddMap = AnnotatorUtils.toAlleleMap(variantEntity.getString(VcfRepository.ALT),
						variantEntity.getString(CaddAnnotator.CADD_SCALED));
				Map<String, Double> exacMap = AnnotatorUtils.toAlleleMap(variantEntity.getString(VcfRepository.ALT),
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

					Judgment judgment = gavinAlgorithm.classifyVariant(impact, caddScaled, exacMAF, category, gene,
							annotationSourceEntity);
					resultEntity.set(CLASSIFICATION, judgment.getClassification().toString());
					resultEntity.set(CONFIDENCE, judgment.getConfidence().toString());
					resultEntity.set(REASON, judgment.getReason());
				}
				else if (sourceEntitiesSize == 0)
				{
					// if we have no data for this gene, immediately fall back to the naive method
					Judgment judgment = gavinAlgorithm.genomewideClassifyVariant(impact, caddScaled, exacMAF, gene);
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
}
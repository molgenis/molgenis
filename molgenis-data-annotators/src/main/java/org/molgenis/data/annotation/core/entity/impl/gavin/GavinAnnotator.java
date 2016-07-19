package org.molgenis.data.annotation.core.entity.impl.gavin;

import autovalue.shaded.com.google.common.common.collect.Iterables;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.annotation.core.EffectsAnnotator;
import org.molgenis.data.annotation.core.effects.EffectsMetaData;
import org.molgenis.data.annotation.core.entity.AnnotatorConfig;
import org.molgenis.data.annotation.core.entity.AnnotatorInfo;
import org.molgenis.data.annotation.core.entity.EntityAnnotator;
import org.molgenis.data.annotation.core.entity.impl.CaddAnnotator;
import org.molgenis.data.annotation.core.entity.impl.ExacAnnotator;
import org.molgenis.data.annotation.core.entity.impl.framework.QueryAnnotatorImpl;
import org.molgenis.data.annotation.core.entity.impl.snpeff.Impact;
import org.molgenis.data.annotation.core.query.GeneNameQueryCreator;
import org.molgenis.data.annotation.core.resources.Resource;
import org.molgenis.data.annotation.core.resources.Resources;
import org.molgenis.data.annotation.core.resources.impl.RepositoryFactory;
import org.molgenis.data.annotation.core.resources.impl.SingleResourceConfig;
import org.molgenis.data.annotation.core.resources.impl.emx.EmxResourceImpl;
import org.molgenis.data.annotation.core.resources.impl.emx.InMemoryRepositoryFactory;
import org.molgenis.data.annotation.core.utils.AnnotatorUtils;
import org.molgenis.data.annotation.web.settings.GavinAnnotatorSettings;
import org.molgenis.data.importer.EmxFileOnlyMetaDataParser;
import org.molgenis.data.meta.model.*;
import org.molgenis.data.vcf.model.VcfAttributes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.molgenis.MolgenisFieldTypes.AttributeType.STRING;
import static org.molgenis.MolgenisFieldTypes.AttributeType.XREF;

@Configuration
public class GavinAnnotator implements AnnotatorConfig
{
	public static final String NAME = "Gavin";
	public static final String RESOURCE = "gavin";
	public static final String RESOURCE_ENTITY_NAME = "gavin";
	private static final String CATEGORY = "Category";

	public static final String CLASSIFICATION = "Classification";
	public static final String CONFIDENCE = "Confidence";
	public static final String REASON = "Reason";
	public static final String VARIANT_ENTITY = "Variant";

	public static final int CADD_MAXIMUM_THRESHOLD = 15;
	public static final int CADD_MINIMUM_THRESHOLD = 15;
	public static final double MAF_THRESHOLD = 0.00474;
	private final GavinAlgorithm gavinAlgorithm = new GavinAlgorithm();

	@Autowired
	private Entity gavinAnnotatorSettings;

	@Autowired
	private DataService dataService;

	@Autowired
	private Resources resources;

	@Autowired
	private VcfAttributes vcfAttributes;

	@Autowired
	private PackageFactory packageFactory;

	@Autowired
	private EntityMetaDataFactory entityMetaDataFactory;

	@Autowired
	private AttributeMetaDataFactory attributeMetaDataFactory;

	@Autowired
	private EffectsMetaData effectsMetaData;

	@Autowired
	GeneNameQueryCreator geneNameQueryCreator;

	@Bean
	Resource GavinResource()
	{
		Resource gavinResource = new EmxResourceImpl(RESOURCE,
				new SingleResourceConfig(GavinAnnotatorSettings.Meta.VARIANT_FILE_LOCATION, gavinAnnotatorSettings))
		{
			@Override
			public RepositoryFactory getRepositoryFactory()
			{
				return new InMemoryRepositoryFactory(RESOURCE_ENTITY_NAME,
						new EmxFileOnlyMetaDataParser(packageFactory, attributeMetaDataFactory, entityMetaDataFactory),
						entityMetaDataFactory, attributeMetaDataFactory);
			}
		};

		return gavinResource;
	}

	private EffectsAnnotator annotator;

	@Bean
	public EffectsAnnotator gavin()
	{
		annotator = new EffectsAnnotator(NAME);
		return annotator;
	}

	public void init()
	{
		LinkedList<AttributeMetaData> attributes = new LinkedList<>();
		AttributeMetaData classification = attributeMetaDataFactory.create().setName(CLASSIFICATION).setDataType(STRING)
				.setDescription(CLASSIFICATION).setLabel(CLASSIFICATION);
		AttributeMetaData confidence = attributeMetaDataFactory.create().setName(CONFIDENCE).setDataType(STRING)
				.setDescription(CONFIDENCE).setLabel(CONFIDENCE);
		AttributeMetaData reason = attributeMetaDataFactory.create().setName(REASON).setDataType(STRING)
				.setDescription(REASON).setLabel(REASON);

		attributes.add(classification);
		attributes.add(confidence);
		attributes.add(reason);

		String description = "Please note that this annotator processes the results from a SnpEff annotation\nTherefor it should be used on the result entity rather than the variant entity itself.\nThe corresponding variant entity should also be annotated with CADD and EXaC";

		AnnotatorInfo gavinInfo = AnnotatorInfo
				.create(AnnotatorInfo.Status.READY, AnnotatorInfo.Type.PATHOGENICITY_ESTIMATE, NAME, description,
						attributes);
		EntityAnnotator entityAnnotator = new QueryAnnotatorImpl(RESOURCE, gavinInfo, geneNameQueryCreator, dataService,
				resources, (annotationSourceFileName) -> {
			gavinAnnotatorSettings.set(GavinAnnotatorSettings.Meta.VARIANT_FILE_LOCATION, annotationSourceFileName);
		})
		{
			@Override
			public List<AttributeMetaData> getRequiredAttributes()
			{
				List<AttributeMetaData> requiredAttributes = new ArrayList<>();
				EntityMetaData entityMetaData = entityMetaDataFactory.create().setName(VARIANT_ENTITY);
				List<AttributeMetaData> refAttributesList = Arrays
						.asList(CaddAnnotator.getCaddScaledAttr(attributeMetaDataFactory),
								ExacAnnotator.getExacAFAttr(attributeMetaDataFactory), vcfAttributes.getAltAttribute());
				entityMetaData.addAttributes(refAttributesList);
				AttributeMetaData refAttr = attributeMetaDataFactory.create().setName(VARIANT_ENTITY).setDataType(XREF)
						.setRefEntity(entityMetaData).setDescription(
								"This annotator needs a references to an entity containing: " + StreamSupport
										.stream(refAttributesList.spliterator(), false).map(AttributeMetaData::getName)
										.collect(Collectors.joining(", ")));

				requiredAttributes.addAll(Arrays
						.asList(effectsMetaData.getGeneNameAttr(), effectsMetaData.getPutativeImpactAttr(), refAttr,
								effectsMetaData.getAltAttr()));
				return requiredAttributes;
			}

			@Override
			protected void processQueryResults(Entity entity, Iterable<Entity> annotationSourceEntities,
					boolean updateMode)
			{
				if (updateMode == true)
				{
					throw new MolgenisDataException("This annotator/filter does not support updating of values");
				}
				String alt = entity.getString(EffectsMetaData.ALT);
				if (alt == null)
				{
					entity.set(CLASSIFICATION, "");
					entity.set(CONFIDENCE, "");
					entity.set(REASON, "Missing ALT allele no judgment could be determined.");
					return;
				}
				if (alt.contains(","))
				{
					throw new MolgenisDataException(
							"The gavin annotator only accepts single allele input ('effect entities').");
				}
				int sourceEntitiesSize = Iterables.size(annotationSourceEntities);

				Entity variantEntity = entity.getEntity(VARIANT_ENTITY);

				Map<String, Double> caddMap = AnnotatorUtils.toAlleleMap(variantEntity.getString(VcfAttributes.ALT),
						variantEntity.getString(CaddAnnotator.CADD_SCALED));
				Map<String, Double> exacMap = AnnotatorUtils.toAlleleMap(variantEntity.getString(VcfAttributes.ALT),
						variantEntity.getString(ExacAnnotator.EXAC_AF));

				Impact impact = Impact.valueOf(entity.getString(EffectsMetaData.PUTATIVE_IMPACT));
				Double exacMAF = exacMap.get(alt);
				Double caddScaled = caddMap.get(alt);
				String gene = entity.getString(EffectsMetaData.GENE_NAME);
				if (exacMAF == null)
				{
					exacMAF = 0.0;
				}

				if (sourceEntitiesSize == 1)
				{
					Entity annotationSourceEntity = annotationSourceEntities.iterator().next();

					Category category = Category.valueOf(annotationSourceEntity.getString(CATEGORY));

					Judgment judgment = gavinAlgorithm
							.classifyVariant(impact, caddScaled, exacMAF, category, gene, annotationSourceEntity);
					entity.set(CLASSIFICATION, judgment.getClassification().toString());
					entity.set(CONFIDENCE, judgment.getConfidence().toString());
					entity.set(REASON, judgment.getReason());
				}
				else if (sourceEntitiesSize == 0)
				{
					// if we have no data for this gene, immediately fall back to the naive method
					Judgment judgment = gavinAlgorithm.genomewideClassifyVariant(impact, caddScaled, exacMAF, gene);
					entity.set(CLASSIFICATION, judgment.getClassification().toString());
					entity.set(CONFIDENCE, judgment.getConfidence().toString());
					entity.set(REASON, judgment.getReason());
				}
				else
				{
					String message = "invalid number [" + sourceEntitiesSize
							+ "] of results for this gene in annotation resource";
					entity.set(REASON, message);
					throw new MolgenisDataException(message);
				}
			}

		};
		annotator.init(entityAnnotator);
	}
}
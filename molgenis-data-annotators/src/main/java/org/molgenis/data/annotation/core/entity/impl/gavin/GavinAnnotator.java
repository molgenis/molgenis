package org.molgenis.data.annotation.core.entity.impl.gavin;

import com.google.common.collect.Iterables;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.annotation.core.EffectBasedAnnotator;
import org.molgenis.data.annotation.core.effects.EffectsMetaData;
import org.molgenis.data.annotation.core.entity.AnnotatorConfig;
import org.molgenis.data.annotation.core.entity.AnnotatorInfo;
import org.molgenis.data.annotation.core.entity.EntityAnnotator;
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
import org.molgenis.data.importer.emx.EmxMetaDataParser;
import org.molgenis.data.meta.EntityTypeDependencyResolver;
import org.molgenis.data.meta.model.*;
import org.molgenis.data.vcf.model.VcfAttributes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.molgenis.data.annotation.core.effects.EffectsMetaData.GENE_NAME;
import static org.molgenis.data.annotation.core.effects.EffectsMetaData.PUTATIVE_IMPACT;
import static org.molgenis.data.annotation.core.entity.AnnotatorInfo.Status.READY;
import static org.molgenis.data.annotation.core.entity.AnnotatorInfo.Type.PATHOGENICITY_ESTIMATE;
import static org.molgenis.data.annotation.core.entity.impl.CaddAnnotator.CADD_SCALED;
import static org.molgenis.data.annotation.core.entity.impl.CaddAnnotator.createCaddScaledAttr;
import static org.molgenis.data.annotation.core.entity.impl.ExacAnnotator.EXAC_AF;
import static org.molgenis.data.annotation.core.entity.impl.ExacAnnotator.getExacAFAttr;
import static org.molgenis.data.annotation.web.settings.GavinAnnotatorSettings.Meta.VARIANT_FILE_LOCATION;
import static org.molgenis.data.meta.AttributeType.STRING;
import static org.molgenis.data.meta.AttributeType.XREF;
import static org.molgenis.data.vcf.model.VcfAttributes.ALT;
import static org.molgenis.data.vcf.utils.VcfWriterUtils.VARIANT;

@Configuration
public class GavinAnnotator implements AnnotatorConfig
{
	public static final String NAME = "Gavin";
	public static final String RESOURCE = "gavin";
	public static final String RESOURCE_ENTITY_NAME = "base_gavin";

	public static final String CLASSIFICATION = "Classification";
	public static final String CONFIDENCE = "Confidence";
	public static final String REASON = "Reason";

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
	private EntityTypeFactory entityTypeFactory;

	@Autowired
	private AttributeFactory attributeFactory;

	@Autowired
	private EffectsMetaData effectsMetaData;

	@Autowired
	private EntityTypeDependencyResolver entityTypeDependencyResolver;

	@Autowired
	GeneNameQueryCreator geneNameQueryCreator;

	@Bean
	Resource GavinResource()
	{
		return new EmxResourceImpl(RESOURCE, new SingleResourceConfig(VARIANT_FILE_LOCATION, gavinAnnotatorSettings))
		{
			@Override
			public RepositoryFactory getRepositoryFactory()
			{
				return new InMemoryRepositoryFactory(RESOURCE_ENTITY_NAME, RESOURCE,
						new EmxMetaDataParser(packageFactory, attributeFactory, entityTypeFactory,
								entityTypeDependencyResolver), entityTypeFactory, attributeFactory);
			}
		};
	}

	private EffectBasedAnnotator annotator;

	@Bean
	public EffectBasedAnnotator gavin()
	{
		annotator = new EffectBasedAnnotator(NAME);
		return annotator;
	}

	public void init()
	{
		LinkedList<Attribute> attributes = createGavinOutputAttributes();

		String description = "Please note that this annotator processes the results from a SnpEff annotation\nTherefor it should be used on the result entity rather than the variant entity itself.\nThe corresponding variant entity should also be annotated with CADD and EXaC";

		AnnotatorInfo gavinInfo = AnnotatorInfo.create(READY, PATHOGENICITY_ESTIMATE, NAME, description, attributes);
		EntityAnnotator entityAnnotator = new QueryAnnotatorImpl(RESOURCE, gavinInfo, geneNameQueryCreator, dataService,
				resources, (annotationSourceFileName) -> gavinAnnotatorSettings.set(VARIANT_FILE_LOCATION,
				annotationSourceFileName))
		{
			@Override
			public List<Attribute> createAnnotatorAttributes(AttributeFactory attributeFactory)
			{
				return createGavinOutputAttributes();
			}

			@Override
			public List<Attribute> getRequiredAttributes()
			{
				List<Attribute> requiredAttributes = new ArrayList<>();
				EntityType entityType = entityTypeFactory.create(VARIANT);
				List<Attribute> refAttributesList = Arrays.asList(createCaddScaledAttr(attributeFactory),
						getExacAFAttr(attributeFactory), vcfAttributes.getAltAttribute());
				entityType.addAttributes(refAttributesList);
				Attribute refAttr = attributeFactory.create()
													.setName(VARIANT)
													.setDataType(XREF)
													.setRefEntity(entityType)
													.setDescription(
															"This annotator needs a references to an entity containing: "
																	+ StreamSupport.stream(
																	refAttributesList.spliterator(), false)
																				   .map(Attribute::getName)
																				   .collect(Collectors.joining(", ")));

				requiredAttributes.addAll(
						Arrays.asList(effectsMetaData.getGeneNameAttr(), effectsMetaData.getPutativeImpactAttr(),
								refAttr, effectsMetaData.getAltAttr()));
				return requiredAttributes;
			}

			@Override
			protected void processQueryResults(Entity entity, Iterable<Entity> annotationSourceEntities,
					boolean updateMode)
			{
				if (updateMode)
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

				Entity variantEntity = entity.getEntity(VARIANT);

				Map<String, Double> caddMap = AnnotatorUtils.toAlleleMap(variantEntity.getString(ALT),
						variantEntity.getString(CADD_SCALED));
				Map<String, Double> exacMap = AnnotatorUtils.toAlleleMap(variantEntity.getString(ALT),
						variantEntity.getString(EXAC_AF));

				Impact impact = Impact.valueOf(entity.getString(PUTATIVE_IMPACT));
				Double exacMAF = exacMap.get(alt);
				Double caddScaled = caddMap.get(alt);
				String gene = entity.getString(GENE_NAME);
				if (exacMAF == null)
				{
					exacMAF = 0.0;
				}

				if (sourceEntitiesSize == 1)
				{
					Entity annotationSourceEntity = annotationSourceEntities.iterator().next();
					Judgment judgment = gavinAlgorithm.classifyVariant(impact, caddScaled, exacMAF, gene,
							GavinThresholds.fromEntity(annotationSourceEntity));
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

	private LinkedList<Attribute> createGavinOutputAttributes()
	{
		LinkedList<Attribute> attributes = new LinkedList<>();
		Attribute classification = attributeFactory.create()
												   .setName(CLASSIFICATION)
												   .setDataType(STRING)
												   .setDescription(CLASSIFICATION)
												   .setLabel(CLASSIFICATION);
		Attribute confidence = attributeFactory.create()
											   .setName(CONFIDENCE)
											   .setDataType(STRING)
											   .setDescription(CONFIDENCE)
											   .setLabel(CONFIDENCE);
		Attribute reason = attributeFactory.create()
										   .setName(REASON)
										   .setDataType(STRING)
										   .setDescription(REASON)
										   .setLabel(REASON);

		attributes.add(classification);
		attributes.add(confidence);
		attributes.add(reason);
		return attributes;
	}
}
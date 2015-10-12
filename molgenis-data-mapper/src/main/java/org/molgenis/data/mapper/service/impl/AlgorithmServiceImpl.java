package org.molgenis.data.mapper.service.impl;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.molgenis.data.mapper.mapping.model.AttributeMapping.AlgorithmState.GENERATED_HIGH;
import static org.molgenis.data.mapper.mapping.model.AttributeMapping.AlgorithmState.GENERATED_LOW;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.measure.converter.ConversionException;
import javax.measure.converter.UnitConverter;
import javax.measure.quantity.Quantity;
import javax.measure.unit.Unit;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.mapper.mapping.model.AttributeMapping;
import org.molgenis.data.mapper.mapping.model.AttributeMapping.AlgorithmState;
import org.molgenis.data.mapper.mapping.model.EntityMapping;
import org.molgenis.data.mapper.service.AlgorithmService;
import org.molgenis.data.mapper.service.UnitResolver;
import org.molgenis.data.semantic.Relation;
import org.molgenis.data.semanticsearch.explain.bean.ExplainedAttributeMetaData;
import org.molgenis.data.semanticsearch.service.OntologyTagService;
import org.molgenis.data.semanticsearch.service.SemanticSearchService;
import org.molgenis.data.support.MapEntity;
import org.molgenis.js.RhinoConfig;
import org.molgenis.js.ScriptEvaluator;
import org.molgenis.ontology.core.model.OntologyTerm;
import org.molgenis.security.core.runas.RunAsSystem;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import utils.MagmaUnitConverter;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

public class AlgorithmServiceImpl implements AlgorithmService
{
	private static final Logger LOG = LoggerFactory.getLogger(AlgorithmServiceImpl.class);

	private final DataService dataService;
	private final OntologyTagService ontologyTagService;
	private final SemanticSearchService semanticSearchService;
	private final UnitResolver unitResolver;
	private final AlgorithmTemplateService algorithmTemplateService;
	private final Pattern MAGMA_ATTRIBUTE_PATTERN = Pattern.compile("\\$\\('([^\\$\\(\\)]*)'\\)");
	private final MagmaUnitConverter magmaUnitConverter = new MagmaUnitConverter();

	@Autowired
	public AlgorithmServiceImpl(DataService dataService, OntologyTagService ontologyTagService,
			SemanticSearchService semanticSearchService, UnitResolver unitResolver,
			AlgorithmTemplateService algorithmTemplateService)
	{
		this.dataService = checkNotNull(dataService);
		this.ontologyTagService = checkNotNull(ontologyTagService);
		this.semanticSearchService = checkNotNull(semanticSearchService);
		this.unitResolver = checkNotNull(unitResolver);
		this.algorithmTemplateService = checkNotNull(algorithmTemplateService);

		new RhinoConfig().init();
	}

	@Override
	@RunAsSystem
	public void autoGenerateAlgorithm(EntityMetaData sourceEntityMetaData, EntityMetaData targetEntityMetaData,
			EntityMapping mapping, AttributeMetaData targetAttribute)
	{

		LOG.debug("createAttributeMappingIfOnlyOneMatch: target= " + targetAttribute.getName());
		Multimap<Relation, OntologyTerm> tagsForAttribute = ontologyTagService.getTagsForAttribute(
				targetEntityMetaData, targetAttribute);

		Map<AttributeMetaData, ExplainedAttributeMetaData> relevantAttributes = semanticSearchService
				.decisionTreeToFindRelevantAttributes(sourceEntityMetaData, targetAttribute, tagsForAttribute.values(),
						null);

		String algorithm = null;
		AlgorithmState algorithmState = null;
		Set<AttributeMetaData> mappedSourceAttributes = null;

		// use existing algorithm template if available
		AlgorithmTemplate algorithmTemplate = algorithmTemplateService.find(relevantAttributes).findFirst()
				.orElse(null);

		if (algorithmTemplate != null)
		{
			algorithm = algorithmTemplate.render();
			algorithmState = GENERATED_HIGH;
			mappedSourceAttributes = extractSourceAttributesFromAlgorithm(algorithm, sourceEntityMetaData);

			algorithm = convertUnitForTemplateAlgorithm(algorithm, targetAttribute, targetEntityMetaData,
					mappedSourceAttributes, sourceEntityMetaData);
		}
		else if (relevantAttributes.size() > 0)
		{
			Entry<AttributeMetaData, ExplainedAttributeMetaData> firstEntry = relevantAttributes.entrySet().stream()
					.findFirst().get();
			AttributeMetaData sourceAttribute = firstEntry.getKey();

			algorithm = generateUnitConversionAlgorithm(targetAttribute, targetEntityMetaData, sourceAttribute,
					sourceEntityMetaData);
			mappedSourceAttributes = Sets.newHashSet(sourceAttribute);
			algorithmState = firstEntry.getValue().isHighQuality() ? GENERATED_HIGH : GENERATED_LOW;
		}

		if (StringUtils.isNotBlank(algorithm))
		{
			AttributeMapping attributeMapping = mapping.addAttributeMapping(targetAttribute.getName());
			attributeMapping.setAlgorithm(algorithm);
			attributeMapping.getSourceAttributeMetaDatas().addAll(mappedSourceAttributes);
			attributeMapping.setAlgorithmState(algorithmState);
			LOG.debug("Creating attribute mapping: " + targetAttribute.getName() + " = " + algorithm);
		}
	}

	Set<AttributeMetaData> extractSourceAttributesFromAlgorithm(String algorithm, EntityMetaData sourceEntityMetaData)
	{
		if (StringUtils.isNotBlank(algorithm))
		{
			Set<String> attributeNames = new HashSet<>();
			Matcher matcher = MAGMA_ATTRIBUTE_PATTERN.matcher(algorithm);
			while (matcher.find())
			{
				attributeNames.add(matcher.group(1));
			}
			return attributeNames.stream().map(attributeName -> sourceEntityMetaData.getAttribute(attributeName))
					.filter(Objects::nonNull).collect(Collectors.toSet());
		}
		return Collections.emptySet();
	}

	String convertUnitForTemplateAlgorithm(String algorithm, AttributeMetaData targetAttribute,
			EntityMetaData targetEntityMetaData, Set<AttributeMetaData> sourceAttributes,
			EntityMetaData sourceEntityMetaData)
	{
		Unit<? extends Quantity> targetUnit = unitResolver.resolveUnit(targetAttribute, targetEntityMetaData);

		for (AttributeMetaData sourceAttribute : sourceAttributes)
		{
			Unit<? extends Quantity> sourceUnit = unitResolver.resolveUnit(sourceAttribute, sourceEntityMetaData);

			String convertUnit = magmaUnitConverter.convertUnit(targetUnit, sourceUnit);

			if (StringUtils.isNotBlank(convertUnit))
			{
				String attrMagamSyntax = String.format("$('%s')", sourceAttribute.getName());
				String unitConvertedMagamSyntax = convertUnit.startsWith(".") ? attrMagamSyntax + convertUnit : attrMagamSyntax
						+ "." + convertUnit;
				algorithm = StringUtils.replace(algorithm, attrMagamSyntax, unitConvertedMagamSyntax);
			}
		}

		return algorithm;
	}

	String generateUnitConversionAlgorithm(AttributeMetaData targetAttribute, EntityMetaData targetEntityMetaData,
			AttributeMetaData sourceAttribute, EntityMetaData sourceEntityMetaData)
	{
		String algorithm = null;

		Unit<? extends Quantity> targetUnit = unitResolver.resolveUnit(targetAttribute, targetEntityMetaData);

		Unit<? extends Quantity> sourceUnit = unitResolver.resolveUnit(sourceAttribute, sourceEntityMetaData);

		if (sourceUnit != null)
		{
			if (targetUnit != null && !sourceUnit.equals(targetUnit))
			{
				// if units are convertible, create convert algorithm
				UnitConverter unitConverter;
				try
				{
					unitConverter = sourceUnit.getConverterTo(targetUnit);
				}
				catch (ConversionException e)
				{
					unitConverter = null;
					// algorithm sets source unit and assigns source value to target
					algorithm = String.format("$('%s').unit('%s').value();", sourceAttribute.getName(),
							sourceUnit.toString());
				}

				if (unitConverter != null)
				{
					// algorithm sets source unit and assigns value converted to target unit to target
					algorithm = String.format("$('%s').unit('%s').toUnit('%s').value();", sourceAttribute.getName(),
							sourceUnit.toString(), targetUnit.toString());
				}
			}
			else
			{
				// algorithm sets source unit and assigns source value to target
				algorithm = String.format("$('%s').unit('%s').value();", sourceAttribute.getName(),
						sourceUnit.toString());
			}
		}

		if (algorithm == null)
		{
			// algorithm assigns source value to target
			algorithm = String.format("$('%s').value();", sourceAttribute.getName());
		}

		return algorithm;
	}

	@Override
	public Iterable<AlgorithmEvaluation> applyAlgorithm(AttributeMetaData targetAttribute, String algorithm,
			Iterable<Entity> sourceEntities)
	{
		final Collection<String> attributeNames = getSourceAttributeNames(algorithm);

		return Iterables.transform(sourceEntities, new Function<Entity, AlgorithmEvaluation>()
		{
			@Override
			public AlgorithmEvaluation apply(Entity entity)
			{
				AlgorithmEvaluation algorithmResult = new AlgorithmEvaluation(entity);

				Object derivedValue;
				MapEntity mapEntity = createMapEntity(attributeNames, entity); // why is this necessary?
				try
				{
					Object result = ScriptEvaluator.eval(algorithm, mapEntity, entity.getEntityMetaData());
					derivedValue = convert(result, targetAttribute);
				}
				catch (RuntimeException e)
				{
					return algorithmResult.errorMessage(e.getMessage());
				}

				return algorithmResult.value(derivedValue);
			}
		});
	}

	private MapEntity createMapEntity(Collection<String> attributeNames, Entity entity)
	{
		MapEntity mapEntity = new MapEntity();
		for (String attributeName : attributeNames)
		{
			Object value = entity.get(attributeName);
			if (value instanceof Entity)
			{
				value = ((Entity) value).getIdValue();
			}
			mapEntity.set(attributeName, value);
		}
		return mapEntity;
	}

	@Override
	public Object apply(AttributeMapping attributeMapping, Entity sourceEntity, EntityMetaData sourceEntityMetaData)
	{
		String algorithm = attributeMapping.getAlgorithm();
		if (StringUtils.isEmpty(algorithm))
		{
			return null;
		}

		MapEntity entity = createMapEntity(getSourceAttributeNames(attributeMapping.getAlgorithm()), sourceEntity);
		Object value = ScriptEvaluator.eval(algorithm, entity, sourceEntityMetaData);
		return convert(value, attributeMapping.getTargetAttributeMetaData());
	}

	private Object convert(Object value, AttributeMetaData attributeMetaData)
	{
		if (value == null)
		{
			return null;
		}
		Object convertedValue;
		FieldTypeEnum targetDataType = attributeMetaData.getDataType().getEnumType();
		try
		{
			switch (targetDataType)
			{
				case DATE:
				case DATE_TIME:
					convertedValue = Context.jsToJava(value, Date.class);
					break;
				case INT:
					convertedValue = Integer.parseInt(Context.toString(value));
					break;
				case DECIMAL:
					convertedValue = Context.toNumber(value);
					break;
				case XREF:
				case CATEGORICAL:
					convertedValue = dataService.findOne(attributeMetaData.getRefEntity().getName(),
							Context.toString(value));
					break;
				case MREF:
				case CATEGORICAL_MREF:
				{
					NativeArray mrefIds = (NativeArray) value;
					if (mrefIds != null && !mrefIds.isEmpty())
					{
						EntityMetaData refEntityMeta = attributeMetaData.getRefEntity();
						convertedValue = dataService.findAll(refEntityMeta.getName(), mrefIds);
					}
					else
					{
						convertedValue = null;
					}
					break;
				}
				default:
					convertedValue = Context.toString(value);
					break;
			}
		}
		catch (RuntimeException e)
		{
			throw new RuntimeException("Error converting value [" + value.toString() + "] to "
					+ targetDataType.toString(), e);
		}
		return convertedValue;
	}

	@Override
	public Collection<String> getSourceAttributeNames(String algorithmScript)
	{
		Collection<String> result = Collections.emptyList();
		if (!StringUtils.isEmpty(algorithmScript))
		{
			result = findMatchesForPattern(algorithmScript, "\\$\\('([^\\$\\(\\)]+)'\\)");
			if (result.isEmpty())
			{
				result = findMatchesForPattern(algorithmScript, "\\$\\(([^\\$\\(\\)]+)\\)");
			}
		}
		return result;
	}

	private static Collection<String> findMatchesForPattern(String algorithmScript, String patternString)
	{
		LinkedHashSet<String> result = new LinkedHashSet<String>();
		Matcher matcher = Pattern.compile(patternString).matcher(algorithmScript);
		while (matcher.find())
		{
			result.add(matcher.group(1));
		}
		return result;
	}

}

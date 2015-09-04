package org.molgenis.data.mapper.service.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import org.molgenis.data.semanticsearch.explain.bean.ExplainedQueryString;
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

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
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
	public void autoGenerateAlgorithm(EntityMetaData sourceEntityMeta, EntityMetaData targetEntityMeta,
			EntityMapping mapping, AttributeMetaData targetAttr)
	{
		LOG.debug("createAttributeMappingIfOnlyOneMatch: target= " + targetAttr.getName());
		Map<AttributeMetaData, Iterable<ExplainedQueryString>> matches = semanticSearchService
				.findAttributes(sourceEntityMeta, targetEntityMeta, targetAttr);

		Multimap<Relation, OntologyTerm> targetAttrTags = ontologyTagService.getTagsForAttribute(targetEntityMeta,
				targetAttr);
		Unit<? extends Quantity> targetUnit = unitResolver.resolveUnit(targetAttr, targetEntityMeta);
		for (Entry<AttributeMetaData, Iterable<ExplainedQueryString>> entry : matches.entrySet())
		{
			AttributeMetaData source = entry.getKey();

			// determine source unit
			Unit<? extends Quantity> sourceUnit = unitResolver.resolveUnit(source, sourceEntityMeta);

			String algorithm = null;
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
						algorithm = String.format("$('%s').unit('%s').value();", source.getName(),
								sourceUnit.toString());
					}

					if (unitConverter != null)
					{
						// algorithm sets source unit and assigns value converted to target unit to target
						algorithm = String.format("$('%s').unit('%s').toUnit('%s').value();", source.getName(),
								sourceUnit.toString(), targetUnit.toString());
					}
				}
				else
				{
					// algorithm sets source unit and assigns source value to target
					algorithm = String.format("$('%s').unit('%s').value();", source.getName(), sourceUnit.toString());

					// FIXME remove hack
					// find suitable algorithm templates
					AlgorithmTemplate algorithmTemplate = algorithmTemplateService
							.find(targetAttr, targetEntityMeta, sourceEntityMeta).findFirst().orElse(null);
					if (algorithmTemplate != null)
					{
						// render algorithm template
						algorithm = algorithmTemplate.render();
					}
				}
			}
			if (algorithm == null)
			{
				// algorithm assigns source value to target
				algorithm = String.format("$('%s').value();", source.getName());
			}
			AttributeMapping attributeMapping = mapping.addAttributeMapping(targetAttr.getName());
			attributeMapping.getSourceAttributeMetaDatas().add(source);
			attributeMapping.setAlgorithm(algorithm);

			if (isSingleMatchHighQuality(targetAttr, targetAttrTags, entry.getValue()))
			{
				attributeMapping.setAlgorithmState(AlgorithmState.GENERATED_HIGH);
			}
			else
			{
				attributeMapping.setAlgorithmState(AlgorithmState.GENERATED_LOW);
			}

			LOG.debug("Creating attribute mapping: " + targetAttr.getName() + " = " + algorithm);
			break;
		}

		if (mapping.getAttributeMapping(targetAttr.getName()) == null && !targetAttrTags.isEmpty())
		{
			// find suitable algorithm templates
			AlgorithmTemplate algorithmTemplate = algorithmTemplateService
					.find(targetAttr, targetEntityMeta, sourceEntityMeta).findFirst().orElse(null);
			if (algorithmTemplate != null)
			{
				// render algorithm template
				String algorithm = algorithmTemplate.render();

				// add mapping with algorithm
				AttributeMapping attributeMapping = mapping.addAttributeMapping(targetAttr.getName());
				attributeMapping.setAlgorithm(algorithm);
			}
		}
	}

	boolean isSingleMatchHighQuality(AttributeMetaData targetAttribute,
			Multimap<Relation, OntologyTerm> ontologyTermTags, Iterable<ExplainedQueryString> explanations)
	{
		Map<String, Double> matchedTags = new HashMap<String, Double>();
		for (ExplainedQueryString explanation : explanations)
		{
			matchedTags.put(explanation.getTagName().toLowerCase(), explanation.getScore());
		}
		String label = StringUtils.isNotEmpty(targetAttribute.getLabel()) ? targetAttribute.getLabel().toLowerCase()
				: StringUtils.EMPTY;
		String description = StringUtils.isNotEmpty(targetAttribute.getDescription())
				? targetAttribute.getDescription().toLowerCase() : StringUtils.EMPTY;

		if (isGoodMatch(matchedTags, label)) return true;
		if (isGoodMatch(matchedTags, description)) return true;

		for (OntologyTerm ontologyTerm : ontologyTermTags.values())
		{
			boolean allMatch = Lists.newArrayList(ontologyTerm.getLabel().toLowerCase().split(",")).stream()
					.allMatch(ontologyTermLabel -> isGoodMatch(matchedTags, ontologyTermLabel));
			if (allMatch) return true;
		}

		return false;
	}

	boolean isGoodMatch(Map<String, Double> matchedTags, String label)
	{
		return matchedTags.containsKey(label) && matchedTags.get(label).intValue() == 100
				|| Sets.newHashSet(label.split(" ")).stream()
						.allMatch(word -> matchedTags.containsKey(word) && matchedTags.get(word).intValue() == 100);
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
			throw new RuntimeException(
					"Error converting value [" + value.toString() + "] to " + targetDataType.toString(), e);
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

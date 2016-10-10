package org.molgenis.data.mapper.service.impl;

import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import org.apache.commons.lang3.StringUtils;
import org.molgenis.MolgenisFieldTypes.AttributeType;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.mapper.algorithmgenerator.bean.GeneratedAlgorithm;
import org.molgenis.data.mapper.algorithmgenerator.service.AlgorithmGeneratorService;
import org.molgenis.data.mapper.mapping.model.AttributeMapping;
import org.molgenis.data.mapper.mapping.model.EntityMapping;
import org.molgenis.data.mapper.service.AlgorithmService;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.semantic.Relation;
import org.molgenis.data.semanticsearch.explain.bean.ExplainedAttribute;
import org.molgenis.data.semanticsearch.service.OntologyTagService;
import org.molgenis.data.semanticsearch.service.SemanticSearchService;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.js.RhinoConfig;
import org.molgenis.ontology.core.model.OntologyTerm;
import org.molgenis.security.core.runas.RunAsSystem;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.collect.Sets.newLinkedHashSet;
import static java.lang.Double.parseDouble;
import static java.lang.Math.round;
import static java.lang.Math.toIntExact;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.molgenis.js.ScriptEvaluator.eval;
import static org.mozilla.javascript.Context.*;

public class AlgorithmServiceImpl implements AlgorithmService
{
	private static final Logger LOG = LoggerFactory.getLogger(AlgorithmServiceImpl.class);

	private final DataService dataService;
	private final OntologyTagService ontologyTagService;
	private final SemanticSearchService semanticSearchService;
	private final AlgorithmGeneratorService algorithmGeneratorService;

	@Autowired
	public AlgorithmServiceImpl(DataService dataService, OntologyTagService ontologyTagService,
			SemanticSearchService semanticSearchService, AlgorithmGeneratorService algorithmGeneratorService)
	{
		this.dataService = requireNonNull(dataService);
		this.ontologyTagService = requireNonNull(ontologyTagService);
		this.semanticSearchService = requireNonNull(semanticSearchService);
		this.algorithmGeneratorService = requireNonNull(algorithmGeneratorService);

		new RhinoConfig().init();
	}

	@Override
	public String generateAlgorithm(Attribute targetAttribute, EntityType targetEntityType,
			List<Attribute> sourceAttributes, EntityType sourceEntityType)
	{
		return algorithmGeneratorService
				.generate(targetAttribute, sourceAttributes, targetEntityType, sourceEntityType);
	}

	@Override
	@RunAsSystem
	public void autoGenerateAlgorithm(EntityType sourceEntityType, EntityType targetEntityType, EntityMapping mapping,
			Attribute targetAttribute)
	{
		LOG.debug("createAttributeMappingIfOnlyOneMatch: target= " + targetAttribute.getName());
		Multimap<Relation, OntologyTerm> tagsForAttribute = ontologyTagService
				.getTagsForAttribute(targetEntityType, targetAttribute);

		Map<Attribute, ExplainedAttribute> relevantAttributes = semanticSearchService
				.decisionTreeToFindRelevantAttributes(sourceEntityType, targetAttribute, tagsForAttribute.values(),
						null);
		GeneratedAlgorithm generatedAlgorithm = algorithmGeneratorService
				.generate(targetAttribute, relevantAttributes, targetEntityType, sourceEntityType);

		if (StringUtils.isNotBlank(generatedAlgorithm.getAlgorithm()))
		{
			AttributeMapping attributeMapping = mapping.addAttributeMapping(targetAttribute.getName());
			attributeMapping.setAlgorithm(generatedAlgorithm.getAlgorithm());
			attributeMapping.getSourceAttributes().addAll(generatedAlgorithm.getSourceAttributes());
			attributeMapping.setAlgorithmState(generatedAlgorithm.getAlgorithmState());
			LOG.debug("Creating attribute mapping: " + targetAttribute.getName() + " = " + generatedAlgorithm
					.getAlgorithm());
		}
	}

	@Override
	public Iterable<AlgorithmEvaluation> applyAlgorithm(Attribute targetAttribute, String algorithm,
			Iterable<Entity> sourceEntities)
	{
		final Collection<String> attributeNames = getSourceAttributeNames(algorithm);

		return Iterables.transform(sourceEntities, entity ->
		{
			AlgorithmEvaluation algorithmResult = new AlgorithmEvaluation(entity);

			Object derivedValue;
			Entity mapEntity = createEntity(attributeNames, entity); // why is this necessary?
			try
			{
				Object result = eval(algorithm, mapEntity, entity.getEntityType());
				derivedValue = convert(result, targetAttribute);
			}
			catch (RuntimeException e)
			{
				return algorithmResult.errorMessage(e.getMessage());
			}

			return algorithmResult.value(derivedValue);
		});
	}

	private Entity createEntity(Collection<String> attributeNames, Entity entity)
	{
		Entity mapEntity = new DynamicEntity(entity.getEntityType());
		for (String attributeName : attributeNames)
		{
			mapEntity.set(attributeName, entity.get(attributeName));
		}
		return mapEntity;
	}

	@Override
	public Object apply(AttributeMapping attributeMapping, Entity sourceEntity, EntityType sourceEntityType)
	{
		String algorithm = attributeMapping.getAlgorithm();
		if (isEmpty(algorithm))
		{
			return null;
		}

		Entity entity = createEntity(getSourceAttributeNames(attributeMapping.getAlgorithm()), sourceEntity);
		Object value = eval(algorithm, entity, sourceEntityType);
		return convert(value, attributeMapping.getTargetAttribute());
	}

	@SuppressWarnings("unchecked")
	private Object convert(Object value, Attribute attribute)
	{
		if (value == null)
		{
			return null;
		}
		Object convertedValue;
		AttributeType targetDataType = attribute.getDataType();
		try
		{
			switch (targetDataType)
			{
				case DATE:
				case DATE_TIME:
					convertedValue = jsToJava(value, Date.class);
					break;
				case BOOL:
					convertedValue = toBoolean(value);
					break;
				case INT:
					// Round it up or down to the nearest integer value
					convertedValue = toIntExact(round(parseDouble(Context.toString(value))));
					break;
				case LONG:
					convertedValue = round(parseDouble(Context.toString(value)));
					break;
				case DECIMAL:
					convertedValue = toNumber(value);
					break;
				case XREF:
				case CATEGORICAL:
					convertedValue = dataService
							.findOneById(attribute.getRefEntity().getName(), Context.toString(value));
					break;
				case MREF:
				case CATEGORICAL_MREF:
				{
					NativeArray mrefIds = (NativeArray) value;
					if (mrefIds != null && !mrefIds.isEmpty())
					{
						EntityType refEntityMeta = attribute.getRefEntity();
						convertedValue = dataService.findAll(refEntityMeta.getName(), mrefIds.stream())
								.collect(toList());
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
		Collection<String> result = emptyList();
		if (!isEmpty(algorithmScript))
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
		LinkedHashSet<String> result = newLinkedHashSet();
		Matcher matcher = Pattern.compile(patternString).matcher(algorithmScript);
		while (matcher.find())
		{
			result.add(matcher.group(1));
		}
		return result;
	}
}
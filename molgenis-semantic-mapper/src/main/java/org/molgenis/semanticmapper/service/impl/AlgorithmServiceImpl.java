package org.molgenis.semanticmapper.service.impl;

import com.google.common.collect.Multimap;
import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityManager;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.semantic.Relation;
import org.molgenis.js.magma.JsMagmaScriptEvaluator;
import org.molgenis.ontology.core.model.OntologyTerm;
import org.molgenis.script.core.ScriptException;
import org.molgenis.security.core.runas.RunAsSystem;
import org.molgenis.semanticmapper.algorithmgenerator.bean.GeneratedAlgorithm;
import org.molgenis.semanticmapper.algorithmgenerator.service.AlgorithmGeneratorService;
import org.molgenis.semanticmapper.mapping.model.AttributeMapping;
import org.molgenis.semanticmapper.mapping.model.EntityMapping;
import org.molgenis.semanticmapper.service.AlgorithmService;
import org.molgenis.semanticsearch.explain.bean.ExplainedAttribute;
import org.molgenis.semanticsearch.service.OntologyTagService;
import org.molgenis.semanticsearch.service.SemanticSearchService;
import org.molgenis.util.UnexpectedEnumException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.collect.Sets.newLinkedHashSet;
import static java.lang.Double.parseDouble;
import static java.lang.Math.round;
import static java.lang.Math.toIntExact;
import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.molgenis.data.DataConverter.toBoolean;
import static org.molgenis.data.meta.AttributeType.*;

public class AlgorithmServiceImpl implements AlgorithmService
{
	private static final Logger LOG = LoggerFactory.getLogger(AlgorithmServiceImpl.class);

	private static final int ENTITY_REFERENCE_FETCHING_DEPTH = 3;

	private final OntologyTagService ontologyTagService;
	private final SemanticSearchService semanticSearchService;
	private final AlgorithmGeneratorService algorithmGeneratorService;
	private final JsMagmaScriptEvaluator jsMagmaScriptEvaluator;
	private final EntityManager entityManager;

	public AlgorithmServiceImpl(OntologyTagService ontologyTagService, SemanticSearchService semanticSearchService,
			AlgorithmGeneratorService algorithmGeneratorService, EntityManager entityManager,
			JsMagmaScriptEvaluator jsMagmaScriptEvaluator)
	{
		this.ontologyTagService = requireNonNull(ontologyTagService);
		this.semanticSearchService = requireNonNull(semanticSearchService);
		this.algorithmGeneratorService = requireNonNull(algorithmGeneratorService);
		this.entityManager = requireNonNull(entityManager);
		this.jsMagmaScriptEvaluator = requireNonNull(jsMagmaScriptEvaluator);
	}

	@Override
	public String generateAlgorithm(Attribute targetAttribute, EntityType targetEntityType,
			List<Attribute> sourceAttributes, EntityType sourceEntityType)
	{
		return algorithmGeneratorService.generate(targetAttribute, sourceAttributes, targetEntityType,
				sourceEntityType);
	}

	@Override
	@RunAsSystem
	public void autoGenerateAlgorithm(EntityType sourceEntityType, EntityType targetEntityType, EntityMapping mapping,
			Attribute targetAttribute)
	{
		LOG.debug("createAttributeMappingIfOnlyOneMatch: target= " + targetAttribute.getName());
		Multimap<Relation, OntologyTerm> tagsForAttribute = ontologyTagService.getTagsForAttribute(targetEntityType,
				targetAttribute);

		Map<Attribute, ExplainedAttribute> relevantAttributes = semanticSearchService.decisionTreeToFindRelevantAttributes(
				sourceEntityType, targetAttribute, tagsForAttribute.values(), null);
		GeneratedAlgorithm generatedAlgorithm = algorithmGeneratorService.generate(targetAttribute, relevantAttributes,
				targetEntityType, sourceEntityType);

		if (StringUtils.isNotBlank(generatedAlgorithm.getAlgorithm()))
		{
			AttributeMapping attributeMapping = mapping.addAttributeMapping(targetAttribute.getName());
			attributeMapping.setAlgorithm(generatedAlgorithm.getAlgorithm());
			attributeMapping.getSourceAttributes().addAll(generatedAlgorithm.getSourceAttributes());
			attributeMapping.setAlgorithmState(generatedAlgorithm.getAlgorithmState());
			LOG.debug("Creating attribute mapping: " + targetAttribute.getName() + " = "
					+ generatedAlgorithm.getAlgorithm());
		}
	}

	@Override
	public Iterable<AlgorithmEvaluation> applyAlgorithm(Attribute targetAttribute, String algorithm,
			Iterable<Entity> sourceEntities)
	{
		return stream(sourceEntities.spliterator(), false).map(entity -> {
			AlgorithmEvaluation algorithmResult = new AlgorithmEvaluation(entity);
			Object derivedValue;

			try
			{
				Object result = jsMagmaScriptEvaluator.eval(algorithm, entity, ENTITY_REFERENCE_FETCHING_DEPTH);

				// jsMagmaScriptEvaluator.eval() catches and returns the error instead of throwing it
				// so check instance of result object here
				if (result instanceof ScriptException)
				{
					return algorithmResult.errorMessage(((ScriptException) result).getMessage());
				}

				derivedValue = convert(result, targetAttribute);
			}
			catch (RuntimeException e)
			{
				if (e.getMessage() == null)
				{
					return algorithmResult.errorMessage(
							"Applying an algorithm on a null source value caused an exception. Is the target attribute required?");
				}
				return algorithmResult.errorMessage(e.getMessage());
			}
			return algorithmResult.value(derivedValue);
		}).collect(toList());
	}

	@Override
	public Object apply(AttributeMapping attributeMapping, Entity sourceEntity, EntityType sourceEntityType)
	{
		String algorithm = attributeMapping.getAlgorithm();
		if (isEmpty(algorithm))
		{
			return null;
		}
		Object result = jsMagmaScriptEvaluator.eval(algorithm, sourceEntity, ENTITY_REFERENCE_FETCHING_DEPTH);

		// jsMagmaScriptEvaluator.eval() catches and returns the error instead of throwing it
		// so check instance of result object here
		if (result instanceof ScriptException)
		{
			throw new ScriptException(((ScriptException) result).getMessage(), ((ScriptException) result).getCause());
		}

		return convert(result, attributeMapping.getTargetAttribute());
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
			result.add(matcher.group(1).split("\\.")[0]);
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	private Object convert(Object value, Attribute attr)
	{
		Object convertedValue;
		AttributeType attrType = attr.getDataType();
		switch (attrType)
		{
			case BOOL:
				convertedValue = value != null ? toBoolean(value) : null;
				break;
			case CATEGORICAL:
			case XREF:
			case FILE:
				convertedValue = value != null ? entityManager.getReference(attr.getRefEntity(),
						convert(value, attr.getRefEntity().getIdAttribute())) : null;
				break;
			case CATEGORICAL_MREF:
			case MREF:
			case ONE_TO_MANY:
				Collection<Object> valueIds = (Collection<Object>) value;

				convertedValue = valueIds.stream()
										 .map(valueId -> entityManager.getReference(attr.getRefEntity(),
												 convert(valueId, attr.getRefEntity().getIdAttribute())))
										 .collect(toList());
				break;
			case DATE:
				convertedValue = convertToDate(value);
				break;
			case DATE_TIME:
				convertedValue = convertToDateTime(value);
				break;
			case DECIMAL:
				convertedValue = convertToDouble(value);
				break;
			case EMAIL:
			case ENUM:
			case HTML:
			case HYPERLINK:
			case SCRIPT:
			case STRING:
			case TEXT:
				convertedValue = value != null ? value.toString() : null;
				break;
			case INT:
				convertedValue = convertToInteger(value);
				break;
			case LONG:
				convertedValue = convertToLong(value);
				break;
			case COMPOUND:
				throw new RuntimeException(format("Illegal attribute type [%s]", attrType.toString()));
			default:
				throw new UnexpectedEnumException(attrType);
		}

		return convertedValue;
	}

	LocalDate convertToDate(Object value)
	{
		try
		{
			return value != null ? Instant.ofEpochMilli(Double.valueOf(value.toString()).longValue())
										  .atZone(ZoneId.systemDefault())
										  .toLocalDate() : null;
		}
		catch (NumberFormatException e)
		{
			LOG.debug("", e);
			throw new AlgorithmException(
					format("'%s' can't be converted to type '%s'", value.toString(), DATE.toString()));
		}
	}

	private Instant convertToDateTime(Object value)
	{
		try
		{
			return value != null ? Instant.ofEpochMilli(Double.valueOf(value.toString()).longValue()) : null;
		}
		catch (NumberFormatException e)
		{
			LOG.debug("", e);
			throw new AlgorithmException(
					format("'%s' can't be converted to type '%s'", value.toString(), DATE_TIME.toString()));
		}
	}

	private Double convertToDouble(Object value)
	{
		try
		{
			return value != null ? parseDouble(value.toString()) : null;
		}
		catch (NumberFormatException e)
		{
			LOG.debug("", e);
			throw new AlgorithmException(
					format("'%s' can't be converted to type '%s'", value.toString(), DECIMAL.toString()));
		}
	}

	private Integer convertToInteger(Object value)
	{
		Integer convertedValue;
		try
		{
			convertedValue = value != null ? toIntExact(round(parseDouble(value.toString()))) : null;
		}
		catch (NumberFormatException e)
		{
			LOG.debug("", e);
			throw new AlgorithmException(
					format("'%s' can't be converted to type '%s'", value.toString(), INT.toString()));
		}
		catch (ArithmeticException e)
		{
			LOG.debug("", e);
			throw new AlgorithmException(
					format("'%s' is larger than the maximum allowed value for type '%s'", value.toString(),
							INT.toString()));
		}
		return convertedValue;
	}

	private Long convertToLong(Object value)
	{
		Long convertedValue;
		try
		{
			convertedValue = value != null ? round(parseDouble(value.toString())) : null;
		}
		catch (NumberFormatException e)
		{
			LOG.debug("", e);
			throw new AlgorithmException(
					format("'%s' can't be converted to type '%s'", value.toString(), LONG.toString()));
		}
		return convertedValue;
	}
}
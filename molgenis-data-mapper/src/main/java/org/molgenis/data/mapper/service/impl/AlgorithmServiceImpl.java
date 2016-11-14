package org.molgenis.data.mapper.service.impl;

import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import org.apache.commons.lang3.StringUtils;
import org.molgenis.AttributeType;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityManager;
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
import org.molgenis.js.magma.JsMagmaScriptEvaluator;
import org.molgenis.ontology.core.model.OntologyTerm;
import org.molgenis.security.core.runas.RunAsSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Timestamp;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.google.common.collect.Sets.newLinkedHashSet;
import static java.lang.Double.parseDouble;
import static java.lang.Long.parseLong;
import static java.lang.Math.round;
import static java.lang.Math.toIntExact;
import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.molgenis.data.DataConverter.toBoolean;

public class AlgorithmServiceImpl implements AlgorithmService
{
	private static final Logger LOG = LoggerFactory.getLogger(AlgorithmServiceImpl.class);

	private final OntologyTagService ontologyTagService;
	private final SemanticSearchService semanticSearchService;
	private final AlgorithmGeneratorService algorithmGeneratorService;
	private final JsMagmaScriptEvaluator jsMagmaScriptEvaluator;
	private final EntityManager entityManager;

	@Autowired
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
		return Iterables.transform(sourceEntities, entity ->
		{
			AlgorithmEvaluation algorithmResult = new AlgorithmEvaluation(entity);

			Object derivedValue;
			try
			{
				Object result = derivedValue = jsMagmaScriptEvaluator.eval(algorithm, entity);
				derivedValue = convert(result, targetAttribute);
			}
			catch (RuntimeException e)
			{
				return algorithmResult.errorMessage(e.getMessage());
			}

			return algorithmResult.value(derivedValue);
		});
	}

	@Override
	public Object apply(AttributeMapping attributeMapping, Entity sourceEntity, EntityType sourceEntityType)
	{
		String algorithm = attributeMapping.getAlgorithm();
		if (isEmpty(algorithm))
		{
			return null;
		}
		Object value = jsMagmaScriptEvaluator.eval(algorithm, sourceEntity);
		return convert(value, attributeMapping.getTargetAttribute());
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
				convertedValue = value != null ? entityManager
						.getReference(attr.getRefEntity(), convert(value, attr.getRefEntity().getIdAttribute())) : null;
				break;
			case CATEGORICAL_MREF:
			case MREF:
			case ONE_TO_MANY:
				Collection<Object> valueIds = (Collection<Object>) value;

				convertedValue = valueIds.stream().map(valueId -> entityManager
						.getReference(attr.getRefEntity(), convert(valueId, attr.getRefEntity().getIdAttribute())))
						.collect(Collectors.toList());
				break;
			case DATE:
				convertedValue = value != null ? new Date(parseLong(value.toString())) : null;
				break;
			case DATE_TIME:
				convertedValue = value != null ? new Timestamp(parseLong(value.toString())) : null;
				break;
			case DECIMAL:
				convertedValue = value != null ? parseDouble(value.toString()) : null;
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
				convertedValue = value != null ? toIntExact(round(parseDouble(value.toString()))) : null;
				break;
			case LONG:
				convertedValue = value != null ? round(parseDouble(value.toString())) : null;
				break;
			case COMPOUND:
				throw new RuntimeException(format("Illegal attribute type [%s]", attrType.toString()));
			default:
				throw new RuntimeException(format("Unknown attribute type [%s]", attrType.toString()));
		}

		return convertedValue;
	}
}
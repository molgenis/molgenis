package org.molgenis.data.mapper.service.impl;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.molgenis.js.magma.JsMagmaScriptRegistrator.SCRIPT_TYPE_JAVASCRIPT_MAGMA;
import static org.molgenis.script.Script.ENTITY_NAME;
import static org.molgenis.script.Script.TYPE;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.semanticsearch.explain.bean.ExplainedQueryString;
import org.molgenis.data.semanticsearch.service.SemanticSearchService;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.script.Script;
import org.molgenis.script.ScriptParameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AlgorithmTemplateServiceImpl implements AlgorithmTemplateService
{
	private final DataService dataService;
	private final SemanticSearchService semanticSearchService;

	@Autowired
	public AlgorithmTemplateServiceImpl(DataService dataService, SemanticSearchService semanticSearchService)
	{
		this.dataService = checkNotNull(dataService);
		this.semanticSearchService = checkNotNull(semanticSearchService);
	}

	@Override
	public Stream<AlgorithmTemplate> find(AttributeMetaData targetAttr, EntityMetaData targetEntityMeta,
			EntityMetaData sourceEntityMeta)
	{
		// get all algorithm templates
		Iterable<Script> jsScripts = dataService.findAll(ENTITY_NAME,
				new QueryImpl().eq(TYPE, SCRIPT_TYPE_JAVASCRIPT_MAGMA), Script.class);

		// select all algorithm templates that can be used with target and sources
		return StreamSupport.stream(jsScripts.spliterator(), false)
				.flatMap(script -> toAlgorithmTemplate(script, targetAttr, targetEntityMeta, sourceEntityMeta));
	}

	private Stream<AlgorithmTemplate> toAlgorithmTemplate(Script script, AttributeMetaData targetAttr,
			EntityMetaData targetEntityMeta, EntityMetaData sourceEntityMeta)
	{
		// find source attributes related to target attribute
		Map<AttributeMetaData, Iterable<ExplainedQueryString>> attrs = semanticSearchService
				.findAttributes(sourceEntityMeta, targetEntityMeta, targetAttr);

		// find attribute for each parameter
		boolean paramMatch = true;
		Map<String, String> model = new HashMap<>();
		for (ScriptParameter param : script.getParameters())
		{
			AttributeMetaData attr = mapParamToAttribute(param, attrs);
			if (attr != null)
			{
				model.put(param.getName(), attr.getName());
			}
			else
			{
				paramMatch = false;
				break;
			}
		}

		// create algorithm template if an attribute was found for all parameters
		AlgorithmTemplate algorithmTemplate = new AlgorithmTemplate(script, model);

		return paramMatch ? Stream.of(algorithmTemplate) : Stream.empty();
	}

	private AttributeMetaData mapParamToAttribute(ScriptParameter param,
			Map<AttributeMetaData, Iterable<ExplainedQueryString>> attrs)
	{
		return attrs.entrySet().stream()
				.filter(entry -> StreamSupport.stream(entry.getValue().spliterator(), false)
						.anyMatch(explain -> explain.getTagName().equalsIgnoreCase(param.getName())))
				.map(entry -> entry.getKey()).findFirst().orElse(null);
	}
}

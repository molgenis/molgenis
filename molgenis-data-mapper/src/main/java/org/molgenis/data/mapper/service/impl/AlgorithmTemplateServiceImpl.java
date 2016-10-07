package org.molgenis.data.mapper.service.impl;

import org.molgenis.data.DataService;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.semanticsearch.explain.bean.ExplainedMatchCandidate;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.js.magma.JsMagmaScriptRunner;
import org.molgenis.script.Script;
import org.molgenis.script.ScriptParameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Objects.requireNonNull;
import static org.molgenis.script.ScriptMetaData.SCRIPT;
import static org.molgenis.script.ScriptMetaData.TYPE;

@Service
public class AlgorithmTemplateServiceImpl implements AlgorithmTemplateService
{
	private final DataService dataService;

	@Autowired
	public AlgorithmTemplateServiceImpl(DataService dataService)
	{
		this.dataService = requireNonNull(dataService);
	}

	@Override
	public Stream<AlgorithmTemplate> find(
			Map<AttributeMetaData, ExplainedMatchCandidate<AttributeMetaData>> attrMatches)
	{
		// get all algorithm templates
		Stream<Script> jsScripts = dataService
				.findAll(SCRIPT, new QueryImpl<Script>().eq(TYPE, JsMagmaScriptRunner.NAME), Script.class);

		// select all algorithm templates that can be used with target and sources
		return jsScripts.flatMap(script -> toAlgorithmTemplate(script, attrMatches));
	}

	private Stream<AlgorithmTemplate> toAlgorithmTemplate(Script script,
			Map<AttributeMetaData, ExplainedMatchCandidate<AttributeMetaData>> attrMatches)
	{
		// find attribute for each parameter
		boolean paramMatch = true;
		Map<String, String> model = new HashMap<>();
		for (ScriptParameter param : script.getParameters())
		{
			AttributeMetaData attr = mapParamToAttribute(param, attrMatches);
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
			Map<AttributeMetaData, ExplainedMatchCandidate<AttributeMetaData>> attrMatches)
	{
		return attrMatches.entrySet().stream().filter(entry -> !entry.getValue().getExplainedQueryStrings().isEmpty())
				.filter(entry -> StreamSupport.stream(entry.getValue().getExplainedQueryStrings().spliterator(), false)
						.allMatch(explain -> explain.getQueryString().equalsIgnoreCase(param.getName())))
				.map(entry -> entry.getKey()).findFirst().orElse(null);
	}
}

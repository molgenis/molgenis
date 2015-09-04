package org.molgenis.data.mapper.service.impl;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.regex.Pattern.CASE_INSENSITIVE;

import java.util.regex.Pattern;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.EntityMetaData;
import org.molgenis.script.Script;
import org.molgenis.script.ScriptParameter;

public class AlgorithmTemplate
{
	public static final Pattern PATTERN_MAGMA = Pattern
			.compile("^<#--\\s*?!magma\\s*?-->\\r?\\n<#--\\s*?target\\s*?:\\s*?(.*?)-->\\r?\\n.*", CASE_INSENSITIVE);;
	public static final Pattern PATTERN_TAG = Pattern.compile("tag:(.+?):(.+)", Pattern.CASE_INSENSITIVE);

	private final Script script;
	private final EntityMetaData entityMeta;
	private final AlgorithmTemplateServiceImpl algorithmTemplateService;

	public AlgorithmTemplate(Script script, EntityMetaData entityMeta,
			AlgorithmTemplateServiceImpl algorithmTemplateService)
	{
		this.script = checkNotNull(script);
		this.entityMeta = checkNotNull(entityMeta);
		this.algorithmTemplateService = checkNotNull(algorithmTemplateService);
	}

	public String render()
	{
		String content = script.getContent();
		for (ScriptParameter param : script.getParameters())
		{
			String paramName = param.getName();
			AttributeMetaData paramAttr = algorithmTemplateService.mapParamToAttr(entityMeta, paramName);
			if (paramAttr == null)
			{
				throw new RuntimeException("Unable to map param [" + paramName + "] to attribute of entity ["
						+ entityMeta.getName() + "]");
			}
			content = content.replaceAll(String.format("\\$\\('%s'\\)", paramName),
					String.format("\\$\\('%s'\\)", paramAttr.getName()));
		}
		return content;
	}
}

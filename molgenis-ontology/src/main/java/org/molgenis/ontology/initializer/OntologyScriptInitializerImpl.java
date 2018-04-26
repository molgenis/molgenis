package org.molgenis.ontology.initializer;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.script.core.*;
import org.molgenis.security.core.runas.RunAsSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.util.Arrays;

import static java.util.Objects.requireNonNull;
import static org.molgenis.script.core.ScriptMetaData.SCRIPT;
import static org.molgenis.script.core.ScriptParameterMetaData.SCRIPT_PARAMETER;

@Service
public class OntologyScriptInitializerImpl implements OntologyScriptInitializer
{
	private static final String ROC_CURVE_SCRIPT_NAME = "roc";
	private static final String ROC_CURVE_SCRIPT_PARAMETER = "filePath";
	private static final Logger LOG = LoggerFactory.getLogger(OntologyScriptInitializerImpl.class);

	private final DataService dataService;
	private final ScriptFactory scriptFactory;
	private final ScriptParameterFactory scriptParameterFactory;

	public OntologyScriptInitializerImpl(DataService dataService, ScriptFactory scriptFactory,
			ScriptParameterFactory scriptParameterFactory)
	{
		this.dataService = requireNonNull(dataService);
		this.scriptFactory = requireNonNull(scriptFactory);
		this.scriptParameterFactory = requireNonNull(scriptParameterFactory);
	}

	@Override
	@RunAsSystem
	public void initialize()
	{
		Resource resource = new ClassPathResource("roc-curve.R");
		if (resource.exists())
		{
			long count = dataService.count(SCRIPT, new QueryImpl<>().eq(ScriptMetaData.NAME, ROC_CURVE_SCRIPT_NAME));
			if (count == 0)
			{
				Entity scriptType = dataService.findOne(ScriptTypeMetaData.SCRIPT_TYPE,
						new QueryImpl<>().eq(ScriptTypeMetaData.NAME, "R"));

				if (scriptType == null) throw new UnknownEntityException("ScriptType R does not exist!");

				String scriptContent;
				try
				{
					scriptContent = FileCopyUtils.copyToString(
							new InputStreamReader(resource.getInputStream(), "UTF-8"));

				}
				catch (IOException e)
				{
					throw new UncheckedIOException(e);
				}

				if (dataService.count(SCRIPT_PARAMETER,
						new QueryImpl<>().eq(ScriptParameterMetaData.NAME, ROC_CURVE_SCRIPT_PARAMETER)) == 0)
				{
					dataService.add(SCRIPT_PARAMETER,
							scriptParameterFactory.create().setName(ROC_CURVE_SCRIPT_PARAMETER));
				}

				Entity scriptParameterEntity = dataService.findOne(SCRIPT_PARAMETER,
						new QueryImpl<>().eq(ScriptParameterMetaData.NAME, ROC_CURVE_SCRIPT_PARAMETER));

				Script script = scriptFactory.create();
				script.setName(ROC_CURVE_SCRIPT_NAME);
				script.setGenerateToken(true);
				script.set(ScriptMetaData.TYPE, scriptType);
				script.setResultFileExtension("png");
				script.setContent(scriptContent);
				script.set(ScriptMetaData.PARAMETERS, Arrays.asList(scriptParameterEntity));
				dataService.add(SCRIPT, script);

				LOG.info("Script entity \"roc\" has been added to the database!");
			}
			else
			{
				LOG.info("Script entity \"roc\" already exists in the database!");
			}
		}
		else
		{
			LOG.info("R script \"roc-curve.R\" does not exist on classpath!");
		}
	}
}

package org.molgenis.ontology.initializer;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.util.Arrays;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.script.Script;
import org.molgenis.script.ScriptParameter;
import org.molgenis.script.ScriptType;
import org.molgenis.security.core.runas.RunAsSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import com.google.common.collect.ImmutableMap;

@Service
public class OntologyScriptInitializerImpl implements OntologyScriptInitializer
{
	private final DataService dataService;
	private static final String ROC_CURVE_SCRIPT_NAME = "roc";
	private static final String ROC_CURVE_SCRIPT_PARAMETER = "filePath";
	private static final Logger LOG = LoggerFactory.getLogger(OntologyScriptInitializerImpl.class);

	@Autowired
	public OntologyScriptInitializerImpl(DataService dataService)
	{
		if (dataService == null) throw new IllegalArgumentException("DataService cannot be null");
		this.dataService = dataService;
	}

	@Override
	@RunAsSystem
	public void initialize()
	{
		Resource resource = new ClassPathResource("roc-curve.R");
		if (resource.exists())
		{
			long count = dataService.count(Script.ENTITY_NAME, new QueryImpl().eq(Script.NAME, ROC_CURVE_SCRIPT_NAME));
			if (count == 0)
			{
				Entity scriptType = dataService.findOne(ScriptType.ENTITY_NAME,
						new QueryImpl().eq(ScriptType.NAME, "R"));

				if (scriptType == null) throw new UnknownEntityException("ScriptType R does not exist!");

				String scriptContent;
				try
				{
					scriptContent = FileCopyUtils
							.copyToString(new InputStreamReader(resource.getInputStream(), "UTF-8"));

				}
				catch (IOException e)
				{
					throw new UncheckedIOException(e);
				}

				if (dataService.count(ScriptParameter.ENTITY_NAME,
						new QueryImpl().eq(ScriptParameter.NAME, ROC_CURVE_SCRIPT_PARAMETER)) == 0)
				{
					dataService.add(ScriptParameter.ENTITY_NAME,
							new MapEntity(ImmutableMap.of(ScriptParameter.NAME, ROC_CURVE_SCRIPT_PARAMETER)));
				}

				Entity scriptParameterEntity = dataService.findOne(ScriptParameter.ENTITY_NAME,
						new QueryImpl().eq(ScriptParameter.NAME, ROC_CURVE_SCRIPT_PARAMETER));

				MapEntity scriptEntity = new MapEntity();
				scriptEntity.set(Script.NAME, ROC_CURVE_SCRIPT_NAME);
				scriptEntity.set(Script.GENERATE_TOKEN, true);
				scriptEntity.set(Script.TYPE, scriptType);
				scriptEntity.set(Script.RESULT_FILE_EXTENSION, "png");
				scriptEntity.set(Script.CONTENT, scriptContent);
				scriptEntity.set(Script.PARAMETERS, Arrays.asList(scriptParameterEntity));
				dataService.add(Script.ENTITY_NAME, scriptEntity);

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

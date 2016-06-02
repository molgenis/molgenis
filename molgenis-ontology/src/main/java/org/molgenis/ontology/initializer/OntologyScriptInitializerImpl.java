package org.molgenis.ontology.initializer;

import static org.molgenis.script.ScriptMetaData.SCRIPT;
import static org.molgenis.script.ScriptParameterMetaData.SCRIPT_PARAMETER;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.util.Arrays;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.script.ScriptMetaData;
import org.molgenis.script.ScriptParameterMetaData;
import org.molgenis.script.ScriptTypeMetaData;
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
			long count = dataService
					.count(SCRIPT, new QueryImpl<Entity>().eq(ScriptMetaData.NAME, ROC_CURVE_SCRIPT_NAME));
			if (count == 0)
			{
				Entity scriptType = dataService.findOne(ScriptTypeMetaData.SCRIPT_TYPE,
						new QueryImpl<Entity>().eq(ScriptTypeMetaData.NAME, "R"));

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

				if (dataService.count(SCRIPT_PARAMETER,
						new QueryImpl<Entity>().eq(ScriptParameterMetaData.NAME, ROC_CURVE_SCRIPT_PARAMETER)) == 0)
				{
					dataService.add(SCRIPT_PARAMETER,
							new MapEntity(ImmutableMap.of(ScriptParameterMetaData.NAME, ROC_CURVE_SCRIPT_PARAMETER)));
				}

				Entity scriptParameterEntity = dataService.findOne(SCRIPT_PARAMETER,
						new QueryImpl<Entity>().eq(ScriptParameterMetaData.NAME, ROC_CURVE_SCRIPT_PARAMETER));

				MapEntity scriptEntity = new MapEntity();
				scriptEntity.set(ScriptMetaData.NAME, ROC_CURVE_SCRIPT_NAME);
				scriptEntity.set(ScriptMetaData.GENERATE_TOKEN, true);
				scriptEntity.set(ScriptMetaData.TYPE, scriptType);
				scriptEntity.set(ScriptMetaData.RESULT_FILE_EXTENSION, "png");
				scriptEntity.set(ScriptMetaData.CONTENT, scriptContent);
				scriptEntity.set(ScriptMetaData.PARAMETERS, Arrays.asList(scriptParameterEntity));
				dataService.add(SCRIPT, scriptEntity);

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

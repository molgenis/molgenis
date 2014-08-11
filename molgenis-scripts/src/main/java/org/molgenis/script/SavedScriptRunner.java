package org.molgenis.script;

import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.DataService;
import org.molgenis.data.mysql.MysqlRepositoryCollection;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.core.utils.SecurityUtils;
import org.molgenis.security.token.TokenService;
import org.molgenis.util.FileStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SavedScriptRunner
{
	private final ScriptRunnerFactory scriptRunnerFactory;
	private final DataService dataService;
	private final FileStore fileStore;
	private final TokenService tokenService;

	@Autowired
	public SavedScriptRunner(ScriptRunnerFactory scriptRunnerFactory, DataService dataService,
			MysqlRepositoryCollection mysqlRepositoryCollection, FileStore fileStore, TokenService tokenService)
	{
		this.scriptRunnerFactory = scriptRunnerFactory;
		this.dataService = dataService;
		this.fileStore = fileStore;
		this.tokenService = tokenService;
		mysqlRepositoryCollection.add(ScriptParameter.META_DATA);
		mysqlRepositoryCollection.add(Script.META_DATA);
	}

	public ScriptResult runScript(String scriptName, Map<String, Object> parameters)
	{
		Script script = dataService.findOne(Script.ENTITY_NAME, new QueryImpl().eq(Script.NAME, scriptName),
				Script.class);

		if (script == null)
		{
			throw new UnknownScriptException("Unknown script [" + scriptName + "]");
		}

		if (script.getParameters() != null)
		{
			for (String param : script.getParameters())
			{
				if (!parameters.containsKey(param))
				{
					throw new GenerateScriptException("Missing parameter [" + param + "]");
				}
			}
		}

		if (script.isGenerateToken())
		{
			String token = tokenService.generateAndStoreToken(SecurityUtils.getCurrentUsername(), "For script "
					+ script.getName());
			parameters.put("molgenisToken", token);
		}

		String outputFile = null;
		if (StringUtils.isNotEmpty(script.getResultFileExtension()))
		{
			String name = generateRandomString();
			outputFile = fileStore.getFile(name + "." + script.getResultFileExtension()).getAbsolutePath();
			parameters.put("outputFile", outputFile);
		}

		ScriptRunner scriptRunner = scriptRunnerFactory.getScriptRunner(script.getType());
		String output = scriptRunner.runScript(script, parameters);

		return new ScriptResult(outputFile, output);
	}

	private String generateRandomString()
	{
		return UUID.randomUUID().toString().replaceAll("-", "");
	}
}

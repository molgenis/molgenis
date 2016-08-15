package org.molgenis.script;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.DataService;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.file.FileStore;
import org.molgenis.security.core.token.TokenService;
import org.molgenis.security.core.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

import static org.molgenis.script.ScriptMetaData.SCRIPT;

/**
 * Runs a script.
 * <p>
 * Retrieve script from the database as freemarker template, render the script and return the result, script output
 * and/or the name of a generated outputfile
 * <p>
 * If the script requires a security token, a token is generated that is available to the script as a parameter named
 * 'molgenisToken'
 * <p>
 * If the script results in an outputfile (if script.resultFileExtension is not null) a ramdom name is generated with
 * the correct extension, this is available to the script as a parameter named 'outputFile'
 */
@Service
public class SavedScriptRunner
{
	private final ScriptRunnerFactory scriptRunnerFactory;
	private final DataService dataService;
	private final FileStore fileStore;
	private final TokenService tokenService;

	@Autowired
	public SavedScriptRunner(ScriptRunnerFactory scriptRunnerFactory, DataService dataService, FileStore fileStore,
			TokenService tokenService)
	{
		this.scriptRunnerFactory = scriptRunnerFactory;
		this.dataService = dataService;
		this.fileStore = fileStore;
		this.tokenService = tokenService;
	}

	/**
	 * Run a script with parameters
	 *
	 * @param scriptName
	 * @param parameters
	 * @return ScripResult
	 * @throws UnknownScriptException  if scriptName is unknown
	 * @throws GenerateScriptException , if parameter is missing
	 */
	public ScriptResult runScript(String scriptName, Map<String, Object> parameters)
	{
		Script script = dataService
				.findOne(SCRIPT, new QueryImpl<Script>().eq(ScriptMetaData.NAME, scriptName), Script.class);

		if (script == null)
		{
			throw new UnknownScriptException("Unknown script [" + scriptName + "]");
		}

		if (script.getParameters() != null)
		{
			for (ScriptParameter param : script.getParameters())
			{
				if (!parameters.containsKey(param.getName()))
				{
					throw new GenerateScriptException("Missing parameter [" + param + "]");
				}
			}
		}

		if (script.isGenerateToken())
		{
			String token = tokenService
					.generateAndStoreToken(SecurityUtils.getCurrentUsername(), "For script " + script.getName());
			parameters.put("molgenisToken", token);
		}

		String outputFile = null;
		if (StringUtils.isNotEmpty(script.getResultFileExtension()))
		{
			String name = generateRandomString();
			outputFile = fileStore.getFile(name + "." + script.getResultFileExtension()).getAbsolutePath();
			parameters.put("outputFile", outputFile);
		}

		ScriptRunner scriptRunner = scriptRunnerFactory.getScriptRunner(script.getScriptType().getName());
		String output = scriptRunner.runScript(script, parameters);

		return new ScriptResult(outputFile, output);
	}

	private String generateRandomString()
	{
		return UUID.randomUUID().toString().replaceAll("-", "");
	}
}

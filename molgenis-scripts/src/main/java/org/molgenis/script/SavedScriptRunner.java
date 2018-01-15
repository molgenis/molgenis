package org.molgenis.script;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.DataService;
import org.molgenis.data.file.FileStore;
import org.molgenis.data.file.model.FileMeta;
import org.molgenis.data.file.model.FileMetaFactory;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.script.core.*;
import org.molgenis.security.core.token.TokenService;
import org.molgenis.security.core.utils.SecurityUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Map;
import java.util.UUID;

import static java.net.URLConnection.guessContentTypeFromName;
import static java.text.MessageFormat.format;
import static org.molgenis.data.file.model.FileMetaMetaData.FILE_META;
import static org.molgenis.script.core.ScriptMetaData.SCRIPT;

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
	private final FileMetaFactory fileMetaFactory;

	public SavedScriptRunner(ScriptRunnerFactory scriptRunnerFactory, DataService dataService, FileStore fileStore,
			TokenService tokenService, FileMetaFactory fileMetaFactory)
	{
		this.scriptRunnerFactory = scriptRunnerFactory;
		this.dataService = dataService;
		this.fileStore = fileStore;
		this.tokenService = tokenService;
		this.fileMetaFactory = fileMetaFactory;
	}

	/**
	 * Run a script with parameters.
	 *
	 * @param scriptName name of the script to run
	 * @param parameters parameters for the script
	 * @return ScriptResult
	 * @throws UnknownScriptException  if scriptName is unknown
	 * @throws GenerateScriptException , if parameter is missing
	 */
	public ScriptResult runScript(String scriptName, Map<String, Object> parameters)
	{
		Script script = dataService.findOne(SCRIPT, new QueryImpl<Script>().eq(ScriptMetaData.NAME, scriptName),
				Script.class);

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
			String token = tokenService.generateAndStoreToken(SecurityUtils.getCurrentUsername(),
					"For script " + script.getName());
			parameters.put("molgenisToken", token);
		}

		FileMeta fileMeta = null;
		if (StringUtils.isNotEmpty(script.getResultFileExtension()))
		{
			String name = generateRandomString();
			File file = fileStore.getFile(name + "." + script.getResultFileExtension());
			parameters.put("outputFile", file.getAbsolutePath());
			fileMeta = createFileMeta(name, file);
			dataService.add(FILE_META, fileMeta);
		}

		ScriptRunner scriptRunner = scriptRunnerFactory.getScriptRunner(script.getScriptType().getName());

		String output = scriptRunner.runScript(script, parameters);

		return new ScriptResult(fileMeta, output);
	}

	private FileMeta createFileMeta(String fileMetaId, File file)
	{
		FileMeta fileMeta = fileMetaFactory.create(fileMetaId);
		fileMeta.setContentType(guessContentTypeFromName(file.getName()));
		fileMeta.setSize(file.length());
		fileMeta.setFilename(file.getName());
		fileMeta.setUrl(format("{0}/{1}", "/files", fileMetaId));
		return fileMeta;
	}

	private String generateRandomString()
	{
		return UUID.randomUUID().toString().replaceAll("-", "");
	}
}

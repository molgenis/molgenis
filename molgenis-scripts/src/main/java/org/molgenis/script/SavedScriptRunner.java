package org.molgenis.script;

import static java.net.URLConnection.guessContentTypeFromName;
import static java.text.MessageFormat.format;
import static org.molgenis.data.file.model.FileMetaMetadata.FILE_META;
import static org.molgenis.script.core.ScriptMetadata.SCRIPT;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.molgenis.data.DataService;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.file.FileStore;
import org.molgenis.data.file.model.FileMeta;
import org.molgenis.data.file.model.FileMetaFactory;
import org.molgenis.script.core.GenerateScriptException;
import org.molgenis.script.core.Script;
import org.molgenis.script.core.ScriptMetadata;
import org.molgenis.script.core.ScriptParameter;
import org.molgenis.script.core.ScriptRunner;
import org.molgenis.script.core.ScriptRunnerFactory;
import org.molgenis.script.core.UnknownScriptException;
import org.molgenis.security.core.token.TokenService;
import org.molgenis.security.core.utils.SecurityUtils;
import org.springframework.stereotype.Service;

/**
 * Runs a script.
 *
 * <p>Retrieve script from the database as freemarker template, render the script and return the
 * result, script output and/or the name of a generated outputfile
 *
 * <p>If the script requires a security token, a token is generated that is available to the script
 * as a parameter named 'molgenisToken'
 *
 * <p>If the script results in an outputfile (if script.resultFileExtension is not null) a ramdom
 * name is generated with the correct extension, this is available to the script as a parameter
 * named 'outputFile'
 */
@Service
public class SavedScriptRunner {
  private final ScriptRunnerFactory scriptRunnerFactory;
  private final DataService dataService;
  private final FileStore fileStore;
  private final TokenService tokenService;
  private final FileMetaFactory fileMetaFactory;

  public SavedScriptRunner(
      ScriptRunnerFactory scriptRunnerFactory,
      DataService dataService,
      FileStore fileStore,
      TokenService tokenService,
      FileMetaFactory fileMetaFactory) {
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
   * @throws UnknownScriptException if scriptName is unknown
   * @throws GenerateScriptException , if parameter is missing
   */
  public ScriptResult runScript(String scriptName, Map<String, Object> parameters) {
    Script script =
        dataService.query(SCRIPT, Script.class).eq(ScriptMetadata.NAME, scriptName).findOne();
    if (script == null) {
      throw new UnknownEntityException(SCRIPT, scriptName);
    }

    if (script.getParameters() != null) {
      for (ScriptParameter param : script.getParameters()) {
        if (!parameters.containsKey(param.getName())) {
          throw new GenerateScriptException("Missing parameter [" + param + "]");
        }
      }
    }

    Map<String, Object> scriptParameters = new HashMap<>(parameters);
    if (script.isGenerateToken()) {
      String token =
          tokenService.generateAndStoreToken(
              SecurityUtils.getCurrentUsername(), "For script " + script.getName());
      scriptParameters.put("molgenisToken", token);
    }

    ScriptRunner scriptRunner =
        scriptRunnerFactory.getScriptRunner(script.getScriptType().getName());

    FileMeta fileMeta = null;
    if (scriptRunner.hasFileOutput(script)) {
      String name = generateRandomString();
      String resultFileExtension = script.getResultFileExtension();
      if (resultFileExtension != null) {
        name += "." + script.getResultFileExtension();
      }
      File file = fileStore.getFileUnchecked(name);
      scriptParameters.put("outputFile", file.getAbsolutePath());
      fileMeta = createFileMeta(name, file);
      dataService.add(FILE_META, fileMeta);
    }
    String output = scriptRunner.runScript(script, scriptParameters);

    return new ScriptResult(fileMeta, output);
  }

  private FileMeta createFileMeta(String fileMetaId, File file) {
    FileMeta fileMeta = fileMetaFactory.create(fileMetaId);
    fileMeta.setContentType(guessContentTypeFromName(file.getName()));
    fileMeta.setSize(file.length());
    fileMeta.setFilename(file.getName());
    fileMeta.setUrl(format("{0}/{1}", "/files", fileMetaId));
    return fileMeta;
  }

  private String generateRandomString() {
    return UUID.randomUUID().toString().replaceAll("-", "");
  }
}

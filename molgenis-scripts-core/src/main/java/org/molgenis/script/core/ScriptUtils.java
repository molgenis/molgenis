package org.molgenis.script.core;

import static java.util.Collections.emptyMap;

import freemarker.core.InvalidReferenceException;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.Version;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class ScriptUtils {
  private static final Version VERSION = Configuration.VERSION_2_3_21;

  private ScriptUtils() {}

  /** Render a script using the given parameter values */
  public static String generateScript(Script script, Map<String, Object> parameterValues) {
    StringWriter stringWriter = new StringWriter();
    try {
      Template template =
          new Template(null, new StringReader(script.getContent()), new Configuration(VERSION));
      template.process(parameterValues, stringWriter);
    } catch (TemplateException | IOException e) {
      throw new GenerateScriptException(
          "Error processing parameters for script [" + script.getName() + "]. " + e.getMessage());
    }
    return stringWriter.toString();
  }

  public static boolean hasScriptVariable(Script script, String variable) {
    return getScriptVariables(script).contains(variable);
  }

  /** @return script variables */
  public static Set<String> getScriptVariables(Script script) {
    // based on https://stackoverflow.com/a/48024379
    Set<String> scriptExpressions = new LinkedHashSet<>();
    Configuration configuration = new Configuration(VERSION);
    configuration.setTemplateExceptionHandler(
        (te, env, out) -> {
          if (te instanceof InvalidReferenceException) {
            scriptExpressions.add(te.getBlamedExpressionString());
            return;
          }
          throw te;
        });

    try {
      Template template = new Template(null, new StringReader(script.getContent()), configuration);
      template.process(emptyMap(), new StringWriter());
    } catch (TemplateException | IOException e) {
      throw new GenerateScriptException(
          "Error processing parameters for script [" + script.getName() + "]. " + e.getMessage());
    }
    return scriptExpressions;
  }
}

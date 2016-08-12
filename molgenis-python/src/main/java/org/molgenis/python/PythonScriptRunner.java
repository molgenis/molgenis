package org.molgenis.python;

import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.molgenis.file.FileStore;
import org.molgenis.script.Script;
import org.molgenis.script.ScriptRunner;
import org.molgenis.script.ScriptRunnerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.UUID;

@Service
public class PythonScriptRunner implements ScriptRunner
{
	private static final String NAME = "python";

	private static final Charset CHARSET = Charset.forName("utf-8");
	private final PythonScriptExecutor pythonScriptExecutor;
	private final FileStore fileStore;
	private final FreeMarkerConfigurer freeMarkerConfig;

	@Autowired
	public PythonScriptRunner(PythonScriptExecutor pythonScriptExecutor, FileStore fileStore,
			FreeMarkerConfigurer freeMarkerConfig, ScriptRunnerFactory scriptRunnerFactory)
	{
		this.pythonScriptExecutor = pythonScriptExecutor;
		this.fileStore = fileStore;
		this.freeMarkerConfig = freeMarkerConfig;
	}

	/**
	 * Run a Python script as freemarker template
	 *
	 * @param templateName
	 * @param parameters
	 * @throws IOException
	 * @throws TemplateException
	 */
	public void runPythonScript(String templateName, Map<String, Object> parameters, PythonOutputHandler outputHandler)
			throws IOException, TemplateException
	{
		String scriptName = generateRandomPythonScriptName();
		runPythonScript(scriptName, templateName, parameters, outputHandler);
	}

	/**
	 * Run an Python script as freemarker template
	 *
	 * @param scriptName
	 * @param templateName
	 * @param parameters
	 * @throws IOException
	 * @throws TemplateException
	 */
	public void runPythonScript(String scriptName, String templateName, Map<String, Object> parameters,
			PythonOutputHandler outputHandler) throws IOException, TemplateException
	{
		File pythonScriptFile = fileStore.getFile(scriptName);

		Template template = freeMarkerConfig.getConfiguration().getTemplate(templateName);
		Writer w = new FileWriterWithEncoding(pythonScriptFile, CHARSET);

		try
		{
			template.process(parameters, w);
		}
		finally
		{
			IOUtils.closeQuietly(w);
		}

		pythonScriptExecutor.executeScript(pythonScriptFile, outputHandler);
	}

	/**
	 * Run an Python script as string
	 *
	 * @param script
	 * @param outputHandler
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public void runPythonScript(String script, PythonOutputHandler outputHandler)
			throws FileNotFoundException, IOException
	{
		String scriptName = generateRandomPythonScriptName();
		File file = fileStore.getFile(scriptName);
		FileCopyUtils.copy(script, new OutputStreamWriter(new FileOutputStream(file), CHARSET));

		pythonScriptExecutor.executeScript(file, outputHandler);
	}

	@Override
	public String getName()
	{
		return NAME;
	}

	@Override
	public String runScript(Script script, Map<String, Object> parameters)
	{
		File pythonScriptFile = script.generateScript(fileStore, "python", parameters);

		StringPythonOutputHandler handler = new StringPythonOutputHandler();
		runPythonScript(pythonScriptFile, handler);

		return handler.toString();
	}

	/**
	 * Run an Python script
	 *
	 * @param script
	 * @param outputHandler
	 */
	public void runPythonScript(File script, PythonOutputHandler outputHandler)
	{
		pythonScriptExecutor.executeScript(script, outputHandler);
	}

	private String generateRandomPythonScriptName()
	{
		return UUID.randomUUID().toString().replaceAll("-", "") + ".python";
	}

}

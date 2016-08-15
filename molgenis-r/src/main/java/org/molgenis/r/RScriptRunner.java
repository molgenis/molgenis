package org.molgenis.r;

import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.molgenis.file.FileStore;
import org.molgenis.script.Script;
import org.molgenis.script.ScriptRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.UUID;

@Service
public class RScriptRunner implements ScriptRunner
{
	private static final String NAME = "R";

	private static final Charset CHARSET = Charset.forName("utf-8");
	private final RScriptExecutor rScriptExecutor;
	private final FileStore fileStore;
	private final FreeMarkerConfigurer freeMarkerConfig;

	@Autowired
	public RScriptRunner(RScriptExecutor rScriptExecutor, FileStore fileStore, FreeMarkerConfigurer freeMarkerConfig)
	{
		this.rScriptExecutor = rScriptExecutor;
		this.fileStore = fileStore;
		this.freeMarkerConfig = freeMarkerConfig;
	}

	/**
	 * Run an R script as freemarker template
	 *
	 * @param templateName
	 * @param parameters
	 * @throws IOException
	 * @throws TemplateException
	 */
	public void runRScript(String templateName, Map<String, Object> parameters, ROutputHandler outputHandler)
			throws IOException, TemplateException
	{
		String scriptName = generateRandomRScriptName();
		runRScript(scriptName, templateName, parameters, outputHandler);
	}

	/**
	 * Run an R script as freemarker template
	 *
	 * @param scriptName
	 * @param templateName
	 * @param parameters
	 * @throws IOException
	 * @throws TemplateException
	 */
	public void runRScript(String scriptName, String templateName, Map<String, Object> parameters,
			ROutputHandler outputHandler) throws IOException, TemplateException
	{
		File rScriptFile = fileStore.getFile(scriptName);

		Template template = freeMarkerConfig.getConfiguration().getTemplate(templateName);
		Writer w = new FileWriterWithEncoding(rScriptFile, CHARSET);

		try
		{
			template.process(parameters, w);
		}
		finally
		{
			IOUtils.closeQuietly(w);
		}

		rScriptExecutor.executeScript(rScriptFile, outputHandler);
	}

	/**
	 * Run an R script as string
	 *
	 * @param script
	 * @param outputHandler
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public void runRScript(String script, ROutputHandler outputHandler) throws FileNotFoundException, IOException
	{
		String scriptName = generateRandomRScriptName();
		File file = fileStore.getFile(scriptName);
		FileCopyUtils.copy(script, new OutputStreamWriter(new FileOutputStream(file), CHARSET));

		rScriptExecutor.executeScript(file, outputHandler);
	}

	@Override
	public String getName()
	{
		return NAME;
	}

	@Override
	public String runScript(Script script, Map<String, Object> parameters)
	{
		File rScriptFile = script.generateScript(fileStore, "R", parameters);

		StringROutputHandler handler = new StringROutputHandler();
		runRScript(rScriptFile, handler);

		return handler.toString();
	}

	/**
	 * Run an R script
	 *
	 * @param script
	 * @param outputHandler
	 */
	public void runRScript(File script, ROutputHandler outputHandler)
	{
		rScriptExecutor.executeScript(script, outputHandler);
	}

	private String generateRandomRScriptName()
	{
		return UUID.randomUUID().toString().replaceAll("-", "") + ".R";
	}

}

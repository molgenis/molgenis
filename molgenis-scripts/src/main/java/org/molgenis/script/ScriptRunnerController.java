package org.molgenis.script;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ScriptRunnerController
{
	private final SavedScriptRunner savedScriptRunner;

	@Autowired
	private ScriptRunnerController(SavedScriptRunner savedScriptRunner)
	{
		this.savedScriptRunner = savedScriptRunner;
	}

	@RequestMapping("/scripts/{name}/run")
	public void runScript(@PathVariable("name") String scriptName, @RequestParam Map<String, Object> parameters,
			HttpServletResponse response) throws IOException
	{
		String outputFile = savedScriptRunner.runScript(scriptName, parameters);
		if (outputFile != null)
		{
			File f = new File(outputFile);
			if (f.exists())
			{
				String guessedContentType = URLConnection.guessContentTypeFromName(f.getName());
				if (guessedContentType != null)
				{
					response.setContentType(guessedContentType);
				}

				FileCopyUtils.copy(new FileInputStream(f), response.getOutputStream());
			}
		}
	}

	@ExceptionHandler(GenerateScriptException.class)
	public void handleGenerateScriptException(GenerateScriptException e, HttpServletResponse response)
			throws IOException
	{
		response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
	}
}

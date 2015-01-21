package org.molgenis.compute.ui.clusterexecutor;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.molgenis.compute.ui.model.AnalysisJob;
import org.molgenis.security.token.MolgenisToken;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class ClusterCurlBuilder
{
	private static final String CALLBACK_URI = "callbackUri";
	private static final String BACKEND_HOST = "backend";
	private static final String JOB_ID = "jobid";
	private static final String JOB_NAME = "jobname";
	private static final String TOKEN = "token";

	public String buildScript(MolgenisToken token, AnalysisJob analysisJob, String callbackUri)
	{
		Map<String, String> model = new HashMap<String, String>();
		model.put(CALLBACK_URI, callbackUri);
		model.put(BACKEND_HOST, analysisJob.getAnalysis().getBackend().getHost());
		model.put(JOB_ID, analysisJob.getIdentifier());
		model.put(JOB_NAME, analysisJob.getName());
		model.put(TOKEN, token.getToken());

		String curlHeaderTemplate = analysisJob.getAnalysis().getBackend().getHeaderCallback();
		String curlFooterTemplate = analysisJob.getAnalysis().getBackend().getFooterCallback();

		String prefix = renderTemplate(curlHeaderTemplate, model);
		String postfix = renderTemplate(curlFooterTemplate, model);

		// insert the prefix after generated script header
		// insert the postfix after generated script
		String script = analysisJob.getGenerateScript();
		String lookup = analysisJob.getName() + ".sh.started";
		int index = script.indexOf(lookup);
		String top = script.substring(0, index + lookup.length() + 2);
		String bottom = script.substring(index + lookup.length() + 3);

		return new StringBuilder().append(top).append(prefix).append("\n\n")
				.append(bottom).append(postfix).toString();
	}

	private String renderTemplate(String strTemplate, Map<String, String> model)
	{
		StringWriter writer = new StringWriter();
		try
		{
			new Template("name", strTemplate, new Configuration(Configuration.VERSION_2_3_21)).process(model, writer);
		}
		catch (TemplateException | IOException e)
		{
			throw new RuntimeException(e);
		}
		return writer.toString();
	}
}

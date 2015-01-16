package org.molgenis.compute.ui.clusterexecutor;

import java.io.*;
import java.util.Hashtable;
import java.util.Properties;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.molgenis.compute.ui.model.AnalysisJob;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by hvbyelas on 5/19/14.
 */
public class ClusterCurlBuilder
{

	private static final String JOB_ID = "jobid";
	private static final String JOB_NAME = "jobname";

	private String curlHeaderTemplate;
	private String curlFooterTemplate;
	private Hashtable<String, String> values = null;

	public String buildScript(AnalysisJob analysisJob)
	{
		readTemplates();

		StringBuilder sb = new StringBuilder();

		values = new Hashtable<String, String>();
		//
		readServerProperties();

		values.put(JOB_ID, analysisJob.getIdentifier());

		values.put(JOB_NAME, analysisJob.getName());


		String prefix = weaveFreemarker(curlHeaderTemplate, values);
		String postfix = weaveFreemarker(curlFooterTemplate, values);

		String script = analysisJob.getGenerateScript();

		String lookup = analysisJob.getName() + ".sh.started";

		int index = script.indexOf(lookup);
		String top = script.substring(0, index + lookup.length() + 2);
		String bottom = script.substring(index + lookup.length() + 3);

		sb.append(top);
		sb.append(prefix);
		sb.append(bottom);
		sb.append(postfix);

		return sb.toString();
	}

	private void readTemplates()
	{
		try
		{
			curlHeaderTemplate = FileUtils.readFileToString(new File("templates/cluster/header.ftl"));
			curlFooterTemplate = FileUtils.readFileToString(new File("templates/cluster/footer.ftl"));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

	}

	private void readServerProperties()
	{
		Properties prop = new Properties();
		InputStream input = null;

		try
		{
			input = new FileInputStream(".cluster.properties");
			prop.load(input);

			values.put("IP", prop.getProperty(ClusterManager.SERVER_IP));
			values.put("PORT", prop.getProperty(ClusterManager.SERVER_PORT));

			values.put(ClusterManager.BACKEND, prop.getProperty(ClusterManager.URL));
 		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}
		finally
		{
			if (input != null)
			{
				try
				{
					input.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	public static String weaveFreemarker(String strTemplate, Hashtable<String, String> values)
	{
		Configuration cfg = new Configuration();

		Template t = null;
		StringWriter out = new StringWriter();
		try
		{
			t = new Template("name", new StringReader(strTemplate), cfg);
			t.process(values, out);
		}
		catch (TemplateException e)
		{
			//e.printStackTrace();
		}
		catch (IOException e)
		{
			//e.printStackTrace();
		}

		return out.toString();
	}



}

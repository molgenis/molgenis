package org.molgenis.compute5.generators;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.Writer;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.molgenis.compute5.ComputeProperties;
import org.molgenis.compute5.model.Task;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/** Parameters of the backend, either PBS, SGE, GRID, etc */
public class BackendGenerator
{
	private String headerTemplate;
	private String footerTemplate;
	private String submitTemplate;

	public BackendGenerator(String headerTemplate, String footerTemplate, String submitTemplate) throws IOException
	{

		this.setHeaderTemplate(readFile(headerTemplate));
		this.setFooterTemplate(readFile(footerTemplate));
		this.setSubmitTemplate(readFile(submitTemplate));

	}

	private String readFile(String file) throws IOException
	{
		URL header = this.getClass().getResource(file);
		if (header == null) throw new IOException("file " + file + " is missing for backend "
				+ this.getClass().getSimpleName());

		BufferedReader stream = new BufferedReader(new InputStreamReader(header.openStream()));

		StringBuilder result = new StringBuilder();
		try
		{

			String inputLine;

			while ((inputLine = stream.readLine()) != null)
				result.append(inputLine + "\n");
		}
		finally
		{
			stream.close();
		}
		return result.toString();
	}

	public void generate(List<Task> tasks, File targetDir) throws IOException
	{
		Configuration conf = new Configuration();

		// get templates for header and footer
		Template header = new Template("header", new StringReader(this.getHeaderTemplate()), conf);
		Template footer = new Template("footer", new StringReader(this.getFooterTemplate()), conf);
		Template submit = new Template("submit", new StringReader(this.getSubmitTemplate()), conf);

		// generate the submit script
		try
		{
			File outFile = new File(targetDir.getAbsolutePath() + "/submit.sh");
			Writer out = new BufferedWriter(new FileWriter(outFile));

			Map<String, Object> taskMap = new HashMap<String, Object>();
			taskMap.put("tasks", tasks);
			
			submit.process(taskMap, out);

			out.close();
			
			System.out.println("Generated " + outFile);
		}
		catch (TemplateException e)
		{
			throw new IOException("Backend generation failed for " + this.getClass().getSimpleName() + "\n\nError is:\n" + e.toString());
		}

		// generate the tasks scripts
		for (Task task : tasks)
		{
			try
			{
				File outFile = new File(targetDir.getAbsolutePath() + "/" + task.getName() + ".sh");
				Writer out = new BufferedWriter(new FileWriter(outFile));

				header.process(task.getParameters(), out);

				out.write("\n" + task.getScript() + "\n");

				footer.process(task.getParameters(), out);

				out.close();

				System.out.println("Generated " + outFile);
			}
			catch (TemplateException e)
			{
				throw new IOException("Backend generation of task '" + task.getName() + "' failed for "
						+ this.getClass().getSimpleName() + ": " + e.getMessage());
			}
		}
	}

	public String getHeaderTemplate()
	{
		return headerTemplate;
	}

	public void setHeaderTemplate(String headerTemplate)
	{
		this.headerTemplate = headerTemplate;
	}

	public void appendCustomHeader(String customHeader)
	{
		this.headerTemplate += "\n" + customHeader;
	}
	
	public String getFooterTemplate()
	{
		return footerTemplate;
	}

	public void setFooterTemplate(String footerTemplate)
	{
		this.footerTemplate = footerTemplate;
	}
	
	public void appendCustomFooter(String customFooter)
	{
		this.footerTemplate += "\n" + customFooter;
	}

	public String getSubmitTemplate()
	{
		return submitTemplate;
	}

	public void setSubmitTemplate(String submitTemplate)
	{
		this.submitTemplate = submitTemplate;
	}

	public BackendGenerator(ComputeProperties cp) throws IOException
	{
		this.setHeaderTemplate(readFile("header.ftl"));
		this.setFooterTemplate(readFile("footer.ftl"));
		this.setSubmitTemplate(readFile("submit.ftl"));


		if (cp.customHeader != null)
		{
			File h = new File(cp.customHeader);
			if (h.exists())
			{
				System.out.println(">> Custom header: " + h);
				this.appendCustomHeader(FileUtils.readFileToString(h));
			}
			else
				System.out.println(">> Custom header not found (" + h + ")");

		}

		if(cp.customFooter != null)
		{
			File f = new File(cp.customFooter);
			if (f.exists())
			{
				System.out.println(">> Custom footer: " + f);
				this.appendCustomFooter(FileUtils.readFileToString(f));
			}
			else
				System.out.println(">> Custom footer not found (" + f + ")");

		}

		if(cp.customSubmit != null)
		{
			File s = new File(cp.customSubmit);
			if (s.exists())
			{
				System.out.println(">> Custom submit script: " + s);
				this.setSubmitTemplate(FileUtils.readFileToString(s));
			}
			else
				System.out.println(">> Custom submit script not found (" + s + ")");

		}

	}
}

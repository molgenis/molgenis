package org.molgenis.compute5.generators;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.molgenis.compute5.model.Task;

/** Generates graphvis diagram */
public class DocTasksDiagramGenerator
{
	public void generate(File dir, List<Task> tasks) throws IOException
	{
		try
		{
			// model
			Map<String, Object> model = new LinkedHashMap<String, Object>();
			model.put("tasks", tasks);

			// apply
			File dotFile = new File(dir.getAbsoluteFile() + "/tasks.dot");
			new FreemarkerUtils().applyTemplate(model, "DocTasksDiagramGenerator.ftl", dotFile);
			System.out.println("Generated "+dotFile);
			
			GraphvizUtils.executeDot(dotFile, "png", true);
			
			
		}
		catch (Exception e)
		{
			throw new IOException("Task diagram generation failed: " + e.getMessage());
		}
	}
}

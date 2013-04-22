package org.molgenis.compute.db.commandline;

import java.io.IOException;

import org.molgenis.compute.db.importer.WorkflowImporter;
import org.molgenis.framework.db.DatabaseException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class WorkflowImport
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		ApplicationContext ctx = new AnnotationConfigApplicationContext(AppConfig.class);
		WorkflowImporter importer = ctx.getBean(WorkflowImporter.class);
		try
		{
			importer.importWorkflowFile(
					"/Users/erwin/projects/molgenis/molgenis-compute-core/src/main/resources/workflows/invitation/workflow.csv",
					"invitation");
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (DatabaseException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}

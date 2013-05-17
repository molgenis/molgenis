package org.molgenis.compute5.generators;

import java.io.File;
import java.io.IOException;

import org.molgenis.compute5.model.Parameters;

public class CreateWorkflowGenerator
{

	public CreateWorkflowGenerator(String createWorkflowDir)
	{
		// create dir 
		new File(createWorkflowDir).mkdirs();

		// TODO: copy folder 'myworkflow' to 'createWorkflow'
		System.err.println("TODO: copy folder 'myworkflow' to 'createWorkflow'");
		
		//CreateWorkflowGenerator.class.getResource("CreateWorkflowGenerator").getPath();
		
	}

}

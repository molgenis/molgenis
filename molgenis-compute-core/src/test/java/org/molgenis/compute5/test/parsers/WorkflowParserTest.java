package org.molgenis.compute5.test.parsers;

import java.io.IOException;


import org.molgenis.compute5.model.Workflow;
import org.molgenis.compute5.parsers.WorkflowCsvParser;
import org.testng.annotations.Test;


public class WorkflowParserTest {

	@Test
	public void test1() throws IOException {
		String workflow = "workflows/copyFile/workflow.csv";

		Workflow result = WorkflowCsvParser.parse(workflow);

		System.out.println(result);
		
		System.out.println("user params=" + result.getUserParameters());
		
		
	}
}

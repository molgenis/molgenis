package org.molgenis.compute.db.pilot;

import static org.testng.Assert.assertEquals;

import org.molgenis.compute.runtime.ComputeRun;
import org.molgenis.compute.runtime.ComputeTask;
import org.testng.annotations.Test;

public class ScriptBuilderTest
{
	@Test
	public void build()
	{
		ComputeRun run = new ComputeRun();
		run.setName("run1");

		ComputeTask prev = new ComputeTask();
		prev.setName("task2");
		prev.setComputeRun(run);

		ComputeTask task = new ComputeTask();
		task.setName("task1");
		task.setComputeScript("echo hallo");
		task.setComputeRun(run);
		task.setPrevSteps(prev);

		ScriptBuilder builder = new ScriptBuilder("username", "password");

		String expected = "echo TASKNAME:task1\n"
				+ "echo RUNNAME:run1\n"
				+ "curl -s -S -u username:password -o user.env http://localhost/environment/run1/user.env\n"
				+ "curl -s -S -u username:password -o task2.env http://localhost/environment/run1/task2.env\n"
				+ "echo hallo\n"
				+ "cp log.log done.log\n"
				+ "if [ -f task1.env ]; then\n"
				+ "curl -s -S -u username:password -F status=done -F pilotid=xxx -F log_file=@done.log -F output_file=@task1.env http://localhost/api/pilot\n"
				+ "else\n"
				+ "curl -s -S -u username:password -F status=done -F pilotid=xxx -F log_file=@done.log http://localhost/api/pilot\n"
				+ "fi\n" + "rm user.env\n" + "rm task2.env\n" + "rm task1.env\n";

		assertEquals(builder.build(task, "http://localhost", "/api/pilot", "xxx"), expected);

	}
}

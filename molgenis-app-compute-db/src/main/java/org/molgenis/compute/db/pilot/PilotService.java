package org.molgenis.compute.db.pilot;

import org.apache.commons.io.FileUtils;
import org.molgenis.compute.runtime.ComputeServer;
import org.molgenis.compute.runtime.ComputeTask;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.server.MolgenisContext;
import org.molgenis.framework.server.MolgenisRequest;
import org.molgenis.framework.server.MolgenisResponse;
import org.molgenis.framework.server.MolgenisService;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

/**
 * Created with IntelliJ IDEA. User: georgebyelas Date: /usr/local/mysql/bin/mysql -u molgenis -pmolgenis compute < $HOME/compute.sql
 20/07/2012 Time: 16:53
 * To change this template use File | Settings | File Templates.
 */
public class PilotService implements MolgenisService
{

    // TODO IMPORTANT: in the new version of maverick http://localhost:8080/api/pilot without compute/

	public PilotService(MolgenisContext mc)
	{
		// super(mc);
	}

	public synchronized void handleRequest(MolgenisRequest request, MolgenisResponse response) throws ParseException,
			DatabaseException, IOException
	{
		System.out.println(">> In handleRequest!");
		System.out.println(request);

		if ("started".equals(request.getString("status")))
		{
            String backend = request.getString("backend");
			System.out.println(">>> looking for a task to execute at " + backend);

//            ComputeTask task = request.getDatabase().query(ComputeTask.class).eq(ComputeTask.STATUSCODE, "ready")
//         					.limit(1).find().get(0);

            //TODO it would be nice to move this query to the constructor, since it should happen once
            ComputeServer server = request.getDatabase().query(ComputeServer.class).eq(ComputeServer.NAME, "default").limit(1).find().get(0);

            //curl call back statement in the end of the analysis script
            //pulse calls are present in the pilot job, final call back in actual analysis script
            String CURL_DONE = "\ncp log.log done.log\ncurl -F status=done -F log_file=@done.log http://" + server.getIp() + ":" + server.getPort() + "/compute/api/pilot\n";

            List<ComputeTask> tasks = request.getDatabase().query(ComputeTask.class)
                                            .equals(ComputeTask.STATUSCODE, "ready")
                                            .equals(ComputeTask.BACKENDNAME, backend).find();

            if(tasks.size() > 0)
            {
                ComputeTask task = tasks.get(0);

                if (task != null)
                {
                    String taskName = task.getName();
                    String taskScript = task.getComputeScript();
                    taskScript = taskScript.replaceAll("\r", "");

                    // we add task id to the run listing to identify task when it is
                    // done
                    taskScript = "echo TASKID:" + taskName + "\n" + taskScript + CURL_DONE;
                    // change status to running
                    System.out.println("script " + taskScript);
                    request.getDatabase().beginTx();
                    task.setStatusCode("running");
                    request.getDatabase().update(task);
                    request.getDatabase().commitTx();

                    // send response
                    response.getResponse().getWriter().write(taskScript);
                }
            }
		}
		else
		{
			String results = FileUtils.readFileToString(request.getFile("log_file"));
			// parsing for TaskID
			int idPos = results.indexOf("TASKID:") + 7;
			int endPos = results.indexOf("\n");

			String taskID = results.substring(idPos, endPos).trim();

			ComputeTask task = request.getDatabase().query(ComputeTask.class).eq(ComputeTask.NAME, taskID).limit(1).find().get(0);

            if(task == null)
            {
                System.out.println("TASK null pointer exception");
            }


            if ("done".equals(request.getString("status")))
            {
                System.out.println(">>> task " + taskID + " is finished");
                if (task.getStatusCode().equalsIgnoreCase("running"))
                {
                    request.getDatabase().beginTx();
                    task.setStatusCode("done");
                    task.setRunLog(results);
                }
                else
                {
                    System.out.println("from done: something is wrong with " + taskID);
                }
		    }
            else if ("pulse".equals(request.getString("status")))
            {
                if (task.getStatusCode().equalsIgnoreCase("running"))
                {
                    System.out.println(">>> pulse from " + taskID);
                    request.getDatabase().beginTx();
                    task.setRunLog(results);

                }
            }
            else if ("nopulse".equals(request.getString("status")))
            {
                if (task.getStatusCode().equalsIgnoreCase("running"))
                 {
                     System.out.println(">>> no pulse from " + taskID);
                     request.getDatabase().beginTx();
                     task.setRunLog(results);
                     task.setStatusCode("failed");
                 }
                else if(task != null && task.getStatusCode().equalsIgnoreCase("done"))
                {
                    System.out.println("double check: job is finished & no pulse from it");
                }
            }

            request.getDatabase().update(task);
            request.getDatabase().commitTx();
        }
    }
}

package org.molgenis.compute.db.generator;

import org.apache.log4j.Logger;
import org.molgenis.compute.db.pilot.PilotService;
import org.molgenis.compute.runtime.ComputeHost;
import org.molgenis.compute.runtime.ComputeTask;
import org.molgenis.compute5.ComputeCommandLine;
import org.molgenis.compute5.model.Compute;
import org.molgenis.compute5.model.Task;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.util.WebAppUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: georgebyelas
 * Date: 23/04/2013
 * Time: 08:47
 * To change this template use File | Settings | File Templates.
 */
public class TaskGeneratorDB
{
    private static final Logger LOG = Logger.getLogger(TaskGeneratorDB.class);
    public static final String BACKEND = "backend";


    public void generateTasks(String file, String backendName)
    {
        Compute compute = null;
        //generate here
        try
        {
            compute = ComputeCommandLine.create(file);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }


        List<Task> tasks = compute.getTasks();

        //convert here
        List<ComputeTask> computeTasks = convertTasks(tasks, backendName);

        try
        {
            for (ComputeTask task : computeTasks)
            {
                WebAppUtil.getDatabase().add(task);
                LOG.info("Task [" + task.getName() + "] is added\n");
            }
        }
        catch (DatabaseException e)
        {
            e.printStackTrace();
        }
        LOG.info("Total: " + computeTasks.size() + " is added to database\n");

    }

    private List<ComputeTask> convertTasks(List<Task> tasks, String backendName)
    {
        List<ComputeTask> computeTasks = new ArrayList<ComputeTask>();

        try
        {
            List<ComputeHost> computeHosts = WebAppUtil.getDatabase().query(ComputeHost.class)
                                .equals(ComputeHost.NAME, backendName).find();

            if(computeHosts.size() > 0)
            {
                ComputeHost computeHost = computeHosts.get(0);

                for(Task task : tasks)
                {
                    String name = task.getName();
                    String script = task.getScript();

                    ComputeTask computeTask = new ComputeTask();
                    computeTask.setName(name);
                    computeTask.setComputeScript(script);
                    computeTask.setComputeHost(computeHost);
                    computeTask.setStatusCode(PilotService.TASK_GENERATED);

                    //find previous tasks in db
                    Set<String> prevTaskNames = task.getPreviousTasks();
                    List<ComputeTask> previousTasks = new ArrayList<ComputeTask>();
                    for(String prevTaskName : prevTaskNames)
                    {
                        List<ComputeTask> prevTasks = WebAppUtil.getDatabase().query(ComputeTask.class)
                                                        .equals(ComputeTask.NAME, prevTaskName).find();

                        if(prevTasks.size() > 0)
                        {
                            ComputeTask prevTask = prevTasks.get(0);
                            previousTasks.add(prevTask);
                        }
                        else
                            LOG.error("No ComputeTask  " + prevTaskName + " is found, when searching for previous task for " + name);

                    }
                    computeTask.setPrevSteps(previousTasks);


                }
            }
            else
                LOG.error("No Backend  " + backendName + " is found");

        }
        catch (DatabaseException e)
        {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


        return computeTasks;
    }

    public static void main(String[] args)
    {
        new TaskGeneratorDB().generateTasks("/Users/georgebyelas/Development/molgenis/molgenis-compute-core/src/main/resources/workflows/demoNBIC2/parameters.csv", "grid");
    }
}

package org.molgenis.compute.db.plugin;

import org.molgenis.compute.db.pilot.PilotService;
import org.molgenis.compute.runtime.ComputeHost;
import org.molgenis.compute.runtime.ComputeTask;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.ui.PluginModel;
import org.molgenis.framework.ui.ScreenController;
import org.molgenis.util.Entity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: georgebyelas
 * Date: 22/04/2013
 * Time: 15:42
 * To change this template use File | Settings | File Templates.
 */
public class TaskStatusPlugin extends PluginModel<Entity>
{


    private Map<String, TaskSummary> summary = null;


    public TaskStatusPlugin(String name, ScreenController<?> parent)
    {
        super(name, parent);
    }

    @Override
    public String getViewTemplate()
    {
        return "templates/TaskStatusPlugin.ftl";
    }

    @Override
    public String getViewName()
    {
        return "TaskStatusPlugin";
    }

    @Override
    public void reload(Database db)
    {
        summary = new HashMap<String, TaskSummary>();
        try
        {
            List<ComputeHost> hosts = db.query(ComputeHost.class).find();

            for (ComputeHost host : hosts)
            {
                String hostname = host.getHostName();

                int tGenerated = db.query(ComputeTask.class).equals(ComputeTask.STATUSCODE, PilotService.TASK_GENERATED)
                        .equals(ComputeTask.COMPUTEHOST_NAME, hostname).count();
                int tReady = db.query(ComputeTask.class).equals(ComputeTask.STATUSCODE, PilotService.TASK_READY)
                        .equals(ComputeTask.COMPUTEHOST_NAME, hostname).count();
                int tRunning = db.query(ComputeTask.class).equals(ComputeTask.STATUSCODE, PilotService.TASK_RUNNING)
                        .equals(ComputeTask.COMPUTEHOST_NAME, hostname).count();
                int tFailed = db.query(ComputeTask.class).equals(ComputeTask.STATUSCODE, PilotService.TASK_FAILED)
                        .equals(ComputeTask.COMPUTEHOST_NAME, hostname).count();
                int tDone = db.query(ComputeTask.class).equals(ComputeTask.STATUSCODE, PilotService.TASK_DONE)
                        .equals(ComputeTask.COMPUTEHOST_NAME, hostname).count();

                TaskSummary backendSummary = new TaskSummary(tGenerated, tReady, tRunning, tFailed, tDone);
                summary.put(hostname, backendSummary);
            }
        }
        catch (DatabaseException e)
        {
            e.printStackTrace();
        }
    }

    public Map<String, TaskSummary> getSummary()
    {
        return summary;
    }
}

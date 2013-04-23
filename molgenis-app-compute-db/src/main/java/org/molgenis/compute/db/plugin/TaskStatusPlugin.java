package org.molgenis.compute.db.plugin;

import org.molgenis.compute.db.pilot.PilotService;
import org.molgenis.compute.runtime.ComputeTask;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.ui.PluginModel;
import org.molgenis.framework.ui.ScreenController;
import org.molgenis.util.Entity;

/**
 * Created with IntelliJ IDEA.
 * User: georgebyelas
 * Date: 22/04/2013
 * Time: 15:42
 * To change this template use File | Settings | File Templates.
 */
public class TaskStatusPlugin extends PluginModel<Entity>
{


    private TaskSummary summary = null;

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
        try
        {
            int tGenerated = db.query(ComputeTask.class).equals(ComputeTask.STATUSCODE, PilotService.TASK_GENERATED).count();
            int tReady = db.query(ComputeTask.class).equals(ComputeTask.STATUSCODE, PilotService.TASK_READY).count();
            int tRunning = db.query(ComputeTask.class).equals(ComputeTask.STATUSCODE, PilotService.TASK_RUNNING).count();
            int tFailed = db.query(ComputeTask.class).equals(ComputeTask.STATUSCODE, PilotService.TASK_FAILED).count();
            int tDone = db.query(ComputeTask.class).equals(ComputeTask.STATUSCODE, PilotService.TASK_DONE).count();

            summary = new TaskSummary(tGenerated, tReady, tRunning, tFailed, tDone);
        }
        catch (DatabaseException e)
        {
            e.printStackTrace();
        }
    }

    public TaskSummary getSummary()
    {
        return summary;
    }
}

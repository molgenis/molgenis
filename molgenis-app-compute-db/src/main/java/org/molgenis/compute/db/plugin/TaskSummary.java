package org.molgenis.compute.db.plugin;

/**
 * Created with IntelliJ IDEA.
 * User: georgebyelas
 * Date: 22/04/2013
 * Time: 15:46
 * To change this template use File | Settings | File Templates.
 */
public class TaskSummary
{
    private int taskGenerated, taskReady, taskRunning, taskFailed, taskDone;

    public TaskSummary(int taskGenerated, int taskReady, int taskRunning, int taskFailed, int taskDone)
    {
        this.taskGenerated = taskGenerated;
        this.taskReady = taskReady;
        this.taskRunning = taskRunning;
        this.taskFailed = taskFailed;
        this.taskDone = taskDone;
    }

    public int getTaskGenerated()
    {
        return taskGenerated;
    }

    public int getTaskReady()
    {
        return taskReady;
    }

    public int getTaskRunning()
    {
        return taskRunning;
    }

    public int getTaskFailed()
    {
        return taskFailed;
    }

    public int getTaskDone()
    {
        return taskDone;
    }
}


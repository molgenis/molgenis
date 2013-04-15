package org.molgenis.compute.db.executor;

import app.JpaDatabase;
import org.molgenis.compute.db.sysexecutor.SysCommandExecutor;
import org.molgenis.compute.runtime.ComputeTask;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;

import java.io.IOException;
import java.util.List;

/**
 * Created with IntelliJ IDEA. User: georgebyelas Date: 22/08/2012 Time: 14:26
 * To change this template use File | Settings | File Templates.
 */
public class ComputeExecutorPilotDB implements ComputeExecutor
{
    public static final String BACK_END_GRID = "grid";
    public static final String BACK_END_CLUSTER = "cluster";
    public static final String BACK_END_LOCALHOST = "localhost";


    private ExecutionHost host = null;
    SysCommandExecutor localExecutor = new SysCommandExecutor();


    public ComputeExecutorPilotDB()
    {
        //startDB();
    }



    //actual start pilots here
    public void executeTasks(String backend, String backendType)
    {
        //evaluate if we have tasks ready to run on a specific back-end
        int readyToSubmitSize = 0;

        try
        {
            Database db = new JpaDatabase();

            db.beginTx();

//            List<ComputeTask> generatedTasks = db.find(ComputeTask.class, new QueryRule(ComputeTask.STATUSCODE, QueryRule.Operator.EQUALS, "generated"));

            List<ComputeTask> generatedTasks = db.query(ComputeTask.class)
                    .equals(ComputeTask.STATUSCODE, "generated")
                    .equals(ComputeTask.BACKENDNAME, backend).find();

            readyToSubmitSize = evaluateTasks(generatedTasks);

            //DatabaseUtil.getDatabase().beginTx();

//            List<ComputeTask> readyTasks = db.find(ComputeTask.class, new QueryRule(ComputeTask.STATUSCODE, QueryRule.Operator.EQUALS, "ready"));
            List<ComputeTask> readyTasks = db.query(ComputeTask.class)
                    .equals(ComputeTask.STATUSCODE, "ready")
                    .equals(ComputeTask.BACKENDNAME, backend).find();

            for(ComputeTask ttt : readyTasks)
            {
                System.out.println("task " + ttt.getName());
            }


            readyToSubmitSize = readyTasks.size();

            db.commitTx();
            db.close();

        }
        catch (DatabaseException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        System.out.println("tasks ready for execution " + readyToSubmitSize);

        //create free pilots for one actual task
        //readyToSubmitSize = readyToSubmitSize * 3;

        //start as many pilots as we have tasks ready to run
        for (int i = 0; i < readyToSubmitSize; i++)
        {
            try
            {
                if (backendType.equalsIgnoreCase(BACK_END_GRID))
                    host.submitPilotGrid();
                else if (backendType.equalsIgnoreCase(BACK_END_CLUSTER))
                    host.submitPilotCluster();
                else if (backendType.equalsIgnoreCase(BACK_END_LOCALHOST))
                    submitPilotLocalhost();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            //sleep, because we have a strange behavior in pilot service
            try
            {
                Thread.sleep(2000);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }


    }


    private void submitPilotLocalhost() throws IOException
    {
        String str = System.nanoTime() + "";

        String command = "sh maverick.sh";// +str;
    	System.out.println(">>> " + command);

        try
        {
            localExecutor.runCommand(command);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        String cmdError = localExecutor.getCommandError();
        String cmdOutput = localExecutor.getCommandOutput();

        System.out.println(cmdError);
      	System.out.println(cmdOutput);
    }

    private int evaluateTasks(List<ComputeTask> generatedTasks) throws DatabaseException
    {
        int count = 0;
        for (ComputeTask task : generatedTasks)
        {
            //System.out.println("---------- Task " + task.getName());
            boolean isReady = true;
            List<ComputeTask> prevSteps = task.getPrevSteps();
            for (ComputeTask prev : prevSteps)
            {
                //System.out.println("prevtask " + prev.getName() + " -> " + prev.getStatusCode());
                if (!prev.getStatusCode().equalsIgnoreCase("done"))
                    isReady = false;
            }

            if (isReady)
            {
                Database db = new JpaDatabase();


                db.beginTx();
                System.out.println(">>> TASK " + task.getName() + " is ready for execution");
                //count++;
                task.setStatusCode("ready");
                db.commitTx();
                try
                {
                    db.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
            //System.out.println("---------- end");

        }
        return count;
    }

    public void startHost(String name)
    {

    }

    public void startHostWithCredentials(String h, String user, String password, int port)
    {
        try
        {
            host = new ExecutionHost(h, user, password, port);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

    }

}

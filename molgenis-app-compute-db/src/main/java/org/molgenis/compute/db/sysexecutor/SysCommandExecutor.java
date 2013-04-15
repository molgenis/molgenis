package org.molgenis.compute.db.sysexecutor;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SysCommandExecutor
{
    private ILogDevice fOuputLogDevice = null;
    private ILogDevice fErrorLogDevice = null;
    private String fWorkingDirectory = null;
    private List fEnvironmentVarList = null;

    private StringBuffer fCmdOutput = null;
    private StringBuffer fCmdError = null;
    private AsyncStreamReader fCmdOutputThread = null;
    private AsyncStreamReader fCmdErrorThread = null;

    public void setOutputLogDevice(ILogDevice logDevice)
    {
        fOuputLogDevice = logDevice;
    }

    public void setErrorLogDevice(ILogDevice logDevice)
    {
        fErrorLogDevice = logDevice;
    }

    public void setWorkingDirectory(String workingDirectory)
    {
        fWorkingDirectory = workingDirectory;
    }

    public void setEnvironmentVar(String name, String value)
    {
        if (fEnvironmentVarList == null)
            fEnvironmentVarList = new ArrayList();

        fEnvironmentVarList.add(new EnvironmentVar(name, value));
    }

    public String getCommandOutput()
    {
        return fCmdOutput.toString();
    }

    public String getCommandError()
    {
        return fCmdError.toString();
    }

    public int runCommand(String commandLine) throws Exception
    {
        /* run command */
        Process process = runCommandHelper(commandLine);

        /* start output and error read threads */
        startOutputAndErrorReadThreads(process.getInputStream(), process.getErrorStream());

        /* wait for command execution to terminate */
        int exitStatus = -1;
        try
        {
            exitStatus = process.waitFor();

        } catch (Throwable ex)
        {
            throw new Exception(ex.getMessage());

        } finally
        {
            /* notify output and error read threads to stop reading */
            notifyOutputAndErrorReadThreadsToStopReading();
        }

        return exitStatus;
    }

    private Process runCommandHelper(String commandLine) throws IOException
    {
        Process process = null;
        if (fWorkingDirectory == null)
            process = Runtime.getRuntime().exec(commandLine, getEnvTokens());
        else
            process = Runtime.getRuntime().exec(commandLine, getEnvTokens(), new File(fWorkingDirectory));

        return process;
    }

    private void startOutputAndErrorReadThreads(InputStream processOut, InputStream processErr)
    {
        fCmdOutput = new StringBuffer();
        fCmdOutputThread = new AsyncStreamReader(processOut, fCmdOutput, fOuputLogDevice, "OUTPUT");
        fCmdOutputThread.start();

        fCmdError = new StringBuffer();
        fCmdErrorThread = new AsyncStreamReader(processErr, fCmdError, fErrorLogDevice, "ERROR");
        fCmdErrorThread.start();
    }

    private void notifyOutputAndErrorReadThreadsToStopReading()
    {
        fCmdOutputThread.stopReading();
        fCmdErrorThread.stopReading();
    }

    private String[] getEnvTokens()
    {
        if (fEnvironmentVarList == null)
            return null;

        String[] envTokenArray = new String[fEnvironmentVarList.size()];
        Iterator envVarIter = fEnvironmentVarList.iterator();
        int nEnvVarIndex = 0;
        while (envVarIter.hasNext() == true)
        {
            EnvironmentVar envVar = (EnvironmentVar) (envVarIter.next());
            String envVarToken = envVar.fName + "=" + envVar.fValue;
            envTokenArray[nEnvVarIndex++] = envVarToken;
        }

        return envTokenArray;
    }
}

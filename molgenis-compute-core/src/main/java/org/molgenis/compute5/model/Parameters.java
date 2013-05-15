package org.molgenis.compute5.model;

import java.util.ArrayList;
import java.util.List;

import org.molgenis.util.tuple.Tuple;
import org.molgenis.util.tuple.WritableTuple;

/** Container for all parameters */
public class Parameters
{
	//unique column used to distinguish the parameter rows provided
	
	// SOME OF THESE CONSTANTS SHOULD BE PLACED ELSEWHERE!
	
	public static final String STEP = "step";
	public static final String PROTOCOL = "script";
	public static final String PARAMETER_MAPPING = "parameterMapping";
	public static final String USER = "user"; // TODO: replace all "user" strings with this constant
	public static final String STEP_PARAM_SEP = "_";
	public static final String USER_PREFIX = USER + STEP_PARAM_SEP;
	public final static String ID_COLUMN = USER_PREFIX + Task.TASKID_COLUMN;
	public static final String WORKFLOW = "workflow";
	public static final String WORKFLOW_DEFAULT = "workflow.csv";
	public static final String WORKFLOW_CMNDLINE_OPTION = "w";
	public final static String WORKFLOW_COLUMN = USER_PREFIX + WORKFLOW;
	public static final String DEFAULTS = "defaults";
	public static final String DEFAULTS_DEFAULT = "defaults.csv";
	public static final String DEFAULTS_COLUMN = USER_PREFIX + DEFAULTS;
	public final static String PATH = "path";
	public final static String PATH_DEFAULT = ".";
	public final static String PATH_CMNDLINE_OPTION = "path";
	public final static String PATH_COLUMN = USER_PREFIX + PATH;
	public static final String PREVIOUS = "PREVIOUS";
	public static final String PREVIOUS_COLUMN = USER_PREFIX + PREVIOUS;
	public static final String PARAMETERS_CMNDLINE_OPTION = "p";
	public static final String PARAMETERS = "parameters";
	public static final String PARAMETERS_DEFAULT = "parameters.csv";
	public static final String PARAMETER_COLUMN = "parameters";
	public static final String ERROR_LOG = "errorlog"; // parameter name of log file
	public static final String ERROR_LOG_COLUMN = USER_PREFIX + ERROR_LOG;
	public static String ERROR_FILE_DEFAULT = "error.log"; // can be changed by user parameter
	public static String ENVIRONMENT_EXTENSION = ".env";
	public static String ENVIRONMENT = "user" + ENVIRONMENT_EXTENSION;
	public static String ENVIRONMENT_FULLPATH = null; // to be set
	public static String NOTAVAILABLE = "_NA";
	public static String LIST = "list";
	public static String STRING = "string";
	public static String INPUT = "input";
	public static final String WALLTIME = "walltime";
	public static final String QUEUE = "queue";
	public static final String NODES = "nodes";
	public static final String PPN = "ppn";
	public static final String MEMORY = "mem";
	public static final String BACKEND = "backend";
	public static final String BACKEND_CMNDLINE_OPTION = "b";
	public static final String BACKEND_COLUMN = USER_PREFIX + BACKEND;
	public static final String BACKEND_LOCAL = "local";
	public static final String BACKEND_PBS = "pbs";
	public static final String BACKEND_DEFAULT = BACKEND_LOCAL;
	public static final String RUNDIR = "rundir";
	public static final String RUNDIR_COLUMN = USER_PREFIX + RUNDIR;
	public static final String RUNDIR_DEFAULT = "rundir";
	public static final String RUNDIR_CMNDLINE_OPTION = "rundir";
	public static final String RUNID = "runid";
	public static final String RUNID_CMNDLINE_OPTION = "runid";
	public static final String RUNID_COLUMN = USER_PREFIX + RUNID;
	public static final String RUNID_DEFAULT = "run01";
	public static final String DATABASE = "database";
	public static final String DATABASE_DEFAULT = "none";
	public static final String DATABASE_CMNDLINE_OPTION = "db";
	public static final String DATABASE_COLUMN = USER_PREFIX + DATABASE;
	public static final String PROPERTIES = "compute.properties";
	
	//table with all the values
	List<WritableTuple> values = new ArrayList<WritableTuple>();

	public List<WritableTuple> getValues()
	{
		return (List<WritableTuple>) values;
	}

	public void setValues(List<WritableTuple> values)
	{
		this.values = values;
	}

	public String toString()
	{
		String result = "";
		for (Tuple t : values)
			result += t;
		return result;
	}
}

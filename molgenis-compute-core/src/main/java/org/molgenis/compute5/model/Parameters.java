package org.molgenis.compute5.model;

import java.util.ArrayList;
import java.util.List;

import org.molgenis.util.tuple.Tuple;
import org.molgenis.util.tuple.WritableTuple;

/** Container for all parameters */
public class Parameters
{
	//unique column used to distinguish the parameter rows provided
	public static final String STEP_PARAM_SEP = "_";
	public static final String USER_PREFIX = "user" + STEP_PARAM_SEP;
	public final static String ID_COLUMN = USER_PREFIX + Task.TASKID_COLUMN;
	public static final String WORKFLOW_COLUMN_INITIAL = "workflow";
	public final static String WORKFLOW_COLUMN = USER_PREFIX + "workflow";
	public final static String WORKDIR_COLUMN = USER_PREFIX + Task.WORKDIR_COLUMN;
	public static final String PARAMETER_COLUMN = "parameters";
	public static String ENVIRONMENT = "environment.txt";
	public static String ENVIRONMENT_FULLPATH = null; // to be set
	public static String NOTAVAILABLE = "_NA";
	public static Object LIST = "list";
	public static Object STRING = "string";
	public static Object INPUT = "input";
	
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

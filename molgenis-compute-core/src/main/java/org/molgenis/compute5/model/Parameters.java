package org.molgenis.compute5.model;

import java.util.ArrayList;
import java.util.List;

import org.molgenis.util.tuple.Tuple;
import org.molgenis.util.tuple.WritableTuple;

/** Container for all parameters */
public class Parameters
{
	//unique column used to distinguish the parameter rows provided
	public final static String ID_COLUMN = "user."+Task.TASKID_COLUMN;
	public final static String WORKFLOW_COLUMN = "user.workflow";
	public final static String WORKDIR_COLUMN = "user."+Task.WORKDIR_COLUMN;
	public static final String PARAMETER_COLUMN = "parameters";
	
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

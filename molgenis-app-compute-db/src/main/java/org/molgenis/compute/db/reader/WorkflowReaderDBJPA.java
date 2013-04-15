package org.molgenis.compute.db.reader;

import org.molgenis.compute.design.ComputeParameter;
import org.molgenis.compute.design.Workflow;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.util.DatabaseUtil;

import java.io.IOException;
import java.util.List;


/**
 * Created with IntelliJ IDEA. User: georgebyelas Date: 22/08/2012 Time: 10:18
 * To change this template use File | Settings | File Templates.
 */
public class WorkflowReaderDBJPA implements WorkflowReader
{
	public Workflow getWorkflow(String name) throws IOException
	{
		try
		{
			Workflow w = DatabaseUtil.getDatabase().find(Workflow.class, new QueryRule(Workflow.NAME, QueryRule.Operator.EQUALS, name)).get(0);
			return w;
		}
		catch (DatabaseException e)
		{
			e.printStackTrace();
		}
		return null;
	}

	public List<ComputeParameter> getParameters() throws IOException
	{
		try
		{
			// Workflow w = db.query(Workflow.class).find().get(0);
			List<ComputeParameter> parameters = DatabaseUtil.getDatabase().query(ComputeParameter.class).find();
			return parameters;
		}
		catch (DatabaseException e)
		{
			e.printStackTrace();
		}
		return null;
	}

}

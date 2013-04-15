package org.molgenis.compute.db.reader;

import java.io.IOException;
import java.util.List;

import org.molgenis.compute.design.ComputeParameter;
import org.molgenis.compute.design.Workflow;

/**
 * Created with IntelliJ IDEA. User: georgebyelas Date: 22/08/2012 Time: 10:07
 * To change this template use File | Settings | File Templates.
 */
public interface WorkflowReader
{

	Workflow getWorkflow(String name) throws IOException;

	List<ComputeParameter> getParameters() throws IOException;

}

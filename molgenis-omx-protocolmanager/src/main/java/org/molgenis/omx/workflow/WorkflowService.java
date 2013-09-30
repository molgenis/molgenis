package org.molgenis.omx.workflow;

import java.util.List;

import org.molgenis.framework.db.DatabaseException;
import org.molgenis.omx.observ.Protocol;

public interface WorkflowService {

	List<Protocol> getWorkflows() throws DatabaseException;

	List<Protocol> getWorkflowStep(Integer protocolId) throws DatabaseException;

	Protocol getWorkflowProtocol(Integer protocolId) throws DatabaseException;
}

package org.molgenis.omx.workflow;

import java.util.List;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
import org.molgenis.omx.observ.Protocol;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WorkflowServiceImpl implements WorkflowService {
	private final Database database;

	@Autowired
	public WorkflowServiceImpl(Database database) {
		if(database == null) throw new IllegalArgumentException("Database is null");
		this.database = database;
	}

	@Override
	public List<Protocol> getWorkflows() throws DatabaseException {
		return database.find(Protocol.class, new QueryRule(Protocol.ROOT, Operator.EQUALS, true));
	}

	@Override
	public List<Protocol> getWorkflowStep(Integer protocolId) throws DatabaseException {
		Protocol protocol = Protocol.findById(database, protocolId);
		return protocol.getSubprotocols();
	}

	@Override
	public Protocol getWorkflowProtocol(Integer protocolId) throws DatabaseException{
		return Protocol.findById(database, protocolId);
	}	
}

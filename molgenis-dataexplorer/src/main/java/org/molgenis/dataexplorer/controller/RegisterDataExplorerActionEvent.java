package org.molgenis.dataexplorer.controller;

import org.springframework.context.ApplicationEvent;

public class RegisterDataExplorerActionEvent extends ApplicationEvent
{
	private static final long serialVersionUID = 1L;

	private final RegisterDataExplorerActionEventHandler source;
	private final String actionId;

	public RegisterDataExplorerActionEvent(RegisterDataExplorerActionEventHandler source, String actionId)
	{
		super(source);
		this.source = source;
		this.actionId = actionId;
	}

	public boolean allowAction(String entityName)
	{
		return source.allowAction(actionId, entityName);
	}

	public String getActionId()
	{
		return actionId;
	}

	@Override
	public RegisterDataExplorerActionEventHandler getSource()
	{
		return source;
	}
}

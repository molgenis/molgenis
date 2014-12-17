package org.molgenis.dataexplorer.controller;

import org.springframework.context.ApplicationEvent;

public class RegisterDataExplorerActionEvent extends ApplicationEvent
{
	private static final long serialVersionUID = 1L;

	private final RegisterDataExplorerActionEventHandler source;
	private final Type type;
	private final String actionId;
	private final String actionLabel;

	public static enum Type
	{
		REGISTER, DEREGISTER
	}

	public RegisterDataExplorerActionEvent(Type type, RegisterDataExplorerActionEventHandler source, String actionId,
			String actionLabel)
	{
		super(source);
		this.type = type;
		this.source = source;
		this.actionId = actionId;
		this.actionLabel = actionLabel;
	}

	public boolean allowAction(String entityName)
	{
		return source.allowAction(actionId, entityName);
	}

	public String getActionId()
	{
		return actionId;
	}

	public String getActionLabel()
	{
		return actionLabel;
	}

	public Type getType()
	{
		return type;
	}

	@Override
	public RegisterDataExplorerActionEventHandler getSource()
	{
		return source;
	}
}

package org.molgenis.dataexplorer.event;

/**
 * Registration event: register a handler for action dropdown clicks in the data tab of the data explorer.
 */
public class DataExplorerRegisterActionEvent extends DataExplorerRegisterEvent
{
	private static final long serialVersionUID = 1L;

	private final String actionLabel;
	private final DataExplorerRegisterActionEventHandler source;

	public DataExplorerRegisterActionEvent(Type type, DataExplorerRegisterActionEventHandler source, String actionId,
			String actionLabel)
	{
		super(type, actionId, source);
		this.source = source;
		this.actionLabel = actionLabel;
	}

	public boolean allowAction(String entityName)
	{
		return source.allowAction(getActionId(), entityName);
	}

	public String getActionId()
	{
		return getId();
	}

	public String getActionLabel()
	{
		return actionLabel;
	}

	@Override
	public DataExplorerRegisterActionEventHandler getSource()
	{
		return source;
	}
}

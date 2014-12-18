package org.molgenis.dataexplorer.event;

/**
 * Registration event: register a handler for categorical/xref/mref/ table cell clicks in the data module table of the
 * data explorer.
 */
public class DataExplorerRegisterRefCellClickEvent extends DataExplorerRegisterEvent
{
	private static final long serialVersionUID = 1L;

	private final String entityName;
	private final String attributeName;
	private final DataExplorerRegisterRefCellClickEventHandler source;

	public DataExplorerRegisterRefCellClickEvent(Type type, String entityName, String attributeName,
			DataExplorerRegisterRefCellClickEventHandler source)
	{
		super(type, entityName + '/' + attributeName, source);
		this.entityName = entityName;
		this.attributeName = attributeName;
		this.source = source;
	}

	public String getEntityName()
	{
		return entityName;
	}

	public String getAttributeName()
	{
		return attributeName;
	}

	@Override
	public DataExplorerRegisterRefCellClickEventHandler getSource()
	{
		return source;
	}
}

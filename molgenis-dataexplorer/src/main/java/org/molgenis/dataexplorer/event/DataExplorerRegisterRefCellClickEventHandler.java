package org.molgenis.dataexplorer.event;

public interface DataExplorerRegisterRefCellClickEventHandler
{
	/**
	 * Returns redirect URL template that contains token {{id}} which will be replaced with an entity id
	 * 
	 * @return
	 */
	String getRefRedirectUrlTemplate();
}

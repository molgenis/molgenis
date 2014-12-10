package org.molgenis.dataexplorer.controller;

public interface RegisterDataExplorerActionEventHandler
{
	boolean allowAction(String actionId, String entityName);
}

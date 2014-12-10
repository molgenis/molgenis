package org.molgenis.dataexplorer.controller;

import java.util.List;

import org.molgenis.data.QueryRule;

public interface RegisterDataExplorerActionEventHandler
{
	/**
	 * @param actionId
	 * @param entityName
	 * @return whether are not the action is allowed for this input
	 */
	boolean allowAction(String actionId, String entityName);

	/**
	 * 
	 * @param actionId
	 * @param entityName
	 * @param queryRules
	 * @return location of resource to navigate to after performing action or null
	 */
	String performAction(String actionId, String entityName, List<QueryRule> queryRules);
}

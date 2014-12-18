package org.molgenis.dataexplorer.event;

import java.util.List;
import java.util.Map;

import org.molgenis.data.QueryRule;

public interface DataExplorerRegisterActionEventHandler
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
	 * @return map with properties resulting from action
	 */
	Map<String, Object> performAction(String actionId, String entityName, List<QueryRule> queryRules);
}

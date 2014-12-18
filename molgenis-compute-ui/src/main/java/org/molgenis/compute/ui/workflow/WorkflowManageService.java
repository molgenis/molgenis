package org.molgenis.compute.ui.workflow;

import org.molgenis.dataexplorer.event.DataExplorerRegisterActionEventHandler;

public interface WorkflowManageService extends DataExplorerRegisterActionEventHandler
{
	/**
	 * Update workflow info
	 * 
	 * @param identifier
	 *            , the identifier of the UIWorkflow to update
	 * @param name
	 *            , the new name
	 * @param description
	 *            , the new description
	 * @param targetFullName
	 *            , the fully qaulified entitymetadata name
	 */
	void updateWorkflow(String identifier, String name, String description, String targetFullName, boolean active);
}

package org.molgenis.data.view.service;

import org.molgenis.data.Entity;

public interface ViewService
{
	/**
	 * Create a new view when the view is not yet in the View Table
	 * 
	 * @param viewName
	 * @param masterEntityName
	 * @param slaveEntityName
	 * @param masterAttributeId
	 * @param slaveAttributeId
	 */
	public void createNewView(String viewName, String masterEntityName, String slaveEntityName,
			String masterAttributeId, String slaveAttributeId);

	/**
	 * Get the View Entity from the View Table based on the viewName and masterEntityName
	 * 
	 * @param viewName
	 * @param masterEntityName
	 * @return a viewEntity
	 */
	public Entity getViewEntity(String viewName, String masterEntityName);

	/**
	 * Get the Slave Entity from the Slave Entity Table based on the slaveEntityName
	 * 
	 * @param slaveEntityName
	 * @return a slaveEntity
	 */
	public Entity getSlaveEntity(String slaveEntityName);

	/**
	 * When the view exists, but the slave does not, add the slave entity to the existing view
	 * 
	 * @param viewEntity
	 * @param slaveEntityName
	 * @param masterAttributeId
	 * @param slaveAttributeId
	 */
	public void addNewSlaveEntityToExistingView(Entity viewEntity, String slaveEntityName, String masterAttributeId,
			String slaveAttributeId);

	/**
	 * When the view and slave exist, add the attribute mapping to the existing slave
	 * 
	 * @param slaveEntityName
	 * @param masterAttributeId
	 * @param slaveAttributeId
	 */
	public void addNewAttributeMappingToExistingSlave(String slaveEntityName, String masterAttributeId,
			String slaveAttributeId);
}

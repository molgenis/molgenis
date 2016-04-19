package org.molgenis.data.view.service;

import org.molgenis.data.Entity;

import com.google.common.base.Optional;

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
	void createNewView(String viewName, String masterEntityName, String slaveEntityName,
			String masterAttributeId, String slaveAttributeId);

	/**
	 * Get the View Entity from the View Table with a viewName
	 * 
	 * @param viewName
	 * @return a viewEntity
	 */
	Entity getViewEntity(String viewName);

	/**
	 * Get the Slave Entity from the Slave Entity Table based on the slaveEntityName
	 * 
	 * @param viewName
	 *            name of the view that the entity should be a slave entity of
	 * @param slaveEntityName
	 *            name of the slave entity
	 * @return Optional<Entity>, present if the slave entity exists
	 */
	Optional<Entity> getSlaveEntity(String viewName, String slaveEntityName);

	/**
	 * When the view exists, but the slave does not, add the slave entity to the existing view
	 * 
	 * @param viewEntity
	 * @param slaveEntityName
	 * @param masterAttributeId
	 * @param slaveAttributeId
	 */
	void addNewSlaveEntityToExistingView(Entity viewEntity, String slaveEntityName, String masterAttributeId,
			String slaveAttributeId);

	/**
	 * If the view and slave exist, adds the attribute mapping to the existing slave.
	 * 
	 * @param viewName
	 *            the name of the view to update
	 * @param slaveEntityName
	 *            the name of the slave entity to update
	 * @param masterAttributeId
	 *            ID of the attribute in the master entity
	 * @param slaveAttributeId
	 *            ID of the attribute in the slave entity
	 */
	void addNewAttributeMappingToExistingSlave(String viewName, String slaveEntityName, String masterAttributeId,
			String slaveAttributeId);

	void deleteView(String viewName);
}

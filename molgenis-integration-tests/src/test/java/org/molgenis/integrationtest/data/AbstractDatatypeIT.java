package org.molgenis.integrationtest.data;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Repository;
import org.molgenis.data.support.DefaultEntity;

/**
 * Test sequence:
 * 
 * 1. Login
 * 
 * 2. Create repository with provided EntityMetaData
 * 
 * 3. Add new entity to the DataService
 * 
 * 4. Retrieve the entity by id
 * 
 * 5. Verify the entity values
 * 
 * 6. Update the entity
 * 
 * 7. Verify the entity values
 * 
 * 8. Delete the entity
 * 
 * 9. Verify entity is deleted
 * 
 */
public abstract class AbstractDatatypeIT extends AbstractDataIntegrationIT
{
	/** Define a data model to test */
	public abstract EntityMetaData createMetaData();

	/**
	 * Define a test object to be used as insert
	 * 
	 * @throws Exception
	 */
	public abstract void populateTestEntity(Entity entity) throws Exception;

	/**
	 * Verify the entity (after insert)
	 * 
	 * @throws Exception
	 */
	public abstract void verifyTestEntityAfterInsert(Entity entity) throws Exception;

	/**
	 * Define a test object to be used for udate
	 * 
	 * @throws Exception
	 */
	public void updateTestEntity(Entity entity) throws Exception
	{

	}

	/**
	 * Verify the entity (after update)
	 * 
	 * @throws Exception
	 */
	public void verifyTestEntityAfterUpdate(Entity entity) throws Exception
	{

	}

	public void testIt() throws Exception
	{
		// Login
		SecuritySupport.login();

		// Create new repository
		EntityMetaData entityMetaData = createMetaData();
		Repository repo = dataService.getMeta().addEntityMeta(entityMetaData);
		assertNotNull(repo, entityMetaData.getName() + " repository not found.");
		assertEquals(repo.getName(), entityMetaData.getName());

		// Create entity
		DefaultEntity entity = new DefaultEntity(entityMetaData, dataService);
		populateTestEntity(entity);

		// Add entity
		dataService.add(entityMetaData.getName(), entity);

		// Retrieve entity
		Entity retrieved = retrieve(entity);
		assertNotNull(retrieved, "Entity with id '" + entity.getIdValue() + "' of type '" + entityMetaData.getName()
				+ "' not found.");

		// Verify
		verifyTestEntityAfterInsert(retrieved);

		// Update
		updateTestEntity(retrieved);
		update(retrieved);

		// Verify updated
		verifyEntityUpdated(retrieved);

		// Delete
		delete(retrieved);

		// Verify deleted
		verifyEntityDeleted(retrieved);
	}

	private Entity retrieve(Entity entity)
	{
		return dataService.findOne(entity.getEntityMetaData().getName(), entity.getIdValue());
	}

	protected void delete(Entity entity)
	{
		dataService.delete(entity.getEntityMetaData().getName(), entity);
	}

	protected void verifyEntityDeleted(Entity entity)
	{
		assertNull(dataService.findOne(entity.getEntityMetaData().getName(), entity.getIdValue()), "Entity with id '"
				+ entity.getIdValue() + "' of type '" + entity.getEntityMetaData().getName() + "' is not deleted.");
	}

	protected void update(Entity entity)
	{
		dataService.update(entity.getEntityMetaData().getName(), entity);
	}

	protected void verifyEntityUpdated(Entity entity) throws Exception
	{
		Entity updated = retrieve(entity);
		verifyTestEntityAfterUpdate(updated);
	}
}

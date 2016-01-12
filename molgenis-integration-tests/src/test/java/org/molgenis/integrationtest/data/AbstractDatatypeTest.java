package org.molgenis.integrationtest.data;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Repository;
import org.molgenis.data.support.DefaultEntity;

public abstract class AbstractDatatypeTest extends AbstractDataIntegrationTest
{
	/** Define a data model to test */
	public abstract EntityMetaData createMetaData();

	/**
	 * Define a test object to be used
	 * 
	 * @throws Exception
	 */
	public abstract void populateTestEntity(DefaultEntity entity) throws Exception;

	/**
	 * verify the returned entity
	 * 
	 * @throws Exception
	 */
	public abstract void verifyTestEntity(Entity entity) throws Exception;

	public void testIt() throws Exception
	{
		// Create new repository
		EntityMetaData entityMetaData = createMetaData();
		Repository repo = dataService.getMeta().addEntityMeta(entityMetaData);
		assertNotNull(repo, entityMetaData.getName() + " repository not found.");
		assertEquals(repo.getName(), entityMetaData.getName());

		// Create entity
		DefaultEntity entity = new DefaultEntity(entityMetaData, dataService);
		populateTestEntity(entity);

		// Login
		SecuritySupport.login();

		// Add entity
		dataService.add(entityMetaData.getName(), entity);

		// Retrieve entity
		Entity retrieved = dataService.findOne(entityMetaData.getName(), entity.getIdValue());
		assertNotNull(retrieved, "Entity with id '" + entity.getIdValue() + "' of type '" + entityMetaData.getName()
				+ "' not found.");

		// Verify
		verifyTestEntity(retrieved);
	}
}

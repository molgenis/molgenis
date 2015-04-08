package org.molgenis.data.mysql;

import org.molgenis.MysqlTestConfig;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.meta.MetaDataServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

/** Superclass for all datatype specific tests, e.g. MysqlRepositoryStringTest */
@ContextConfiguration(classes = MysqlTestConfig.class)
public abstract class MysqlRepositoryAbstractDatatypeTest extends AbstractTestNGSpringContextTests
{
	protected static final Logger LOG = LoggerFactory.getLogger(MysqlRepositoryAbstractDatatypeTest.class);

	@Autowired
	MetaDataServiceImpl metaDataService;

	private EntityMetaData metaData;

	/** Define a data model to test */
	public abstract EntityMetaData createMetaData();

	/** Define the expected mysql create table for the data model */
	public abstract String createSql();

	/** Define a test object to be used */
	public abstract Entity defaultEntity();

	public EntityMetaData getMetaData()
	{
		if (metaData == null)
		{
			metaData = createMetaData();
		}
		return metaData;
	}

	@Test
	public void test() throws Exception
	{
		// drop if needed
		if (metaDataService.getEntityMetaData(getMetaData().getName()) != null) metaDataService
				.deleteEntityMeta(getMetaData().getName());

		// test create table
		MysqlRepository repo = (MysqlRepository) metaDataService.addEntityMeta(getMetaData());
		Assert.assertEquals(repo.getCreateSql(), createSql());

		// verify default value
		Entity defaultEntity = defaultEntity();
		LOG.debug("inserting: " + defaultEntity);
		repo.add(defaultEntity());

		for (Entity e : repo)
		{
			LOG.debug("found back " + e);
			Object value = e.get("col3");
			Object defaultValue = repo.getEntityMetaData().getAttribute("col3").getDefaultValue();
			LOG.debug("defaultClass=" + defaultValue.getClass().getName() + " - valueClass="
					+ value.getClass().getName());
			Assert.assertEquals(defaultValue, value);

		}

		// verify not null error
		// TODO

		// verify default
		// TODO

		// allow time for logger to finish... (premature end of program results in loss of output)
		Thread.sleep(100);
	}

	@AfterClass
	public void afterClass()
	{
		metaDataService.recreateMetaDataRepositories();
	}
}

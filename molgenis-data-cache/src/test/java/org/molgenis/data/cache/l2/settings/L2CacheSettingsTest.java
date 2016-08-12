package org.molgenis.data.cache.l2.settings;

import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.meta.model.EntityMetaDataFactory;
import org.molgenis.test.data.AbstractMolgenisSpringTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.molgenis.data.cache.l2.settings.L2CacheSettings.ValueReferenceType.Soft;
import static org.molgenis.data.cache.l2.settings.L2CacheSettings.ValueReferenceType.Strong;
import static org.testng.Assert.assertEquals;

@ContextConfiguration(classes = { L2CacheSettingsTest.Config.class })
public class L2CacheSettingsTest extends AbstractMolgenisSpringTest
{
	@Autowired
	private L2CacheSettingsFactory l2CacheSettingsFactory;

	@Autowired
	private EntityMetaDataFactory entityMetaDataFactory;

	private L2CacheSettings l2CacheSettings;
	private EntityMetaData emd;

	@BeforeClass
	public void beforeClass()
	{
		emd = entityMetaDataFactory.create("org_molgenis_test_TypeTest");
	}

	@BeforeMethod
	public void beforeMethod()
	{
		l2CacheSettings = l2CacheSettingsFactory.create();
	}

	@Test
	public void testGetSpecStringAllValuesProvided()
	{
		l2CacheSettings.setCachedEntity(emd);
		l2CacheSettings.setConcurrencyLevel(3);
		l2CacheSettings.setInitialCapacity(16);
		l2CacheSettings.setMaximumSize(200L);
		l2CacheSettings.setExpireAfterAccess("10m");
		l2CacheSettings.setExpireAfterWrite("1d");
		l2CacheSettings.setRefreshAfterWrite("2h");
		l2CacheSettings.setWeakKeys(true);
		l2CacheSettings.setValueReferenceType(Soft);
		l2CacheSettings.setRecordStats(true);

		assertEquals(l2CacheSettings.getCacheBuilderSpecString(),
				"concurrencyLevel=3,initialCapacity=16,maximumSize=200,expireAfterAccess=10m,expireAfterWrite=1d,"
						+ "refreshAfterWrite=2h,weakKeys,softValues,recordStats");
	}

	@Test
	public void testGetSpecStringOnlyDefaultValuesProvided()
	{
		l2CacheSettings.setCachedEntity(null);
		l2CacheSettings.setConcurrencyLevel(4);
		l2CacheSettings.setInitialCapacity(16);
		l2CacheSettings.setWeakKeys(false);
		l2CacheSettings.setValueReferenceType(Strong);
		l2CacheSettings.setRecordStats(true);

		assertEquals(l2CacheSettings.getCacheBuilderSpecString(), "concurrencyLevel=4,initialCapacity=16,recordStats");
	}

	@Import({ L2CacheSettingsFactory.class, L2CacheSettingsMetaData.class })
	@Configuration
	public static class Config
	{

	}
}

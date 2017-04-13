package org.molgenis.data.importer;

import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.importer.config.ImportTestConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

@ContextConfiguration(classes = { ImportTestConfig.class })

public class ImportRunTest extends AbstractMolgenisSpringTest
{
	@Autowired
	private ImportRunFactory importRunFactory;

	private ImportRun importRun;

	@BeforeClass
	public void setUp()
	{
		importRun = importRunFactory.create();
	}

	@Test
	public void testGetNotifyDefaultFalse() throws Exception
	{
		assertFalse(importRun.getNotify());
	}

	@Test
	public void testSetNotify() throws Exception
	{
		importRun.setNotify(true);
		assertTrue(importRun.getNotify());
	}

}
package org.molgenis.data.importer;

import java.io.IOException;

import org.molgenis.AppConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.Test;

@ContextConfiguration(classes = AppConfig.class)
public class MEntityImportServiceTest extends AbstractTestNGSpringContextTests
{
	@Autowired
	MEntityImportService importer;

	@Test
	public void test1() throws IOException
	{
		Assert.assertNotNull(importer);

        //create test excel

        importer.doImport(null, null);
	}
}

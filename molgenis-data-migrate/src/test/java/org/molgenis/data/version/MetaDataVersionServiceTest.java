package org.molgenis.data.version;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.springframework.util.FileCopyUtils;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.io.Files;

public class MetaDataVersionServiceTest
{
	private MetaDataVersionService metaDataVersionService;
	private File molgenisHomeFolder;
	private File propertiesFile;

	@BeforeMethod
	public void beforeMethod() throws IOException
	{
		molgenisHomeFolder = Files.createTempDir();
		System.setProperty("molgenis.home", molgenisHomeFolder.getAbsolutePath());
		propertiesFile = new File(molgenisHomeFolder, "molgenis-server.properties");
		propertiesFile.createNewFile();
		metaDataVersionService = new MetaDataVersionService();
	}

	@AfterMethod
	public void afterMethod() throws IOException
	{
		FileUtils.deleteDirectory(molgenisHomeFolder);
	}

	@Test
	public void getDatabaseMetaDataVersion()
	{
		assertEquals(metaDataVersionService.getDatabaseMetaDataVersion(),
				MetaDataVersionService.CURRENT_META_DATA_VERSION);
	}

	@Test
	public void getMolgenisServerProperties()
	{
		assertNotNull(metaDataVersionService.getMolgenisServerProperties());
	}

	@Test
	public void updateToCurrentVersion() throws IOException
	{
		FileCopyUtils.copy("meta.data.version=0", new FileWriter(propertiesFile));
		assertEquals(metaDataVersionService.getDatabaseMetaDataVersion(), 0);

		metaDataVersionService.updateToCurrentVersion();
		assertEquals(metaDataVersionService.getDatabaseMetaDataVersion(),
				MetaDataVersionService.CURRENT_META_DATA_VERSION);
	}
}

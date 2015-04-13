package org.molgenis.data.version;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.sql.DataSource;

import org.apache.commons.io.FileUtils;
import org.mockito.Mockito;
import org.springframework.util.FileCopyUtils;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.io.Files;

public class MetaDataVersionServiceTest
{
	private MolgenisVersionService metaDataVersionService;
	private File molgenisHomeFolder;
	private File propertiesFile;
	private DataSource dataSource;

	@BeforeMethod
	public void beforeMethod() throws IOException
	{
		molgenisHomeFolder = Files.createTempDir();
		System.setProperty("molgenis.home", molgenisHomeFolder.getAbsolutePath());
		propertiesFile = new File(molgenisHomeFolder, "molgenis-server.properties");
		propertiesFile.createNewFile();
		dataSource = Mockito.mock(DataSource.class);
		metaDataVersionService = new MolgenisVersionService(dataSource);
	}

	@AfterMethod
	public void afterMethod() throws IOException
	{
		FileUtils.deleteDirectory(molgenisHomeFolder);
	}

	@Test
	public void getDatabaseMetaDataVersion()
	{
		assertEquals(metaDataVersionService.getMolgenisVersionFromServerProperties(),
				MolgenisVersionService.CURRENT_VERSION);
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
		assertEquals(metaDataVersionService.getMolgenisVersionFromServerProperties(), 0);

		metaDataVersionService.updateToCurrentVersion();
		assertEquals(metaDataVersionService.getMolgenisVersionFromServerProperties(),
				MolgenisVersionService.CURRENT_VERSION);
	}
}

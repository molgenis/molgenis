package org.molgenis.data.version;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.commons.io.FileUtils;
import org.molgenis.migrate.version.MigrationUtils;
import org.molgenis.migrate.version.MolgenisVersionService;
import org.springframework.util.FileCopyUtils;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.io.Files;
import com.mysql.jdbc.DatabaseMetaData;

public class MetaDataVersionServiceTest
{
	private MolgenisVersionService metaDataVersionService;
	private File molgenisHomeFolder;
	private File propertiesFile;
	private ResultSet molgenisUserTableResultSet;

	@BeforeMethod
	public void beforeMethod() throws IOException, SQLException
	{
		molgenisHomeFolder = Files.createTempDir();
		System.setProperty("molgenis.home", molgenisHomeFolder.getAbsolutePath());
		propertiesFile = new File(molgenisHomeFolder, "molgenis-server.properties");
		propertiesFile.createNewFile();
	}

	private void createMetaDataVersionNoServerProperties(boolean withMolgenisUserTable)
	{
		try
		{
			DataSource dataSource = mock(DataSource.class);
			Connection connection = mock(Connection.class);
			when(dataSource.getConnection()).thenReturn(connection);
			DatabaseMetaData dbMeta = mock(DatabaseMetaData.class);
			when(connection.getMetaData()).thenReturn(dbMeta);
			molgenisUserTableResultSet = mock(ResultSet.class);
			when(dbMeta.getTables(null, null, "MolgenisUser", new String[]
			{ "TABLE" })).thenReturn(molgenisUserTableResultSet);
			when(molgenisUserTableResultSet.first()).thenReturn(withMolgenisUserTable);
			metaDataVersionService = new MolgenisVersionService(dataSource);
		}
		catch (SQLException e)
		{
			Assert.fail("Shouldn't happen.", e);
		}
	}

	@AfterMethod
	public void afterMethod() throws IOException
	{
		FileUtils.deleteDirectory(molgenisHomeFolder);
	}

	@Test
	public void getDatabaseMetaDataVersionNoMolgenisUserTable() throws SQLException
	{
		createMetaDataVersionNoServerProperties(false);
		assertEquals(metaDataVersionService.getMolgenisVersionFromServerProperties(),
				MolgenisVersionService.CURRENT_VERSION);
	}

	@Test
	public void getDatabaseMetaDataVersionMolgenisUserTablePresent() throws SQLException
	{
		createMetaDataVersionNoServerProperties(true);
		assertEquals(metaDataVersionService.getMolgenisVersionFromServerProperties(), 0);
	}

	@Test
	public void getMolgenisServerProperties()
	{
		createMetaDataVersionNoServerProperties(false);
		assertNotNull(MigrationUtils.getMolgenisServerProperties());
	}

	@Test
	public void updateToCurrentVersion() throws IOException
	{
		FileCopyUtils.copy("molgenis.version=0", new FileWriter(propertiesFile));
		assertEquals(metaDataVersionService.getMolgenisVersionFromServerProperties(), 0);

		metaDataVersionService.updateToCurrentVersion();
		assertEquals(metaDataVersionService.getMolgenisVersionFromServerProperties(),
				MolgenisVersionService.CURRENT_VERSION);
	}
}

package org.molgenis.data.mysql;

import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import com.mysql.management.MysqldResource;

public class EmbeddedMysqlDatabase extends DriverManagerDataSource
{
	private final Logger logger = Logger.getLogger(EmbeddedMysqlDatabase.class);
	private final MysqldResource mysqldResource;

	public EmbeddedMysqlDatabase(MysqldResource mysqldResource)
	{
		this.mysqldResource = mysqldResource;
	}

	public void shutdown()
	{
		if (mysqldResource != null)
		{
			mysqldResource.shutdown();
			if (!mysqldResource.isRunning())
			{
				logger.info(">>>>>>>>>> DELETING MYSQL BASE DIR [{" + mysqldResource.getBaseDir() + "}] <<<<<<<<<<");
				try
				{
					FileUtils.forceDelete(mysqldResource.getBaseDir());
				}
				catch (IOException e)
				{
					logger.error(e.getMessage(), e);
				}
			}
		}
	}
}

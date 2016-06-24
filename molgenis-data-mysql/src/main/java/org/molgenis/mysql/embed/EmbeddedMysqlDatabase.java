package org.molgenis.mysql.embed;

import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import com.mysql.management.MysqldResource;

public class EmbeddedMysqlDatabase extends DriverManagerDataSource
{
	private static final Logger LOG = LoggerFactory.getLogger(EmbeddedMysqlDatabase.class);

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
				LOG.info(">>>>>>>>>> DELETING MYSQL BASE DIR [{" + mysqldResource.getBaseDir() + "}] <<<<<<<<<<");
				try
				{
					FileUtils.forceDelete(mysqldResource.getBaseDir());
				}
				catch (IOException e)
				{
					LOG.error(e.getMessage(), e);
				}
			}
		}
	}
}

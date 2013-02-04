package org.molgenis.framework.db.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.log4j.Logger;

public abstract class AbstractDataSourceWrapper implements DataSourceWrapper
{
	private static final Logger logger = Logger.getLogger(AbstractDataSourceWrapper.class);

	@Override
	public Connection getConnection() throws NamingException, SQLException
	{
		return this.getDataSource().getConnection();
	}

	@Override
	public String getDriverClassName()
	{
		DataSource ds;
		try
		{
			ds = getDataSource();
			if (ds instanceof BasicDataSource)
			{
				return ((BasicDataSource) ds).getDriverClassName();
			}
		}
		catch (NamingException e)
		{
			e.printStackTrace();
		}

		logger.error("UNKNOWN DRIVER");
		return "UNKNOWN";
	}

	@Override
	public int countOpenConnections() throws NamingException
	{
		if (this.getDataSource() instanceof org.apache.commons.dbcp.BasicDataSource)
		{
			return ((org.apache.commons.dbcp.BasicDataSource) this.getDataSource()).getNumActive();
		}
		logger.debug(getDataSource().getClass());
		return 0;
	}

	@Override
	public int getMaxActive() throws NamingException
	{
		if (this.getDataSource() instanceof org.apache.commons.dbcp.BasicDataSource)
		{
			return ((org.apache.commons.dbcp.BasicDataSource) this.getDataSource()).getMaxActive();
		}
		logger.debug(getDataSource().getClass());
		return 0;
	}

	protected abstract DataSource getDataSource() throws NamingException;
}

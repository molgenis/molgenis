package org.molgenis.framework.db.jdbc;

import javax.naming.NamingException;
import javax.sql.DataSource;

public class SimpleDataSourceWrapper extends AbstractDataSourceWrapper
{
	DataSource dSource;

	public SimpleDataSourceWrapper(DataSource dSource)
	{
		if (dSource == null) throw new IllegalArgumentException("DataSource cannot be null");
		this.dSource = dSource;
	}

	@Override
	protected DataSource getDataSource() throws NamingException
	{
		return this.dSource;
	}
}

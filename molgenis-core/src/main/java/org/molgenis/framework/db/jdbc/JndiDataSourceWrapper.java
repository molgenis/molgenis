package org.molgenis.framework.db.jdbc;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class JndiDataSourceWrapper extends AbstractDataSourceWrapper
{
	String jndiPath;

	public JndiDataSourceWrapper(String jndiPath)
	{
		this.jndiPath = jndiPath;
	}

	@Override
	protected DataSource getDataSource() throws NamingException
	{
		InitialContext context = new InitialContext();
		DataSource dsource = (DataSource) context.lookup(jndiPath);
		// logger.debug("Getting dataSource");
		return dsource;
	}
}

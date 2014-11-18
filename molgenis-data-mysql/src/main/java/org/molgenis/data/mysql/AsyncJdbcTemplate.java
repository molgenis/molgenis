package org.molgenis.data.mysql;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.persistence.QueryTimeoutException;

import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * JdbcTemplate implementation that calls {@link #execute(String)} on a different thread.
 */
public class AsyncJdbcTemplate
{
	private final ExecutorService executors = Executors.newCachedThreadPool();
	private JdbcTemplate jdbcTemplate;

	private final static Logger LOG = Logger.getLogger(AsyncJdbcTemplate.class);

	public AsyncJdbcTemplate(JdbcTemplate jdbcTemplate)
	{
		this.jdbcTemplate = jdbcTemplate;
	}

	public void execute(final String sql) throws DataAccessException
	{
		LOG.debug("executing:" + sql + "...");
		Future<?> result = executors.submit(new Runnable()
		{
			@Override
			public void run()
			{
				jdbcTemplate.execute(sql);
				LOG.debug("Executed " + sql);
			}
		});

		try
		{
			result.get(30, TimeUnit.SECONDS);
		}
		catch (InterruptedException | TimeoutException e)
		{
			LOG.warn("Interrupted awaiting SQL statement: " + sql, e);
			throw new QueryTimeoutException(e);
		}
		catch (ExecutionException e)
		{
			LOG.error("Error executing SQL statement", e.getCause());
			throw (DataAccessException) e.getCause();
		}
	}

}

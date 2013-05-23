package org.molgenis.compute.db.decorators;

import java.util.Date;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.molgenis.compute.runtime.ComputeTask;
import org.molgenis.compute.runtime.ComputeTaskHistory;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.Mapper;
import org.molgenis.framework.db.MapperDecorator;
import org.molgenis.util.ApplicationUtil;

/**
 * Automatically adds a new entry to the ComputeTaskHisory if the statuscode
 * changed
 * 
 * @author erwin
 * 
 */
public class ComputeTaskDecorator extends MapperDecorator<ComputeTask>
{

	public ComputeTaskDecorator(Mapper<ComputeTask> generatedMapper)
	{
		super(generatedMapper);
	}

	@Override
	public int add(List<ComputeTask> entities) throws DatabaseException
	{
		int result = super.add(entities);

		for (ComputeTask task : entities)
		{
			ComputeTaskHistory history = new ComputeTaskHistory();
			history.setComputeTask(task);
			history.setStatusTime(new Date());
			history.setNewStatusCode(task.getStatusCode());
			getDatabase().add(history);
		}

		return result;
	}

	@Override
	public int update(List<ComputeTask> entities) throws DatabaseException
	{
		Database database = ApplicationUtil.getUnauthorizedPrototypeDatabase();
		try
		{
			for (ComputeTask task : entities)
			{
				ComputeTask old = ComputeTask.findById(database, task.getId());

				if ((old != null) && !old.getStatusCode().equalsIgnoreCase(task.getStatusCode()))
				{
					ComputeTaskHistory history = new ComputeTaskHistory();
					history.setComputeTask(task);
					history.setRunLog(task.getRunLog());
					history.setStatusTime(new Date());
					history.setStatusCode(old.getStatusCode());
					history.setNewStatusCode(task.getStatusCode());

					database.add(history);
				}

			}
		}
		finally
		{
			IOUtils.closeQuietly(database);
		}

		return super.update(entities);
	}
}

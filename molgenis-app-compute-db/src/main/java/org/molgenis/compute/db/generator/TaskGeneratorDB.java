package org.molgenis.compute.db.generator;

import org.apache.log4j.Logger;

/**
 * Created with IntelliJ IDEA. User: georgebyelas Date: 23/04/2013 Time: 08:47
 * To change this template use File | Settings | File Templates.
 */
public class TaskGeneratorDB
{
	private static final Logger LOG = Logger.getLogger(TaskGeneratorDB.class);

	public void generateTasks(String parametersFile, String backend)
	{
		LOG.info("Generating task for [" + backend + "] with parametersfile [" + parametersFile + "]");
	}
}

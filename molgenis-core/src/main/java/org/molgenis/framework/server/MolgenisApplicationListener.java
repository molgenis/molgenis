package org.molgenis.framework.server;

import org.molgenis.framework.db.Database;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextStartedEvent;

public class MolgenisApplicationListener implements ApplicationListener<ContextStartedEvent>
{
	@Autowired
	@Qualifier("unauthorizedPrototypeDatabase")
	private Database database;

	@Override
	public void onApplicationEvent(ContextStartedEvent event)
	{

	}
}

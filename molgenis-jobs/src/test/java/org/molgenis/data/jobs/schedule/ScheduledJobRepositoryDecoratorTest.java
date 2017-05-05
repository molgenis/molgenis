package org.molgenis.data.jobs.schedule;

import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.Repository;
import org.molgenis.data.jobs.model.ScheduledJob;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class ScheduledJobRepositoryDecoratorTest
{

	private ScheduledJobRepositoryDecorator scheduledJobRepositoryDecorator;

	@Mock
	private JobScheduler jobScheduler;
	@Mock
	private Repository<ScheduledJob> decoratedRepo;
	@Mock
	private DataService dataService;

	@SuppressWarnings("unchecked")
	@BeforeMethod
	public void setUpBeforeMethod()
	{
		scheduledJobRepositoryDecorator = new ScheduledJobRepositoryDecorator(decoratedRepo, jobScheduler,
				dataService);
	}

	@Test
	public void testDelegate() throws Exception
	{
		assertEquals(scheduledJobRepositoryDecorator.delegate(), decoratedRepo);
	}

	@Test
	public void testQuery() throws Exception
	{
		assertEquals(scheduledJobRepositoryDecorator.query().getRepository(), scheduledJobRepositoryDecorator);
	}
}
package org.molgenis.data.jobs.schedule;

import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Repository;
import org.molgenis.data.jobs.model.ScheduledJob;
import org.molgenis.data.jobs.model.ScheduledJobType;
import org.molgenis.data.validation.JsonValidator;
import org.springframework.security.test.context.support.WithMockUser;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

public class ScheduledJobRepositoryDecoratorTest extends AbstractMolgenisSpringTest
{
	private ScheduledJobRepositoryDecorator scheduledJobRepositoryDecorator;

	@Mock
	private JobScheduler jobScheduler;
	@Mock
	private Repository<ScheduledJob> decoratedRepo;
	@Mock
	private ScheduledJob scheduledJob;
	@Mock
	private JsonValidator jsonValidator;

	private static String schema = "{\"properties\": {\n" + "\"text\": {\n\"type\": \"string\"}}";
	private static String parameters = "{\"text\": \"test\"}";

	@Captor
	private ArgumentCaptor<Stream<ScheduledJob>> jobStreamCaptor;

	@BeforeClass
	public void beforeClass()
	{
		initMocks(this);
	}

	@SuppressWarnings("unchecked")
	@BeforeMethod
	public void setUpBeforeMethod()
	{
		reset(jobScheduler, decoratedRepo, scheduledJob, jsonValidator);

		ScheduledJobType scheduledJobType = mock(ScheduledJobType.class);
		when(scheduledJobType.getSchema()).thenReturn(schema);
		when(scheduledJob.getParameters()).thenReturn(parameters);
		when(scheduledJob.getType()).thenReturn(scheduledJobType);

		scheduledJobRepositoryDecorator = new ScheduledJobRepositoryDecorator(decoratedRepo, jobScheduler,
				jsonValidator);
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

	@Test
	public void testUpdate()
	{
		scheduledJobRepositoryDecorator.update(scheduledJob);
		verify(jsonValidator).validate(parameters, schema);
		verify(decoratedRepo).update(scheduledJob);
		verify(jobScheduler).schedule(scheduledJob);
	}

	@Test
	public void testDelete()
	{
		when(scheduledJob.getId()).thenReturn("id");
		scheduledJobRepositoryDecorator.delete(scheduledJob);
		verify(decoratedRepo).delete(scheduledJob);
		verify(jobScheduler).unschedule("id");
	}

	@Test
	public void testDeleteFails()
	{
		doThrow(new MolgenisDataException("Failed")).when(decoratedRepo).delete(scheduledJob);
		when(scheduledJob.getId()).thenReturn("id");
		try
		{
			scheduledJobRepositoryDecorator.delete(scheduledJob);
			fail("delete method should rethrow exception from delegate repository");
		}
		catch (MolgenisDataException expected)
		{
		}
		verifyNoMoreInteractions(jobScheduler);
	}

	@Test
	@WithMockUser("admin")
	public void testSetUsernameAdd()
	{
		scheduledJobRepositoryDecorator.add(scheduledJob);
		verify(scheduledJob).setUser("admin");
	}

	@Test
	@WithMockUser("other_user")
	public void testSetUsernameUpdate()
	{
		when(scheduledJob.getUser()).thenReturn("admin");
		scheduledJobRepositoryDecorator.update(scheduledJob);
		verify(scheduledJob).setUser("other_user");
	}

	@SuppressWarnings("unchecked")
	@Test(enabled = false) //FIXME
	public void testDeleteStreamFails()
	{
		doAnswer((InvocationOnMock invocation) ->
		{
			Stream<ScheduledJob> jobStream = invocation.getArgument(0);
			jobStream.collect(Collectors.toList());
			throw new MolgenisDataException("Failed");
		}).when(decoratedRepo).delete(any(Stream.class));
		when(scheduledJob.getId()).thenReturn("id");
		try
		{
			scheduledJobRepositoryDecorator.delete(Stream.of(scheduledJob));
			fail("delete method should rethrow exception from delegate repository");
		}
		catch (MolgenisDataException expected)
		{
		}

		verifyNoMoreInteractions(jobScheduler, "Jobs should not be unscheduled if deletion fails.");
	}
}
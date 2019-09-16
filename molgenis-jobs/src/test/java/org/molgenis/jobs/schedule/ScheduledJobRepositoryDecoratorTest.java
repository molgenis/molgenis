package org.molgenis.jobs.schedule;

import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Repository;
import org.molgenis.jobs.model.ScheduledJob;
import org.molgenis.jobs.model.ScheduledJobType;
import org.molgenis.validation.JsonValidator;
import org.springframework.security.test.context.annotation.SecurityTestExecutionListeners;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith({SpringExtension.class, MockitoExtension.class})
@SecurityTestExecutionListeners
class ScheduledJobRepositoryDecoratorTest {
  private static final String SCHEMA =
      "{\"properties\": {\n" + "\"text\": {\n\"type\": \"string\"}}";
  private static final String PARAMETERS = "{\"text\": \"test\"}";

  @Mock private JobScheduler jobScheduler;
  @Mock private Repository<ScheduledJob> delegateRepository;
  @Mock private ScheduledJob scheduledJob;
  @Mock private JsonValidator jsonValidator;
  private ScheduledJobRepositoryDecorator scheduledJobRepositoryDecorator;

  @BeforeEach
  void setUpBeforeMethod() {
    scheduledJobRepositoryDecorator =
        new ScheduledJobRepositoryDecorator(delegateRepository, jobScheduler, jsonValidator);
  }

  @Test
  void testQuery() {
    assertEquals(
        scheduledJobRepositoryDecorator, scheduledJobRepositoryDecorator.query().getRepository());
  }

  @Test
  void testUpdate() {
    ScheduledJobType scheduledJobType = mock(ScheduledJobType.class);
    when(scheduledJobType.getSchema()).thenReturn(SCHEMA);
    when(scheduledJob.getParameters()).thenReturn(PARAMETERS);
    when(scheduledJob.getType()).thenReturn(scheduledJobType);

    scheduledJobRepositoryDecorator.update(scheduledJob);
    verify(jsonValidator).validate(PARAMETERS, SCHEMA);
    verify(delegateRepository).update(scheduledJob);
    verify(jobScheduler).schedule(scheduledJob);
  }

  @Test
  void testDelete() {
    when(scheduledJob.getId()).thenReturn("id");
    scheduledJobRepositoryDecorator.delete(scheduledJob);
    verify(delegateRepository).delete(scheduledJob);
    verify(jobScheduler).unschedule("id");
  }

  @Test
  void testDeleteFails() {
    doThrow(new MolgenisDataException("Failed")).when(delegateRepository).delete(scheduledJob);
    when(scheduledJob.getId()).thenReturn("id");
    try {
      scheduledJobRepositoryDecorator.delete(scheduledJob);
      fail("delete method should rethrow exception from delegate repository");
    } catch (MolgenisDataException expected) {
    }
    verifyNoMoreInteractions(jobScheduler);
  }

  @Test
  @WithMockUser("admin")
  void testSetUsernameAdd() {
    ScheduledJobType scheduledJobType = mock(ScheduledJobType.class);
    when(scheduledJobType.getSchema()).thenReturn(SCHEMA);
    when(scheduledJob.getParameters()).thenReturn(PARAMETERS);
    when(scheduledJob.getType()).thenReturn(scheduledJobType);

    scheduledJobRepositoryDecorator.add(scheduledJob);
    verify(scheduledJob).setUser("admin");
  }

  @Test
  @WithMockUser("other_user")
  void testSetUsernameUpdate() {
    ScheduledJobType scheduledJobType = mock(ScheduledJobType.class);
    when(scheduledJobType.getSchema()).thenReturn(SCHEMA);
    when(scheduledJob.getParameters()).thenReturn(PARAMETERS);
    when(scheduledJob.getType()).thenReturn(scheduledJobType);

    scheduledJobRepositoryDecorator.update(scheduledJob);
    verify(scheduledJob).setUser("other_user");
  }

  @SuppressWarnings("unchecked")
  @Disabled
  @Test
  void testDeleteStreamFails() {
    ScheduledJobType scheduledJobType = mock(ScheduledJobType.class);
    when(scheduledJobType.getSchema()).thenReturn(SCHEMA);
    when(scheduledJob.getParameters()).thenReturn(PARAMETERS);
    when(scheduledJob.getType()).thenReturn(scheduledJobType);

    doAnswer(
            (InvocationOnMock invocation) -> {
              Stream<ScheduledJob> jobStream = invocation.getArgument(0);
              jobStream.collect(Collectors.toList());
              throw new MolgenisDataException("Failed");
            })
        .when(delegateRepository)
        .delete(any(Stream.class));
    when(scheduledJob.getId()).thenReturn("id");
    try {
      scheduledJobRepositoryDecorator.delete(Stream.of(scheduledJob));
      fail("delete method should rethrow exception from delegate repository");
    } catch (MolgenisDataException expected) {
    }

    verifyNoMoreInteractions(jobScheduler, "Jobs should not be unscheduled if deletion fails.");
  }
}

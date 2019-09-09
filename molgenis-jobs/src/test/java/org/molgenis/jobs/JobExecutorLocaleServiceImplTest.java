package org.molgenis.jobs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Locale;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.jobs.model.JobExecution;
import org.molgenis.test.AbstractMockitoTest;
import org.molgenis.web.i18n.UserLocaleResolver;

class JobExecutorLocaleServiceImplTest extends AbstractMockitoTest {
  @Mock private UserLocaleResolver userLocaleResolver;
  private JobExecutorLocaleServiceImpl jobExecutorLocaleServiceImpl;

  @BeforeEach
  void setUpBeforeMethod() {
    jobExecutorLocaleServiceImpl = new JobExecutorLocaleServiceImpl(userLocaleResolver);
  }

  @Test
  void testJobExecutorLocaleServiceImpl() {
    assertThrows(NullPointerException.class, () -> new JobExecutorLocaleServiceImpl(null));
  }

  @Test
  void testCreateLocaleUser() {
    String username = "MyUsername";
    JobExecution jobExecution =
        when(mock(JobExecution.class).getUser()).thenReturn(Optional.of(username)).getMock();
    Locale locale = Locale.getDefault();
    when(userLocaleResolver.resolveLocale(username)).thenReturn(locale);
    assertEquals(jobExecutorLocaleServiceImpl.createLocale(jobExecution), locale);
  }

  @Test
  void testCreateLocaleSystem() {
    Locale locale = Locale.getDefault();
    JobExecution jobExecution = mock(JobExecution.class);
    assertEquals(jobExecutorLocaleServiceImpl.createLocale(jobExecution), locale);
  }
}

package org.molgenis.data.importer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.molgenis.data.importer.ImportRunMetaData.IMPORT_RUN;
import static org.molgenis.data.importer.ImportStatus.FAILED;
import static org.testng.Assert.assertEquals;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Locale;
import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.security.user.UserService;
import org.molgenis.i18n.MessageSourceHolder;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.mail.MailSender;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ImportRunServiceTest extends AbstractMockitoTest {

  @Mock private DataService dataService;
  @Mock private MailSender mailSender;
  @Mock private UserService userService;
  @Mock private ImportRunFactory importRunFactory;
  private ImportRunService importRunService;

  @BeforeMethod
  public void setUpBeforeMethod() {
    importRunService = new ImportRunService(dataService, mailSender, userService, importRunFactory);
  }

  @Test
  public void testCreateEnglishMailText() {
    ImportRun importRun = mock(ImportRun.class);
    when(importRun.getMessage()).thenReturn("Entity already exists.");
    when(importRun.getStatus()).thenReturn(FAILED.toString());
    Instant startDate = Instant.parse("2016-02-13T12:34:56.217Z");
    when(importRun.getStartDate()).thenReturn(startDate);
    Instant endDate = Instant.parse("2016-02-13T12:35:12.231Z");
    when(importRun.getEndDate()).thenReturn(endDate);

    String mailText =
        importRunService.createEnglishMailText(importRun, ZoneId.of("Europe/Amsterdam"));
    assertEquals(
        mailText,
        "The import started by you on Saturday, February 13, 2016 1:34:56 PM CET "
            + "finished on 1:35:12 PM with status: FAILED\nMessage:\nEntity already exists.");
  }

  @Test
  public void testFailImportRun() {
    String importRunId = "importRunId";
    String mesage = "import run failed";
    ImportRun importRun = mock(ImportRun.class);
    when(dataService.findOneById(IMPORT_RUN, importRunId, ImportRun.class)).thenReturn(importRun);

    importRunService.failImportRun(importRunId, mesage);

    verify(dataService).update(IMPORT_RUN, importRun);
    verify(importRun).setMessage(mesage);
    verify(importRun).setStatus("FAILED");
    verify(importRun).setEndDate(any());
  }

  @Test
  public void testFailImportRunTrancatedMessage() {
    StringBuilder messageBuilder = new StringBuilder(65545);
    for (int i = 0; i < 65535; ++i) {
      messageBuilder.append("x");
    }
    String truncated64KbMessage = messageBuilder.toString();
    for (int i = 0; i < 10; ++i) {
      messageBuilder.append("y");
    }
    String moreThan64KbMessage = messageBuilder.toString();

    String importRunId = "importRunId";
    ImportRun importRun = mock(ImportRun.class);
    when(dataService.findOneById(IMPORT_RUN, importRunId, ImportRun.class)).thenReturn(importRun);

    importRunService.failImportRun(importRunId, moreThan64KbMessage);

    verify(dataService).update(IMPORT_RUN, importRun);
    verify(importRun).setMessage(truncated64KbMessage);
    verify(importRun).setStatus("FAILED");
    verify(importRun).setEndDate(any());
  }

  @Test
  public void testFailImportRunDoesNotExist() {
    String importRunId = "importRunId";
    String message = "import run failed";
    when(dataService.findOneById(IMPORT_RUN, importRunId, ImportRun.class)).thenReturn(null);

    importRunService.failImportRun(importRunId, message);

    verifyNoMoreInteractions(dataService);
  }

  @Test
  public void testFailImportRunNoMessage() {
    MessageSource originalMessageSource = MessageSourceHolder.getMessageSource();
    Locale originalLocale = LocaleContextHolder.getLocale();
    MessageSource messageSource = mock(MessageSource.class);
    Locale locale = Locale.getDefault();
    try {
      MessageSourceHolder.setMessageSource(messageSource);
      LocaleContextHolder.setLocale(locale);
      String persistedMessage = "unknown error";
      when(messageSource.getMessage("import_unknown_error", null, locale))
          .thenReturn(persistedMessage);
      String importRunId = "importRunId";
      ImportRun importRun = mock(ImportRun.class);
      when(dataService.findOneById(IMPORT_RUN, importRunId, ImportRun.class)).thenReturn(importRun);

      importRunService.failImportRun(importRunId, null);

      verify(dataService).update(IMPORT_RUN, importRun);
      verify(importRun).setMessage(persistedMessage);
      verify(importRun).setStatus("FAILED");
      verify(importRun).setEndDate(any());
    } finally {
      LocaleContextHolder.setLocale(originalLocale);
      MessageSourceHolder.setMessageSource(originalMessageSource);
    }
  }
}

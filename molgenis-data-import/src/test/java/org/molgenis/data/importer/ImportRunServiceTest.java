package org.molgenis.data.importer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.molgenis.data.importer.ImportRunMetadata.IMPORT_RUN;
import static org.molgenis.data.importer.ImportStatus.FAILED;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.security.user.UserService;
import org.molgenis.i18n.ContextMessageSource;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.mail.MailSender;

class ImportRunServiceTest extends AbstractMockitoTest {

  @Mock private DataService dataService;
  @Mock private MailSender mailSender;
  @Mock private UserService userService;
  @Mock private ContextMessageSource contextMessageSource;
  @Mock private ImportRunFactory importRunFactory;
  private ImportRunService importRunService;

  @BeforeEach
  void setUpBeforeMethod() {
    importRunService =
        new ImportRunService(
            dataService, mailSender, userService, contextMessageSource, importRunFactory);
  }

  @Test
  void testCreateEnglishMailText() {
    ImportRun importRun = mock(ImportRun.class);
    when(importRun.getMessage()).thenReturn("Entity already exists.");
    when(importRun.getStatus()).thenReturn(FAILED.toString());
    Instant startDate = Instant.parse("2016-02-13T12:34:56.217Z");
    when(importRun.getStartDate()).thenReturn(startDate);
    Instant endDate = Instant.parse("2016-02-13T12:35:12.231Z");
    when(importRun.getEndDate()).thenReturn(Optional.of(endDate));

    String mailText =
        importRunService.createEnglishMailText(importRun, ZoneId.of("Europe/Amsterdam"));
    assertEquals(
        "The import started by you on Saturday, February 13, 2016 at 1:34:56 PM Central European Standard Time finished on 1:35:12 PM with status: FAILED\n"
            + "Message:\n"
            + "Entity already exists.",
        mailText);
  }

  @Test
  void testFailImportRun() {
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
  void testFailImportRunTrancatedMessage() {
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
  void testFailImportRunDoesNotExist() {
    String importRunId = "importRunId";
    String message = "import run failed";
    when(dataService.findOneById(IMPORT_RUN, importRunId, ImportRun.class)).thenReturn(null);

    importRunService.failImportRun(importRunId, message);

    verifyNoMoreInteractions(dataService);
  }

  @Test
  void testFailImportRunNoMessage() {
    String persistedMessage = "unknown error";
    String importRunId = "importRunId";
    ImportRun importRun = mock(ImportRun.class);
    when(dataService.findOneById(IMPORT_RUN, importRunId, ImportRun.class)).thenReturn(importRun);
    when(contextMessageSource.getMessage("import_unknown_error")).thenReturn(persistedMessage);
    importRunService.failImportRun(importRunId, null);

    verify(dataService).update(IMPORT_RUN, importRun);
    verify(importRun).setMessage(persistedMessage);
    verify(importRun).setStatus("FAILED");
    verify(importRun).setEndDate(any());
  }

  @Test
  void testFinishImportRun() {
    String importRunId = "MyImportRunId";
    String message = "message";
    String importedEntities = "importedEntities";
    ImportRun importRun = mock(ImportRun.class);
    when(dataService.findOneById(IMPORT_RUN, importRunId, ImportRun.class)).thenReturn(importRun);
    importRunService.finishImportRun(importRunId, message, importedEntities);

    verify(importRun).setStatus(ImportStatus.FINISHED.toString());
    verify(importRun).setEndDate(any());
    verify(importRun).setMessage(message);
    verify(importRun).setImportedEntities(importedEntities);
    verify(dataService).update(IMPORT_RUN, importRun);
  }

  @Test
  void testFinishImportRunUnknown() {
    assertThrows(
        UnknownEntityException.class,
        () ->
            importRunService.finishImportRun("unknownImportRunId", "message", "importedEntities"));
  }
}

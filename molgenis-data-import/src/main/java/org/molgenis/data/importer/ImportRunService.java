package org.molgenis.data.importer;

import static java.lang.Math.toIntExact;
import static java.time.Instant.now;
import static java.time.format.DateTimeFormatter.ofLocalizedDateTime;
import static java.time.format.DateTimeFormatter.ofLocalizedTime;
import static java.time.format.FormatStyle.FULL;
import static java.time.format.FormatStyle.MEDIUM;
import static java.util.Locale.ENGLISH;
import static java.util.Objects.requireNonNull;
import static org.molgenis.data.importer.ImportRunMetaData.IMPORT_RUN;
import static org.molgenis.data.meta.AttributeType.TEXT;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Locale;
import javax.annotation.Nullable;
import org.molgenis.data.DataService;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.security.user.UserService;
import org.molgenis.i18n.MessageSourceHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Component;

@Component
public class ImportRunService {
  private static final Logger LOG = LoggerFactory.getLogger(ImportRunService.class);

  private final DataService dataService;
  private final MailSender mailSender;
  private final UserService userService;
  private final ImportRunFactory importRunFactory;

  ImportRunService(
      DataService dataService,
      MailSender mailSender,
      UserService userService,
      ImportRunFactory importRunFactory) {
    this.dataService = requireNonNull(dataService);
    this.mailSender = requireNonNull(mailSender);
    this.userService = requireNonNull(userService);
    this.importRunFactory = requireNonNull(importRunFactory);
  }

  public ImportRun addImportRun(String userName, boolean notify) {
    ImportRun importRun = importRunFactory.create();
    importRun.setStartDate(now());
    importRun.setProgress(0);
    importRun.setStatus(ImportStatus.RUNNING.toString());
    importRun.setUsername(userName); // required and visible
    importRun.setNotify(notify);
    dataService.add(IMPORT_RUN, importRun);

    return importRun;
  }

  void finishImportRun(String importRunId, String message, String importedEntities) {
    ImportRun importRun = dataService.findOneById(IMPORT_RUN, importRunId, ImportRun.class);
    try {
      importRun.setStatus(ImportStatus.FINISHED.toString());
      importRun.setEndDate(now());
      importRun.setMessage(message);
      importRun.setImportedEntities(importedEntities);
      dataService.update(IMPORT_RUN, importRun);
    } catch (Exception e) {
      LOG.error("Error updating run status", e);
    }
    if (importRun.getNotify()) createAndSendStatusMail(importRun);
  }

  private void createAndSendStatusMail(ImportRun importRun) {
    try {
      SimpleMailMessage mailMessage = new SimpleMailMessage();
      mailMessage.setTo(userService.getUser(importRun.getUsername()).getEmail());
      mailMessage.setSubject(createMailTitle(importRun));
      mailMessage.setText(createEnglishMailText(importRun, ZoneId.systemDefault()));
      mailSender.send(mailMessage);
    } catch (MailException mce) {
      LOG.error("Could not send import status mail", mce);
      throw new MolgenisDataException("An error occurred. Please contact the administrator.");
    }
  }

  /**
   * Creates an English mail message describing a finished {@link ImportRun}. Formats the run's
   * start and end times using {@link ZoneId#systemDefault()}.
   *
   * @param importRun the ImportRun to describe, it should have non-null start and end dates.
   * @return String containing the mail message.
   */
  @SuppressWarnings("squid:S3457")
  // do not use platform specific line ending
  String createEnglishMailText(ImportRun importRun, ZoneId zone) {
    ZonedDateTime start = importRun.getStartDate().atZone(zone);
    ZonedDateTime end = importRun.getEndDate().atZone(zone);
    String startDateTimeString = ofLocalizedDateTime(FULL).withLocale(ENGLISH).format(start);
    String endTimeString = ofLocalizedTime(MEDIUM).withLocale(ENGLISH).format(end);

    return String.format(
        "The import started by you on %1s finished on %2s with status: %3s\nMessage:\n%4s",
        startDateTimeString, endTimeString, importRun.getStatus(), importRun.getMessage());
  }

  private String createMailTitle(ImportRun importRun) {
    return "importRun " + importRun.getStatus();
  }

  void failImportRun(String importRunId, @Nullable String message) {
    ImportRun importRun = dataService.findOneById(IMPORT_RUN, importRunId, ImportRun.class);
    if (importRun == null) {
      return;
    }

    try {
      String persistedMessage;
      if (message == null) {
        persistedMessage = getUnknownErrorMessage();
      } else {
        if (message.length() > TEXT.getMaxLength()) {
          persistedMessage = message.substring(0, toIntExact(TEXT.getMaxLength()));
        } else {
          persistedMessage = message;
        }
      }

      importRun.setStatus(ImportStatus.FAILED.toString());
      importRun.setEndDate(now());
      importRun.setMessage(persistedMessage);
      dataService.update(IMPORT_RUN, importRun);

    } catch (Exception e) {
      LOG.error("Error updating run status", e);
    }

    if (importRun.getNotify()) {
      createAndSendStatusMail(importRun);
    }
  }

  private String getUnknownErrorMessage() {
    Locale locale = LocaleContextHolder.getLocale();
    MessageSource messageSource = MessageSourceHolder.getMessageSource();
    return messageSource.getMessage("import_unknown_error", null, locale);
  }
}

package org.molgenis.data.importer;

import org.molgenis.data.DataService;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.security.core.runas.RunAsSystem;
import org.molgenis.security.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Component;

import java.util.Date;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.importer.ImportRunMetaData.IMPORT_RUN;

@Component
public class ImportRunService
{
	private static final Logger LOG = LoggerFactory.getLogger(ImportRunService.class);

	private final DataService dataService;
	private final MailSender mailSender;
	private final UserService userService;
	private final ImportRunFactory importRunFactory;

	@Autowired
	public ImportRunService(DataService dataService, MailSender mailSender, UserService userService,
			ImportRunFactory importRunFactory)
	{
		this.dataService = requireNonNull(dataService);
		this.mailSender = requireNonNull(mailSender);
		this.userService = requireNonNull(userService);
		this.importRunFactory = requireNonNull(importRunFactory);
	}

	@RunAsSystem
	public ImportRun addImportRun(String userName, boolean notify)
	{
		ImportRun importRun = importRunFactory.create();
		importRun.setStartDate(new Date());
		importRun.setProgress(0);
		importRun.setStatus(ImportStatus.RUNNING.toString());
		importRun.setUsername(userName); // required and visible
		importRun.setOwner(userName); // not required and not visible
		importRun.setNotify(notify);
		dataService.add(IMPORT_RUN, importRun);

		return importRun;
	}

	@RunAsSystem
	public void finishImportRun(String importRunId, String message, String importedEntities)
	{
		ImportRun importRun = dataService.findOneById(IMPORT_RUN, importRunId, ImportRun.class);
		try
		{
			if (importRun != null)
			{
				importRun.setStatus(ImportStatus.FINISHED.toString());
				importRun.setEndDate(new Date());
				importRun.setMessage(message);
				importRun.setImportedEntities(importedEntities);
				dataService.update(IMPORT_RUN, importRun);
			}
		}
		catch (Exception e)
		{
			LOG.error("Error updating run status", e);
		}
		if (importRun.getNotify()) createAndSendStatusMail(importRun);
	}

	private void createAndSendStatusMail(ImportRun importRun)
	{
		try
		{
			SimpleMailMessage mailMessage = new SimpleMailMessage();
			mailMessage.setTo(userService.getUser(importRun.getUsername()).getEmail());
			mailMessage.setSubject(createMailTitle(importRun));
			mailMessage.setText(createMailText(importRun));
			mailSender.send(mailMessage);
		}
		catch (MailException mce)
		{
			LOG.error("Could not send import status mail", mce);
			throw new MolgenisDataException("An error occurred. Please contact the administrator.");
		}
	}

	private String createMailText(ImportRun importRun)
	{
		return "The import started by you at: " + importRun.getStartDate() + " has finished with status: " + importRun
				.getStatus() + "\nMessage:\n" + importRun.getMessage();
	}

	private String createMailTitle(ImportRun importRun)
	{
		return "importRun " + importRun.getStatus();
	}

	@RunAsSystem
	public void failImportRun(String importRunId, String message)
	{
		ImportRun importRun = dataService.findOneById(IMPORT_RUN, importRunId, ImportRun.class);
		try
		{
			if (importRun != null)
			{
				importRun.setStatus(ImportStatus.FAILED.toString());
				importRun.setEndDate(new Date());
				importRun.setMessage(message);
				dataService.update(IMPORT_RUN, importRun);
			}
		}
		catch (Exception e)
		{
			LOG.error("Error updating run status", e);
		}
		if (importRun.getNotify()) createAndSendStatusMail(importRun);
	}
}

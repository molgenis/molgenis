package org.molgenis.rdconnect;

import static java.util.Objects.requireNonNull;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.molgenis.data.DataService;
import org.molgenis.security.core.runas.RunAsSystemProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
public class IdCardBiobankIndexerImpl implements IdCardBiobankIndexer
{
	private final IdCardBiobankRepository idCardBiobankRepository;
	private final IdCardBiobankIndexerSettings idCardBiobankIndexerSettings;
	private final DataService dataService;
	private final JavaMailSender mailSender;

	@Autowired
	public IdCardBiobankIndexerImpl(IdCardBiobankRepository idCardBiobankRepository,
			IdCardBiobankIndexerSettings idCardBiobankIndexerSettings, DataService dataService,
			JavaMailSender mailSender)
	{
		this.idCardBiobankRepository = requireNonNull(idCardBiobankRepository);
		this.idCardBiobankIndexerSettings = requireNonNull(idCardBiobankIndexerSettings);
		this.mailSender = requireNonNull(mailSender);
		this.dataService = requireNonNull(dataService);
	}

	@Override
	public void rebuildIndex(String username)
	{
		IdCardIndexingEvent idCardIndexingEvent = new IdCardIndexingEvent(dataService);
		RuntimeException runtimeException = null;
		try
		{
			idCardBiobankRepository.rebuildIndex();

			idCardIndexingEvent.setStatus(IdCardIndexingEventStatus.SUCCESS);
			idCardIndexingEvent.setMessage(String.format("Index rebuild [%s]", username != null ? username : "System"));
		}
		catch (RuntimeException e)
		{
			idCardIndexingEvent.setStatus(IdCardIndexingEventStatus.FAILED);
			idCardIndexingEvent.setMessage(e.getMessage());
			runtimeException = e;
		}

		RunAsSystemProxy.runAsSystem(() -> {
			dataService.add(IdCardIndexingEvent.ENTITY_NAME, idCardIndexingEvent);
		});

		if (idCardIndexingEvent.getStatus() == IdCardIndexingEventStatus.FAILED)
		{
			if (username == null)
			{
				mailSender.send(createMessage(idCardIndexingEvent));
			}
			if (runtimeException != null)
			{
				throw runtimeException;
			}
		}
	}

	@Override
	public void onIndexConfigurationUpdate(String updateMessage)
	{
		// write log event to db
		IdCardIndexingEvent idCardIndexingEvent = new IdCardIndexingEvent(dataService);
		idCardIndexingEvent.setStatus(IdCardIndexingEventStatus.CONFIGURATION_CHANGE);
		idCardIndexingEvent.setMessage(updateMessage);
		RunAsSystemProxy.runAsSystem(() -> {
			dataService.add(IdCardIndexingEvent.ENTITY_NAME, idCardIndexingEvent);
		});
	}

	private MimeMessage createMessage(IdCardIndexingEvent idCardIndexingEvent)
	{
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper;
		try
		{
			helper = new MimeMessageHelper(message, false);
			helper.setTo(idCardBiobankIndexerSettings.getNotificationEmail());
			helper.setReplyTo("no-reply@molgenis.org");
			helper.setSubject("ID-Card index rebuild failed");
			helper.setText("ID-Card index rebuild failed with message:\n" + idCardIndexingEvent.getMessage());
		}
		catch (MessagingException e)
		{
			throw new RuntimeException(e);
		}
		return message;
	}
}

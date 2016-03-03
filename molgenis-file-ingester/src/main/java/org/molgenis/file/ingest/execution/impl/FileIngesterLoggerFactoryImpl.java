package org.molgenis.file.ingest.execution.impl;

import org.molgenis.data.DataService;
import org.molgenis.file.ingest.execution.FileIngesterLogger;
import org.molgenis.file.ingest.execution.FileIngesterLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class FileIngesterLoggerFactoryImpl implements FileIngesterLoggerFactory
{
	private final DataService dataService;
	private final JavaMailSender mailSender;

	@Autowired
	public FileIngesterLoggerFactoryImpl(DataService dataService, JavaMailSender mailSender)
	{
		this.dataService = dataService;
		this.mailSender = mailSender;
	}

	@Override
	public FileIngesterLogger createLogger()
	{
		return new FileIngesterLoggerImpl(dataService, mailSender);
	}
}

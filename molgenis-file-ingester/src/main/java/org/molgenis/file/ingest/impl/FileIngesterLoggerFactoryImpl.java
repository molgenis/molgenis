package org.molgenis.file.ingest.impl;

import org.molgenis.data.DataService;
import org.molgenis.file.ingest.FileIngesterLogger;
import org.molgenis.file.ingest.FileIngesterLoggerFactory;
import org.molgenis.file.ingest.meta.FileIngestJobMetaDataMetaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class FileIngesterLoggerFactoryImpl implements FileIngesterLoggerFactory
{
	private final DataService dataService;
	private final FileIngestJobMetaDataMetaData fileIngestJobMetaDataMetaData;
	private final JavaMailSender mailSender;

	@Autowired
	public FileIngesterLoggerFactoryImpl(DataService dataService,
			FileIngestJobMetaDataMetaData fileIngestJobMetaDataMetaData, JavaMailSender mailSender)
	{
		this.dataService = dataService;
		this.fileIngestJobMetaDataMetaData = fileIngestJobMetaDataMetaData;
		this.mailSender = mailSender;
	}

	@Override
	public FileIngesterLogger createLogger()
	{
		return new FileIngesterLoggerImpl(dataService, fileIngestJobMetaDataMetaData, mailSender);
	}


}

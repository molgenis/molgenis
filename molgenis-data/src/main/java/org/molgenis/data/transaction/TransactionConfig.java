package org.molgenis.data.transaction;

import javax.annotation.PostConstruct;

import org.molgenis.data.DataService;
import org.molgenis.data.transaction.index.IndexTransactionLogEntryMetaData;
import org.molgenis.data.transaction.index.IndexTransactionLogMetaData;
import org.molgenis.data.transaction.index.IndexTransactionLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TransactionConfig
{
	@Autowired
	private DataService dataService;

	@Autowired
	private MolgenisTransactionManager transactionManager;

	@Value("${transaction.log.backend:ElasticSearch}")
	private String transactionLogBackend;

	@Bean
	public MolgenisTransactionLogMetaData molgenisTransactionLogMetaData()
	{
		return new MolgenisTransactionLogMetaData(transactionLogBackend);
	}

	@Bean
	public MolgenisTransactionLogEntryMetaData molgenisTransactionLogEntryMetaData()
	{
		return new MolgenisTransactionLogEntryMetaData(molgenisTransactionLogMetaData(), transactionLogBackend);
	}

	@Bean
	public IndexTransactionLogMetaData indexTransactionLogMetaData()
	{
		return new IndexTransactionLogMetaData(transactionLogBackend);
	}

	@Bean
	public IndexTransactionLogEntryMetaData indexTransactionLogEntryMetaData()
	{
		return new IndexTransactionLogEntryMetaData(indexTransactionLogMetaData(), transactionLogBackend);
	}

	@Bean
	public AsyncTransactionLog asyncTransactionLog()
	{
		return new AsyncTransactionLog(dataService);
	}

	@Bean
	public TransactionLogService transactionLogService()
	{
		return new TransactionLogService(dataService, molgenisTransactionLogMetaData(),
				molgenisTransactionLogEntryMetaData(), asyncTransactionLog());
	}

	@Bean
	public IndexTransactionLogService indexTransactionLogService()
	{
		return new IndexTransactionLogService(dataService, indexTransactionLogMetaData(),
				indexTransactionLogEntryMetaData());
	}

	@PostConstruct
	public void init()
	{
		transactionManager.addTransactionListener(transactionLogService());
	}
}

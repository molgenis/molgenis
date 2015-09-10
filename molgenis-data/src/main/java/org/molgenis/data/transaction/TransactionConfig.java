package org.molgenis.data.transaction;

import javax.annotation.PostConstruct;

import org.molgenis.data.DataService;
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
	public LockMetaData lockMetaData()
	{
		return new LockMetaData(molgenisTransactionLogMetaData(), transactionLogBackend);
	}

	@Bean
	public MolgenisTransactionLogEntryMetaData molgenisTransactionLogEntryMetaData()
	{
		return new MolgenisTransactionLogEntryMetaData(molgenisTransactionLogMetaData(), transactionLogBackend);
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
				molgenisTransactionLogEntryMetaData(), lockMetaData(), asyncTransactionLog());
	}

	@PostConstruct
	public void init()
	{
		transactionManager.addTransactionListener(transactionLogService());
	}
}

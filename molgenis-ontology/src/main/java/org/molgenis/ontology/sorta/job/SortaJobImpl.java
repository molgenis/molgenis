package org.molgenis.ontology.sorta.job;

import org.molgenis.data.jobs.Progress;
import org.molgenis.data.jobs.TransactionalJob;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.support.TransactionTemplate;

import static java.util.Objects.requireNonNull;

public class SortaJobImpl extends TransactionalJob<Void>
{
	private final SortaJobProcessor sortaJobProcessor;

	public SortaJobImpl(SortaJobProcessor matchInputTermBatchService, Authentication authentication, Progress progress,
			TransactionTemplate transactionTemplate)
	{
		super(progress, transactionTemplate, authentication);
		this.sortaJobProcessor = requireNonNull(matchInputTermBatchService);
	}

	@Override
	public Void call(Progress progress) throws Exception
	{
		sortaJobProcessor.process();
		return null;
	}
}

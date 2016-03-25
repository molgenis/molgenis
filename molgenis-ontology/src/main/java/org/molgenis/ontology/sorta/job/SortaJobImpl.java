package org.molgenis.ontology.sorta.job;

import static java.util.Objects.requireNonNull;

import org.molgenis.data.jobs.Job;
import org.molgenis.data.jobs.Progress;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.support.TransactionTemplate;

public class SortaJobImpl extends Job<Void>
{
	private final SortaJobProcessor sortaJobProcessor;

	public SortaJobImpl(SortaJobProcessor matchInputTermBatchService, Authentication authentication,
			Progress progress, TransactionTemplate transactionTemplate)
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

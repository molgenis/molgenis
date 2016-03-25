package org.molgenis.ontology.sorta.job;

import org.molgenis.data.jobs.Job;
import org.molgenis.data.jobs.Progress;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.transaction.support.TransactionTemplate;

import static java.util.Objects.requireNonNull;

public class SortaJobImpl extends Job<Void>
{
	private final SortaJobProcessor sortaJobProcessor;

	public SortaJobImpl(SortaJobProcessor matchInputTermBatchService, SecurityContext securityContext,
			Progress progress, TransactionTemplate transactionTemplate)
	{
		super(progress, transactionTemplate, securityContext.getAuthentication());
		this.sortaJobProcessor = requireNonNull(matchInputTermBatchService);
	}

	@Override
	public Void call(Progress progress) throws Exception
	{
		sortaJobProcessor.process();
		return null;
	}
}

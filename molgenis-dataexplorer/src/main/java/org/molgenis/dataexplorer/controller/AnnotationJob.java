package org.molgenis.dataexplorer.controller;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.Repository;
import org.molgenis.data.annotation.CrudRepositoryAnnotator;
import org.molgenis.data.annotation.RepositoryAnnotator;
import org.molgenis.data.jobs.Job;
import org.molgenis.data.jobs.Progress;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.support.TransactionTemplate;

import com.google.common.collect.Lists;

public class AnnotationJob extends Job<Void>
{
	private final CrudRepositoryAnnotator crudRepositoryAnnotator;
	private final String username;
	private final List<RepositoryAnnotator> annotators;
	private final Repository repository;
	List<String> successfulAnnotators = Lists.newArrayList();
	List<String> failedAnnotators = Lists.newArrayList();
	Exception firstException = null;

	public AnnotationJob(CrudRepositoryAnnotator crudRepositoryAnnotator, String username,
			List<RepositoryAnnotator> annotators, Repository repository, Progress progress,
			Authentication userAuthentication, TransactionTemplate transactionTemplate)
	{
		super(progress, transactionTemplate, userAuthentication);

		this.crudRepositoryAnnotator = requireNonNull(crudRepositoryAnnotator);
		this.username = requireNonNull(username);
		this.annotators = requireNonNull(annotators);
		this.repository = requireNonNull(repository);
	}

	@Override
	public Void call(Progress progress) throws IOException
	{
		progress.setProgressMax(annotators.size());
		int i = 0;

		for (RepositoryAnnotator annotator : annotators)
		{
			progress.progress(i, getMessage(i, annotator));
			try
			{
				crudRepositoryAnnotator.annotate(annotator, repository);
				successfulAnnotators.add(annotator.getSimpleName());
			}
			catch (Exception ex)
			{
				if (firstException == null)
				{
					firstException = ex;
				}
				failedAnnotators.add(annotator.getSimpleName());
			}
			i++;
		}
		progress.progress(annotators.size(), getMessage());
		if (firstException != null)
		{
			progress.status("Failed annotators: " + StringUtils.join(failedAnnotators, ","));
			progress.failed(firstException);
		}
		return null;
	}

	private String getMessage()
	{
		return String.format("Annotated \"%s\" with %s (started by \"%s\")", repository.getEntityMetaData().getLabel(),
				StringUtils.join(successfulAnnotators, ","), username);
	}

	private String getMessage(int i, RepositoryAnnotator annotator)
	{
		return String.format("Annotating \"%s\" with %s (annotator %d of %d, started by \"%s\")",
				repository.getEntityMetaData().getLabel(), annotator.getSimpleName(), i + 1, annotators.size(),
				username);
	}
}

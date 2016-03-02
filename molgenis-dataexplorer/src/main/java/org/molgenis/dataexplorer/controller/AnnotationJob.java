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

public class AnnotationJob extends Job
{
	private final CrudRepositoryAnnotator crudRepositoryAnnotator;
	private final String username;
	private final List<RepositoryAnnotator> annotators;
	private final Repository repository;

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
	public void run(Progress progress) throws IOException
	{
		progress.setProgressMax(annotators.size());
		int i = 0;
		for (RepositoryAnnotator annotator : annotators)
		{
			progress.progress(i, getMessage(i, annotator));
			crudRepositoryAnnotator.annotate(annotator, repository);
			i++;
		}
		progress.progress(annotators.size(), getSuccessMessage());
	}

	private String getSuccessMessage()
	{
		Iterable<String> annotatorNames = (Iterable<String>) Lists.transform(annotators,
				RepositoryAnnotator::getSimpleName);
		return String.format("Annotated \"%s\" with %s (started by \"%s\")", repository.getEntityMetaData().getLabel(),
				StringUtils.join(annotatorNames, ","), username);
	}

	private String getMessage(int i, RepositoryAnnotator annotator)
	{
		return String.format("Annotating \"%s\" with %s (annotator %d of %d, started by \"%s\")",
				repository.getEntityMetaData().getLabel(), annotator.getSimpleName(), i + 1, annotators.size(),
				username);
	}
}

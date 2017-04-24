package org.molgenis.data.mapper.job;

import com.google.common.collect.Lists;
import org.molgenis.data.jobs.Job;
import org.molgenis.data.jobs.Progress;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

import static java.util.Objects.requireNonNull;

public class MappingJob extends Job<Void>
{
	private final String username;
	List<String> successfulAnnotators = Lists.newArrayList();
	List<String> failedAnnotators = Lists.newArrayList();
	Exception firstException = null;

	public MappingJob(String username, Progress progress, Authentication userAuthentication,
			TransactionTemplate transactionTemplate)
	{
		super(progress, transactionTemplate, userAuthentication);

		this.username = requireNonNull(username);
	}

	@Override
	public Void call(Progress progress) throws Exception
	{
		//		progress.setProgressMax(annotators.size());
		//		int i = 0;
		//
		//		for (RepositoryAnnotator annotator : annotators)
		//		{
		//			progress.progress(i, getMessage(i, annotator));
		//			try
		//			{
		//				//	crudRepositoryAnnotator.annotate(annotator, repository);
		//				successfulAnnotators.add(annotator.getSimpleName());
		//			}
		//			catch (Exception ex)
		//			{
		//				if (firstException == null)
		//				{
		//					firstException = ex;
		//				}
		//				failedAnnotators.add(annotator.getSimpleName());
		//			}
		//			i++;
		//		}
		//		progress.progress(annotators.size(), getMessage());
		//		try
		//		{
		//			// TODO: Workaround to make sure that the progress bar gets loaded
		//			Thread.sleep(1000);
		//		}
		//		catch (InterruptedException e)
		//		{
		//		}
		//		if (firstException != null)
		//		{
		//			progress.status(
		//					"Failed annotators: " + StringUtils.join(failedAnnotators, ",") + ". Successful annotators: "
		//							+ StringUtils.join(successfulAnnotators, ","));
		//			throw firstException;
		//		}
		//		return null;
		return null;
	}

	//	private String getMessage()
	//	{
	//		return String.format("Annotated \"%s\" with %s (started by \"%s\")", repository.getEntityType().getLabel(),
	//				StringUtils.join(successfulAnnotators, ","), username);
	//	}
	//
	//	private String getMessage(int i, RepositoryAnnotator annotator)
	//	{
	//		return String.format("Annotating \"%s\" with %s (annotator %d of %d, started by \"%s\")",
	//				repository.getEntityType().getLabel(), annotator.getSimpleName(), i + 1, annotators.size(), username);
	//	}
}

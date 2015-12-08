package org.molgenis.data.annotation;

import org.molgenis.data.DataService;
import org.molgenis.data.Repository;
import org.molgenis.security.core.MolgenisPermissionService;
import org.molgenis.security.permission.PermissionSystemService;
import org.molgenis.security.user.UserAccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.http.HttpSession;

public class AnnotatorJob implements Runnable
{
	private static final Logger LOG = LoggerFactory.getLogger(AnnotatorJob.class);

	private final SecurityContext securityContext;
	private final String annotationRunId;
	private final AnnotatorRunService annotatorRunService;
	private final HttpSession session;
	private final String[] annotatorNames;
	private final DataService dataService;
	private final PermissionSystemService permissionSystemService;
	private final UserAccountService userAccountService;
	private final MolgenisPermissionService molgenisPermissionService;
	private final AnnotationService annotationService;
	private Repository repository;

	public AnnotatorJob(DataService dataService, SecurityContext securityContext, String[] annotatorNames,
			String annotationRunId, AnnotatorRunService annotatorRunService, HttpSession session, Repository repository,
			PermissionSystemService permissionSystemService, UserAccountService userAccountService,
			MolgenisPermissionService molgenisPermissionService, AnnotationService annotationService)
	{
		//FIXME: give proper messages
		//FIXME: add nullchecks for concats
		//TODO: figure out order of annotators
		//TODO unit test everything
		//TODO move whole system to Quartz
		this.securityContext = securityContext;
		this.dataService = dataService;
		this.annotationRunId = annotationRunId;
		this.annotatorRunService = annotatorRunService;
		this.session = session;
		this.annotatorNames = annotatorNames;
		this.permissionSystemService = permissionSystemService;
		this.userAccountService = userAccountService;
		this.molgenisPermissionService = molgenisPermissionService;
		this.annotationService = annotationService;
		this.repository = repository;
	}

	@Override
	public void run()
	{
		try
		{
			long t0 = System.currentTimeMillis();
			LOG.info("Annotations started");

			SecurityContextHolder.setContext(securityContext);

			CrudRepositoryAnnotator crudRepositoryAnnotator = new CrudRepositoryAnnotator(dataService,
					getNewRepositoryName(annotatorNames, repository.getEntityMetaData().getSimpleName()),
					permissionSystemService, userAccountService, molgenisPermissionService);

			for (String annotatorName : annotatorNames)
			{
				annotatorRunService.updateAnnotatorStarted(annotationRunId, annotatorName);
				RepositoryAnnotator annotator = annotationService.getAnnotatorByName(annotatorName);
				if (annotator != null)
				{
					try
					{
						repository = crudRepositoryAnnotator.annotate(annotator, repository, false);
					}
					catch (Exception e)
					{
						annotatorRunService.updateAnnotatorFailed(annotationRunId, annotatorName);
					}
				}
				annotatorRunService.updateAnnotatorFinished(annotationRunId, annotatorName);
			}

			session.setAttribute("SPRING_SECURITY_CONTEXT", securityContext);

			try
			{
				session.setAttribute("SPRING_SECURITY_CONTEXT", securityContext);
			}
			catch (IllegalStateException e)
			{
				// session invalidated
			}
			annotatorRunService.finishAnnotationRun(annotationRunId, "ALL DONE!");
			long t = System.currentTimeMillis();
			LOG.info("Annotations finished in " + (t - t0) + " msec.");
		}
		catch (Exception e)
		{
			LOG.info("Annotations failed.", e);
			annotatorRunService.failAnnotationRun(annotationRunId, e.getMessage());
		}
	}

	private String getNewRepositoryName(String[] annotatorNames, String repositoryName)
	{
		String newRepositoryName = repositoryName;
		for (String annotatorName : annotatorNames)
		{
			newRepositoryName = newRepositoryName + "_" + annotatorName;
		}
		return newRepositoryName;
	}
}

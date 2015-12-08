package org.molgenis.data.annotation;

import org.molgenis.data.DataService;
import org.molgenis.data.annotators.AnnotationRun;
import org.molgenis.security.core.runas.RunAsSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class AnnotatorRunService
{
	private static final Logger LOG = LoggerFactory.getLogger(AnnotatorRunService.class);

	private final DataService dataService;

	@Autowired
	public AnnotatorRunService(DataService dataService)
	{
		this.dataService = dataService;
	}

	@RunAsSystem
	public AnnotationRun addAnnotationRun(String userName)
	{
		AnnotationRun annotationRun = new AnnotationRun();
		annotationRun.setStartDate(new Date());
		annotationRun.setStatus("RUNNING");
		annotationRun.setUserName(userName);
		dataService.add(AnnotationRun.ENTITY_NAME, annotationRun);

		return annotationRun;
	}

	@RunAsSystem
	public void finishAnnotationRun(String annotationRunId, String message)
	{
		try
		{
			AnnotationRun annotationRun = dataService.findOne(AnnotationRun.ENTITY_NAME, annotationRunId, AnnotationRun.class);
			if (annotationRun != null)
			{
				annotationRun.setStatus("FINISHED");
				annotationRun.setEndDate(new Date());
				annotationRun.setMessage(message);
				dataService.update(AnnotationRun.ENTITY_NAME, annotationRun);
			}
		}
		catch (Exception e)
		{
			LOG.error("Error updating run status", e);
		}
	}

	@RunAsSystem
	public void failAnnotationRun(String annotationRunId, String message)
	{
		try
		{
			AnnotationRun annotationRun = dataService.findOne(AnnotationRun.ENTITY_NAME, annotationRunId, AnnotationRun.class);
			if (annotationRun != null)
			{
				annotationRun.setStatus("FAILED");
				annotationRun.setEndDate(new Date());
				annotationRun.setMessage(message);
				dataService.update(AnnotationRun.ENTITY_NAME, annotationRun);
			}
		}
		catch (Exception e)
		{
			LOG.error("Error updating run status", e);
		}
	}

	@RunAsSystem
	public void updateAnnotatorFinished(String annotationRunId, String annotator)
	{
		try
		{
			AnnotationRun annotationRun = dataService.findOne(AnnotationRun.ENTITY_NAME, annotationRunId, AnnotationRun.class);
			if (annotationRun != null)
			{
				annotationRun.setAnnotatorsFinished(annotationRun.getAnnotatorsFinished() + "," + annotator);
				dataService.update(AnnotationRun.ENTITY_NAME, annotationRun);
			}
		}
		catch (Exception e)
		{
			LOG.error("Error updating run status", e);
		}
	}

	@RunAsSystem
	public void updateAnnotatorFailed(String annotationRunId, String annotator)
	{
		try
		{
			AnnotationRun annotationRun = dataService.findOne(AnnotationRun.ENTITY_NAME, annotationRunId, AnnotationRun.class);
			if (annotationRun != null)
			{
				annotationRun.setAnnotatorsFailed(annotationRun.getAnnotatorsFailed() + "," + annotator);
				dataService.update(AnnotationRun.ENTITY_NAME, annotationRun);
			}
		}
		catch (Exception e)
		{
			LOG.error("Error updating run status", e);
		}
	}

	@RunAsSystem
	public void updateAnnotatorStarted(String annotationRunId, String annotator)
	{
		try
		{
			AnnotationRun annotationRun = dataService.findOne(AnnotationRun.ENTITY_NAME, annotationRunId, AnnotationRun.class);
			if (annotationRun != null)
			{
				annotationRun.setAnnotatorsStarted(annotationRun.getAnnotatorsStarted() + "," + annotator);
				dataService.update(AnnotationRun.ENTITY_NAME, annotationRun);
			}
		}
		catch (Exception e)
		{
			LOG.error("Error updating run status", e);
		}
	}
}

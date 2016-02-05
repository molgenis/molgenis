package org.molgenis.data.annotation;

import org.molgenis.data.DataService;
import org.molgenis.security.core.runas.RunAsSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

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
	public AnnotationRun addAnnotationRun(String userName, String[] annotatorNames, String entityName)
	{
		AnnotationRun annotationRun = new AnnotationRun();
		annotationRun.setId(UUID.randomUUID().toString());
		annotationRun.setStatus("RUNNING");
		annotationRun.setUser(userName);
		annotationRun.setMessage("Starting to annotate entity: "+entityName+" with annotators: " + String.join(",", annotatorNames));
		annotationRun.setEntity(entityName);
		dataService.add(AnnotationRunMetadata.ENTITY_NAME, annotationRun);

		return annotationRun;
	}

	@RunAsSystem
	public void finishAnnotationRun(String annotationRunId, String message)
	{
		try
		{
			AnnotationRun annotationRun = dataService.findOne(AnnotationRunMetadata.ENTITY_NAME, annotationRunId, AnnotationRun.class);
			if (annotationRun != null)
			{
				annotationRun.setStatus("FINISHED");
				annotationRun.setMessage(annotationRun.getMessage() +"\n"+ message);
				dataService.update(AnnotationRunMetadata.ENTITY_NAME, annotationRun);
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
			AnnotationRun annotationRun = dataService.findOne(AnnotationRunMetadata.ENTITY_NAME, annotationRunId, AnnotationRun.class);
			if (annotationRun != null)
			{
				annotationRun.setStatus("FAILED");
				annotationRun.setMessage(annotationRun.getMessage() +"\n"+ message);
				dataService.update(AnnotationRunMetadata.ENTITY_NAME, annotationRun);
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
			AnnotationRun annotationRun = dataService.findOne(AnnotationRunMetadata.ENTITY_NAME, annotationRunId, AnnotationRun.class);
			if (annotationRun != null)
			{
				annotationRun.setMessage(annotationRun.getMessage()+"\nFinished annotating with "+annotator);
				dataService.update(AnnotationRunMetadata.ENTITY_NAME, annotationRun);
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
			AnnotationRun annotationRun = dataService.findOne(AnnotationRunMetadata.ENTITY_NAME, annotationRunId, AnnotationRun.class);
			if (annotationRun != null)
			{
				annotationRun.setMessage(annotationRun.getMessage()+ "\nFailed annotating with " + annotator);
				dataService.update(AnnotationRunMetadata.ENTITY_NAME, annotationRun);
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
			AnnotationRun annotationRun = dataService.findOne(AnnotationRunMetadata.ENTITY_NAME, annotationRunId, AnnotationRun.class);
			if (annotationRun != null)
			{
				annotationRun.setMessage(annotationRun.getMessage()+ "\nStarted annotating with " + annotator);
				dataService.update(AnnotationRunMetadata.ENTITY_NAME, annotationRun);
			}
		}
		catch (Exception e)
		{
			LOG.error("Error updating run status", e);
		}
	}
}

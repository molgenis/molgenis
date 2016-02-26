package org.molgenis.data.annotation;

import static java.util.stream.Collectors.toList;

import java.util.Arrays;
import java.util.List;

import org.molgenis.data.DataService;
import org.molgenis.data.Repository;
import org.molgenis.data.annotation.meta.AnnotationJobExecution;
import org.molgenis.data.jobs.ProgressImpl;
import org.molgenis.data.support.AnnotatorDependencyOrderResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

/**
 * Creates AnnotationJob based on its {@link AnnotationJobExecution}. Is a bean so that it can use {@link Autowired}
 * services needed to rehydrate the primitive data types. Runs at execution time.
 */
@Component
public class AnnotationJobFactory
{
	@Autowired
	CrudRepositoryAnnotator crudRepositoryAnnotator;

	@Autowired
	DataService dataService;

	@Autowired
	private AnnotationService annotationService;

	public AnnotationJob createJob(AnnotationJobExecution metaData)
	{
		dataService.add(AnnotationJobExecution.ENTITY_NAME, metaData);
		String annotatorNames = metaData.getAnnotators();
		String target = metaData.getTarget();
		String username = metaData.getUser().getUsername();

		Repository repository = dataService.getRepository(target);
		List<RepositoryAnnotator> availableAnnotators = annotationService.getAllAnnotators().stream()
				.filter(RepositoryAnnotator::annotationDataExists).collect(toList());
		List<RepositoryAnnotator> requestedAnnotators = Arrays.stream(annotatorNames.split(","))
				.map(annotationService::getAnnotatorByName).collect(toList());
		AnnotatorDependencyOrderResolver resolver = new AnnotatorDependencyOrderResolver();
		List<RepositoryAnnotator> annotators = Lists.newArrayList(
				resolver.getAnnotatorSelectionDependencyList(availableAnnotators, requestedAnnotators, repository));
		return new AnnotationJob(crudRepositoryAnnotator, username, annotators, repository,
				new ProgressImpl(metaData, dataService));
	}
}

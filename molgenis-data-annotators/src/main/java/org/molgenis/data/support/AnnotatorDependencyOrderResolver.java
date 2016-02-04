package org.molgenis.data.support;

import autovalue.shaded.com.google.common.common.collect.Lists;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Repository;
import org.molgenis.data.annotation.RepositoryAnnotator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

public class AnnotatorDependencyOrderResolver
{
	private RepositoryAnnotator requestedAnnotator;

	public Queue<RepositoryAnnotator> getAnnotatorSelectionDependencyList(
			List<RepositoryAnnotator> availableAnnotatorList,
			List<RepositoryAnnotator> requestedAnnotatorList, Repository repo)
	{
		Queue<RepositoryAnnotator> sortedList = new LinkedList<>();
		for (RepositoryAnnotator annotator : requestedAnnotatorList)
		{
			if (!sortedList.contains(annotator))
			{
				requestedAnnotator = annotator;
				sortedList = getSingleAnnotatorDependencyList(annotator, availableAnnotatorList, sortedList,
						repo.getEntityMetaData());
			}
		}
		return sortedList;
	}

	private Queue<RepositoryAnnotator> getSingleAnnotatorDependencyList(RepositoryAnnotator selectedAnnotator,
			List<RepositoryAnnotator> annotatorList, Queue<RepositoryAnnotator> queue, EntityMetaData emd)
	{
		EntityMetaData entityMetaData = new DefaultEntityMetaData(emd); // create a copy because we do not want to
																		// change the actual metadata of the entity
		resolveAnnotatorDependencies(selectedAnnotator, annotatorList, queue, entityMetaData);
		return queue;
	}

	private void resolveAnnotatorDependencies(RepositoryAnnotator selectedAnnotator,
			List<RepositoryAnnotator> annotatorList, Queue<RepositoryAnnotator> queue, EntityMetaData entityMetaData)
	{
		if (!areRequiredAttributesAvailable(Lists.newArrayList(entityMetaData.getAtomicAttributes()),
				selectedAnnotator.getInputMetaData()))
		{
			for (AttributeMetaData input : selectedAnnotator.getInputMetaData())
			{
				if (!areRequiredAttributesAvailable(Arrays.asList(input),
						Lists.newArrayList(entityMetaData.getAtomicAttributes())))
				{
					annotatorList.stream().filter(a -> !a.equals(selectedAnnotator)).collect(Collectors.toList())
							.forEach(annotator -> resolveAnnotatorDependencies(selectedAnnotator, annotatorList, queue,
									entityMetaData, input, annotator));
				}
			}
		}
		else
		{
			if (!queue.contains(selectedAnnotator)) queue.add(selectedAnnotator);
			if (!selectedAnnotator.equals(requestedAnnotator))
				resolveAnnotatorDependencies(requestedAnnotator, annotatorList, queue, entityMetaData);
		}
		if (queue.size() == 0)
		{
			throw new UnresolvedAnnotatorDependencyException("unsolved for: " + requestedAnnotator);
		}

	}

	private void resolveAnnotatorDependencies(RepositoryAnnotator selectedAnnotator,
			List<RepositoryAnnotator> annotatorList, Queue<RepositoryAnnotator> queue, EntityMetaData entityMetaData,
			AttributeMetaData input, RepositoryAnnotator annotator)
	{
		if (isRequiredAttributeAvailable(annotator.getInfo().getOutputAttributes(), input))
		{
			if (areRequiredAttributesAvailable(Lists.newArrayList(entityMetaData.getAtomicAttributes()),
					annotator.getInputMetaData()))
			{
				if (!queue.contains(selectedAnnotator))
				{
					queue.add(annotator);
				}
				annotator.getInfo().getOutputAttributes()
						.forEach(((DefaultEntityMetaData) entityMetaData)::addAttributeMetaData);
				annotatorList.remove(annotator);
				resolveAnnotatorDependencies(requestedAnnotator, annotatorList, queue, entityMetaData);
			}
			else
			{
				resolveAnnotatorDependencies(annotator, annotatorList, queue, entityMetaData);
			}
		}
	}

	private boolean areRequiredAttributesAvailable(List<AttributeMetaData> annotatorOutputs,
			List<AttributeMetaData> requiredInputs)
	{
		for (AttributeMetaData attr : requiredInputs)
		{
			if (!isRequiredAttributeAvailable(annotatorOutputs, attr))
			{
				return false;
			}
		}
		return true;
	}

	private boolean isRequiredAttributeAvailable(List<AttributeMetaData> annotatorOutputs, AttributeMetaData attr)
	{
		for (AttributeMetaData annotatorAttr : annotatorOutputs)
		{
			if (attr.getName().equals(annotatorAttr.getName())
					&& attr.getDataType().equals(annotatorAttr.getDataType()))
			{
				return true;
			}
		}
		return false;
	}
}

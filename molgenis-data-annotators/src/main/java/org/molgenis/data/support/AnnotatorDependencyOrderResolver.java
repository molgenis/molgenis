package org.molgenis.data.support;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.annotation.RepositoryAnnotator;
import org.molgenis.data.meta.AttributeMetaData;
import org.molgenis.data.meta.EntityMetaData;

import com.google.common.collect.Lists;

public class AnnotatorDependencyOrderResolver
{
	private RepositoryAnnotator requestedAnnotator;

	public Queue<RepositoryAnnotator> getAnnotatorSelectionDependencyList(
			List<RepositoryAnnotator> availableAnnotatorList, List<RepositoryAnnotator> requestedAnnotatorList,
			Repository<Entity> repo)
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
		EntityMetaData entityMetaData = null; // FIXME new EntityMetaData(emd); // create a copy because we do not want to
																		// change the actual metadata of the entity
		resolveAnnotatorDependencies(selectedAnnotator, annotatorList, queue, entityMetaData);
		return queue;
	}

	private void resolveAnnotatorDependencies(RepositoryAnnotator selectedAnnotator,
			List<RepositoryAnnotator> annotatorList, Queue<RepositoryAnnotator> annotatorQueue, EntityMetaData entityMetaData)
	{
		if (!areRequiredAttributesAvailable(Lists.newArrayList(entityMetaData.getAtomicAttributes()),
				selectedAnnotator.getRequiredAttributes()))
		{
			for (AttributeMetaData requiredInputAttribute : selectedAnnotator.getRequiredAttributes())
			{
				if (!areRequiredAttributesAvailable(Lists.newArrayList(entityMetaData.getAtomicAttributes()),
						Arrays.asList(requiredInputAttribute)))
				{
					annotatorList.stream().filter(a -> !a.equals(selectedAnnotator)).collect(Collectors.toList())
							.forEach(annotator -> resolveAnnotatorDependencies(selectedAnnotator, annotatorList, annotatorQueue,
									entityMetaData, requiredInputAttribute, annotator));
				}
			}
		}
		else
		{
			if (!annotatorQueue.contains(selectedAnnotator)) annotatorQueue.add(selectedAnnotator);
			if (!selectedAnnotator.equals(requestedAnnotator))
				resolveAnnotatorDependencies(requestedAnnotator, annotatorList, annotatorQueue, entityMetaData);
		}
		if (annotatorQueue.size() == 0)
		{
			throw new UnresolvedAnnotatorDependencyException("unsolved for: " + requestedAnnotator);
		}

	}

	private void resolveAnnotatorDependencies(RepositoryAnnotator selectedAnnotator,
			List<RepositoryAnnotator> annotatorList, Queue<RepositoryAnnotator> annotatorQueue, EntityMetaData entityMetaData,
			AttributeMetaData requiredAttribute, RepositoryAnnotator annotator)
	{
		if (isRequiredAttributeAvailable(annotator.getInfo().getOutputAttributes(), requiredAttribute))
		{
			if (areRequiredAttributesAvailable(
					Lists.newArrayList(entityMetaData.getAtomicAttributes()),annotator.getRequiredAttributes()))
			{
				if (!annotatorQueue.contains(selectedAnnotator))
				{
					annotatorQueue.add(annotator);
				}
				annotator.getInfo().getOutputAttributes()
						.forEach(((EntityMetaData) entityMetaData)::addAttribute);
				annotatorList.remove(annotator);
				resolveAnnotatorDependencies(requestedAnnotator, annotatorList, annotatorQueue, entityMetaData);
			}
			else
			{
				resolveAnnotatorDependencies(annotator, annotatorList, annotatorQueue, entityMetaData);
			}
		}
	}

	private boolean areRequiredAttributesAvailable(List<AttributeMetaData> availableAttributes,
			List<AttributeMetaData> requiredAttributes)
	{
		for (AttributeMetaData attr : requiredAttributes)
		{
			if (!isRequiredAttributeAvailable(availableAttributes, attr))
			{
				return false;
			}
		}
		return true;
	}

	private boolean isRequiredAttributeAvailable(List<AttributeMetaData> availableAttributes, AttributeMetaData requiredAttribute)
	{
		for (AttributeMetaData availableAttribute : availableAttributes)
		{
			if (requiredAttribute.getName().equals(availableAttribute.getName()))
			{
				if (requiredAttribute.getDataType().getEnumType().equals(MolgenisFieldTypes.FieldTypeEnum.TEXT))
				{
					return requiredAttribute.getDataType().getEnumType().equals(MolgenisFieldTypes.FieldTypeEnum.TEXT)
							|| requiredAttribute.getDataType().getEnumType().equals(MolgenisFieldTypes.FieldTypeEnum.STRING);
				}
				else return requiredAttribute.getDataType().equals(availableAttribute.getDataType());
			}
		}
		return false;
	}
}

package org.molgenis.data.annotation.core.utils;

import com.google.common.collect.Lists;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.annotation.core.EffectBasedAnnotator;
import org.molgenis.data.annotation.core.RepositoryAnnotator;
import org.molgenis.data.annotation.core.exception.UnresolvedAnnotatorDependencyException;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

import static org.molgenis.data.meta.AttributeType.STRING;
import static org.molgenis.data.meta.AttributeType.TEXT;

public class AnnotatorDependencyOrderResolver
{
	private RepositoryAnnotator requestedAnnotator;

	public Queue<RepositoryAnnotator> getAnnotatorSelectionDependencyList(
			List<RepositoryAnnotator> availableAnnotatorList, List<RepositoryAnnotator> requestedAnnotatorList,
			Repository<Entity> repo, EntityTypeFactory entityTypeFactory)
	{
		Queue<RepositoryAnnotator> sortedList = new LinkedList<>();
		for (RepositoryAnnotator annotator : requestedAnnotatorList)
		{
			if (annotator instanceof EffectBasedAnnotator)
			{
				// FIXME: implement correct dependency resolving for Effect annotator
				sortedList.add(annotator);
			}
			else if (!sortedList.contains(annotator))
			{
				requestedAnnotator = annotator;
				sortedList = getSingleAnnotatorDependencyList(annotator, availableAnnotatorList, sortedList,
						repo.getEntityType(), entityTypeFactory);
			}
		}
		return sortedList;
	}

	private Queue<RepositoryAnnotator> getSingleAnnotatorDependencyList(RepositoryAnnotator selectedAnnotator,
			List<RepositoryAnnotator> annotatorList, Queue<RepositoryAnnotator> queue, EntityType emd,
			EntityTypeFactory entityTypeFactory)
	{
		EntityType entityType = entityTypeFactory.create(emd);
		resolveAnnotatorDependencies(selectedAnnotator, annotatorList, queue, entityType);
		return queue;
	}

	private void resolveAnnotatorDependencies(RepositoryAnnotator selectedAnnotator,
			List<RepositoryAnnotator> annotatorList, Queue<RepositoryAnnotator> annotatorQueue, EntityType entityType)
	{
		if (!areRequiredAttributesAvailable(Lists.newArrayList(entityType.getAtomicAttributes()),
				selectedAnnotator.getRequiredAttributes()))
		{
			selectedAnnotator.getRequiredAttributes()
							 .stream()
							 .filter(requiredInputAttribute -> !areRequiredAttributesAvailable(
									 Lists.newArrayList(entityType.getAtomicAttributes()),
									 Collections.singletonList(requiredInputAttribute)))
							 .forEachOrdered(requiredInputAttribute -> annotatorList.stream()
										  .filter(a -> !a.equals(selectedAnnotator))
										  .collect(Collectors.toList())
										  .forEach(annotator -> resolveAnnotatorDependencies(selectedAnnotator,
												  annotatorList, annotatorQueue, entityType, requiredInputAttribute,
												  annotator)));
		}
		else
		{
			if (!annotatorQueue.contains(selectedAnnotator)) annotatorQueue.add(selectedAnnotator);
			if (!selectedAnnotator.equals(requestedAnnotator))
				resolveAnnotatorDependencies(requestedAnnotator, annotatorList, annotatorQueue, entityType);
		}
		if (annotatorQueue.size() == 0)
		{
			// FIXME: what to do for ref entity annotator.
			throw new UnresolvedAnnotatorDependencyException("unsolved for: " + requestedAnnotator);
		}

	}

	private void resolveAnnotatorDependencies(RepositoryAnnotator selectedAnnotator,
			List<RepositoryAnnotator> annotatorList, Queue<RepositoryAnnotator> annotatorQueue, EntityType entityType,
			Attribute requiredAttribute, RepositoryAnnotator annotator)
	{
		if (isRequiredAttributeAvailable(annotator.getInfo().getOutputAttributes(), requiredAttribute))
		{
			if (areRequiredAttributesAvailable(Lists.newArrayList(entityType.getAtomicAttributes()),
					annotator.getRequiredAttributes()))
			{
				if (!annotatorQueue.contains(selectedAnnotator))
				{
					annotatorQueue.add(annotator);
				}
				annotator.getInfo().getOutputAttributes().forEach(((EntityType) entityType)::addAttribute);
				annotatorList.remove(annotator);
				resolveAnnotatorDependencies(requestedAnnotator, annotatorList, annotatorQueue, entityType);
			}
			else
			{
				resolveAnnotatorDependencies(annotator, annotatorList, annotatorQueue, entityType);
			}
		}
	}

	private boolean areRequiredAttributesAvailable(List<Attribute> availableAttributes,
			List<Attribute> requiredAttributes)
	{
		for (Attribute attr : requiredAttributes)
		{
			if (!isRequiredAttributeAvailable(availableAttributes, attr))
			{
				return false;
			}
		}
		return true;
	}

	private boolean isRequiredAttributeAvailable(List<Attribute> availableAttributes, Attribute requiredAttribute)
	{
		for (Attribute availableAttribute : availableAttributes)
		{
			if (requiredAttribute.getName().equals(availableAttribute.getName()))
			{
				if (requiredAttribute.getDataType() == TEXT)
				{
					return availableAttribute.getDataType() == TEXT || availableAttribute.getDataType() == STRING;
				}
				else return requiredAttribute.getDataType() == availableAttribute.getDataType();
			}
		}
		return false;
	}
}

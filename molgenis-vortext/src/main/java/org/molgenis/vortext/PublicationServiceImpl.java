package org.molgenis.vortext;

import java.util.ArrayList;
import java.util.List;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.support.DefaultEntity;
import org.molgenis.textmining.PublicationAnnotationGroupMetaData;
import org.molgenis.textmining.PublicationAnnotationMetaData;
import org.molgenis.textmining.PublicationMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PublicationServiceImpl implements PublicationService
{
	private static final Logger LOG = LoggerFactory.getLogger(PublicationServiceImpl.class);
	private final DataService dataService;

	@Autowired
	public PublicationServiceImpl(DataService dataService)
	{
		this.dataService = dataService;
	}

	@Transactional
	@Override
	public void addAnnotationGroups(String publicationId, List<AnnotationGroup> annotationGroups)
	{
		LOG.info("Going to add AnnotationGroups to publication '" + publicationId + "'");

		List<Entity> groupEntities = new ArrayList<Entity>();
		for (AnnotationGroup group : annotationGroups)
		{
			Entity groupEntity = new DefaultEntity(new PublicationAnnotationGroupMetaData(), dataService);
			groupEntity.set(PublicationAnnotationGroupMetaData.TYPE, group.getType());
			groupEntity.set(PublicationAnnotationGroupMetaData.DESCRIPTION, group.getDescription());

			List<Entity> annotationEntities = new ArrayList<Entity>();
			for (Annotation annotation : group.getAnnotations())
			{
				Entity annotationEntity = new DefaultEntity(new PublicationAnnotationMetaData(), dataService);
				annotationEntity.set(PublicationAnnotationMetaData.CONTENT, annotation.getContent());
				dataService.add(PublicationAnnotationMetaData.ENTITY_NAME, annotationEntity);
				annotationEntities.add(annotationEntity);
			}

			groupEntity.set(PublicationAnnotationGroupMetaData.ANNOTATIONS, annotationEntities);
			dataService.add(PublicationAnnotationGroupMetaData.ENTITY_NAME, groupEntity);
			groupEntities.add(groupEntity);
		}

		synchronized (this)
		{
			Entity publicationEntity = dataService.findOne(PublicationMetaData.ENTITY_NAME, publicationId);
			publicationEntity.set(PublicationMetaData.ANNOTATION_GROUPS, groupEntities);
			dataService.update(PublicationMetaData.ENTITY_NAME, publicationEntity);
		}

		LOG.info("Publication '" + publicationId + " updated");
	}

	@Override
	public boolean exists(String publicationId)
	{
		return dataService.findOne(PublicationMetaData.ENTITY_NAME, publicationId) != null;
	}

}

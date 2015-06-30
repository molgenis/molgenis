package org.molgenis.vortext;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.IdGenerator;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.support.DefaultEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.file.FileDownloadController;
import org.molgenis.file.FileMeta;
import org.molgenis.file.FileStore;
import org.molgenis.textmining.PublicationAnnotationGroupMetaData;
import org.molgenis.textmining.PublicationAnnotationMetaData;
import org.molgenis.textmining.PublicationMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;

@Service
public class PublicationServiceImpl implements PublicationService
{
	private static final Logger LOG = LoggerFactory.getLogger(PublicationServiceImpl.class);
	private static final String PDF_CONTENT_TYPE = "application/pdf";
	private final DataService dataService;
	private final IdGenerator idGenerator;
	private final FileStore fileStore;

	@Autowired
	public PublicationServiceImpl(DataService dataService, IdGenerator idGenerator, FileStore fileStore)
	{
		this.dataService = dataService;
		this.idGenerator = idGenerator;
		this.fileStore = fileStore;
	}

	@Override
	public void saveMarginalia(String fileMetaId, Marginalia marginalia)
	{
		FileMeta pdf = dataService.findOne(FileMeta.ENTITY_NAME, fileMetaId, FileMeta.class);
		if (pdf == null)
		{
			throw new UnknownEntityException("Unknown FileMeta with id '" + fileMetaId + "'");
		}

		synchronized (this)
		{
			Entity existing = dataService.findOne(PublicationMetaData.ENTITY_NAME,
					new QueryImpl().eq(PublicationMetaData.PDF_FILE, pdf));
			if (existing != null)
			{
				deletePublication(existing);
			}
		}

		Entity publicationEntity = new DefaultEntity(new PublicationMetaData(), dataService);

		List<Entity> groupEntities = new ArrayList<Entity>();
		for (Marginalis marginalis : marginalia)
		{
			List<Entity> annotationEntities = new ArrayList<Entity>();
			for (Annotation annotation : marginalis)
			{
				Entity annotationEntity = new DefaultEntity(new PublicationAnnotationMetaData(), dataService);
				annotationEntity.set(PublicationAnnotationMetaData.CONTENT, annotation.getContent());
				annotationEntity.set(PublicationAnnotationMetaData.UUID, annotation.getUuid());
				annotationEntities.add(annotationEntity);
			}

			if (!annotationEntities.isEmpty())
			{
				dataService.add(PublicationAnnotationMetaData.ENTITY_NAME, annotationEntities);
			}

			Entity groupEntity = new DefaultEntity(new PublicationAnnotationGroupMetaData(), dataService);
			groupEntity.set(PublicationAnnotationGroupMetaData.ANNOTATIONS, annotationEntities);
			groupEntity.set(PublicationAnnotationGroupMetaData.TYPE, marginalis.getType());
			groupEntity.set(PublicationAnnotationGroupMetaData.TITLE, marginalis.getTitle());
			groupEntity.set(PublicationAnnotationGroupMetaData.DESCRIPTION, marginalis.getDescription());

			groupEntities.add(groupEntity);
		}

		if (!groupEntities.isEmpty())
		{
			dataService.add(PublicationAnnotationGroupMetaData.ENTITY_NAME, groupEntities);
		}

		publicationEntity.set(PublicationMetaData.ANNOTATION_GROUPS, groupEntities);
		publicationEntity.set(PublicationMetaData.PDF_FILE, pdf);
		dataService.add(PublicationMetaData.ENTITY_NAME, publicationEntity);

		LOG.info("Saved pdf '" + pdf.getFilename() + "' and annotations.");
	}

	@Override
	public FileMeta savePdf(String baseUri, String filename, long size, InputStream pdf)
	{
		String id = idGenerator.generateId();
		try
		{
			fileStore.store(pdf, id);

		}
		catch (IOException e)
		{
			throw new UncheckedIOException(e);
		}

		FileMeta fileEntity = new FileMeta(dataService);
		fileEntity.setId(id);
		fileEntity.setFilename(filename);
		fileEntity.setContentType(PDF_CONTENT_TYPE);
		fileEntity.setSize(size);
		fileEntity.setUrl(baseUri + FileDownloadController.URI + "/" + id);
		dataService.add(FileMeta.ENTITY_NAME, fileEntity);

		return fileEntity;
	}

	@Override
	public Marginalia getMarginalia(String fileMetaId)
	{
		Marginalia marginalia = new Marginalia();

		Entity publicationEntity = dataService.findOne(PublicationMetaData.ENTITY_NAME,
				new QueryImpl().eq(PublicationMetaData.PDF_FILE, fileMetaId));

		if (publicationEntity != null)
		{
			for (Entity groupEntity : publicationEntity.getEntities(PublicationMetaData.ANNOTATION_GROUPS))
			{
				Marginalis marginalis = new Marginalis();
				marginalis.setDescription(groupEntity.getString(PublicationAnnotationGroupMetaData.DESCRIPTION));
				marginalis.setType(groupEntity.getString(PublicationAnnotationGroupMetaData.TYPE));
				marginalis.setTitle(groupEntity.getString(PublicationAnnotationGroupMetaData.TITLE));

				for (Entity annotationEntity : groupEntity.getEntities(PublicationAnnotationGroupMetaData.ANNOTATIONS))
				{
					Annotation annotation = new Annotation();
					annotation.setContent(annotationEntity.getString(PublicationAnnotationMetaData.CONTENT));
					annotation.setUuid(annotationEntity.getString(PublicationAnnotationMetaData.UUID));
					marginalis.addAnnotation(annotation);
				}
				marginalia.addMarginalis(marginalis);
			}
		}

		return marginalia;
	}

	private void deletePublication(Entity publication)
	{
		Iterable<Entity> annotationGroups = publication.getEntities(PublicationMetaData.ANNOTATION_GROUPS);
		List<Entity> annotations = new ArrayList<Entity>();

		for (Entity annotationGroup : annotationGroups)
		{
			annotations.addAll(Lists.newArrayList(annotationGroup
					.getEntities(PublicationAnnotationGroupMetaData.ANNOTATIONS)));

		}

		dataService.delete(PublicationMetaData.ENTITY_NAME, publication);
		dataService.delete(PublicationAnnotationGroupMetaData.ENTITY_NAME, annotationGroups);
		dataService.delete(PublicationAnnotationMetaData.ENTITY_NAME, annotations);
	}

}

package org.molgenis.lifelines.resourcemanager;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.molgenis.atom.ContentType;
import org.molgenis.atom.EntryType;
import org.molgenis.atom.FeedType;
import org.molgenis.atom.IdType;
import org.molgenis.lifelines.catalogue.CatalogInfo;
import org.molgenis.lifelines.hl7.jaxb.QualityMeasureDocument;
import org.molgenis.lifelines.studydefinition.StudyDefinitionInfo;
import org.w3c.dom.Node;

/**
 * Connection to the LL Resource Manager REST Service
 * 
 * 
 * @author erwin
 * 
 */
public class ResourceManagerService
{
	private static final Logger LOG = Logger.getLogger(ResourceManagerService.class);
	private final String resourceManagerServiceUrl;

	public ResourceManagerService(String resourceManagerServiceUrl)
	{
		if (resourceManagerServiceUrl == null) throw new IllegalArgumentException("ResourceManagerServiceUrl is null");
		this.resourceManagerServiceUrl = resourceManagerServiceUrl;
	}

	/**
	 * Gets all available StudyDefinitions
	 * 
	 * @return
	 */
	public List<StudyDefinitionInfo> findStudyDefinitions()
	{
		try
		{
			FeedType feed = getFeed("/studydefinition");

			List<StudyDefinitionInfo> studyDefinitions = new ArrayList<StudyDefinitionInfo>();
			for (Object entryElementObj : feed.getAuthorOrCategoryOrContributor())
			{

				@SuppressWarnings("unchecked")
				EntryType entry = ((JAXBElement<EntryType>) entryElementObj).getValue();

				for (Object obj : entry.getAuthorOrCategoryOrContent())
				{
					JAXBElement<?> element = (JAXBElement<?>) obj;
					if (element.getDeclaredType() == IdType.class)
					{
						IdType idType = (IdType) element.getValue();
						studyDefinitions.add(new StudyDefinitionInfo(idType.getValue()));
					}
				}
			}

			return studyDefinitions;
		}
		catch (JAXBException e)
		{
			LOG.error("JAXBException findStudyDefinitions()", e);
			throw new RuntimeException(e);
		}
		catch (IOException e)
		{
			LOG.error("JAXBException findStudyDefinitions()", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Gets all available catalogs
	 * 
	 * @return List of CatalogInfo
	 */
	public List<CatalogInfo> findCatalogs()
	{

		try
		{
			FeedType feed = getFeed("/catalogrelease");

			List<CatalogInfo> catalogs = new ArrayList<CatalogInfo>();

			for (Object entryElementObj : feed.getAuthorOrCategoryOrContributor())
			{

				@SuppressWarnings("unchecked")
				EntryType entry = ((JAXBElement<EntryType>) entryElementObj).getValue();

				for (Object obj : entry.getAuthorOrCategoryOrContent())
				{
					JAXBContext jaxbContext = JAXBContext.newInstance(QualityMeasureDocument.class);
					Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

					JAXBElement<?> element = (JAXBElement<?>) obj;
					if (element.getDeclaredType() == ContentType.class)
					{
						ContentType content = (ContentType) element.getValue();
						Node qualityMeasureDocumentNode = (Node) content.getContent().get(0);

						JAXBElement<QualityMeasureDocument> qualityMeasureDocumentElement = jaxbUnmarshaller.unmarshal(
								qualityMeasureDocumentNode, QualityMeasureDocument.class);

						QualityMeasureDocument qualityMeasureDocument = qualityMeasureDocumentElement.getValue();
						if (qualityMeasureDocument.getId() != null)
						{
							catalogs.add(new CatalogInfo(qualityMeasureDocument.getId().getExtension(),
									qualityMeasureDocument.getName()));
						}
						else
						{
							LOG.error("Found QualityMeasureDocument without an id");
						}
					}
				}

			}

			return catalogs;
		}
		catch (JAXBException e)
		{
			LOG.error("JAXBException findCatalogs()", e);
			throw new RuntimeException(e);
		}
		catch (IOException e)
		{
			LOG.error("JAXBException findCatalogs()", e);
			throw new RuntimeException(e);
		}

	}

	private FeedType getFeed(String uri) throws JAXBException, IOException
	{
		JAXBContext jaxbContext = JAXBContext.newInstance("org.molgenis.atom");
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

		String resourceUrl = resourceManagerServiceUrl + uri;
		JAXBElement<FeedType> feed = null;

		URL url = new URL(resourceUrl);
		InputStream xml = url.openStream();
		try
		{
			feed = jaxbUnmarshaller.unmarshal(new StreamSource(xml), FeedType.class);
			return feed.getValue();
		}
		finally
		{
			IOUtils.closeQuietly(xml);
		}

	}

}

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
import org.molgenis.lifelines.catalogue.CatalogInfo;
import org.molgenis.lifelines.hl7.jaxb.QualityMeasureDocument;
import org.molgenis.lifelines.studydefinition.StudyDefinitionInfo;
import org.w3c.dom.Node;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

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
		List<CatalogSearchResult> catalogs = findCatalogs("/studydefinition");
		return Lists.transform(catalogs, new Function<CatalogSearchResult, StudyDefinitionInfo>()
		{
			@Override
			public StudyDefinitionInfo apply(CatalogSearchResult input)
			{
				return new StudyDefinitionInfo(input.getId(), input.getName());
			}
		});
	}

	public QualityMeasureDocument findStudyDefinition(String id)
	{
		InputStream xmlStream = null;
		try
		{
			URL url = new URL(resourceManagerServiceUrl + "/studydefinition/" + id);
			xmlStream = url.openStream();

			JAXBContext jaxbContext = JAXBContext.newInstance(QualityMeasureDocument.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			return jaxbUnmarshaller.unmarshal(new StreamSource(xmlStream), QualityMeasureDocument.class).getValue();
		}
		catch (JAXBException e)
		{
			throw new RuntimeException(e);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
		finally
		{
			IOUtils.closeQuietly(xmlStream);
		}
	}

	/**
	 * Gets all available catalogs
	 * 
	 * @return List of CatalogInfo
	 */
	public List<CatalogInfo> findCatalogs()
	{
		List<CatalogSearchResult> catalogs = findCatalogs("/catalogrelease");
		return Lists.transform(catalogs, new Function<CatalogSearchResult, CatalogInfo>()
		{
			@Override
			public CatalogInfo apply(CatalogSearchResult input)
			{
				return new CatalogInfo(input.getId(), input.getName());
			}
		});
	}

	private List<CatalogSearchResult> findCatalogs(String uri)
	{
		try
		{
			FeedType feed = getFeed(uri);

			List<CatalogSearchResult> catalogs = new ArrayList<CatalogSearchResult>();

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
							catalogs.add(new CatalogSearchResult(qualityMeasureDocument.getId().getExtension(),
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

	private class CatalogSearchResult
	{
		private final String id;
		private final String name;

		public CatalogSearchResult(String id, String name)
		{
			this.id = id;
			this.name = name;
		}

		public String getId()
		{
			return id;
		}

		public String getName()
		{
			return name;
		}

	}
}

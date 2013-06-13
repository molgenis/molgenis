package org.molgenis.lifelines.resourcemanager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.molgenis.atom.ContentType;
import org.molgenis.atom.EntryType;
import org.molgenis.atom.FeedType;
import org.molgenis.hl7.ObjectFactory;
import org.molgenis.hl7.POQMMT000001UVQualityMeasureDocument;
import org.molgenis.hl7.ST;
import org.molgenis.lifelines.catalogue.CatalogInfo;
import org.molgenis.omx.study.StudyDefinitionInfo;
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
public class GenericLayerResourceManagerService
{
	private static final Logger LOG = Logger.getLogger(GenericLayerResourceManagerService.class);
	private final String resourceManagerServiceUrl;
	private final Schema emeasureSchema;
	private final boolean validate;

	public GenericLayerResourceManagerService(String resourceManagerServiceUrl, Schema emeasureSchema, boolean validate)
	{
		if (resourceManagerServiceUrl == null) throw new IllegalArgumentException("ResourceManagerServiceUrl is null");
		if (emeasureSchema == null) throw new IllegalArgumentException("EmeasureSchema is null");
		this.resourceManagerServiceUrl = resourceManagerServiceUrl;
		this.emeasureSchema = emeasureSchema;
		this.validate = validate;
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

	private Marshaller createQualityMeasureDocumentMarshaller() throws JAXBException
	{
		JAXBContext jaxbContext = JAXBContext.newInstance(POQMMT000001UVQualityMeasureDocument.class);
		Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

		if (validate)
		{
			jaxbMarshaller.setSchema(emeasureSchema);
		}

		return jaxbMarshaller;
	}

	private Unmarshaller createQualityMeasureDocumentUnmarshaller() throws JAXBException
	{
		JAXBContext jaxbContext = JAXBContext.newInstance(POQMMT000001UVQualityMeasureDocument.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

		if (validate)
		{
			jaxbUnmarshaller.setSchema(emeasureSchema);
		}

		return jaxbUnmarshaller;
	}

	public POQMMT000001UVQualityMeasureDocument findStudyDefinition(String id)
	{
		InputStream xmlStream = null;
		try
		{
			URL url = new URL(resourceManagerServiceUrl + "/studydefinition/" + id);
			xmlStream = url.openStream();

			return createQualityMeasureDocumentUnmarshaller().unmarshal(new StreamSource(xmlStream),
					POQMMT000001UVQualityMeasureDocument.class).getValue();
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
	 * Persist a studydefinition
	 * 
	 * @param studyDefinition
	 */
	public void persistStudyDefinition(POQMMT000001UVQualityMeasureDocument studyDefinition)
	{
		HttpURLConnection connection = null;
		try
		{
			URL url = new URL(resourceManagerServiceUrl + "/studydefinition");
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/xml");
			connection.setUseCaches(false);
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setConnectTimeout(10000);

			OutputStream outputStream = connection.getOutputStream();
			createQualityMeasureDocumentMarshaller().marshal(
					new ObjectFactory().createQualityMeasureDocument(studyDefinition), outputStream);
			IOUtils.closeQuietly(outputStream);

			int responseCode = connection.getResponseCode();
			if (responseCode < 200 || responseCode > 299) throw new IOException(
					"Error persisting study definition (statuscode " + responseCode + ")");
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
		catch (JAXBException e)
		{
			throw new RuntimeException(e);
		}
		finally
		{
			if (connection != null) connection.disconnect();
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
					Unmarshaller jaxbUnmarshaller = createQualityMeasureDocumentUnmarshaller();

					JAXBElement<?> element = (JAXBElement<?>) obj;
					if (element.getDeclaredType() == ContentType.class)
					{
						ContentType content = (ContentType) element.getValue();
						Node qualityMeasureDocumentNode = (Node) content.getContent().get(0);

						JAXBElement<POQMMT000001UVQualityMeasureDocument> qualityMeasureDocumentElement = jaxbUnmarshaller
								.unmarshal(qualityMeasureDocumentNode, POQMMT000001UVQualityMeasureDocument.class);

						POQMMT000001UVQualityMeasureDocument qualityMeasureDocument = qualityMeasureDocumentElement
								.getValue();
						if (qualityMeasureDocument.getId() != null)
						{
							ST title = qualityMeasureDocument.getTitle();
							catalogs.add(new CatalogSearchResult(qualityMeasureDocument.getId().getExtension(),
									title != null ? title.getContent().toString() : ""));
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

	private static class CatalogSearchResult
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

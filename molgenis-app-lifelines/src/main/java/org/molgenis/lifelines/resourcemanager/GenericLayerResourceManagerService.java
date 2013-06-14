package org.molgenis.lifelines.resourcemanager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.log4j.Logger;
import org.molgenis.atom.ContentType;
import org.molgenis.atom.EntryType;
import org.molgenis.atom.FeedType;
import org.molgenis.hl7.ObjectFactory;
import org.molgenis.hl7.POQMMT000001UVQualityMeasureDocument;
import org.molgenis.hl7.ST;
import org.molgenis.lifelines.catalogue.CatalogInfo;
import org.molgenis.lifelines.utils.GenericLayerDataBinder;
import org.molgenis.lifelines.utils.OutputStreamHttpEntity;
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

	private final HttpClient httpClient;
	private final String resourceManagerServiceUrl;
	private final GenericLayerDataBinder genericLayerDataBinder;

	public GenericLayerResourceManagerService(HttpClient httpClient, String resourceManagerServiceUrl,
			GenericLayerDataBinder genericLayerDataBinder)
	{
		if (httpClient == null) throw new IllegalArgumentException("HttpClient is null");
		if (resourceManagerServiceUrl == null) throw new IllegalArgumentException("ResourceManagerServiceUrl is null");
		if (genericLayerDataBinder == null) throw new IllegalArgumentException("GenericLayerDataBinder is null");
		this.httpClient = httpClient;
		this.resourceManagerServiceUrl = resourceManagerServiceUrl;
		this.genericLayerDataBinder = genericLayerDataBinder;
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

	public POQMMT000001UVQualityMeasureDocument findStudyDefinition(String id)
	{
		HttpGet httpGet = new HttpGet(resourceManagerServiceUrl + "/studydefinition/" + id);
		InputStream xmlStream = null;
		try
		{
			HttpResponse response = httpClient.execute(httpGet);
			xmlStream = response.getEntity().getContent();
			return genericLayerDataBinder.createQualityMeasureDocumentUnmarshaller()
					.unmarshal(new StreamSource(xmlStream), POQMMT000001UVQualityMeasureDocument.class).getValue();
		}
		catch (RuntimeException e)
		{
			httpGet.abort();
			throw e;
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
			IOUtils.closeQuietly(xmlStream);
		}
	}

	/**
	 * Persist a studydefinition
	 * 
	 * @param studyDefinition
	 */
	public void persistStudyDefinition(final POQMMT000001UVQualityMeasureDocument studyDefinition)
	{
		HttpPost httpPost = new HttpPost(resourceManagerServiceUrl + "/studydefinition");
		httpPost.setHeader("Content-Type", "application/xml");
		httpPost.setEntity(new OutputStreamHttpEntity()
		{
			@Override
			public void writeTo(final OutputStream outstream) throws IOException
			{
				try
				{
					genericLayerDataBinder.createQualityMeasureDocumentMarshaller().marshal(
							new ObjectFactory().createQualityMeasureDocument(studyDefinition), outstream);
				}
				catch (JAXBException e)
				{
					throw new RuntimeException(e);
				}
				outstream.close();
			}
		});

		try
		{
			HttpResponse response = httpClient.execute(httpPost);
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode < 200 || statusCode > 299) throw new IOException(
					"Error persisting study definition (statuscode " + statusCode + ")");
		}
		catch (RuntimeException e)
		{
			httpPost.abort();
			throw e;
		}
		catch (IOException e)
		{
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
					Unmarshaller jaxbUnmarshaller = genericLayerDataBinder.createQualityMeasureDocumentUnmarshaller();

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

		HttpGet httpGet = new HttpGet(resourceManagerServiceUrl + uri);
		InputStream xmlStream = null;
		try
		{
			HttpResponse response = httpClient.execute(httpGet);
			xmlStream = response.getEntity().getContent();
			return jaxbUnmarshaller.unmarshal(new StreamSource(xmlStream), FeedType.class).getValue();
		}
		catch (RuntimeException e)
		{
			httpGet.abort();
			throw e;
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
			IOUtils.closeQuietly(xmlStream);
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

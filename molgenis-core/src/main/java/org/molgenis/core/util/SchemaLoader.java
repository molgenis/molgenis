package org.molgenis.core.util;

import org.apache.commons.io.IOUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

/**
 * Compiles a xsd. Searches on the classpath for the xsd. You don't need to specify the whole path, just the name.
 * Example: <code>new SchemaLoader("EMeasure.xsd")</code>
 *
 * @author erwin
 */
public class SchemaLoader implements LSResourceResolver
{
	private Schema schema;

	public SchemaLoader(String schemaName)
	{
		try
		{

			Resource schemaResource = getSchema(schemaName);

			SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			schemaFactory.setResourceResolver(this);

			schema = schemaFactory.newSchema(new StreamSource(schemaResource.getInputStream()));
		}
		catch (SAXException | IOException e)
		{
			throw new RuntimeException("Could not load schemas", e);
		}
	}

	public SchemaLoader(InputStream is)
	{
		try
		{
			SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			schemaFactory.setResourceResolver(this);

			schema = schemaFactory.newSchema(new StreamSource(is));
		}
		catch (SAXException e)
		{
			throw new RuntimeException("Could not load schemas", e);
		}
	}

	public Schema getSchema()
	{
		return schema;
	}

	private Resource getSchema(String schemaName) throws IOException
	{
		if (schemaName.contains("/"))
		{
			schemaName = schemaName.substring(schemaName.lastIndexOf('/'));
		}

		ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
		String searchPattern = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + "/**/" + schemaName;
		Resource[] resources = resourcePatternResolver.getResources(searchPattern);

		if ((resources == null) || (resources.length == 0))
		{
			throw new RuntimeException("Could not find schema [" + schemaName + "]");
		}

		return resources[0];
	}

	@Override
	public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI)
	{
		InputStream resourceAsStream;
		try
		{
			resourceAsStream = getSchema(systemId).getInputStream();
		}
		catch (IOException e)
		{

			throw new RuntimeException(e);
		}

		return new LSInputImpl(publicId, systemId, resourceAsStream);
	}

	protected static class LSInputImpl implements LSInput
	{

		private String publicId;

		private String systemId;

		@Override
		public String getPublicId()
		{
			return publicId;
		}

		@Override
		public void setPublicId(String publicId)
		{
			this.publicId = publicId;
		}

		@Override
		public String getBaseURI()
		{
			return null;
		}

		@Override
		public InputStream getByteStream()
		{
			return null;
		}

		@Override
		public boolean getCertifiedText()
		{
			return false;
		}

		@Override
		public Reader getCharacterStream()
		{
			return null;
		}

		@Override
		public String getEncoding()
		{
			return null;
		}

		@Override
		public String getStringData()
		{
			synchronized (inputStream)
			{
				try
				{
					return IOUtils.toString(inputStream, "UTF-8");
				}
				catch (IOException e)
				{
					throw new RuntimeException(e);
				}
			}
		}

		@Override
		public void setBaseURI(String baseURI)
		{
		}

		@Override
		public void setByteStream(InputStream byteStream)
		{
		}

		@Override
		public void setCertifiedText(boolean certifiedText)
		{
		}

		@Override
		public void setCharacterStream(Reader characterStream)
		{
		}

		@Override
		public void setEncoding(String encoding)
		{
		}

		@Override
		public void setStringData(String stringData)
		{
		}

		@Override
		public String getSystemId()
		{
			return systemId;
		}

		@Override
		public void setSystemId(String systemId)
		{
			this.systemId = systemId;
		}

		public BufferedInputStream getInputStream()
		{
			return inputStream;
		}

		public void setInputStream(BufferedInputStream inputStream)
		{
			this.inputStream = inputStream;
		}

		private BufferedInputStream inputStream;

		public LSInputImpl(String publicId, String sysId, InputStream input)
		{
			this.publicId = publicId;
			this.systemId = sysId;
			this.inputStream = new BufferedInputStream(input);
		}

	}

}

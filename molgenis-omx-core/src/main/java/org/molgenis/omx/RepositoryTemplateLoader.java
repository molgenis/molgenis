package org.molgenis.omx;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.apache.log4j.Logger;
import org.molgenis.data.Queryable;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.omx.core.FreemarkerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import freemarker.cache.TemplateLoader;

/**
 * Loads FreemarkerTemplates from a DataService.
 */
@Component
public class RepositoryTemplateLoader implements TemplateLoader
{
	private final Queryable repository;

	private static final Logger LOGGER = Logger.getLogger(RepositoryTemplateLoader.class);

	@Autowired
	public RepositoryTemplateLoader(Queryable repository)
	{
		this.repository = repository;
	}
	
	@Override
	public void closeTemplateSource(Object arg0) throws IOException
	{
		// noop
	}

	@Override
	public Object findTemplateSource(String name) throws IOException
	{
		FreemarkerTemplate template = (FreemarkerTemplate) repository.findOne(new QueryImpl().eq("Name", name));
		if (template == null)
		{
			return null;
		}
		LOGGER.info("Loaded Freemarker Template " + template.getName());
		return new TemplateSource(template);
	}

	@Override
	public long getLastModified(Object source)
	{
		return ((TemplateSource) source).getLastModified();
	}

	@Override
	public Reader getReader(Object source, String encoding) throws IOException
	{
		return ((TemplateSource) source).getReader();
	}

	/**
	 * Template source class. Needed to enable caching.
	 */
	public class TemplateSource
	{
		private final FreemarkerTemplate template;
		private final long lastModified = System.currentTimeMillis();

		public TemplateSource(FreemarkerTemplate template)
		{
			this.template = template;
		}

		@Override
		public String toString()
		{
			return template.getName();
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + ((template == null) ? 0 : template.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			TemplateSource other = (TemplateSource) obj;
			if (!template.equals(other.template)) return false;
			if (!template.getValue().equals(other.template.getValue()))
			{
				return false;
			}
			return true;
		}

		public String getValue()
		{
			return template.getValue();
		}

		public Reader getReader()
		{
			LOGGER.info("Read Freemarker Template " + this + " from FreemarkerTemplate repository.");
			return new StringReader(getValue());
		}

		/**
		 * Checks if this version still exists in the repository.
		 */
		private long getLastModified()
		{
			long result = lastModified;
			FreemarkerTemplate currentVersion = (FreemarkerTemplate) repository.findOne(template.getId());
			if (currentVersion == null || !currentVersion.getValue().equals(template.getValue()))
			{
				LOGGER.debug(this + " is modified!");
				result = System.currentTimeMillis();
			}
			else
			{
				LOGGER.debug(this + " is unchanged");
			}
			return result;
		}
	}
}

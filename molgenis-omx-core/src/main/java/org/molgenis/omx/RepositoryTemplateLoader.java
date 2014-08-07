package org.molgenis.omx;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.apache.log4j.Logger;
import org.molgenis.data.Queryable;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.omx.core.FreemarkerTemplate;

import freemarker.cache.TemplateLoader;

/**
 * Loads FreemarkerTemplates from a repository.
 */
public class RepositoryTemplateLoader implements TemplateLoader
{
	private final Queryable repository;

	private static final Logger LOGGER = Logger.getLogger(RepositoryTemplateLoader.class);

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
		TemplateSource templateSource = new TemplateSource(template);
		LOGGER.debug("Created " + templateSource);
		return templateSource;
	}

	/**
	 * The repository does not offer a way to check last modified date.
	 */
	@Override
	public long getLastModified(Object source)
	{
		return -1;
	}

	@Override
	public Reader getReader(Object source, String encoding) throws IOException
	{
		TemplateSource r = ((TemplateSource) source);
		return new StringReader(r.getValue());
	}

	/**
	 * Template source class. Needed to have a correct {@link #equals(Object)} method. Also used for logging creation time.
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
			return String.format("Freemarker Template \"%s\" (loaded on %TT.%2$TL)", template.getName(), lastModified);
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

		private String getValue()
		{
			return template.getValue();
		}
	}
}

/*
 * Date: December 24, 2010 Template: PluginScreenJavaTemplateGen.java.ftl
 * generator: org.molgenis.generators.ui.PluginScreenJavaTemplateGen 3.3.3
 * 
 * THIS FILE IS A TEMPLATE. PLEASE EDIT :-)
 */

package org.molgenis.framework.ui;

import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.TreeMap;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.server.MolgenisRequest;
import org.molgenis.util.Entity;

import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.ext.beans.BeansWrapper;
import freemarker.template.Template;

@Deprecated
public class GenericPlugin extends PluginModel<Entity>
{
	// serialization id
	private static final long serialVersionUID = 1L;
	// wrapper of this template
	private freemarker.template.Configuration cfg = null;
	// should this Plugin generate a form or not
	public boolean isForm = true;

	public GenericPlugin(String name, ScreenController<?> parent)
	{
		super(name, parent);
	}

	@Override
	public String getViewName()
	{
		return "plugins_newmodel_GenericPlugin";
	}

	@Override
	public String getViewTemplate()
	{
		return "org/molgenis/framework/ui/GenericPlugin.ftl";
	}

	@Override
	public void handleRequest(Database db, MolgenisRequest request)
	{
		String action = request.getAction();
		try
		{
			logger.debug("trying to use reflection to call " + this.getClass().getName() + "." + action);
			Method m = this.getClass().getMethod(action, Database.class, MolgenisRequest.class);
			m.invoke(this, db, request);
			logger.debug("call of " + this.getClass().getName() + "(name=" + this.getName() + ")." + action
					+ " completed");
		}
		catch (Exception e)
		{
			logger.error("call of " + this.getClass().getName() + "(name=" + this.getName() + ")." + action
					+ " failed: " + e.getMessage());
			e.printStackTrace();
		}
	}

	@Override
	public boolean isVisible()
	{
		if (this.getLogin().isAuthenticated())
		{
			try
			{
				if (this.getLogin().canRead(this))
				{
					return true;
				}
			}
			catch (DatabaseException e)
			{
				e.printStackTrace();
			}
		}
		return false;
	}

	public String render(String templatePath)
	{
		logger.debug("trying to render " + templatePath);
		try
		{
			// keep configuration in session so we can reuse it
			if (cfg == null)
			{
				logger.debug("create freemarker config");
				// create configuration
				cfg = new freemarker.template.Configuration();
				BeansWrapper wrapper = new BeansWrapper();
				wrapper.setExposeFields(true);
				cfg.setObjectWrapper(wrapper);

				// create template loader
				ClassTemplateLoader loader1 = new ClassTemplateLoader(getClass(), "");
				ClassTemplateLoader loader2 = new ClassTemplateLoader(getClass().getSuperclass(), "");
				TemplateLoader[] loaders = new TemplateLoader[]
				{ loader1, loader2 };
				MultiTemplateLoader mLoader = new MultiTemplateLoader(loaders);
				cfg.setTemplateLoader(mLoader);
				logger.debug("created freemarker config");
			}

			// create template parameters
			Map<String, Object> templateArgs = new TreeMap<String, Object>();
			templateArgs.put("screen", this);

			// merge template
			Template template = cfg.getTemplate(templatePath);
			StringWriter writer = new StringWriter();
			template.process(templateArgs, writer);
			writer.close();

			return writer.toString();
		}
		catch (Exception e)
		{
			logger.error("rendering of template " + templatePath + " failed:" + e.getMessage());
			e.printStackTrace();
		}

		return null;
	}

	@Override
	public String render()
	{
		// ouch: because we use superclass during generation we solve it like
		// this. Ouch!
		return render(this.getViewTemplate());
	}

	public boolean renderAsForm()
	{
		return this.isForm;
	}

	public void clearMessage()
	{
		this.setMessages();
	}

	@Override
	public void reset()
	{
	}

	@Override
	public ScreenView getView()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void reload(Database db)
	{
	}
}

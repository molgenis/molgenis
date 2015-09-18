package org.molgenis.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapterFactory;

/**
 * A {@link FactoryBean} for creating a Google Gson 2.x {@link Gson} instance.
 */
public class GsonFactoryBean implements FactoryBean<Gson>, InitializingBean
{
	private boolean serializeNulls = false;

	private boolean prettyPrinting = false;

	private boolean disableHtmlEscaping = false;

	private String dateFormatPattern = MolgenisDateFormat.DATEFORMAT_DATETIME;

	private List<TypeAdapterFactory> typeAdapterFactoryList;

	private Gson gson;

	/**
	 * Whether to use the {@link GsonBuilder#serializeNulls()} option when writing JSON. This is a shortcut for setting
	 * up a {@code Gson} as follows:
	 * 
	 * <pre class="code">
	 * new GsonBuilder().serializeNulls().create();
	 * </pre>
	 */
	public void setSerializeNulls(boolean serializeNulls)
	{
		this.serializeNulls = serializeNulls;
	}

	/**
	 * Whether to use the {@link GsonBuilder#setPrettyPrinting()} when writing JSON. This is a shortcut for setting up a
	 * {@code Gson} as follows:
	 * 
	 * <pre class="code">
	 * new GsonBuilder().setPrettyPrinting().create();
	 * </pre>
	 */
	public void setPrettyPrinting(boolean prettyPrinting)
	{
		this.prettyPrinting = prettyPrinting;
	}

	/**
	 * Whether to use the {@link GsonBuilder#disableHtmlEscaping()} when writing JSON. Set to {@code true} to disable
	 * HTML escaping in JSON. This is a shortcut for setting up a {@code Gson} as follows:
	 * 
	 * <pre class="code">
	 * new GsonBuilder().disableHtmlEscaping().create();
	 * </pre>
	 */
	public void setDisableHtmlEscaping(boolean disableHtmlEscaping)
	{
		this.disableHtmlEscaping = disableHtmlEscaping;
	}

	/**
	 * Define the date/time format with a {@link SimpleDateFormat}-style pattern. This is a shortcut for setting up a
	 * {@code Gson} as follows:
	 * 
	 * <pre class="code">
	 * new GsonBuilder().setDateFormat(dateFormatPattern).create();
	 * </pre>
	 */
	public void setDateFormatPattern(String dateFormatPattern)
	{
		this.dateFormatPattern = dateFormatPattern;
	}

	public void registerTypeAdapterFactory(TypeAdapterFactory typeAdapterFactory)
	{
		if (typeAdapterFactoryList == null) typeAdapterFactoryList = new ArrayList<TypeAdapterFactory>();
		typeAdapterFactoryList.add(typeAdapterFactory);
	}

	@Override
	public void afterPropertiesSet()
	{
		GsonBuilder builder = new GsonBuilder();
		if (this.serializeNulls)
		{
			builder.serializeNulls();
		}
		if (this.prettyPrinting)
		{
			builder.setPrettyPrinting();
		}
		if (this.disableHtmlEscaping)
		{
			builder.disableHtmlEscaping();
		}
		if (this.dateFormatPattern != null)
		{
			builder.setDateFormat(this.dateFormatPattern);
		}
		if (this.typeAdapterFactoryList != null)
		{
			typeAdapterFactoryList.forEach(builder::registerTypeAdapterFactory);
		}
		this.gson = builder.create();
	}

	/**
	 * Return the created Gson instance.
	 */
	@Override
	public Gson getObject()
	{
		return this.gson;
	}

	@Override
	public Class<?> getObjectType()
	{
		return Gson.class;
	}

	@Override
	public boolean isSingleton()
	{
		return true;
	}
}

package org.molgenis.util;

import org.molgenis.data.Entity;
import org.molgenis.gson.AutoValueTypeAdapterFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GsonConfig
{
	@Value("${environment:production}")
	private String environment;

	@Bean
	public GsonHttpMessageConverter gsonHttpMessageConverter()
	{
		return new GsonHttpMessageConverter(gsonFactoryBean().getObject());
	}

	@Bean
	public GsonFactoryBean gsonFactoryBean()
	{
		boolean prettyPrinting =
				environment != null && (environment.equals("development") || environment.equals("test"));

		GsonFactoryBean gsonFactoryBean = new GsonFactoryBean();
		gsonFactoryBean.registerTypeHierarchyAdapter(Entity.class, new EntitySerializer());
		gsonFactoryBean.setDateFormatPattern(MolgenisDateFormat.getDateTimeFormat().toPattern());
		gsonFactoryBean.setDisableHtmlEscaping(true);
		gsonFactoryBean.setPrettyPrinting(prettyPrinting);
		gsonFactoryBean.setSerializeNulls(false);
		gsonFactoryBean.registerTypeAdapterFactory(new AutoValueTypeAdapterFactory());
		return gsonFactoryBean;
	}
}
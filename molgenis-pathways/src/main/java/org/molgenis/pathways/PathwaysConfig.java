package org.molgenis.pathways;

import org.apache.http.client.HttpClient;
import org.molgenis.data.config.HttpClientConfig;
import org.molgenis.wikipathways.client.WikiPathwaysPortType;
import org.molgenis.wikipathways.client.WikiPathwaysRESTBindingStub;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(HttpClientConfig.class)
public class PathwaysConfig
{
	@Autowired
	private HttpClient httpClient;

	@Bean
	public WikiPathwaysPortType service()
	{
		return new WikiPathwaysRESTBindingStub(httpClient, "http://webservice.wikipathways.org");
	}
}

package org.molgenis.pathways;

import java.net.MalformedURLException;

import org.apache.http.impl.client.HttpClients;
import org.molgenis.wikipathways.client.WikiPathwaysPortType;
import org.molgenis.wikipathways.client.WikiPathwaysRESTBindingStub;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PathwaysConfig
{
	@Bean
	public WikiPathwaysPortType service() throws MalformedURLException
	{
		return new WikiPathwaysRESTBindingStub(HttpClients.createDefault(), "http://webservice.wikipathways.org");
	}
}

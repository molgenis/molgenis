package org.molgenis.pathways;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.rpc.ServiceException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.wikipathways.client.WikiPathwaysClient;

@Configuration
public class PathwaysConfig
{
	@Bean
	public WikiPathwaysClient service() throws ServiceException, MalformedURLException
	{
		URL wsURL = new URL("http://webservice.wikipathways.org");
		return new WikiPathwaysClient(wsURL);
	}
	
}

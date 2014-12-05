package org.molgenis.pathways;

import org.molgenis.dataWikiPathways.WikiPathways;
import org.molgenis.dataWikiPathways.WikiPathwaysPortType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PathwaysConfig
{
	@Bean
	public WikiPathwaysPortType service()
	{
		WikiPathways wikiPathways = new WikiPathways();
		return wikiPathways.getWikiPathwaysSOAPPortHttp();
	}
	
}

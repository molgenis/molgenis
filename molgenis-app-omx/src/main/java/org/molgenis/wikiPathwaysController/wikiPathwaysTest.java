package org.molgenis.wikiPathwaysController;

import org.molgenis.dataWikiPathways.WikiPathways;
import org.molgenis.dataWikiPathways.WikiPathwaysPortType;

public class wikiPathwaysTest
{
	public static void main(String args[]){
		WikiPathways wikiPathways = new WikiPathways();
		WikiPathwaysPortType service = wikiPathways.getWikiPathwaysSOAPPortHttp();
		System.out.println(service.listOrganisms());
	}

}

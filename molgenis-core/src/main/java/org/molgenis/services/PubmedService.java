package org.molgenis.services;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;
import org.molgenis.services.pubmed.ESearchResult;
import org.molgenis.services.pubmed.PubmedArticle;
import org.molgenis.services.pubmed.PubmedArticleSet;

public class PubmedService
{
	private static final Logger logger = Logger.getLogger(PubmedService.class);

	public List<PubmedArticle> searchPubmedArticles(String term) throws MalformedURLException, JAXBException,
			IOException
	{
		return getPubmedArticlesForIds(searchPubmedIds(term));
	}

	List<Integer> searchPubmedIds(String term) throws JAXBException, IOException
	{
		String url = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=pubmed&term="
				+ term.replace(" ", "%20");
		ESearchResult res = getSearchResult(new URL(url));
		return res.idList;
	}

	public List<PubmedArticle> getPubmedArticlesForIds(List<Integer> ids) throws MalformedURLException, JAXBException,
			IOException
	{
		StringBuilder urlBuilder = new StringBuilder(
				"http://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=pubmed&retmode=xml&id=");
		for (Integer i : ids)
			urlBuilder.append(i).append(',');
		urlBuilder.deleteCharAt(urlBuilder.length() - 1);
		return getCitations(new URL(urlBuilder.toString())).articles;
	}

	private PubmedArticleSet getCitations(URL url) throws JAXBException, IOException
	{
		logger.debug("load eSearchResult from " + url);
		JAXBContext jaxbContext = JAXBContext.newInstance("org.molgenis.services.pubmed");
		Unmarshaller m = jaxbContext.createUnmarshaller();
		return (PubmedArticleSet) m.unmarshal(url.openStream());
	}

	private ESearchResult getSearchResult(URL url) throws JAXBException, IOException
	{
		logger.debug("load eSearchResult from " + url);
		JAXBContext jaxbContext = JAXBContext.newInstance("org.molgenis.services.pubmed");
		Unmarshaller m = jaxbContext.createUnmarshaller();
		return (ESearchResult) m.unmarshal(url.openStream());
	}

	public static void main(String[] args) throws JAXBException, IOException
	{
		PubmedService pubmed = new PubmedService();
		// test
		for (PubmedArticle art : pubmed.searchPubmedArticles("swertz ma[au]"))
		{
			logger.debug(art.MedlineCitation);
		}
	}
}

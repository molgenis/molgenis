// WikiPathways Java library,
// Copyright 2014 WikiPathways
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//

package org.molgenis.wikipathways.utils;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.molgenis.wikipathways.client.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Utils
{

	public static Document connect(String url, HttpClient client) throws IOException, JDOMException
	{
		HttpGet httpget = new HttpGet(url);
		HttpResponse response = client.execute(httpget);
		HttpEntity entity = response.getEntity();
		try (InputStream instream = entity.getContent())
		{
			StringBuilder content = new StringBuilder();
			BufferedReader reader = new BufferedReader(new InputStreamReader(instream, "UTF-8"));
			String line;
			while ((line = reader.readLine()) != null)
			{
				content.append(line).append("\n");
			}
			reader.close();

			SAXBuilder jdomBuilder = new SAXBuilder();
			Document jdomDocument = jdomBuilder.build(new StringReader(content.toString()));
			return jdomDocument;
		}
	}

	public static String update(String url, HttpClient client, Map<String, String> attributes) throws Exception
	{

		HttpPost httpost = new HttpPost(url);
		// Adding all form parameters in a List of type NameValuePair
		List<NameValuePair> nvps = new ArrayList<>();
		for (String key : attributes.keySet())
		{
			nvps.add(new BasicNameValuePair(key, attributes.get(key)));
		}
		httpost.addHeader("Accept-Encoding", "application/xml");
		httpost.setEntity(new UrlEncodedFormEntity(nvps, Consts.UTF_8));
		HttpResponse response = client.execute(httpost);

		SAXBuilder jdomBuilder = new SAXBuilder();
		Document jdomDocument = jdomBuilder.build(response.getEntity().getContent());
		String success = jdomDocument.getRootElement().getChildText("success", WSNamespaces.NS1);
		return success;
	}

	public static WSPathwayInfo parseWSPathwayInfo(Element pathwayInfo)
	{

		String identifier = pathwayInfo.getChildText("id", WSNamespaces.NS2);
		String url = pathwayInfo.getChildText("url", WSNamespaces.NS2);
		String name = pathwayInfo.getChildText("name", WSNamespaces.NS2);
		String species = pathwayInfo.getChildText("species", WSNamespaces.NS2);
		String revision = pathwayInfo.getChildText("revision", WSNamespaces.NS2);

		return new WSPathwayInfo(identifier, url, name, species, revision);
	}

	public static WSIndexField parseWSIndexField(Element indexField)
	{
		String name = indexField.getChildText("name", WSNamespaces.NS2);
		String values = indexField.getChildText("values", WSNamespaces.NS2);
		String[] v = new String[1];
		v[0] = values;
		WSIndexField field = new WSIndexField(name, v);
		return field;
	}

	public static WSSearchResult parseWSSearchResult(Element searchResult)
	{
		WSSearchResult res = new WSSearchResult();
		String score = searchResult.getChildText("score", WSNamespaces.NS2);
		res.setScore(Double.parseDouble(score));

		@SuppressWarnings("unchecked")
		List<Element> fieldList = searchResult.getChildren("fields", WSNamespaces.NS2);
		WSIndexField[] fields = new WSIndexField[fieldList.size()];
		for (int i = 0; i < fieldList.size(); i++)
		{
			WSIndexField f = parseWSIndexField(fieldList.get(i));
			fields[i] = f;
		}
		res.setFields(fields);

		String id = searchResult.getChildText("id", WSNamespaces.NS2);
		String url = searchResult.getChildText("url", WSNamespaces.NS2);
		String name = searchResult.getChildText("name", WSNamespaces.NS2);
		String species = searchResult.getChildText("species", WSNamespaces.NS2);
		String revision = searchResult.getChildText("revision", WSNamespaces.NS2);

		res.setId(id);
		res.setName(name);
		res.setUrl(url);
		res.setSpecies(species);
		res.setRevision(revision);

		return res;
	}

	public static WSOntologyTerm parseOntologyTerm(Element term)
	{
		String ontology = term.getChildText("ontology", WSNamespaces.NS2);
		String id = term.getChildText("id", WSNamespaces.NS2);
		String name = term.getChildText("name", WSNamespaces.NS2);
		WSOntologyTerm ontTerm = new WSOntologyTerm(ontology, id, name);
		return ontTerm;
	}

	public static WSPathway parsePathway(Element pathway) throws UnsupportedEncodingException
	{
		String gpml = new String(Base64.decodeBase64(pathway.getChildText("gpml", WSNamespaces.NS2)), "UTF-8");
		String id = pathway.getChildText("id", WSNamespaces.NS2);
		String name = pathway.getChildText("name", WSNamespaces.NS2);
		String url = pathway.getChildText("url", WSNamespaces.NS2);
		String species = pathway.getChildText("species", WSNamespaces.NS2);
		String revision = pathway.getChildText("revision", WSNamespaces.NS2);
		WSPathway p = new WSPathway(gpml, id, url, name, species, revision);
		return p;
	}

	public static WSPathwayHistory parsePathwayHistory(Element hist)
	{
		String id = hist.getChildText("id", WSNamespaces.NS2);
		String url = hist.getChildText("url", WSNamespaces.NS2);
		String name = hist.getChildText("name", WSNamespaces.NS2);
		String species = hist.getChildText("species", WSNamespaces.NS2);
		String revision = hist.getChildText("revision", WSNamespaces.NS2);

		@SuppressWarnings("unchecked")
		List<Element> list = hist.getChildren("history", WSNamespaces.NS2);
		WSHistoryRow[] histRows = new WSHistoryRow[list.size()];
		for (int i = 0; i < list.size(); i++)
		{
			histRows[i] = Utils.parseHistoryRow(list.get(i));
		}

		return new WSPathwayHistory(histRows, id, url, name, species, revision);
	}

	public static WSHistoryRow parseHistoryRow(Element historyRow)
	{
		String revision = historyRow.getChildText("revision", WSNamespaces.NS2);
		String comment = historyRow.getChildText("comment", WSNamespaces.NS2);
		String user = historyRow.getChildText("user", WSNamespaces.NS2);
		String timestamp = historyRow.getChildText("timestamp", WSNamespaces.NS2);

		return new WSHistoryRow(revision, comment, user, timestamp);
	}

	public static WSCurationTagHistory parseCurationTagHistory(Element history)
	{
		String tagName = history.getChildText("tagName", WSNamespaces.NS2);
		String text = history.getChildText("text", WSNamespaces.NS2);
		String pathwayId = history.getChildText("pathwayId", WSNamespaces.NS2);
		String action = history.getChildText("action", WSNamespaces.NS2);
		String user = history.getChildText("user", WSNamespaces.NS2);
		String time = history.getChildText("time", WSNamespaces.NS2);

		return new WSCurationTagHistory(tagName, text, pathwayId, action, user, time);
	}

	public static WSCurationTag parseCurationTag(Element tag)
	{
		String name = tag.getChildText("name", WSNamespaces.NS2);
		String displayName = tag.getChildText("displayName", WSNamespaces.NS2);
		String text = tag.getChildText("text", WSNamespaces.NS2);
		String timeModified = tag.getChildText("timeModified", WSNamespaces.NS2);
		String userModified = tag.getChildText("userModified", WSNamespaces.NS2);

		WSPathwayInfo pathway = Utils.parseWSPathwayInfo(tag.getChild("pathway", WSNamespaces.NS2));

		WSCurationTag t = new WSCurationTag(name, displayName, pathway, pathway.getRevision(), text,
				Long.parseLong(timeModified), userModified);
		return t;
	}
}

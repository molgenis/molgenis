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

package org.molgenis.wikipathways.client;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
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
import org.jdom.input.SAXBuilder;
import org.molgenis.wikipathways.utils.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * connects to the new WikiPathways REST webservice
 *
 * @author msk
 */
@SuppressWarnings("unchecked")
public class WikiPathwaysRESTBindingStub implements WikiPathwaysPortType
{

	private HttpClient client;
	private String baseUrl;

	public WikiPathwaysRESTBindingStub(HttpClient client, String baseUrl)
	{
		this.client = client;
		this.baseUrl = baseUrl;
	}

	@Override
	public WSSearchResult[] findInteractions(String query) throws RemoteException
	{
		try
		{
			query = query.replace(" ", "+");
			String url = baseUrl + "/findInteractions?query=" + query;

			Document jdomDocument = Utils.connect(url, client);
			Element root = jdomDocument.getRootElement();
			List<Element> list = root.getChildren("result", WSNamespaces.NS1);
			WSSearchResult[] res = new WSSearchResult[list.size()];
			for (int i = 0; i < list.size(); i++)
			{
				res[i] = Utils.parseWSSearchResult(list.get(i));
			}
			return res;
		}
		catch (Exception e)
		{
			throw new RemoteException(e.getMessage(), e.getCause());
		}
	}

	@Override
	public WSSearchResult[] findPathwaysByLiterature(String query) throws RemoteException
	{
		try
		{
			query = query.replace(" ", "+");
			String url = baseUrl + "/findPathwaysByLiterature?query=" + query;
			Document jdomDocument = Utils.connect(url, client);
			Element root = jdomDocument.getRootElement();
			List<Element> list = root.getChildren("result", WSNamespaces.NS1);
			WSSearchResult[] res = new WSSearchResult[list.size()];
			for (int i = 0; i < list.size(); i++)
			{
				res[i] = Utils.parseWSSearchResult(list.get(i));
			}
			return res;
		}
		catch (Exception e)
		{
			throw new RemoteException(e.getMessage(), e.getCause());
		}
	}

	@Override
	public WSSearchResult[] findPathwaysByText(String query, String species) throws RemoteException
	{
		try
		{
			query = query.replace(" ", "+");
			String url = baseUrl + "/findPathwaysByText?query=" + query;
			if (species != null)
			{
				species = species.replace(" ", "+");
				url = url + "&species=" + species;
			}

			Document jdomDocument = Utils.connect(url, client);
			Element root = jdomDocument.getRootElement();
			List<Element> list = root.getChildren("result", WSNamespaces.NS1);
			WSSearchResult[] res = new WSSearchResult[list.size()];
			for (int i = 0; i < list.size(); i++)
			{
				res[i] = Utils.parseWSSearchResult(list.get(i));
			}
			return res;
		}
		catch (Exception e)
		{
			throw new RemoteException(e.getMessage(), e.getCause());
		}
	}

	@Override
	public WSSearchResult[] findPathwaysByXref(String[] ids, String[] codes) throws RemoteException
	{
		try
		{
			String url = baseUrl + "/findPathwaysByXref";
			int count = 0;
			for (String i : ids)
			{
				if (count == 0)
				{
					url = url + "?ids=" + i;
					count++;
				}
				else
				{
					url = url + "&ids=" + i;
				}
			}
			for (String c : codes)
			{
				url = url + "&codes=" + c;
			}

			Document jdomDocument = Utils.connect(url, client);
			Element root = jdomDocument.getRootElement();
			List<Element> list = root.getChildren("result", WSNamespaces.NS1);
			WSSearchResult[] res = new WSSearchResult[list.size()];
			for (int i = 0; i < list.size(); i++)
			{
				res[i] = Utils.parseWSSearchResult(list.get(i));
			}
			return res;
		}
		catch (Exception e)
		{
			throw new RemoteException(e.getMessage(), e.getCause());
		}
	}

	@Override
	public byte[] getColoredPathway(String pwId, String revision, String[] graphId, String[] color, String fileType)
			throws RemoteException
	{
		try
		{
			String url = baseUrl + "/getColoredPathway?pwId=" + pwId + "&revision=" + revision;
			for (String g : graphId)
			{
				url = url + "&graphId=" + g;
			}
			for (String c : color)
			{
				url = url + "&color=" + c;
			}
			url = url + "&fileType=" + fileType;
			Document jdomDocument = Utils.connect(url, client);
			Element data = jdomDocument.getRootElement().getChild("data", WSNamespaces.NS1);
			return Base64.decodeBase64(data.getValue());
		}
		catch (Exception e)
		{
			throw new RemoteException(e.getMessage(), e.getCause());
		}
	}

	@Override
	public WSCurationTagHistory[] getCurationTagHistory(String pwId, String timestamp) throws RemoteException
	{
		try
		{
			String url = baseUrl + "/getCurationTagHistory?pwId=" + pwId;
			if (timestamp != null)
			{
				url = url + "&timestamp=" + timestamp;
			}
			Document jdomDocument = Utils.connect(url, client);
			Element root = jdomDocument.getRootElement();
			List<Element> list = root.getChildren("history", WSNamespaces.NS1);
			WSCurationTagHistory[] hist = new WSCurationTagHistory[list.size()];
			for (int i = 0; i < list.size(); i++)
			{
				hist[i] = Utils.parseCurationTagHistory(list.get(i));
			}
			return hist;
		}
		catch (Exception e)
		{
			throw new RemoteException(e.getMessage(), e.getCause());
		}
	}

	@Override
	public WSCurationTag[] getCurationTags(String pwId) throws RemoteException
	{
		try
		{
			String url = baseUrl + "/getCurationTags?pwId=" + pwId;
			Document jdomDocument = Utils.connect(url, client);
			Element root = jdomDocument.getRootElement();
			List<Element> list = root.getChildren("tags", WSNamespaces.NS1);
			WSCurationTag[] tags = new WSCurationTag[list.size()];
			for (int i = 0; i < list.size(); i++)
			{
				tags[i] = Utils.parseCurationTag(list.get(i));
			}
			return tags;
		}
		catch (Exception e)
		{
			throw new RemoteException(e.getMessage(), e.getCause());
		}
	}

	@Override
	public WSCurationTag[] getCurationTagsByName(String tagName) throws RemoteException
	{
		try
		{
			String url = baseUrl + "/getCurationTagsByName?tagName=" + tagName;
			Document jdomDocument = Utils.connect(url, client);
			Element root = jdomDocument.getRootElement();
			List<Element> list = root.getChildren("tags", WSNamespaces.NS1);
			WSCurationTag[] tags = new WSCurationTag[list.size()];
			for (int i = 0; i < list.size(); i++)
			{
				tags[i] = Utils.parseCurationTag(list.get(i));
			}
			return tags;
		}
		catch (Exception e)
		{
			throw new RemoteException(e.getMessage(), e.getCause());
		}
	}

	@Override
	public WSOntologyTerm[] getOntologyTermsByPathway(String pwId) throws RemoteException
	{
		try
		{
			String url = baseUrl + "/getOntologyTermsByPathway?pwId=" + pwId;
			Document jdomDocument = Utils.connect(url, client);
			Element root = jdomDocument.getRootElement();
			List<Element> list = root.getChildren("terms", WSNamespaces.NS1);
			WSOntologyTerm[] terms = new WSOntologyTerm[list.size()];
			for (int i = 0; i < list.size(); i++)
			{
				terms[i] = Utils.parseOntologyTerm(list.get(i));
			}
			return terms;
		}
		catch (Exception e)
		{
			throw new RemoteException(e.getMessage(), e.getCause());
		}
	}

	@Override
	public WSPathway getPathway(String pwId, int revision) throws RemoteException
	{
		try
		{
			String url = baseUrl + "/getPathway?pwId=" + pwId;
			if (revision != 0)
			{
				url = url + "&revision=" + revision;
			}
			Document jdomDocument = Utils.connect(url, client);
			Element root = jdomDocument.getRootElement();
			Element p = root.getChild("pathway", WSNamespaces.NS1);
			return Utils.parsePathway(p);
		}
		catch (Exception e)
		{
			throw new RemoteException(e.getMessage(), e.getCause());
		}
	}

	@Override
	public byte[] getPathwayAs(String fileType, String pwId, int revision) throws RemoteException
	{
		InputStream instream = null;
		try
		{
			String url = baseUrl;
			if (url.contains("webservice/webservice.php"))
			{
				url = url.replace("webservice/webservice.php", "wpi.php");
			}
			else if (url.contains("webservicetest"))
			{
				url = url.replace("webservicetest/webservice.php", "wpi.php");
			}
			else if (url.contains("webservice.wikipathways.org"))
			{
				url = "http://www.wikipathways.org/wpi/wpi.php";
			}
			url = url + "?action=downloadFile&type=" + fileType + "&pwTitle=Pathway:" + pwId + "&oldid=" + revision;

			HttpGet httpget = new HttpGet(url);
			HttpResponse response = client.execute(httpget);
			HttpEntity entity = response.getEntity();
			instream = entity.getContent();

			return IOUtils.toByteArray(instream);
		}
		catch (Exception e)
		{
			throw new RemoteException(e.getMessage(), e.getCause());
		}
		finally
		{
			try
			{
				if (instream != null)
				{
					instream.close();
				}
			}
			catch (IOException e)
			{
				throw new RemoteException(e.getMessage(), e.getCause());
			}
		}
	}

	@Override
	public WSPathwayInfo[] getPathwaysByOntologyTerm(String term) throws RemoteException
	{
		try
		{
			term = term.replace(" ", "+");
			String url = baseUrl + "/getPathwaysByOntologyTerm?term=" + term;
			Document jdomDocument = Utils.connect(url, client);
			Element root = jdomDocument.getRootElement();
			List<Element> list = root.getChildren("pathways", WSNamespaces.NS1);
			WSPathwayInfo[] info = new WSPathwayInfo[list.size()];
			for (int i = 0; i < list.size(); i++)
			{
				info[i] = Utils.parseWSPathwayInfo(list.get(i));
			}
			return info;
		}
		catch (Exception e)
		{
			throw new RemoteException(e.getMessage(), e.getCause());
		}
	}

	@Override
	public WSPathwayInfo[] getPathwaysByParentOntologyTerm(String term) throws RemoteException
	{
		try
		{
			String url = baseUrl + "/getPathwaysByParentOntologyTerm?term=" + term;
			Document jdomDocument = Utils.connect(url, client);
			Element root = jdomDocument.getRootElement();
			List<Element> list = root.getChildren("pathways", WSNamespaces.NS1);
			WSPathwayInfo[] info = new WSPathwayInfo[list.size()];
			for (int i = 0; i < list.size(); i++)
			{
				info[i] = Utils.parseWSPathwayInfo(list.get(i));
			}
			return info;
		}
		catch (Exception e)
		{
			throw new RemoteException(e.getMessage(), e.getCause());
		}
	}

	@Override
	public WSPathwayHistory getPathwayHistory(String pwId, String timestamp) throws RemoteException
	{
		try
		{
			String url = baseUrl + "/getPathwayHistory?pwId=" + pwId + "&timestamp=" + timestamp;
			Document jdomDocument = Utils.connect(url, client);
			Element root = jdomDocument.getRootElement();
			Element hist = root.getChild("history", WSNamespaces.NS1);
			return Utils.parsePathwayHistory(hist);
		}
		catch (Exception e)
		{
			throw new RemoteException(e.getMessage(), e.getCause());
		}
	}

	@Override
	public WSPathwayInfo getPathwayInfo(String pwId) throws RemoteException
	{
		try
		{
			String url = baseUrl + "/getPathwayInfo?pwId=" + pwId;
			Document jdomDocument = Utils.connect(url, client);
			Element root = jdomDocument.getRootElement();
			Element p = root.getChild("pathwayInfo", WSNamespaces.NS1);
			return Utils.parseWSPathwayInfo(p);
		}
		catch (Exception e)
		{
			throw new RemoteException(e.getMessage(), e.getCause());
		}
	}

	@Override
	public WSPathwayInfo[] getRecentChanges(String timestamp) throws RemoteException
	{
		try
		{
			String url = baseUrl + "/getRecentChanges?timestamp=" + timestamp;
			Document jdomDocument = Utils.connect(url, client);
			Element root = jdomDocument.getRootElement();
			List<Element> list = root.getChildren("pathways", WSNamespaces.NS1);
			WSPathwayInfo[] info = new WSPathwayInfo[list.size()];
			for (int i = 0; i < list.size(); i++)
			{
				info[i] = Utils.parseWSPathwayInfo(list.get(i));
			}
			return info;
		}
		catch (Exception e)
		{
			throw new RemoteException(e.getMessage(), e.getCause());
		}
	}

	@Override
	public String getUserByOrcid(String orcid) throws RemoteException
	{
		try
		{
			String url = baseUrl + "/getUserByOrcid?orcid=" + orcid;
			Document jdomDocument = Utils.connect(url, client);
			Element root = jdomDocument.getRootElement();

			return root.getChildText("Result", WSNamespaces.NS1).substring(5);
		}
		catch (Exception e)
		{
			throw new RemoteException(e.getMessage(), e.getCause());
		}
	}

	@Override
	public String[] getXrefList(String pwId, String code) throws RemoteException
	{
		try
		{
			String url = baseUrl + "/getXrefList?pwId=" + pwId + "&code=" + code;
			Document jdomDocument = Utils.connect(url, client);
			Element root = jdomDocument.getRootElement();
			List<Element> list = root.getChildren();
			String[] xrefs = new String[list.size()];
			for (int i = 0; i < list.size(); i++)
			{
				xrefs[i] = list.get(i).getValue();
			}
			return xrefs;
		}
		catch (Exception e)
		{
			throw new RemoteException(e.getMessage(), e.getCause());
		}
	}

	@Override
	public String[] listOrganisms() throws RemoteException
	{
		try
		{
			String url = baseUrl + "/listOrganisms";
			Document jdomDocument = Utils.connect(url, client);
			Element root = jdomDocument.getRootElement();
			List<Element> list = root.getChildren();
			String[] organisms = new String[list.size()];
			for (int i = 0; i < list.size(); i++)
			{
				organisms[i] = list.get(i).getValue();
			}
			return organisms;
		}
		catch (Exception e)
		{
			throw new RemoteException(e.getMessage(), e.getCause());
		}
	}

	@Override
	public WSPathwayInfo[] listPathways(String organism) throws RemoteException
	{
		try
		{
			String url = "";
			if (organism == null)
			{
				url = baseUrl + "/listPathways";
			}
			else
			{
				organism = organism.replace(" ", "+");
				url = baseUrl + "/listPathways?organism=" + organism;
			}
			Document jdomDocument = Utils.connect(url, client);
			Element root = jdomDocument.getRootElement();
			List<Element> list = root.getChildren("pathways", WSNamespaces.NS1);
			WSPathwayInfo[] info = new WSPathwayInfo[list.size()];
			for (int i = 0; i < list.size(); i++)
			{
				info[i] = Utils.parseWSPathwayInfo(list.get(i));
			}
			return info;
		}
		catch (Exception e)
		{
			throw new RemoteException(e.getMessage(), e.getCause());
		}
	}

	@Override
	public String login(String name, String pass) throws RemoteException
	{
		try
		{
			String url = baseUrl + "/login?name=" + name + "&pass=" + pass;
			Document jdomDocument = Utils.connect(url, client);
			Element root = jdomDocument.getRootElement();
			String auth = root.getChildText("auth", WSNamespaces.NS1);
			if (auth == null)
			{
				throw new RemoteException("Invalid username or password");
			}
			return auth;
		}
		catch (Exception e)
		{
			throw new RemoteException(e.getMessage(), e.getCause());
		}
	}

	@Override
	public boolean removeCurationTag(String pwId, String tagName, WSAuth auth) throws RemoteException
	{
		try
		{
			String url = baseUrl + "/removeCurationTag?pwId=" + pwId + "&tagName=" + tagName + "&auth=" + auth.getKey()
					+ "&username=" + auth.getUser();
			Document jdomDocument = Utils.connect(url, client);
			String success = jdomDocument.getRootElement().getChild("success", WSNamespaces.NS1).getValue();
			if (success.equals("1"))
			{
				return true;
			}
			return false;
		}
		catch (Exception e)
		{
			throw new RemoteException(e.getMessage(), e.getCause());
		}
	}

	@Override
	public boolean saveCurationTag(String pwId, String tagName, String tagText, int revision, WSAuth auth)
			throws RemoteException
	{
		try
		{
			String url =
					baseUrl + "/saveCurationTag?pwId=" + pwId + "&tagName=" + tagName + "&text=" + URLEncoder.encode(
							tagText, "UTF-8") + "&revision=" + revision + "&auth=" + auth.getKey() + "&username=" + auth
							.getUser();
			Document jdomDocument = Utils.connect(url, client);
			String success = jdomDocument.getRootElement().getChild("success", WSNamespaces.NS1).getValue();
			if (success.equals("1"))
			{
				return true;
			}
			return false;
		}
		catch (Exception e)
		{
			throw new RemoteException(e.getMessage(), e.getCause());
		}
	}

	@Override
	public boolean saveOntologyTag(String pwId, String term, String termId, WSAuth auth) throws RemoteException
	{
		try
		{
			String url =
					baseUrl + "/saveOntologyTag?pwId=" + pwId + "&term=" + URLEncoder.encode(term, "UTF-8") + "&termId="
							+ termId + "&auth=" + auth.getKey() + "&user=" + auth.getUser();
			Document jdomDocument = Utils.connect(url, client);
			String success = jdomDocument.getRootElement().getChild("success", WSNamespaces.NS1).getValue();
			if (success.equals("1"))
			{
				return true;
			}
			return false;
		}
		catch (Exception e)
		{
			throw new RemoteException(e.getMessage(), e.getCause());
		}
	}

	@Override
	public boolean removeOntologyTag(String pwId, String termId, WSAuth auth) throws RemoteException
	{
		try
		{
			String url = baseUrl + "/removeOntologyTag?pwId=" + pwId + "&termId=" + termId + "&auth=" + auth.getKey()
					+ "&user=" + auth.getUser();
			Document jdomDocument = Utils.connect(url, client);
			String success = jdomDocument.getRootElement().getChild("success", WSNamespaces.NS1).getValue();
			if (success.equals("1"))
			{
				return true;
			}
			return false;
		}
		catch (Exception e)
		{
			throw new RemoteException(e.getMessage(), e.getCause());
		}
	}

	@Override
	public boolean updatePathway(String pwId, String description, String gpml, int revision, WSAuth auth)
			throws RemoteException
	{
		try
		{
			Map<String, String> map = new HashMap<>();
			map.put("pwId", pwId);
			map.put("description", description);
			map.put("gpml", gpml);
			map.put("revision", revision + "");
			map.put("auth", auth.getKey());
			map.put("username", auth.getUser());

			String url = baseUrl + "/updatePathway";
			String success = Utils.update(url, client, map);
			if (success.equals("1"))
			{
				return true;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new RemoteException(e.getMessage(), e.getCause());
		}
		return false;
	}

	@Override
	public WSPathwayInfo createPathway(String gpml, WSAuth auth) throws RemoteException
	{
		try
		{
			Map<String, String> map = new HashMap<>();
			map.put("gpml", gpml);
			map.put("auth", auth.getKey());
			map.put("username", auth.getUser());

			String url = baseUrl + "/createPathway";

			HttpPost httpost = new HttpPost(url);
			// Adding all form parameters in a List of type NameValuePair
			List<NameValuePair> nvps = new ArrayList<>();
			for (String key : map.keySet())
			{
				nvps.add(new BasicNameValuePair(key, map.get(key)));
			}
			httpost.addHeader("Accept-Encoding", "application/xml");
			httpost.setEntity(new UrlEncodedFormEntity(nvps, Consts.UTF_8));
			HttpResponse response = client.execute(httpost);

			SAXBuilder jdomBuilder = new SAXBuilder();
			Document jdomDocument = jdomBuilder.build(response.getEntity().getContent());
			Element root = jdomDocument.getRootElement();
			Element p = root.getChild("pathwayInfo", WSNamespaces.NS1);
			return Utils.parseWSPathwayInfo(p);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new RemoteException(e.getMessage(), e.getCause());
		}
	}
}

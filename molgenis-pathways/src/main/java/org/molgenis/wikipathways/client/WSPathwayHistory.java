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

/**
 * contains the history of a pathway
 *
 * @author msk
 */
public class WSPathwayHistory
{

	private WSHistoryRow[] history;
	private String id;
	private String url;
	private String name;
	private String species;
	private String revision;

	public WSPathwayHistory(WSHistoryRow[] history, String id, String url, String name, String species, String revision)
	{
		this.history = history;
		this.id = id;
		this.url = url;
		this.name = name;
		this.species = species;
		this.revision = revision;
	}

	public WSHistoryRow[] getHistory()
	{
		return history;
	}

	public void setHistory(WSHistoryRow[] history)
	{
		this.history = history;
	}

	public WSHistoryRow getHistory(int i)
	{
		return this.history[i];
	}

	public void setHistory(int i, WSHistoryRow _value)
	{
		this.history[i] = _value;
	}

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public String getUrl()
	{
		return url;
	}

	public void setUrl(String url)
	{
		this.url = url;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getSpecies()
	{
		return species;
	}

	public void setSpecies(String species)
	{
		this.species = species;
	}

	public String getRevision()
	{
		return revision;
	}

	public void setRevision(String revision)
	{
		this.revision = revision;
	}
}
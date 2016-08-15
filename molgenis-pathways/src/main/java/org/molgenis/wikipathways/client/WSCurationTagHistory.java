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
 * curation tag history object
 *
 * @author msk
 */
public class WSCurationTagHistory
{
	private String tagName;
	private String text;
	private String pathwayId;
	private String action;
	private String user;
	private String time;

	public WSCurationTagHistory(String tagName, String text, String pathwayId, String action, String user, String time)
	{
		this.tagName = tagName;
		this.text = text;
		this.pathwayId = pathwayId;
		this.action = action;
		this.user = user;
		this.time = time;
	}

	public String getTagName()
	{
		return tagName;
	}

	public void setTagName(String tagName)
	{
		this.tagName = tagName;
	}

	public String getText()
	{
		return text;
	}

	public void setText(String text)
	{
		this.text = text;
	}

	public String getPathwayId()
	{
		return pathwayId;
	}

	public void setPathwayId(String pathwayId)
	{
		this.pathwayId = pathwayId;
	}

	public String getAction()
	{
		return action;
	}

	public void setAction(String action)
	{
		this.action = action;
	}

	public String getUser()
	{
		return user;
	}

	public void setUser(String user)
	{
		this.user = user;
	}

	public String getTime()
	{
		return time;
	}

	public void setTime(String time)
	{
		this.time = time;
	}
}
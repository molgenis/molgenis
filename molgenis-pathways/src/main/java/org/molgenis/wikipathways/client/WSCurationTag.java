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
 * curation tag data object
 *
 * @author msk
 */
public class WSCurationTag
{
	private String name;
	private String displayName;
	private WSPathwayInfo pathway;
	private String revision;
	private String text;
	private long timeModified;
	private String userModified;

	public WSCurationTag(String name, String displayName, WSPathwayInfo pathway, String revision, String text,
			long timeModified, String userModified)
	{
		this.name = name;
		this.displayName = displayName;
		this.pathway = pathway;
		this.revision = revision;
		this.text = text;
		this.timeModified = timeModified;
		this.userModified = userModified;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getDisplayName()
	{
		return displayName;
	}

	public void setDisplayName(String displayName)
	{
		this.displayName = displayName;
	}

	public WSPathwayInfo getPathway()
	{
		return pathway;
	}

	public void setPathway(WSPathwayInfo pathway)
	{
		this.pathway = pathway;
	}

	public String getRevision()
	{
		return revision;
	}

	public void setRevision(String revision)
	{
		this.revision = revision;
	}

	public String getText()
	{
		return text;
	}

	public void setText(String text)
	{
		this.text = text;
	}

	public long getTimeModified()
	{
		return timeModified;
	}

	public void setTimeModified(long timeModified)
	{
		this.timeModified = timeModified;
	}

	public String getUserModified()
	{
		return userModified;
	}

	public void setUserModified(String userModified)
	{
		this.userModified = userModified;
	}
}
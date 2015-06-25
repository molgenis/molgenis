package org.molgenis.vortext;

import java.util.List;

public class AnnotationGroup
{
	private String type;
	private String id;
	private String description;
	private String title;
	private List<Annotation> annotations;

	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public List<Annotation> getAnnotations()
	{
		return annotations;
	}

	public void setAnnotations(List<Annotation> annotations)
	{
		this.annotations = annotations;
	}

	@Override
	public String toString()
	{
		return "AnnotationGroup [type=" + type + ", id=" + id + ", description=" + description + ", title=" + title
				+ ", annotations=" + annotations + "]";
	}

}

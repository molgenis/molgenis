package org.molgenis.vortext;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Represents a block on the right side of the screen
 */
public class Marginalis implements Iterable<Annotation>
{
	private String type;
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

	public void addAnnotation(Annotation annotation)
	{
		getAnnotations().add(annotation);
	}

	public List<Annotation> getAnnotations()
	{
		if (annotations == null)
		{
			annotations = new ArrayList<Annotation>();
		}

		return annotations;
	}

	public void setAnnotations(List<Annotation> annotations)
	{
		this.annotations = annotations;
	}

	@Override
	public Iterator<Annotation> iterator()
	{
		return getAnnotations().iterator();
	}

}

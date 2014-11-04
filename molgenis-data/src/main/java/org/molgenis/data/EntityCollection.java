package org.molgenis.data;


public interface EntityCollection extends Iterable<Entity>
{
	Iterable<String> getAttributeNames();
}

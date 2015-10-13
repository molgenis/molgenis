package org.molgenis.data;

public interface RepositoryDecorator extends Repository
{
	void setTarget(Repository target);
}
package org.molgenis.omx.study;

import org.molgenis.omx.auth.MolgenisUser;
import org.molgenis.omx.observ.ObservableFeature;

public interface StudyDefinition
{
	public String getId();

	public String getName();

	public Iterable<ObservableFeature> getFeatures();

	public MolgenisUser getAuthor();
}

package org.molgenis.omx.studymanager;

import org.molgenis.omx.catalogmanager.OmxCatalogFolder;
import org.molgenis.omx.observ.Protocol;

public class OmxStudyDefinitionItem extends OmxCatalogFolder
{
	@SuppressWarnings("unused")
	private final Integer catalogId;

	public OmxStudyDefinitionItem(Protocol protocol, Integer catalogId)
	{
		super(protocol);
		if (catalogId == null) throw new IllegalArgumentException("catalogId is null");
		this.catalogId = catalogId;
	}
}

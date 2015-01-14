package org.molgenis.data.mapping;

import org.molgenis.auth.MolgenisUser;

import java.util.List;

/**
 * Created by charbonb on 14/01/15.
 */
public class MappingProject {
	private String id;
	private MolgenisUser owner;
	private List<EntityMapping> entityMappings;
}

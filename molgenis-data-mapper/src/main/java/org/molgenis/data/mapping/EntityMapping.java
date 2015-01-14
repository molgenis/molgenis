package org.molgenis.data.mapping;

import org.molgenis.data.EntityMetaData;

import java.util.List;

/**
 * Created by charbonb on 14/01/15.
 */
public class EntityMapping {
	private String id;
	private EntityMetaData sourceEntityMetaData;
	private EntityMetaData targetEntityMetaData;
	private List<AttributeMapping> entityMappings;
}

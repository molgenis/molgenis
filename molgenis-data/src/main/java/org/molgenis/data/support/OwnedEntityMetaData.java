package org.molgenis.data.support;

import static org.molgenis.MolgenisFieldTypes.STRING;

import org.springframework.stereotype.Component;

/**
 * Defines an abstract EntityMetaData for entities that have an 'owner'.
 * 
 * These entities can only be viewed/updated/deleted by it's creator.
 * 
 * Defines one attribute 'ownerUsername', that is the username of the owner. You can extend this EntityMetaData to
 * inherit this behavior.
 */
@Component
public class OwnedEntityMetaData extends DefaultEntityMetaData
{
	public static final String ENTITY_NAME = "Owned";
	public static final String ATTR_OWNER_USERNAME = "ownerUsername";

	public OwnedEntityMetaData()
	{
		super(ENTITY_NAME);
		setAbstract(true);
		addAttribute(ATTR_OWNER_USERNAME).setDataType(STRING).setVisible(false);
	}
}

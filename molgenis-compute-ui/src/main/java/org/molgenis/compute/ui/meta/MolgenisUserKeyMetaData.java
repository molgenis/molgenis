package org.molgenis.compute.ui.meta;

import static org.molgenis.MolgenisFieldTypes.TEXT;

import org.molgenis.data.support.DefaultEntityMetaData;

public class MolgenisUserKeyMetaData extends DefaultEntityMetaData
{
	public static final MolgenisUserKeyMetaData INSTANCE = new MolgenisUserKeyMetaData();

	private static final String ENTITY_NAME = "MolgenisUserKey";
	public static final String IDENTIFIER = "identifier";
	public static final String USER = "user";
	public static final String SSH_KEY_PRIVATE = "sshKeyPrivate";
	public static final String SSH_KEY_PUBLIC = "sshKeyPublic";
	public static final String PASSPHRASE = "passphrase";

	private MolgenisUserKeyMetaData()
	{
		super(ENTITY_NAME, ComputeUiPackage.INSTANCE);
		addAttribute(IDENTIFIER).setIdAttribute(true).setNillable(false);
		// FIXME user xref to MolgenisUser when https://github.com/molgenis/molgenis/issues/2054 is fixed
		addAttribute(USER).setNillable(false).setUnique(true);
		addAttribute(SSH_KEY_PRIVATE).setDataType(TEXT).setNillable(false);
		addAttribute(SSH_KEY_PUBLIC).setDataType(TEXT).setNillable(false);
		addAttribute(PASSPHRASE).setDataType(TEXT).setNillable(false);
	}

}

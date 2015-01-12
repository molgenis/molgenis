package org.molgenis.compute.ui.model;

import org.molgenis.compute.ui.meta.MolgenisUserKeyMetaData;
import org.molgenis.data.support.MapEntity;

public class MolgenisUserKey extends MapEntity
{
	private static final long serialVersionUID = 1L;

	public MolgenisUserKey()
	{
		super(MolgenisUserKeyMetaData.INSTANCE);
	}

	public MolgenisUserKey(String identifier, String username, String sshKeyPrivate, String sshKeyPublic,
			String passphrase)
	{
		super(MolgenisUserKeyMetaData.INSTANCE);
		set(MolgenisUserKeyMetaData.IDENTIFIER, identifier);
		setUser(username);
		setSshKeyPrivate(sshKeyPrivate);
		setSshKeyPublic(sshKeyPublic);
		setPassphrase(passphrase);
	}

	public String getUser()
	{
		return getString(MolgenisUserKeyMetaData.USER);
	}

	public void setUser(String username)
	{
		set(MolgenisUserKeyMetaData.USER, username);
	}

	public String getSshKeyPrivate()
	{
		return getString(MolgenisUserKeyMetaData.SSH_KEY_PRIVATE);
	}

	public void setSshKeyPrivate(String sshKeyPrivate)
	{
		set(MolgenisUserKeyMetaData.SSH_KEY_PRIVATE, sshKeyPrivate);
	}

	public String getSshKeyPublic()
	{
		return getString(MolgenisUserKeyMetaData.SSH_KEY_PUBLIC);
	}

	public void setSshKeyPublic(String sshKeyPublic)
	{
		set(MolgenisUserKeyMetaData.SSH_KEY_PUBLIC, sshKeyPublic);
	}

	public String getPassphrase()
	{
		return getString(MolgenisUserKeyMetaData.PASSPHRASE);
	}

	private void setPassphrase(String passphrase)
	{
		set(MolgenisUserKeyMetaData.PASSPHRASE, passphrase);
	}
}

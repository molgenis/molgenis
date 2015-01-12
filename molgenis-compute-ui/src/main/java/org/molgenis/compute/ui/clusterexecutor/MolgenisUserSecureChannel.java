package org.molgenis.compute.ui.clusterexecutor;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.molgenis.compute.ui.model.MolgenisUserKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class MolgenisUserSecureChannel implements Closeable
{
	private static final Logger LOG = LoggerFactory.getLogger(MolgenisUserSecureChannel.class);

	private final JSch jSch;
	private final MolgenisUserKey userKeyPair;
	private File tmpPublicKeyFile;
	private File tmpPrivateKeyFile;

	public MolgenisUserSecureChannel(MolgenisUserKey userKeyPair) throws IOException
	{
		if (userKeyPair == null) throw new IllegalArgumentException("userKeyPair is null");
		this.jSch = new JSch();
		this.userKeyPair = userKeyPair;

		init();
	}

	private void init() throws IOException
	{
		// write temporary SSH public and private key file to disk
		tmpPublicKeyFile = File.createTempFile("ssh_", null);
		String publicSshKey = userKeyPair.getSshKeyPublic();
		try
		{
			FileUtils.writeStringToFile(tmpPublicKeyFile, publicSshKey, "UTF-8");
		}
		catch (IOException e)
		{
			boolean ok = tmpPublicKeyFile.delete();
			if (!ok)
			{
				LOG.error("Failed to delete public SSH key file [" + tmpPublicKeyFile.getPath() + "]");
			}
		}
		tmpPublicKeyFile.deleteOnExit(); // in case user forgets to call disconnect

		tmpPrivateKeyFile = File.createTempFile("ssh_", null);
		String privateSshKey = userKeyPair.getSshKeyPrivate();
		try
		{
			FileUtils.writeStringToFile(tmpPrivateKeyFile, privateSshKey, "UTF-8");
		}
		catch (IOException e)
		{
			boolean ok = tmpPrivateKeyFile.delete();
			if (!ok)
			{
				LOG.error("Failed to delete private SSH key file [" + tmpPrivateKeyFile.getPath() + "]");
			}
		}
		tmpPrivateKeyFile.deleteOnExit(); // in case user forgets to call disconnect

		String passphrase = userKeyPair.getPassphrase();
		try
		{
			jSch.addIdentity(tmpPrivateKeyFile.getPath(), tmpPublicKeyFile.getPath(), passphrase.getBytes("UTF-8"));
		}
		catch (JSchException e)
		{
			throw new IOException(e);
		}
	}

	public Session getSession(String host, int port) throws IOException
	{
		String username = userKeyPair.getUser();
		try
		{
			Session session = jSch.getSession(username, host, port);
			java.util.Properties config = new java.util.Properties();
			config.put("StrictHostKeyChecking", "no");
			session.setConfig(config);
			return session;
		}
		catch (JSchException e)
		{
			throw new IOException(e);
		}
	}

	@Override
	public void close() throws IOException
	{
		// delete temporary SSH public and private key file from disk
		boolean deletePrivateOk = tmpPrivateKeyFile.delete();
		if (!deletePrivateOk)
		{
			throw new IOException("Failed to delete private SSH key file [" + tmpPrivateKeyFile.getPath() + "]");
		}

		boolean deletePublicOk = tmpPublicKeyFile.delete();
		if (!deletePublicOk)
		{
			throw new IOException("Failed to delete public SSH key file [" + tmpPublicKeyFile.getPath() + "]");
		}
	}
}

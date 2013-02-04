package org.molgenis.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

import org.apache.log4j.Logger;

import ch.ethz.ssh2.ChannelCondition;
import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.InteractiveCallback;
import ch.ethz.ssh2.LocalPortForwarder;
import ch.ethz.ssh2.SCPClient;
import ch.ethz.ssh2.Session;

/**
 * Wrapper arround ssh. Build on top of
 * 
 * http://www.cleondris.ch/opensource/ssh2/javadoc/ (BSD type license)
 * http://www.ganymed.ethz.ch/ssh2/FAQ.html
 */
public class Ssh
{
	// general ssh settings
	private String hostname;
	private String username;
	private String password;
	private int timeout = 10000;
	private int port = 22;

	Logger logger = Logger.getLogger(Ssh.class);

	// implementation specific
	Connection conn;
	Session sess;
	// for forwarding
	LocalPortForwarder lpf;

	public Ssh(String host, String user, String password) throws IOException
	{
		this(host, user, password, 22);
	}

	public Ssh(String host, String user, String password, int port) throws IOException
	{
		this.hostname = host;
		this.username = user;
		this.password = password;
		this.port = port;

		this.connect();
	}

	public Ssh(String host, String user, String password, int port, String forwardHost, String forwardUser,
			String forwardPassword, int forwardPort) throws IOException
	{
		// first create a tunnel, then connect to second host
		Ssh forwardSsh = new Ssh(forwardHost, forwardUser, forwardPassword, forwardPort);

		// setup the tunnel via the forwardHost to the port
		forwardSsh.forward(9999, host, port);

		// create the ssh to the forwarded port
		this.hostname = "127.0.0.1";
		this.username = user;
		this.password = password;
		this.port = 9999;

		this.connect();
	}

	public void forward(int local_port, String host_to_connect, int port_to_connect) throws IOException
	{
		logger.debug("creating a tunnel from L:" + local_port + "->" + host_to_connect + ":" + port_to_connect);
		lpf = conn.createLocalPortForwarder(local_port, host_to_connect, port_to_connect);

		// conn.requestRemotePortForwarding("127.0.0.1", local_port,
		// host_to_connect, port_to_connect);
	}

	/**
	 * Connect to server as a session
	 * 
	 * @throws IOException
	 */
	private void connect() throws IOException
	{
		logger.debug("trying to connect to " + this.username + "@" + this.hostname + ":" + this.port);
		/* Create a connection instance */
		conn = new Connection(this.hostname, this.port);

		/* Now connect */
		conn.connect();

		/* Authenticate */
		try
		{
			boolean isAuthenticated = conn.authenticateWithPassword(username, password);

			if (isAuthenticated == false)
			{
				throw new IOException("Authentication failed.");
			}

		}
		catch (Exception e)
		{ // authenticated method not supported
			// try to use keyboard interactive
			conn.authenticateWithKeyboardInteractive(username, new InteractiveCallback()
			{
				@Override
				public String[] replyToChallenge(String name, String instruction, int numPrompts, String[] prompt,
						boolean[] echo) throws Exception
				{
					String[] responses = new String[numPrompts];
					for (int x = 0; x < numPrompts; x++)
					{
						responses[x] = password;
					}
					return responses;
				}
			});
		}

		logger.debug("connected to " + this.username + "@" + this.hostname + ":" + this.port);
	}

	public SshResult executeCommand(String command) throws IOException
	{
		return this.executeCommand(command, timeout);
	}

	/** Execute one command and wait for the result to return */
	public SshResult executeCommand(String command, int timeout) throws IOException
	{
		logger.debug("executing command: " + command);

		try
		{
			/* Create a session */
			sess = conn.openSession();

			// code thanks to SingleThreadStdoutStderr example from ch.ethz.ssh2
			StringBuffer stdOutBuffer = new StringBuffer();
			StringBuffer stdErrBuffer = new StringBuffer();

			InputStream stdout = sess.getStdout();
			InputStream stderr = sess.getStderr();

			// sess.startShell()
			sess.execCommand(command);

			byte[] buffer = new byte[8192];

			while (true)
			{
				if ((stdout.available() == 0) && (stderr.available() == 0))
				{
					/*
					 * Even though currently there is no data available, it may
					 * be that new data arrives and the session's underlying
					 * channel is closed before we call waitForCondition(). This
					 * means that EOF and STDOUT_DATA (or STDERR_DATA, or both)
					 * may be set together.
					 */

					int conditions = sess.waitForCondition(ChannelCondition.STDOUT_DATA | ChannelCondition.STDERR_DATA
							| ChannelCondition.EOF, timeout);

					/* Wait no longer than 2 seconds (= 2000 milliseconds) */

					if ((conditions & ChannelCondition.TIMEOUT) != 0)
					{
						/* A timeout occured. */
						throw new IOException("Timeout while waiting for data from peer.");
					}

					/*
					 * Here we do not need to check separately for CLOSED, since
					 * CLOSED implies EOF
					 */

					if ((conditions & ChannelCondition.EOF) != 0)
					{
						/* The remote side won't send us further data... */

						if ((conditions & (ChannelCondition.STDOUT_DATA | ChannelCondition.STDERR_DATA)) == 0)
						{
							/*
							 * ... and we have consumed all data in the local
							 * arrival window.
							 */
							break;
						}
					}

					/* OK, either STDOUT_DATA or STDERR_DATA (or both) is set. */

					// You can be paranoid and check that the library is not
					// going
					// nuts:
					if ((conditions & (ChannelCondition.STDOUT_DATA | ChannelCondition.STDERR_DATA)) == 0) throw new IOException(
							"Unexpected condition result (" + conditions + ")");
				}

				/*
				 * If you below replace "while" with "if", then the way the
				 * output appears on the local stdout and stder streams is more
				 * "balanced". Addtionally reducing the buffer size will also
				 * improve the interleaving, but performance will slightly
				 * suffer. OKOK, that all matters only if you get HUGE amounts
				 * of stdout and stderr data =)
				 */

				while (stdout.available() > 0)
				{
					int len = stdout.read(buffer);
					if (len > 0) // this check is somewhat paranoid
					{
						stdOutBuffer.append(new String(buffer, 0, len, Charset.forName("UTF-8")));
					}
				}

				while (stderr.available() > 0)
				{
					int len = stderr.read(buffer);
					if (len > 0) // this check is somewhat paranoid
					stdErrBuffer.append(new String(buffer, 0, len, Charset.forName("UTF-8")));
				}
			}

			return new SshResult(stdOutBuffer.toString(), stdErrBuffer.toString());
		}
		catch (IOException e)
		{
			throw e;
		}
		finally
		{
			/** Always close the session! */
			sess.close();
		}

	}

	public void close()
	{
		if (lpf != null) try
		{
			lpf.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		this.sess.close();
		this.conn.close();
	}

	/**
	 * Download remote file to local file via scp
	 */
	public void uploadFile(File localFile, String remoteFile) throws IOException
	{
		logger.debug("upload local file '" + localFile + "' to remote file '" + remoteFile + "'");

		SCPClient scp = conn.createSCPClient();

		OutputStream out = new FileOutputStream(localFile);
		try
		{
			// split remote file in directory
			if (remoteFile.contains("/"))
			{
				String dir = remoteFile.substring(0, remoteFile.lastIndexOf("/"));
				String file = remoteFile.substring(remoteFile.lastIndexOf("/") + 1);
				scp.put(localFile.getAbsolutePath(), file, dir, "0600");
			}
			else
			{
				scp.put(localFile.getAbsolutePath(), remoteFile, "", "0600");
			}
			out.flush();
		}
		finally
		{

			out.close();
		}
		logger.debug("upload file complete");
	}

	/**
	 * Upload string to remote file.
	 * 
	 * @param string
	 *            to upload
	 * @param remoteFile
	 *            full path including directories
	 * @throws IOException
	 */
	public void uploadStringToFile(String string, String remoteFile) throws IOException
	{
		logger.debug("upload string to remote file '" + remoteFile + "'");

		SCPClient scp = conn.createSCPClient();

		if (remoteFile.contains("/"))
		{
			String dir = remoteFile.substring(0, remoteFile.lastIndexOf("/"));
			String file = remoteFile.substring(remoteFile.lastIndexOf("/") + 1);
			scp.put(string.getBytes("UTF-8"), file, dir, "0600");
		}
		else
		{
			scp.put(string.getBytes("UTF-8"), remoteFile, "", "0600");
		}
		logger.debug("upload file complete");

	}

	/**
	 * Upload a string using scp, with seperate directory and file parameters.
	 * 
	 * @param string
	 *            to upload
	 * @param remoteFile
	 *            only the file name
	 * @param remoteDir
	 *            path to the directory
	 * @throws IOException
	 */
	public void uploadStringToFile(String string, String remoteFile, String remoteDir) throws IOException
	{
		if (!"".equals(remoteDir))
		{
			this.uploadStringToFile(string, remoteDir + "/" + remoteFile);
		}
		else
		{
			this.uploadStringToFile(string, remoteFile);
		}

	}

	/**
	 * Download remote file to local file via scp
	 * 
	 * @param remoteFile
	 * @param localFile
	 * @throws IOException
	 */
	public void downloadFile(String remoteFile, File localFile) throws IOException
	{
		logger.debug("download remote file '" + remoteFile + "' to local file " + localFile);
		SCPClient scp = conn.createSCPClient();

		OutputStream out = new FileOutputStream(localFile);
		try
		{
			scp.get(remoteFile, out);

			out.flush();
		}
		finally
		{
			out.close();
		}

		logger.debug("download file complete");
	}

	public String downloadFileIntoString(String remoteFile) throws IOException
	{
		logger.debug("download remote file '" + remoteFile);
		SCPClient scp = conn.createSCPClient();

		ByteArrayOutputStream out = new ByteArrayOutputStream();

		scp.get(remoteFile, out);

		return out.toString("UTF-8");
	}

	@Override
	protected void finalize()
	{
		this.close();
	}

	// stupid getters and setters

	public String getHost()
	{
		return hostname;
	}

	public void setHost(String host)
	{
		this.hostname = host;
	}

	public String getUsername()
	{
		return username;
	}

	public void setUsername(String username)
	{
		this.username = username;
	}

	public String getPassword()
	{
		return password;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

	public int getPort()
	{
		return port;
	}

	public void setPort(int port)
	{
		this.port = port;
	}

	public String downloadFile(String remoteFile) throws IOException
	{
		logger.debug("download remoteFile file '" + remoteFile + "' as string");

		SCPClient scp = conn.createSCPClient();

		ByteArrayOutputStream out = new ByteArrayOutputStream();

		scp.get(remoteFile, out);

		logger.debug("download file complete");

		return out.toString("UTF-8");
	}

}

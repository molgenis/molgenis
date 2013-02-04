package org.molgenis.omx.services;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;

import org.molgenis.omx.core.RuntimeProperty;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
import org.molgenis.util.DetectOS;

public class StorageHandler
{

	static String RUNTIME_FILE_STORAGE_PATH = "file_storage_path";
	static String RUNTIME_FILE_STORAGE_VALIDATED = "file_storage_validated";

	Report report;

	/**
	 * On instantiation, build a simple report of the current state, without
	 * running actual validation yet.
	 * 
	 * @param db
	 */
	public StorageHandler(Database db)
	{
		report = new Report();

		RuntimeProperty pathRp = null;
		RuntimeProperty validRp = null;

		try
		{
			pathRp = getRuntimeProperty(RUNTIME_FILE_STORAGE_PATH, db);
			validRp = getRuntimeProperty(RUNTIME_FILE_STORAGE_VALIDATED, db);
		}
		catch (DatabaseException e)
		{
			// tables do not exist (yet)
			// that's OK, see below
		}

		if (pathRp == null && validRp == null)
		{
			report.setFileStoragePropsPresent(false);
		}
		else
		{
			// try-catch not so nice here, this should never fail if the
			// storage props are set.. but it saves you from a lot of
			// try-catch in plugin reloads etc. Exception is re-thrown as
			// IllegalStateException.
			try
			{
				report.setFileStoragePropsPresent(true);
				File storageDir = getFileStorage(db);
				report.setFileStorage(storageDir);
				report.setFolderExists(storageDir.exists());
				report.setFolderHasContent(folderHasContent(storageDir));
				// check if the path has already been validated
				if (validRp.getValue().equals("true"))
				{
					report.setVerified(true);
				}
				else
				{
					report.setVerified(false);
				}
			}
			catch (Exception e)
			{
				throw new IllegalStateException(e);
			}
		}
	}

	/**
	 * Get the path of the file storage, regardless whether it is valid or not.
	 * Throws exception if there is no storage location set.
	 * 
	 * @throws Exception
	 */
	public File getFileStorage(Database db) throws Exception
	{
		return getFileStorage(false, db);
	}

	/**
	 * Get the path of the file storage. Uses the application name to make it
	 * (more?) unique. Throws exception if there is no storage location set.
	 * 
	 * @param mustBeValid
	 * @return
	 * @throws Exception
	 */
	public File getFileStorage(boolean mustBeValid, Database db) throws Exception
	{
		File storage = getFileStorageRoot(mustBeValid, db);
		if (storage == null)
		{
			throw new Exception("No retrievable or valid file storage location present.");
		}
		return new File(storage.getAbsolutePath());
	}

	/**
	 * Remove file storage location and validation status.
	 * 
	 * @throws Exception
	 */
	public void deleteFileStorage(Database db) throws Exception
	{
		RuntimeProperty pathRp = getRuntimeProperty(RUNTIME_FILE_STORAGE_PATH, db);
		RuntimeProperty validRp = getRuntimeProperty(RUNTIME_FILE_STORAGE_VALIDATED, db);
		if (pathRp == null && validRp == null)
		{
			throw new Exception("Nothing to delete");
		}
		else if (pathRp != null && validRp == null)
		{
			throw new Exception("SEVERE: Validation prop is empty, but path is not!");
		}
		else if (pathRp == null && validRp != null)
		{
			throw new Exception("SEVERE: Path prop is empty, but validation is not!");
		}
		db.remove(pathRp);
		db.remove(validRp);
		report = new Report();
	}

	/**
	 * Ask if these is a file storage location available, regardless whether it
	 * is valid or not.
	 * 
	 * @param mustBeValid
	 * @return
	 * @throws Exception
	 */
	public boolean hasFileStorage(boolean mustBeValid, Database db) throws Exception
	{
		File storage = getFileStorageRoot(mustBeValid, db);
		if (storage == null)
		{
			return false;
		}
		return true;
	}

	/**
	 * Ask if these is a valid file storage location available.
	 * 
	 * @return
	 * @throws Exception
	 */
	public boolean hasValidFileStorage(Database db) throws Exception
	{
		return hasFileStorage(true, db);
	}

	/**
	 * Save this path in the form of a string to the file storage path entity.
	 * 
	 * @param filesource
	 * @throws Exception
	 */
	public void setFileStorage(String filesource, Database db) throws Exception
	{
		report = new Report();

		if (filesource == null || filesource.equals("") || filesource.equals("null")) throw new DatabaseException(
				"Empty path not allowed");

		filesource = addSepIfneeded(filesource);

		RuntimeProperty rp = getRuntimeProperty(RUNTIME_FILE_STORAGE_PATH, db);
		if (rp == null)
		{
			RuntimeProperty rp1 = new RuntimeProperty();
			rp1.setName(RUNTIME_FILE_STORAGE_PATH);

			filesource = filesource.replace("\\", "\\\\"); // escape slashes for
															// windows? FIXME
															// needed??
			rp1.setValue(filesource);
			db.add(rp1);

			RuntimeProperty rp2 = new RuntimeProperty();
			rp2.setName(RUNTIME_FILE_STORAGE_VALIDATED);
			rp2.setValue("false");
			db.add(rp2);
		}
		else
		{
			throw new DatabaseException("Could not set file storage: Properties already present. Please delete first.");
		}
	}

	/**
	 * Run validation check on the current set file storage path.
	 * 
	 * @throws Exception
	 */
	public void validateFileStorage(Database db) throws Exception
	{

		RuntimeProperty pathRp = getRuntimeProperty(RUNTIME_FILE_STORAGE_PATH, db);
		RuntimeProperty validRp = getRuntimeProperty(RUNTIME_FILE_STORAGE_VALIDATED, db);

		if (pathRp != null && validRp == null)
		{
			throw new Exception("SEVERE: Validation prop is empty, but path is not!");
		}
		else if (pathRp == null && validRp != null)
		{
			throw new Exception("SEVERE: Path prop is empty, but validation is not!");
		}

		if (pathRp == null && validRp == null)
		{
			throw new Exception("Please set file storage before validating");
		}

		File f = new File(pathRp.getValue());

		if (!f.exists())
		{
			boolean mkDirSuccess = f.mkdirs();
			if (!mkDirSuccess)
			{
				throw new Exception("Could not create directory");
			}
		}

		if (f.exists())
		{
			File tmp = new File(f.getAbsolutePath() + File.separator + "tmp" + System.currentTimeMillis() + ".txt");
			boolean createSuccess = tmp.createNewFile();
			if (createSuccess)
			{
				FileOutputStream fos = new FileOutputStream(tmp);
				DataOutputStream dos = new DataOutputStream(fos);
				dos.writeChars("test");
				dos.close();
				fos.close();

				boolean deleteSuccess = tmp.delete();
				if (!deleteSuccess)
				{
					throw new Exception("Could not delete file");
				}
			}
			else
			{
				throw new Exception("Could not write to file");
			}
		}
		else
		{
			throw new Exception("Directory does not exist");
		}

		RuntimeProperty rp = getRuntimeProperty(RUNTIME_FILE_STORAGE_VALIDATED, db);

		rp.setValue("true");
		db.update(rp);
		report.setVerified(true);

	}

	/**
	 * Internal function. Get the root of the file storage location as a file
	 * pointer.
	 * 
	 * @param mustBeValid
	 * @return
	 * @throws DatabaseException
	 * @throws UnsupportedEncodingException
	 */
	private File getFileStorageRoot(boolean mustBeValid, Database db) throws UnsupportedEncodingException,
			DatabaseException
	{
		URI loc = getURIStorageRoot(mustBeValid, db);
		if (loc == null)
		{
			return null;
		}
		String decodedURI = URLDecoder.decode(loc.toString(), "UTF-8");
		File f = new File(decodedURI.toString());
		return f;
	}

	/**
	 * Internal function. Get the root of the file storage location as a URI.
	 * 
	 * @param mustBeValid
	 * @return
	 */
	private URI getURIStorageRoot(boolean mustBeValid, Database db) throws UnsupportedEncodingException,
			DatabaseException
	{
		RuntimeProperty path = getRuntimeProperty(RUNTIME_FILE_STORAGE_PATH, db);
		RuntimeProperty valid = getRuntimeProperty(RUNTIME_FILE_STORAGE_VALIDATED, db);

		if (path == null || valid == null)
		{
			return null;
		}

		if (mustBeValid && !valid.getValue().equals("true"))
		{
			return null;
		}

		String dir = path.getValue();
		if (!DetectOS.getOS().startsWith("windows"))
		{
			// if there is no starting seperator (ie. /data)
			if (!dir.startsWith(File.separator))
			{
				// add one
				dir = File.separator + dir;
			}
		}
		String encodedToUrl = URLEncoder.encode(dir, "UTF-8");
		URI res = URI.create(encodedToUrl);
		return res;
	}

	/**
	 * Helper function
	 * 
	 * @return
	 * @throws DatabaseException
	 */
	private RuntimeProperty getRuntimeProperty(String propName, Database db) throws DatabaseException
	{
		QueryRule name = new QueryRule(RuntimeProperty.NAME, Operator.EQUALS, propName);
		List<RuntimeProperty> rpList = null;
		try
		{
			rpList = db.find(RuntimeProperty.class, name);
		}
		catch (Exception e)
		{
			// maybe database or table does not exist yet
			// or db.find comes up null
			return null;
		}

		if (rpList.size() == 0)
		{
			return null;
		}
		else if (rpList.size() == 1)
		{
			return rpList.get(0);
		}
		else
		{
			throw new DatabaseException("SEVERE: RuntimeProperty name '" + propName + "' not unique");
		}
	}

	/**
	 * If OS is unix like, and path does not start with a separator, add
	 * separator in front of the path
	 * 
	 * @param path
	 * @return
	 * @throws Exception
	 */
	private String addSepIfneeded(String path) throws Exception
	{
		if (!DetectOS.getOS().startsWith("windows") && !path.startsWith(File.separator))
		{
			path = File.separator + path;
		}
		return path;
	}

	/**
	 * Helper function
	 * 
	 * @param path
	 * @return
	 */
	private boolean folderHasContent(File f)
	{
		if (f.exists())
		{
			if (f.listFiles().length == 0)
			{
				return false;
			}
			else
			{
				return true;
			}
		}
		return false;
	}

	public Report getReport()
	{
		return report;
	}

}

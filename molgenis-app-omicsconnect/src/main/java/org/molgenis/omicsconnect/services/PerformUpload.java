package org.molgenis.omicsconnect.services;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;
import org.molgenis.omx.core.MolgenisFile;
import org.molgenis.framework.db.Database;
import org.molgenis.omx.decorators.MolgenisFileHandler;
import org.molgenis.omx.decorators.NameConvention;
import org.molgenis.util.ValueLabel;


public class PerformUpload
{

	/**
	 * Generic file upload. This function assumes the MolgenisFile is already in
	 * the database, and only a file source corresponding to this MolgenisFile
	 * is added.
	 * 
	 * @param db
	 * @param mf
	 * @param content
	 * @throws Exception
	 * @throws XGAPStorageException
	 */
	public static void doUpload(Database db, MolgenisFile mf, File content, boolean skipWhenDestExists)
			throws Exception
	{
		File storageForFileType = new MolgenisFileHandler(db).getStorageDirFor(mf.get__Type(), db);

		// copy the file in the right place
		String storageFileName = NameConvention.escapeFileName(mf.getName());
		File dest = new File(storageForFileType.getAbsolutePath() + File.separator + storageFileName + "."
				+ mf.getExtension());
		copyHelper(content, dest, skipWhenDestExists);
	}

	/**
	 * Generic file upload. This function will create a new MolgenisFile object
	 * in the database, as well as put the referenced file source for this
	 * MolgenisFile in the correct place. The 'type' must correspond to the name
	 * of a subclass of MolgenisFile. Fields and types are dynamically checked
	 * and casted.
	 * 
	 * @param db
	 *            The database to use.
	 * @param name
	 *            Full name of the original file (eg. 'mypicture.png')
	 * @param type
	 *            Type of the file. Must correspond to a MolgenisFile subclass.
	 *            (eg. 'Image' or 'BinaryDataMatrix') Used as part of the
	 *            storage location. (eg. {storagedir}/{deployname}/image)
	 * @param content
	 *            Any existing file of which only the content is submitted to
	 *            the upload.
	 * @param extraFields
	 *            A hashmap containing values of the additional fields that are
	 *            used by this subclass of MolgenisFile. For example,
	 *            'investigation_name' for Image.
	 * @throws Exception
	 */
	public static void doUpload(Database db, boolean useTx, String name, String type, File content,
			HashMap<String, String> extraFields, boolean skipWhenDestExists) throws Exception
	{

		// file checks
		if (content == null)
		{
			throw new Exception("File holding content is a nullpointer!");
		}

		if (!content.exists())
		{
			throw new Exception("File holding content does not exist: " + content.getAbsolutePath());
		}

		// see if we need extra fields for this type of MolgenisFile
		ArrayList<String> extraNeededFields = getNeededExtraFields(type, db);

		// if so, check if they are present in the input
		if (extraNeededFields.size() > 0)
		{
			for (String neededField : extraNeededFields)
			{
				if (!extraFields.keySet().contains(neededField))
				{
					throw new Exception("Missing needed field '" + neededField + "' for MolgenisFile type '" + type
							+ "'");
				}
			}
		}

		// perform checks on filename and extension
		fileNameChecks(name);

		// filename and extension supposed to be ok for splitting
		String[] split = name.split("\\.");

		// create new MolgenisFile --> which is actually of the subclass type!
		MolgenisFile mfAdd = (MolgenisFile) db.getClassForName(type).newInstance();

		// set name and extension
		mfAdd.setName(split[0]);
		mfAdd.setExtension(split[1]);

		// copy extra fields for this type of MolgenisFile
		for (String extraField : extraFields.keySet())
		{
			mfAdd.set(extraField, extraFields.get(extraField));
		}

		// start db transaction if there is none yet, and useTx is selected,
		// otherwise assume the user started a transaction elsewhere and wants
		// to manage this him/herself!
		boolean txStartedHere = false;
		if (useTx && !db.inTx())
		{
			db.beginTx();
			txStartedHere = true;
		}

		try
		{
			// try adding to db - if add succeeds, try sorting out the file
			db.add(mfAdd);

			// get storage dir for this MolgenisFile type
			File storageForFileType = new MolgenisFileHandler(db).getStorageDirFor(type, db);

			// copy the file in the right place
			String storageFileName = NameConvention.escapeFileName(mfAdd.getName());
			File dest = new File(storageForFileType.getAbsolutePath() + File.separator + storageFileName + "."
					+ mfAdd.getExtension());
			copyHelper(content, dest, skipWhenDestExists);

			// commit if file is in the right place, and if the transaction is
			// started in this function (see comment above for beginTx)
			if (txStartedHere)
			{
				db.commitTx();
			}
		}
		catch (Exception e)
		{
			// if something fails, rollback and throw error
			if (txStartedHere)
			{
				db.rollbackTx();
			}
			throw e;
		}
	}

	public static void fileNameChecks(String name) throws Exception
	{
		// see if trimmed name is empty, and continue to trim if not
		if (name.trim().length() == 0)
		{
			throw new Exception("No filename specified");
		}
		name = name.trim();

		// we know: filename (already trimmed) has length > 0
		// now check of starting or ending '.'
		if (name.startsWith(".") || name.endsWith("."))
		{
			throw new Exception("Please provide proper filename (not starting or ending with '.')");
		}

		// file name must contain 1 '.'
		if (name.contains("."))
		{
			int countPeriods = 0;
			for (char c : name.toCharArray())
			{
				if (c == '.')
				{
					countPeriods++;
				}
				if (countPeriods > 1)
				{
					throw new Exception("Please provide proper filename (not more than one '.') instead of '" + name
							+ "'");
				}
			}
		}
		else
		{
			throw new Exception("No '.' in filename '" + name + "' found - please provide an extension (ie. harry.png)");
		}
		// split and do some extra checks
		String[] split = name.split("\\.");
		if ((split.length != 2) || split[0].length() == 0 || split[1].length() == 0)
		{
			throw new Exception("Error when splitting file name '" + name
					+ "' using '.'. Filename doesn't consist of two parts or a part was an empty string.");
		}
	}

	/**
	 * Reusable function that will return a list of fieldnames that are needed
	 * for this subclass of 'MolgenisFile'.
	 * 
	 * @param type
	 * @param db
	 * @return
	 * @throws Exception
	 */
	public static ArrayList<String> getNeededExtraFields(String type, Database db) throws Exception
	{

		ArrayList<String> result = new ArrayList<String>();

		// create regular MolgenisFile
		MolgenisFile mfSuperClass = new MolgenisFile();

		// add all regular fields to a list
		ArrayList<String> options = new ArrayList<String>();
		for (ValueLabel vl : mfSuperClass.get__TypeOptions())
		{
			options.add(vl.getValue().toString());
		}

		// check if there is a subclass for this type
		if (!options.contains(type))
		{
			throw new Exception("Type '" + type
					+ "' is not a subclass of MolgenisFile. Maybe you should use uppercase?");
		}

		// try to instantiate the subclass
		MolgenisFile mfSubClass = (MolgenisFile) db.getClassForName(type).newInstance();

		// DISCUSSION:
		// maybe use..
		// JDBCMetaDatabase meta = new JDBCMetaDatabase();
		// org.molgenis.model.elements.Entity entity = meta.getEntity("Marker");
		// for(org.molgenis.model.elements.Field f : entity.getFields()){
		// f.isNillable();
		// }

		// iterate through the fields of a subclassed type, and find any
		// additional fields however don't add them if they have a '_name'
		for (String subClassField : mfSubClass.getFields())
		{
			// if the field is not in MolgenisFile
			if (!mfSuperClass.getFields().contains(subClassField))
			{
				// field has no '_name' (a convenient unique xref pointer)
				if (!subClassField.endsWith("_name"))
				{
					// check if there is a '_name' ending field, only add if
					// there is no such field
					if (!(mfSubClass.getFields().contains(subClassField + "_name")))
					{
						result.add(subClassField);
					}
				}
				else
				{
					// field has '_name', so use it in any case
					result.add(subClassField);
				}
			}
		}
		return result;
	}

	public static void copyHelper(File content, File dest, boolean skipWhenDestExists) throws IOException
	{
		if (skipWhenDestExists)
		{
			if (!dest.exists())
			{
				FileUtils.copyFile(content, dest);
			}
		}
		else
		{
			if (dest.exists())
			{
				throw new IOException("Destination file " + dest.getAbsolutePath() + " already exists");
			}
			else
			{
				FileUtils.copyFile(content, dest);
			}
		}
	}
}

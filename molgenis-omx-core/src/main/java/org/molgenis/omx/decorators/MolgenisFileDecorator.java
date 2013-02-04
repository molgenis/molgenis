package org.molgenis.omx.decorators;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.molgenis.omx.core.MolgenisFile;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.Mapper;
import org.molgenis.framework.db.MapperDecorator;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;

/**
 * Generic decorator for MOLGENIS files. These files are coupled to entities and
 * stored in a user defined, verified place. The 'FileType' denotes the final
 * placement folder. So for example, the storage location has been set to
 * '/data/xgap', the deploy name is 'tomatodb', and an image is uploaded; it is
 * stored as /data/xgap/tomatodb/myImg'. Note that the extension is stripped off
 * the filename. This is stored as part of the MolgenisFile object, and used to
 * serve out the file with the correct MIME type, obtained at runtime.
 * 
 * @author joerivandervelde
 * 
 * @param <E>
 */
public class MolgenisFileDecorator<E extends MolgenisFile> extends MapperDecorator<E>
{

	protected boolean strict = false;

	// TODO: Danny Parameterize the JDBCMapper object <Object> ??
	public MolgenisFileDecorator(Mapper<E> generatedMapper)
	{
		super(generatedMapper);
	}

	public void genericAddUpdateChecks(List<E> entities) throws DatabaseException
	{

		// default check for every entity in XGAP
		// this means e.g. that file names are not allowed to start with
		// numbers.
		// later on we will escape the entity name to filename and do uniqueness
		// checks. this means file can have 'pretty' names in the database but
		// are simple on the filesystem
		if (strict)
		{
			NameConvention.validateEntityNamesStrict(entities);
		}
		else
		{
			NameConvention.validateEntityNames(entities);
		}

		// Check file extensions according to regular filename rules
		for (MolgenisFile mf : entities)
		{
			try
			{
				// treat extension as filename and do checks
				// the char limit of 50 is pointless because in datamodel we
				// define 8 as max for extensions
				// remember extensions are allowed to start with a number
				// but contain no capital letters
				NameConvention.validateFileName(mf.getExtension());
			}
			catch (DatabaseException e)
			{
				// customize the error
				throw new DatabaseException(
						"While treating file extension as a file name, the following error occured: " + e.getMessage());
			}
		}

		// make sure the would-be file names within the imported list are
		// allowed in respect to eachother
		// we leave out extensions because this is a separate thing, and not
		// part of download protocol (using just name)
		List<String> newEscapedNames = new ArrayList<String>();
		for (MolgenisFile mf : entities)
		{
			String escapedName = NameConvention.escapeFileName(mf.getName());
			if (newEscapedNames.contains(escapedName))
			{
				throw new DatabaseException("File name '" + mf.getName()
						+ "' within list of adds exists twice when escaped to filesafe format. ('" + escapedName + "')");
			}
			newEscapedNames.add(escapedName);
		}
	}

	@Override
	public int add(List<E> entities) throws DatabaseException
	{

		genericAddUpdateChecks(entities);

		// make sure the names within the imported list are allowed in respect
		// to the existing names in the database
		List<MolgenisFile> existingMFList = this.getDatabase().find(MolgenisFile.class);
		List<String> existingEscapedNames = new ArrayList<String>();
		for (MolgenisFile mf : existingMFList)
		{
			existingEscapedNames.add(NameConvention.escapeFileName(mf.getName()));
		}
		for (MolgenisFile mf : entities)
		{
			String newEscapedName = NameConvention.escapeFileName(mf.getName());
			if (existingEscapedNames.contains(newEscapedName))
			{
				throw new DatabaseException("File name '" + mf.getName()
						+ "' already exists in database when escaped to filesafe format. ('" + newEscapedName + "')");
			}
		}

		// here we call the standard 'add'
		int count = super.add(entities);

		// add your post-processing here
		// if you throw and exception the previous add will be rolled back

		return count;
	}

	@Override
	public int update(List<E> entities) throws DatabaseException
	{
		boolean dbAlreadyInTx = this.getDatabase().inTx();

		genericAddUpdateChecks(entities);

		// make sure the names within the imported list are allowed in
		// respect to the existing entities in the database, MINUS the entities
		// to be updated
		List<MolgenisFile> existingMFList = this.getDatabase().find(MolgenisFile.class);
		List<MolgenisFile> existingMFListMinusUpdates = new ArrayList<MolgenisFile>();
		// get existing minus update entities
		for (MolgenisFile existingMF : existingMFList)
		{
			boolean isUpdatedMF = false;
			for (MolgenisFile updatedMF : entities)
			{
				if (updatedMF.getId().intValue() == existingMF.getId().intValue())
				{
					isUpdatedMF = true;
					break;
				}
			}
			if (!isUpdatedMF)
			{
				existingMFListMinusUpdates.add(existingMF);
			}
		}
		// make list of the escaped names
		List<String> existingEscapedNames = new ArrayList<String>();
		for (MolgenisFile existingMF : existingMFListMinusUpdates)
		{
			existingEscapedNames.add(NameConvention.escapeFileName(existingMF.getName()));
		}
		// compare this list vs. each to be updated entity
		for (MolgenisFile mf : entities)
		{
			String newEscapedName = NameConvention.escapeFileName(mf.getName());
			if (existingEscapedNames.contains(newEscapedName))
			{
				throw new DatabaseException("File name '" + mf.getName()
						+ "' already exists or exists when escaped to filesafe format. ('" + newEscapedName + "')");
			}
		}

		// also update the filenames so mapping stays correct
		int count = 0;
		for (MolgenisFile mf : entities)
		{
			MolgenisFile oldMF = this.getDatabase()
					.find(MolgenisFile.class, new QueryRule("id", Operator.EQUALS, mf.getId())).get(0);

			String oldFileName = NameConvention.escapeFileName(oldMF.getName());
			String newFileName = NameConvention.escapeFileName(mf.getName());

			String oldExtension = oldMF.getExtension();
			String newExtension = mf.getExtension();

			if (!oldFileName.equals(newFileName) || !oldExtension.equals(newExtension))
			{
				try
				{
					MolgenisFileHandler mfh = new MolgenisFileHandler(this.getDatabase());
					File oldFile = null;

					try
					{
						oldFile = mfh.getFile(oldMF, this.getDatabase());
					}
					catch (FileNotFoundException fnfe)
					{
						// no file was uploaded for the record yet!
						// skip rest of the function
					}

					if (!dbAlreadyInTx)
					{
						this.getDatabase().beginTx();
					}

					if (oldFile != null)
					{

						int fileNameLength = oldFileName.length() + 1 + oldMF.getExtension().length();
						String oldFileMinusName = oldFile.getAbsolutePath().substring(0,
								(oldFile.getAbsolutePath().length() - fileNameLength));
						String newFilePathName = oldFileMinusName + newFileName + "." + newExtension;
						File newFile = new File(newFilePathName);
						System.gc(); // HACK FOR MS WINDOWS
						boolean success = oldFile.renameTo(newFile);

						if (!success)
						{
							Thread.sleep(100);
							System.gc();

							// try again
							success = oldFile.renameTo(newFile);
							if (!success)
							{
								throw new DatabaseException("SEVERE: Rename of " + oldFile.getAbsolutePath() + " to "
										+ newFile.getAbsolutePath() + " failed. Please check database consistency!");
							}
						}
					}

					// update record for just this file
					super.update(entities.subList(count, count + 1));

					// commit and count
					if (!dbAlreadyInTx)
					{
						this.getDatabase().commitTx();
					}

					count++;

				}
				catch (Exception e)
				{
					if (!dbAlreadyInTx)
					{
						this.getDatabase().rollbackTx();
					}
					throw new DatabaseException(e.getMessage());
				}
			}
		}

		return count;
	}

	@Override
	public int remove(List<E> entities) throws DatabaseException
	{
		boolean dbAlreadyInTx = this.getDatabase().inTx();

		// find backend file, and if exists, delete
		int count = 0;
		for (MolgenisFile mf : entities)
		{

			if (!dbAlreadyInTx)
			{
				this.getDatabase().beginTx();
			}

			try
			{
				try
				{
					// attempt delete, only catch FileNotFound (this is okay)
					MolgenisFileHandler mfh = new MolgenisFileHandler(this.getDatabase());
					mfh.deleteFile(mf, this.getDatabase());
					// System.out.println("Deleted MolgenisFile: " +
					// mf.toString());
				}
				catch (FileNotFoundException fnfe)
				{
					// apparently the file is not there. maybe deleted already,
					// or never there in the first place. just continue to
					// delete database record. all other exceptions are not
					// caught here ofcourse (eg. DatabaseException,
					// InterruptedException, TypeUnknownException,
					// XGAPStorageException, IOException)

					// System.out.println("Trying to delete '" + mf.getName()
					// + "', but file not found. Exception caught: " +
					// fnfe.getMessage());
				}

				// update record for just this file
				super.remove(entities.subList(count, count + 1));

				// commit and count
				if (!dbAlreadyInTx)
				{
					this.getDatabase().commitTx();
				}

				count++;

			}
			catch (Exception e)
			{
				if (!dbAlreadyInTx)
				{
					this.getDatabase().rollbackTx();
				}
				throw new DatabaseException(e.getMessage());
			}
		}

		return count;
	}
}

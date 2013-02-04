package org.molgenis.framework.db;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;

import org.apache.log4j.Logger;
import org.molgenis.io.TupleReader;
import org.molgenis.io.TupleWriter;
import org.molgenis.util.Entity;
import org.molgenis.util.tuple.EntityTuple;
import org.molgenis.util.tuple.Tuple;

public abstract class AbstractMapper<E extends Entity> implements Mapper<E>
{
	/** database */
	private Database database;

	/** batch size */
	public static final int BATCH_SIZE = 500;

	/** log messages */
	private static final Logger logger = Logger.getLogger(AbstractMapper.class);

	public AbstractMapper(Database database)
	{
		this.database = database;
	}

	@Override
	public Database getDatabase()
	{
		return database;
	}

	/**
	 * helper method create a new instance of E
	 */
	@Override
	public abstract E create();

	/**
	 * Method to build a list for Entity E. This allows the finder to pick a
	 * more efficient list implementation than the generic lists.
	 * 
	 * @param size
	 *            of the list
	 * @return list
	 */
	@Override
	public abstract List<E> createList(int size);

	/**
	 * helper method to prepares file for saving.
	 * 
	 * @throws IOException
	 */
	protected abstract void prepareFileAttachements(List<E> entities, File dir) throws IOException;

	/**
	 * helper method to do some actions after the transaction. For example:
	 * write files to disk. FIXME make a listener?
	 * 
	 * @return true if files were saved (will cause additional update to the
	 *         database)
	 * @throws IOException
	 */
	protected abstract boolean saveFileAttachements(List<E> entities, File dir) throws IOException;

	/**
	 * translate into sql
	 * 
	 * @throws DatabaseException
	 */
	@Override
	public abstract int executeAdd(List<? extends E> entities) throws DatabaseException;

	/**
	 * translate into sql
	 * 
	 * @throws DatabaseException
	 */
	@Override
	public abstract int executeUpdate(List<? extends E> entities) throws DatabaseException;

	/**
	 * translate into sql
	 */
	@Override
	public abstract int executeRemove(List<? extends E> entities) throws DatabaseException;

	/**
	 * Foreign key values may be only given via the 'label'. This function
	 * allows resolves the underlying references for a list of entities.
	 * 
	 * @param entities
	 * @throws DatabaseException
	 * @throws ParseException
	 */
	@Override
	public abstract void resolveForeignKeys(List<E> entities) throws DatabaseException, ParseException;

	/**
	 * Helper method for storing multiplicative references. This function should
	 * check wether any mref values have been newly selected or deselected. The
	 * newly selected elements should be added, the deselected elements should
	 * be removed (from the entity that holds the mrefs).
	 * 
	 * @param entities
	 * @throws DatabaseException
	 * @throws IOException
	 * @throws ParseException
	 */
	public abstract void storeMrefs(List<E> entities) throws DatabaseException, IOException, ParseException;

	/**
	 * Helper method for removing multiplicative references ('mrefs')
	 * 
	 * @param entities
	 * @throws SQLException
	 * @throws IOException
	 * @throws DatabaseException
	 * @throws ParseException
	 */
	public abstract void removeMrefs(List<E> entities) throws SQLException, IOException, DatabaseException,
			ParseException;

	@Override
	public void find(TupleWriter writer, QueryRule... rules) throws DatabaseException
	{
		this.find(writer, null, rules);
	}

	public int add(E entity) throws DatabaseException
	{
		List<E> entities = createList(1);
		entities.add(entity);
		return add(entities);
	}

	@Override
	public int add(List<E> entities) throws DatabaseException
	{
		// count rows updated
		int updatedRows = 0;

		// create a transaction unless already in it
		boolean privateTx = !getDatabase().inTx();

		try
		{
			if (privateTx) getDatabase().beginTx();

			// prepare all file attachments
			this.prepareFileAttachements(entities, getDatabase().getFilesource());

			// insert this class in batches
			for (int i = 0; i < entities.size(); i += BATCH_SIZE)
			{
				// attempt to resolve foreign keys by label (ie. 'name')
				this.resolveForeignKeys(entities);

				int endindex = Math.min(i + BATCH_SIZE, entities.size());
				List<E> sublist = entities.subList(i, endindex);
				updatedRows += this.executeAdd(sublist);
			}

			// update any mrefs for this entity
			this.storeMrefs(entities);

			// store file attachments and then update the file paths to them
			if (this.saveFileAttachements(entities, getDatabase().getFilesource()))
			{
				this.update(entities);
			}

			// commit all batches
			if (privateTx) getDatabase().commitTx();

			logger.info(updatedRows + " " + this.create().getClass().getSimpleName() + " objects added");
			return updatedRows;
		}
		catch (Exception sqle)
		{
			sqle.printStackTrace();
			if (privateTx) getDatabase().rollbackTx();
			logger.error("ADD failed on " + this.create().getClass().getSimpleName() + ": " + sqle.getMessage());
			throw new DatabaseException(sqle);
		}
	}

	@Override
	public int add(TupleReader reader, TupleWriter writer) throws DatabaseException
	{
		// count affected rows
		int rowsAffected = 0;

		// start private tx
		boolean privateTx = !getDatabase().inTx();

		try
		{
			if (privateTx) getDatabase().beginTx();

			List<E> entities = toList(reader, BATCH_SIZE);

			if (writer != null) writer.writeColNames(new EntityTuple(entities.get(0)).getColNames());

			while (entities.size() > 0)
			{
				// resolve foreign keys
				this.resolveForeignKeys(entities);

				// add to the database
				rowsAffected += getDatabase().add(entities);
				if (writer != null)
				{
					for (E entity : entities)
						writer.write(new EntityTuple(entity));
				}
				entities = toList(reader, BATCH_SIZE);
			}

			if (privateTx) getDatabase().commitTx();
		}
		catch (Exception e)
		{
			if (privateTx) getDatabase().rollbackTx();
			throw new DatabaseException("add(" + create().getClass().getSimpleName() + ") failed: " + e.getMessage(), e);
		}
		return rowsAffected;
	}

	public int update(E entity) throws DatabaseException
	{
		List<E> entities = createList(1);
		entities.add(entity);
		return update(entities);
	}

	@Override
	public int update(List<E> entities) throws DatabaseException
	{
		// count rows affected
		int updatedRows = 0;

		// privateTx
		boolean privateTx = !getDatabase().inTx();

		try
		{
			// start anonymous transaction for the batched update
			if (privateTx) getDatabase().beginTx();

			// prepare file attachments
			this.prepareFileAttachements(entities, getDatabase().getFilesource());

			// update in batches
			for (int i = 0; i < entities.size(); i += BATCH_SIZE)
			{
				int endindex = Math.min(i + BATCH_SIZE, entities.size());
				List<E> sublist = entities.subList(i, endindex);

				// put the files in their place
				this.saveFileAttachements(sublist, getDatabase().getFilesource());

				// attempt to resolve foreign keys by label (ie. 'name')
				this.resolveForeignKeys(sublist);

				updatedRows += this.executeUpdate(sublist);
			}

			this.storeMrefs(entities);

			if (privateTx) getDatabase().commitTx();

			logger.info(updatedRows + " " + this.create().getClass().getSimpleName() + " objects updated");
			return updatedRows;
		}
		catch (Exception sqle)
		{
			if (privateTx) getDatabase().rollbackTx();

			throw new DatabaseException("Update(" + create().getClass().getSimpleName() + ") failed: "
					+ sqle.getMessage(), sqle);
		}
	}

	@Override
	public int update(TupleReader reader) throws DatabaseException
	{
		// count rows affected
		int rowsAffected = 0;

		// privateTx
		boolean privateTx = !getDatabase().inTx();

		try
		{
			if (privateTx) getDatabase().beginTx();

			List<E> entities = toList(reader, BATCH_SIZE);
			while (entities.size() > 0)
			{
				// resolve foreign keys
				this.resolveForeignKeys(entities);

				// update to the database
				rowsAffected += getDatabase().update(entities);
				entities = toList(reader, BATCH_SIZE);
			}

			if (privateTx) getDatabase().commitTx();
		}
		catch (Exception e)
		{
			if (privateTx) getDatabase().rollbackTx();
			throw new DatabaseException(
					"update(" + create().getClass().getSimpleName() + ") failed: " + e.getMessage(), e);
		}
		return rowsAffected;
	}

	public int remove(E entity) throws DatabaseException
	{
		List<E> entities = createList(1);
		entities.add(entity);
		return remove(entities);
	}

	@Override
	public int remove(List<E> entities) throws DatabaseException
	{
		int updatedRows = 0;
		boolean privateTx = !getDatabase().inTx();
		try
		{
			// start anonymous transaction for the batched remove
			if (privateTx) getDatabase().beginTx();

			// prepare file attachments
			this.prepareFileAttachements(entities, getDatabase().getFilesource());

			// remove in batches
			for (int i = 0; i < entities.size(); i += BATCH_SIZE)
			{
				int endindex = Math.min(i + BATCH_SIZE, entities.size());
				List<E> sublist = entities.subList(i, endindex);

				// attempt to resolve foreign keys by label (ie. 'name')
				this.resolveForeignKeys(sublist);

				// remove mrefs before the entity itself
				this.removeMrefs(sublist);
				updatedRows += this.executeRemove(sublist);
				getDatabase().flush();
			}
			getDatabase().flush();
			if (privateTx) getDatabase().commitTx();

			logger.info(updatedRows + " " + this.create().getClass().getSimpleName() + " objects removed");
			return updatedRows;
		}
		catch (Exception sqle)
		{
			if (privateTx) getDatabase().rollbackTx();

			logger.error("remove failed on " + this.create().getClass().getSimpleName() + ": " + sqle.getMessage());
			sqle.printStackTrace();
			throw new DatabaseException("remove(" + create().getClass().getSimpleName() + ") failed: "
					+ sqle.getMessage(), sqle);
		}
	}

	@Override
	public int remove(TupleReader reader) throws DatabaseException
	{
		int rowsAffected = 0;
		boolean privateTx = !getDatabase().inTx();
		try
		{
			if (privateTx) getDatabase().beginTx();

			List<E> entities = toList(reader, BATCH_SIZE);
			while (entities.size() > 0)
			{
				// resolve foreign keys
				this.resolveForeignKeys(entities);

				// update to the database
				rowsAffected += getDatabase().remove(entities);
				entities = toList(reader, BATCH_SIZE);
			}

			if (privateTx) getDatabase().commitTx();
		}
		catch (Exception e)
		{
			if (privateTx) getDatabase().rollbackTx();
			throw new DatabaseException(
					"remove(" + create().getClass().getSimpleName() + ") failed: " + e.getMessage(), e);
		}
		return rowsAffected;
	}

	@Override
	// FIXME: limit argument is never used?
	public List<E> toList(TupleReader reader, int limit) throws DatabaseException
	{
		// note to self: removed close-check hack, does this have side effects?

		final List<E> entities = createList(10); // TODO why 10?
		try
		{
			for (Tuple row : reader) // TODO should limit not be used somehow?
			{
				E e = create();
				e.set(row, false); // parse the tuple
				entities.add(e);
			}
		}
		catch (Exception ex)
		{
			throw new DatabaseException(ex);
		}
		return entities;
	}
}

package org.molgenis.file;

import org.molgenis.data.AbstractRepositoryDecorator;
import org.molgenis.data.Repository;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.file.model.FileMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * Repository decorator that updates {@link FileStore} on {@link FileMeta} changes.
 */
public class FileMetaRepositoryDecorator extends AbstractRepositoryDecorator<FileMeta>
{
	private static final Logger LOG = LoggerFactory.getLogger(FileMetaRepositoryDecorator.class);

	private final FileStore fileStore;

	public FileMetaRepositoryDecorator(Repository<FileMeta> delegateRepository, FileStore fileStore)
	{
		super(delegateRepository);
		this.fileStore = requireNonNull(fileStore);
	}

	@Override
	public void delete(FileMeta fileMeta)
	{
		deleteFile(fileMeta);
		super.delete(fileMeta);
	}

	@Override
	public void deleteById(Object id)
	{
		deleteFile(getFileMeta(id));
		super.deleteById(id);
	}

	@Override
	public void deleteAll()
	{
		query().findAll().forEach(this::deleteFile);
		super.deleteAll();
	}

	@Override
	public void delete(Stream<FileMeta> fileMetaStream)
	{
		super.delete(fileMetaStream.filter(fileMeta ->
		{
			this.deleteFile(fileMeta);
			return true;
		}));
	}

	@Override
	public void deleteAll(Stream<Object> ids)
	{
		super.deleteAll(ids.filter(id ->
		{
			this.deleteFile(getFileMeta(id));
			return true;
		}));
	}

	private void deleteFile(FileMeta fileMeta)
	{
		boolean deleteOk = fileStore.delete(fileMeta.getId());
		if (!deleteOk)
		{
			LOG.warn("Could not delete file '{}' from file store", fileMeta.getId());
		}
	}

	private FileMeta getFileMeta(Object id)
	{
		FileMeta fileMeta = findOneById(id);
		if (fileMeta == null)
		{
			throw new UnknownEntityException(getEntityType(), id);
		}
		return fileMeta;
	}
}

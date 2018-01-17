package org.molgenis.data.file;

import org.molgenis.data.AbstractSystemRepositoryDecoratorFactory;
import org.molgenis.data.Repository;
import org.molgenis.data.file.model.FileMeta;
import org.molgenis.data.file.model.FileMetaMetaData;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;

@Component
public class FileMetaRepositoryDecoratorFactory
		extends AbstractSystemRepositoryDecoratorFactory<FileMeta, FileMetaMetaData>
{
	private final FileStore fileStore;

	public FileMetaRepositoryDecoratorFactory(FileMetaMetaData fileMetaMetadata, FileStore fileStore)
	{
		super(fileMetaMetadata);
		this.fileStore = requireNonNull(fileStore);
	}

	@Override
	public Repository<FileMeta> createDecoratedRepository(Repository<FileMeta> repository)
	{
		return new FileMetaRepositoryDecorator(repository, fileStore);
	}
}

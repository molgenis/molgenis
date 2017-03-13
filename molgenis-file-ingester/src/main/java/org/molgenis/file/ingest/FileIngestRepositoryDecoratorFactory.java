package org.molgenis.file.ingest;

import org.molgenis.data.DataService;
import org.molgenis.data.Repository;
import org.molgenis.data.StaticEntityRepositoryDecoratorFactory;
import org.molgenis.file.ingest.meta.FileIngest;
import org.molgenis.file.ingest.meta.FileIngestMetaData;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;

@Component
public class FileIngestRepositoryDecoratorFactory
		extends StaticEntityRepositoryDecoratorFactory<FileIngest, FileIngestMetaData>
{
	private final FileIngesterJobScheduler fileIngesterJobScheduler;
	private final DataService dataService;

	public FileIngestRepositoryDecoratorFactory(FileIngestMetaData fileIngestMetaData,
			FileIngesterJobScheduler fileIngesterJobScheduler, DataService dataService)
	{
		super(fileIngestMetaData);
		this.fileIngesterJobScheduler = requireNonNull(fileIngesterJobScheduler);
		this.dataService = requireNonNull(dataService);
	}

	@Override
	public Repository<FileIngest> createDecoratedRepository(Repository<FileIngest> repository)
	{
		return new FileIngestRepositoryDecorator(repository, fileIngesterJobScheduler, dataService);
	}
}

package org.molgenis.data.importer;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.molgenis.data.DataService;
import org.molgenis.data.DatabaseAction;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.meta.WritableMetaDataService;
import org.molgenis.data.mysql.MysqlRepositoryCollection;
import org.molgenis.framework.db.EntitiesValidationReport;
import org.molgenis.framework.db.EntityImportReport;
import org.molgenis.security.permission.PermissionSystemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;

import com.google.common.collect.Lists;

@Component
public class EmxImportService implements ImportService
{
	static final Logger logger = Logger.getLogger(EmxImportService.class);

	private static final List<String> SUPPORTED_FILE_EXTENSIONS = Arrays.asList("xls", "xlsx", "csv", "zip");

	MysqlRepositoryCollection targetCollection;
	TransactionTemplate transactionTemplate;
	final DataService dataService;
	private PermissionSystemService permissionSystemService;
	WritableMetaDataService metaDataService;

	final EmxMetaDataParser parser = new EmxMetaDataParser();

	@Autowired
	public EmxImportService(DataService dataService)
	{
		if (dataService == null) throw new IllegalArgumentException("dataService is null");
		logger.debug("EmxImportService created");
		this.dataService = dataService;
	}

	@Autowired
	public void setRepositoryCollection(MysqlRepositoryCollection targetCollection,
			WritableMetaDataService metaDataService)
	{
		this.targetCollection = targetCollection;
		this.metaDataService = metaDataService;
		logger.debug("EmxImportService created with targetCollection=" + targetCollection + " and metaDataService="
				+ metaDataService);
	}

	@Autowired
	public void setPlatformTransactionManager(PlatformTransactionManager transactionManager)
	{
		this.transactionTemplate = new TransactionTemplate(transactionManager);
	}

	@Autowired
	public void setPermissionSystemService(PermissionSystemService permissionSystemService)
	{
		this.permissionSystemService = permissionSystemService;
	}

	@Override
	public boolean canImport(File file, RepositoryCollection source)
	{
		String fileNameExtension = StringUtils.getFilenameExtension(file.getName());
		if (SUPPORTED_FILE_EXTENSIONS.contains(fileNameExtension.toLowerCase()))
		{
			for (String entityName : source.getEntityNames())
			{
				if (entityName.equalsIgnoreCase(EmxMetaDataParser.ATTRIBUTES)) return true;
				if (targetCollection.getRepositoryByEntityName(entityName) != null) return true;
			}
		}

		return false;
	}

	@Override
	public EntityImportReport doImport(final RepositoryCollection source, DatabaseAction databaseAction)
	{
		if (targetCollection == null) throw new RuntimeException("targetCollection was not set");
		if (metaDataService == null) throw new RuntimeException("metadataService was not set");

		List<EntityMetaData> metadataList = parser.combineMetaDataToList(dataService, source);

		// TODO altered entities (merge, see getEntityMetaData)
		EmxImportWriter writer = new EmxImportWriter(this, databaseAction, source, metadataList,
				permissionSystemService);
		return writer.doImport();

	}

	@Override
	public EntitiesValidationReport validateImport(File file, RepositoryCollection source)
	{
		return parser.validateInput(dataService, source);
	}

	@Override
	public int getOrder()
	{
		return Ordered.HIGHEST_PRECEDENCE;
	}

	@Override
	public List<DatabaseAction> getSupportedDatabaseActions()
	{
		return Lists.newArrayList(DatabaseAction.values());
	}

	@Override
	public boolean getMustChangeEntityName()
	{
		return false;
	}

}

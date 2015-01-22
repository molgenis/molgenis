package org.molgenis.vkgl;

import java.io.File;
import java.util.Collection;
import java.util.Date;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.molgenis.data.CrudRepository;
import org.molgenis.data.Entity;
import org.molgenis.data.mysql.MysqlRepositoryCollection;
import org.molgenis.data.support.MapEntity;
import org.molgenis.security.runas.SystemSecurityToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class VkglImportJob
{
	private static Logger logger = Logger.getLogger(VkglImportJob.class);
	private static final String[] FILE_EXTENSIONS = new String[]
	{ "vcf", "VCF" };
	private final String baseDir;
	private final VkglImportService vkglImportService;
	private final MysqlRepositoryCollection mysqlRepositoryCollection;

	@Autowired
	public VkglImportJob(VkglImportService vkglImportService, MysqlRepositoryCollection mysqlRepositoryCollection,
			@Value("${vkgl.basedir:}") String baseDir)
	{
		this.vkglImportService = vkglImportService;
		this.mysqlRepositoryCollection = mysqlRepositoryCollection;
		this.baseDir = baseDir;
		mysqlRepositoryCollection.add(ImportedFilesEntityMetaData.INSTANCE);
	}

	// Every night at 1 AM
	@Scheduled(cron = "0 0 1 * * ?")
	@PostConstruct
	public void importVcfFiles()
	{
		if (StringUtils.isBlank(baseDir)) return;

		try
		{
			SecurityContextHolder.getContext().setAuthentication(new SystemSecurityToken());

			CrudRepository reportRepo = (CrudRepository) mysqlRepositoryCollection
					.getRepositoryByEntityName(ImportedFilesEntityMetaData.INSTANCE.getName());

			// Find all vcf files in baseDir and subdirectories
			Collection<File> vcfFiles = FileUtils.listFiles(new File(baseDir), FILE_EXTENSIONS, true);

			for (File vcfFile : vcfFiles)
			{
				if (reportRepo.findOne(vcfFile.toString()) == null)
				{
					logger.info("Importing '" + vcfFile + "'");

					Entity report = new MapEntity(ImportedFilesEntityMetaData.FILENAME);
					report.set(ImportedFilesEntityMetaData.FILENAME, vcfFile.toString());
					report.set(ImportedFilesEntityMetaData.IMPORT_DATE, new Date());

					try
					{
						vkglImportService.importVcf(vcfFile);
						logger.info("Import '" + vcfFile + "' done");
					}
					catch (Exception e)
					{
						//e.printStackTrace();
						logger.error("Exception importing '" + vcfFile + "'", e);
						report.set(ImportedFilesEntityMetaData.ERROR_MESSAGE, e.getMessage());
					}

					reportRepo.add(report);
				}

			}
		}
		catch (Exception e)
		{
			logger.error("Exception importing VKGL VCF files", e);
		}
		finally
		{
			SecurityContextHolder.getContext().setAuthentication(null);
		}
	}

}

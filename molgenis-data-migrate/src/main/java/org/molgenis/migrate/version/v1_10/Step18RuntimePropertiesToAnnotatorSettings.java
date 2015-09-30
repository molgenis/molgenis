package org.molgenis.migrate.version.v1_10;

import static org.molgenis.security.core.runas.RunAsSystemProxy.runAsSystem;

import java.util.ArrayList;
import java.util.List;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.support.DefaultEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.framework.MolgenisUpgrade;
import org.molgenis.system.core.RuntimeProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@SuppressWarnings("deprecation")
@Component
public class Step18RuntimePropertiesToAnnotatorSettings extends MolgenisUpgrade
		implements ApplicationListener<ContextRefreshedEvent>
{
	private final DataService dataService;

	/**
	 * Whether or not this migrator is enabled
	 */
	private boolean enabled = false;

	@Autowired
	public Step18RuntimePropertiesToAnnotatorSettings(DataService dataService)
	{
		super(17, 18);
		this.dataService = dataService;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event)
	{
		if (enabled)
		{
			runAsSystem(this::migrateAnnotatorSettings);
		}
	}

	@Override
	public void upgrade()
	{
		enabled = true;
	}

	private void migrateAnnotatorSettings()
	{
		List<RuntimeProperty> runtimeProperties = new ArrayList<>();

		// Cadd
		RuntimeProperty caddLocation = dataService.findOne(RuntimeProperty.ENTITY_NAME,
				QueryImpl.EQ(RuntimeProperty.NAME, "cadd_location"), RuntimeProperty.class);
		if (caddLocation != null)
		{
			runtimeProperties.add(caddLocation);

			Entity entity = new DefaultEntity(dataService.getEntityMetaData("settings_cadd"), dataService);
			entity.set("id", "cadd");
			entity.set("caddLocation", caddLocation.getValue());
			dataService.update("settings_cadd", entity);
		}

		// SnpEff
		RuntimeProperty snpEffLocation = dataService.findOne(RuntimeProperty.ENTITY_NAME,
				QueryImpl.EQ(RuntimeProperty.NAME, "snpeff_jar_location"), RuntimeProperty.class);
		if (snpEffLocation != null)
		{
			runtimeProperties.add(snpEffLocation);

			Entity entity = new DefaultEntity(dataService.getEntityMetaData("settings_snpEff"), dataService);
			entity.set("id", "snpEff");
			entity.set("snpEffJarLocation", snpEffLocation.getValue());
			dataService.update("settings_snpEff", entity);
		}

		// GoNL
		Entity gonlSettingsEntity = new DefaultEntity(dataService.getEntityMetaData("settings_gonl"), dataService);
		gonlSettingsEntity.set("id", "gonl");

		RuntimeProperty gonlChromosomes = dataService.findOne(RuntimeProperty.ENTITY_NAME,
				QueryImpl.EQ(RuntimeProperty.NAME, "gonl_chromosomes"), RuntimeProperty.class);
		if (gonlChromosomes != null)
		{
			runtimeProperties.add(gonlChromosomes);
			gonlSettingsEntity.set("chromosomes", gonlChromosomes.getValue());
		}

		RuntimeProperty gonlFilePattern = dataService.findOne(RuntimeProperty.ENTITY_NAME,
				QueryImpl.EQ(RuntimeProperty.NAME, "gonl_file_pattern"), RuntimeProperty.class);
		if (gonlFilePattern != null)
		{
			runtimeProperties.add(gonlFilePattern);
			gonlSettingsEntity.set("filepattern", gonlFilePattern.getValue());
		}

		RuntimeProperty gonlOverrideChromosome = dataService.findOne(RuntimeProperty.ENTITY_NAME,
				QueryImpl.EQ(RuntimeProperty.NAME, "gonl_override_chromosome_files"), RuntimeProperty.class);
		if (gonlOverrideChromosome != null)
		{
			runtimeProperties.add(gonlOverrideChromosome);
			gonlSettingsEntity.set("overrideChromosomeFiles", gonlOverrideChromosome.getValue());
		}

		RuntimeProperty gonlRootDirectory = dataService.findOne(RuntimeProperty.ENTITY_NAME,
				QueryImpl.EQ(RuntimeProperty.NAME, "gonl_root_directory"), RuntimeProperty.class);
		if (gonlRootDirectory != null)
		{
			runtimeProperties.add(gonlRootDirectory);
			gonlSettingsEntity.set("rootDirectory", gonlRootDirectory.getValue());
		}

		dataService.update("settings_gonl", gonlSettingsEntity);

		// ThousendGenomes
		Entity thousendGenomesSettingsEntity = new DefaultEntity(
				dataService.getEntityMetaData("settings_thousand_genomes"), dataService);
		thousendGenomesSettingsEntity.set("id", "thousand_genomes");

		RuntimeProperty tgChromosomes = dataService.findOne(RuntimeProperty.ENTITY_NAME,
				QueryImpl.EQ(RuntimeProperty.NAME, "thousand_genome_chromosomes"), RuntimeProperty.class);
		if (tgChromosomes != null)
		{
			runtimeProperties.add(tgChromosomes);
			thousendGenomesSettingsEntity.set("chromosomes", tgChromosomes.getValue());
		}

		RuntimeProperty tgFilePattern = dataService.findOne(RuntimeProperty.ENTITY_NAME,
				QueryImpl.EQ(RuntimeProperty.NAME, "thousand_genome_file_pattern"), RuntimeProperty.class);
		if (tgFilePattern != null)
		{
			runtimeProperties.add(tgFilePattern);
			thousendGenomesSettingsEntity.set("filepattern", tgFilePattern.getValue());
		}

		RuntimeProperty tgRootDirectory = dataService.findOne(RuntimeProperty.ENTITY_NAME,
				QueryImpl.EQ(RuntimeProperty.NAME, "thousand_genome_root_directory"), RuntimeProperty.class);
		if (tgRootDirectory != null)
		{
			runtimeProperties.add(tgRootDirectory);
			thousendGenomesSettingsEntity.set("rootDirectory", tgRootDirectory.getValue());
		}

		RuntimeProperty tgOverrideChromFiles = dataService.findOne(RuntimeProperty.ENTITY_NAME,
				QueryImpl.EQ(RuntimeProperty.NAME, "thousand_override_chromosome_files"), RuntimeProperty.class);
		if (tgOverrideChromFiles != null)
		{
			runtimeProperties.add(tgOverrideChromFiles);
			thousendGenomesSettingsEntity.set("overrideChromosomeFile", tgOverrideChromFiles.getValue());
		}

		dataService.update("settings_thousand_genomes", thousendGenomesSettingsEntity);

		// CGD
		RuntimeProperty cgdLocation = dataService.findOne(RuntimeProperty.ENTITY_NAME,
				QueryImpl.EQ(RuntimeProperty.NAME, "cgd_location"), RuntimeProperty.class);
		if (cgdLocation != null)
		{
			runtimeProperties.add(cgdLocation);

			Entity entity = new DefaultEntity(dataService.getEntityMetaData("settings_CGD"), dataService);
			entity.set("id", "CGD");
			entity.set("cgdLocation", cgdLocation.getValue());
			dataService.update("settings_CGD", entity);
		}

		// Clinvar
		RuntimeProperty clinvarLocation = dataService.findOne(RuntimeProperty.ENTITY_NAME,
				QueryImpl.EQ(RuntimeProperty.NAME, "clinvar_location"), RuntimeProperty.class);
		if (clinvarLocation != null)
		{
			runtimeProperties.add(clinvarLocation);

			Entity entity = new DefaultEntity(dataService.getEntityMetaData("settings_clinvar"), dataService);
			entity.set("id", "clinvar");
			entity.set("clinvarLocation", clinvarLocation.getValue());
			dataService.update("settings_clinvar", entity);
		}

		// Dann
		RuntimeProperty dannLocation = dataService.findOne(RuntimeProperty.ENTITY_NAME,
				QueryImpl.EQ(RuntimeProperty.NAME, "dann_location"), RuntimeProperty.class);
		if (dannLocation != null)
		{
			runtimeProperties.add(dannLocation);

			Entity entity = new DefaultEntity(dataService.getEntityMetaData("settings_dann"), dataService);
			entity.set("id", "dann");
			entity.set("dannLocation", dannLocation.getValue());
			dataService.update("settings_dann", entity);
		}

		// Fitcon
		RuntimeProperty fitconLocation = dataService.findOne(RuntimeProperty.ENTITY_NAME,
				QueryImpl.EQ(RuntimeProperty.NAME, "fitcon_location"), RuntimeProperty.class);
		if (fitconLocation != null)
		{
			runtimeProperties.add(fitconLocation);

			Entity entity = new DefaultEntity(dataService.getEntityMetaData("settings_fitcon"), dataService);
			entity.set("id", "fitcon");
			entity.set("fitconLocation", fitconLocation.getValue());
			dataService.update("settings_fitcon", entity);
		}

		// Exac
		RuntimeProperty exacLocation = dataService.findOne(RuntimeProperty.ENTITY_NAME,
				QueryImpl.EQ(RuntimeProperty.NAME, "exac_location"), RuntimeProperty.class);
		if (exacLocation != null)
		{
			runtimeProperties.add(exacLocation);

			Entity entity = new DefaultEntity(dataService.getEntityMetaData("settings_exac"), dataService);
			entity.set("id", "exac");
			entity.set("exacLocation", exacLocation.getValue());
			dataService.update("settings_exac", entity);
		}

		// HPO
		RuntimeProperty hpoLocation = dataService.findOne(RuntimeProperty.ENTITY_NAME,
				QueryImpl.EQ(RuntimeProperty.NAME, "hpo_location"), RuntimeProperty.class);
		if (hpoLocation != null)
		{
			runtimeProperties.add(hpoLocation);

			Entity entity = new DefaultEntity(dataService.getEntityMetaData("settings_hpo"), dataService);
			entity.set("id", "hpo");
			entity.set("hpoLocation", hpoLocation.getValue());
			dataService.update("settings_hpo", entity);
		}

		if (!runtimeProperties.isEmpty())
		{
			dataService.delete(RuntimeProperty.ENTITY_NAME, runtimeProperties);
		}
	}

}

package org.molgenis.migrate.version.v1_9;

import static java.util.Objects.requireNonNull;
import static org.molgenis.system.core.RuntimeProperty.ENTITY_NAME;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.molgenis.data.DataService;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.dataexplorer.settings.DataExplorerSettings;
import org.molgenis.security.core.runas.RunAsSystemProxy;
import org.molgenis.system.core.RuntimeProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@SuppressWarnings("deprecation")
@Component
public class RuntimePropertyToDataExplorerSettingsMigrator implements ApplicationListener<ContextRefreshedEvent>
{
	private static final Logger LOG = LoggerFactory.getLogger(RuntimePropertyToDataExplorerSettingsMigrator.class);

	private final DataService dataService;
	private final DataExplorerSettings dataExplorerSettings;

	/**
	 * Whether or not this migrator is enabled
	 */
	private boolean enabled;

	@Autowired
	public RuntimePropertyToDataExplorerSettingsMigrator(DataService dataService,
			DataExplorerSettings dataExplorerSettings)
	{
		this.dataService = requireNonNull(dataService);
		this.dataExplorerSettings = requireNonNull(dataExplorerSettings);
	}

	private RuntimePropertyToDataExplorerSettingsMigrator migrateSettings()
	{
		if (enabled)
		{
			LOG.info("Migrating RuntimeProperty instances to DataExplorerSettings instance ...");

			{
				String key = "plugin.dataexplorer.hide.searchbox";
				RuntimeProperty property = getProperty(key);
				if (property != null)
				{
					boolean rtpValue = Boolean.parseBoolean(property.getValue());
					boolean value = dataExplorerSettings.getSearchbox();
					if (rtpValue == value)
					{
						LOG.info("Updating DataExplorerSettings for RuntimeProperty [" + key + "]");
						dataExplorerSettings.setSearchbox(!rtpValue); // inverse
					}
					LOG.info("Deleting RuntimeProperty [" + key + "]");
					dataService.delete(ENTITY_NAME, property.getId());
				}
			}

			{
				String key = "plugin.dataexplorer.hide.itemselection";
				RuntimeProperty property = getProperty(key);
				if (property != null)
				{
					boolean rtpValue = Boolean.parseBoolean(property.getValue());
					boolean value = dataExplorerSettings.getItemSelection();
					if (rtpValue == value)
					{
						LOG.info("Updating DataExplorerSettings for RuntimeProperty [" + key + "]");
						dataExplorerSettings.setItemSelection(!value); // inverse
					}
					LOG.info("Deleting RuntimeProperty [" + key + "]");
					dataService.delete(ENTITY_NAME, property.getId());
				}
			}

			{
				String key = "plugin.dataexplorer.wizard.oninit";
				RuntimeProperty property = getProperty(key);
				if (property != null)
				{
					boolean rtpValue = Boolean.parseBoolean(property.getValue());
					boolean value = dataExplorerSettings.getLaunchWizard();
					if (rtpValue != value)
					{
						LOG.info("Updating DataExplorerSettings for RuntimeProperty [" + key + "]");
						dataExplorerSettings.setLaunchWizard(rtpValue);
					}
					LOG.info("Deleting RuntimeProperty [" + key + "]");
					dataService.delete(ENTITY_NAME, property.getId());
				}
			}

			{
				String key = "plugin.dataexplorer.header.abbreviate";
				RuntimeProperty property = getProperty(key);
				if (property != null)
				{
					int rtpValue = Integer.parseInt(property.getValue());
					int value = dataExplorerSettings.getHeaderAbbreviate();
					if (rtpValue != value)
					{
						LOG.info("Updating DataExplorerSettings for RuntimeProperty [" + key + "]");
						dataExplorerSettings.setHeaderAbbreviate(rtpValue);
					}
					LOG.info("Deleting RuntimeProperty [" + key + "]");
					dataService.delete(ENTITY_NAME, property.getId());
				}
			}

			{
				String key = "plugin.dataexplorer.mod.aggregates";
				RuntimeProperty property = getProperty(key);
				if (property != null)
				{
					boolean rtpValue = Boolean.parseBoolean(property.getValue());
					boolean value = dataExplorerSettings.getModAggregates();
					if (rtpValue != value)
					{
						LOG.info("Updating DataExplorerSettings for RuntimeProperty [" + key + "]");
						dataExplorerSettings.setModAggregates(rtpValue);
					}
					LOG.info("Deleting RuntimeProperty [" + key + "]");
					dataService.delete(ENTITY_NAME, property.getId());
				}
			}

			{
				String key = "plugin.dataexplorer.mod.annotators";
				RuntimeProperty property = getProperty(key);
				if (property != null)
				{
					boolean rtpValue = Boolean.parseBoolean(property.getValue());
					boolean value = dataExplorerSettings.getModAnnotators();
					if (rtpValue != value)
					{
						LOG.info("Updating DataExplorerSettings for RuntimeProperty [" + key + "]");
						dataExplorerSettings.setModAnnotators(rtpValue);
					}
					LOG.info("Deleting RuntimeProperty [" + key + "]");
					dataService.delete(ENTITY_NAME, property.getId());
				}
			}

			{
				String key = "plugin.dataexplorer.mod.charts";
				RuntimeProperty property = getProperty(key);
				if (property != null)
				{
					boolean rtpValue = Boolean.parseBoolean(property.getValue());
					boolean value = dataExplorerSettings.getModCharts();
					if (rtpValue != value)
					{
						LOG.info("Updating DataExplorerSettings for RuntimeProperty [" + key + "]");
						dataExplorerSettings.setModCharts(rtpValue);
					}
					LOG.info("Deleting RuntimeProperty [" + key + "]");
					dataService.delete(ENTITY_NAME, property.getId());
				}
			}

			{
				String key = "plugin.dataexplorer.mod.data";
				RuntimeProperty property = getProperty(key);
				if (property != null)
				{
					boolean rtpValue = Boolean.parseBoolean(property.getValue());
					boolean value = dataExplorerSettings.getModData();
					if (rtpValue != value)
					{
						LOG.info("Updating DataExplorerSettings for RuntimeProperty [" + key + "]");
						dataExplorerSettings.setModData(rtpValue);
					}
					LOG.info("Deleting RuntimeProperty [" + key + "]");
					dataService.delete(ENTITY_NAME, property.getId());
				}
			}

			{
				String key = "plugin.dataexplorer.mod.diseasematcher";
				RuntimeProperty property = getProperty(key);
				if (property != null)
				{
					LOG.info("Deleting RuntimeProperty [" + key + "]");
					dataService.delete(ENTITY_NAME, property.getId());
				}
			}

			{
				String key = "plugin.dataexplorer.genomebrowser";
				RuntimeProperty property = getProperty(key);
				if (property != null)
				{
					boolean rtpValue = Boolean.parseBoolean(property.getValue());
					boolean value = dataExplorerSettings.getGenomeBrowser();
					if (rtpValue != value)
					{
						LOG.info("Updating DataExplorerSettings for RuntimeProperty [" + key + "]");
						dataExplorerSettings.setGenomeBrowser(rtpValue);
					}
					LOG.info("Deleting RuntimeProperty [" + key + "]");
					dataService.delete(ENTITY_NAME, property.getId());
				}
			}

			{
				String key = "plugin.dataexplorer.galaxy.enabled";
				RuntimeProperty property = getProperty(key);
				if (property != null)
				{
					boolean rtpValue = Boolean.parseBoolean(property.getValue());
					boolean value = dataExplorerSettings.getGalaxyExport();
					if (rtpValue != value)
					{
						LOG.info("Updating DataExplorerSettings for RuntimeProperty [" + key + "]");
						dataExplorerSettings.setGalaxyExport(rtpValue);
					}
					LOG.info("Deleting RuntimeProperty [" + key + "]");
					dataService.delete(ENTITY_NAME, property.getId());
				}
			}

			{
				String key = "plugin.dataexplorer.galaxy.url";
				RuntimeProperty property = getProperty(key);
				if (property != null)
				{
					URI rtpValue;
					try
					{
						rtpValue = new URI(property.getValue());
						URI value = dataExplorerSettings.getGalaxyUrl();
						if ((rtpValue == null && value != null) || (rtpValue != null && !rtpValue.equals(value)))
						{
							LOG.info("Updating AppSettings for RuntimeProperty [" + key + "]");
							dataExplorerSettings.setGalaxyUrl(rtpValue);
						}
						LOG.info("Deleting RuntimeProperty [" + key + "]");
						dataService.delete(ENTITY_NAME, property.getId());
					}
					catch (URISyntaxException e)
					{
						LOG.error("Failed to update AppSettings for RuntimeProperty [" + key + "]");
					}
				}
			}

			{
				String key = "genomebrowser.init.initLocation";
				RuntimeProperty property = getProperty(key);
				if (property != null)
				{
					String rtpValue = property.getValue();
					rtpValue = "null".equals(rtpValue) ? null : rtpValue;
					String value = dataExplorerSettings.getGenomeBrowserLocation();
					if ((rtpValue == null && value != null) || (rtpValue != null && !rtpValue.equals(value)))
					{
						LOG.info("Updating AppSettings for RuntimeProperty [" + key + "]");
						dataExplorerSettings.setGenomeBrowserLocation(rtpValue);
					}
					LOG.info("Deleting RuntimeProperty [" + key + "]");
					dataService.delete(ENTITY_NAME, property.getId());
				}
			}

			{
				String key = "genomebrowser.init.coordSystem";
				RuntimeProperty property = getProperty(key);
				if (property != null)
				{
					String rtpValue = property.getValue();
					rtpValue = "null".equals(rtpValue) ? null : rtpValue;
					String value = dataExplorerSettings.getGenomeBrowserCoordSystem();
					if ((rtpValue == null && value != null) || (rtpValue != null && !rtpValue.equals(value)))
					{
						LOG.info("Updating AppSettings for RuntimeProperty [" + key + "]");
						dataExplorerSettings.setGenomeBrowserCoordSystem(rtpValue);
					}
					LOG.info("Deleting RuntimeProperty [" + key + "]");
					dataService.delete(ENTITY_NAME, property.getId());
				}
			}

			{
				String key = "genomebrowser.init.chains";
				RuntimeProperty property = getProperty(key);
				if (property != null)
				{
					LOG.info("Deleting RuntimeProperty [" + key + "]");
					dataService.delete(ENTITY_NAME, property.getId());
				}
			}

			{
				String key = "genomebrowser.init.sources";
				RuntimeProperty property = getProperty(key);
				if (property != null)
				{
					String rtpValue = property.getValue();
					rtpValue = "null".equals(rtpValue) ? null : rtpValue;
					String value = dataExplorerSettings.getGenomeBrowserSources();
					if ((rtpValue == null && value != null) || (rtpValue != null && !rtpValue.equals(value)))
					{
						LOG.info("Updating AppSettings for RuntimeProperty [" + key + "]");
						dataExplorerSettings.setGenomeBrowserSources(rtpValue);
					}
					LOG.info("Deleting RuntimeProperty [" + key + "]");
					dataService.delete(ENTITY_NAME, property.getId());
				}
			}

			{
				String key = "genomebrowser.init.browserLinks";
				RuntimeProperty property = getProperty(key);
				if (property != null)
				{
					String rtpValue = property.getValue();
					rtpValue = "null".equals(rtpValue) ? null : rtpValue;
					String value = dataExplorerSettings.getGenomeBrowserLinks();
					if ((rtpValue == null && value != null) || (rtpValue != null && !rtpValue.equals(value)))
					{
						LOG.info("Updating AppSettings for RuntimeProperty [" + key + "]");
						dataExplorerSettings.setGenomeBrowserLinks(rtpValue);
					}
					LOG.info("Deleting RuntimeProperty [" + key + "]");
					dataService.delete(ENTITY_NAME, property.getId());
				}
			}

			{
				String key = "genomebrowser.init.highlightRegion";
				RuntimeProperty property = getProperty(key);
				if (property != null)
				{
					boolean rtpValue = Boolean.parseBoolean(property.getValue());
					boolean value = dataExplorerSettings.getGenomeBrowserHighlightRegion();
					if (rtpValue != value)
					{
						LOG.info("Updating DataExplorerSettings for RuntimeProperty [" + key + "]");
						dataExplorerSettings.setGenomeBrowserHighlightRegion(rtpValue);
					}
					LOG.info("Deleting RuntimeProperty [" + key + "]");
					dataService.delete(ENTITY_NAME, property.getId());
				}
			}

			{
				String key = "plugin.dataexplorer.mod.aggregates.distinct.hide";
				RuntimeProperty property = getProperty(key);
				if (property != null)
				{
					boolean rtpValue = Boolean.parseBoolean(property.getValue());
					boolean value = dataExplorerSettings.getAggregatesDistinctSelect();
					if (rtpValue == value)
					{
						LOG.info("Updating DataExplorerSettings for RuntimeProperty [" + key + "]");
						dataExplorerSettings.setAggregatesDistinctSelect(!rtpValue); // inverse
					}
					LOG.info("Deleting RuntimeProperty [" + key + "]");
					dataService.delete(ENTITY_NAME, property.getId());
				}
			}

			{
				String prefix = "plugin.dataexplorer.mod.aggregates.distinct.override";
				Map<String, String> rtpValue = new LinkedHashMap<String, String>();
				for (RuntimeProperty runtimeProperty : dataService.findAll(ENTITY_NAME, RuntimeProperty.class))
				{
					String name = runtimeProperty.getName();
					if (name.startsWith(prefix))
					{
						String entityName = name.substring(prefix.length() + 1);
						rtpValue.put(entityName, runtimeProperty.getValue());
					}
				}
				Map<String, String> value = dataExplorerSettings.getAggregatesDistinctOverrides();
				if ((rtpValue == null && value != null) || (rtpValue != null && !rtpValue.equals(value)))
				{
					dataExplorerSettings.setAggregatesDistinctOverrides(rtpValue);
				}
			}

			{
				String key = "plugin.dataexplorer.mod.aggregates.noresults";
				RuntimeProperty property = getProperty(key);
				if (property != null)
				{
					LOG.info("Deleting RuntimeProperty [" + key + "]");
					dataService.delete(ENTITY_NAME, property.getId());
				}
			}

			{
				String key = "plugin.dataexplorer.editable";
				RuntimeProperty property = getProperty(key);
				if (property != null)
				{
					LOG.info("Deleting RuntimeProperty [" + key + "]");
					dataService.delete(ENTITY_NAME, property.getId());
				}
			}

			{
				String key = "plugin.dataexplorer.rowClickable";
				RuntimeProperty property = getProperty(key);
				if (property != null)
				{
					LOG.info("Deleting RuntimeProperty [" + key + "]");
					dataService.delete(ENTITY_NAME, property.getId());
				}
			}

			{
				String key = "plugin.dataexplorer.mod.entitiesreport";
				RuntimeProperty property = getProperty(key);
				if (property != null)
				{
					String rtpValue = property.getValue();
					rtpValue = "null".equals(rtpValue) ? null : rtpValue;
					String value = dataExplorerSettings.getEntityReports();
					if (rtpValue != value)
					{
						LOG.info("Updating DataExplorerSettings for RuntimeProperty [" + key + "]");
						dataExplorerSettings.setEntityReports(rtpValue);
					}
					LOG.info("Deleting RuntimeProperty [" + key + "]");
					dataService.delete(ENTITY_NAME, property.getId());
				}
			}

			LOG.info("Migrated RuntimeProperty instances to DataExplorerSettings instances");
		}
		return this;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent)
	{
		RunAsSystemProxy.runAsSystem(() -> migrateSettings());
	}

	private RuntimeProperty getProperty(String key)
	{
		return dataService.findOne(ENTITY_NAME, new QueryImpl().eq(RuntimeProperty.NAME, key), RuntimeProperty.class);
	}

	public void enableMigrator()
	{
		this.enabled = true;
	}
}

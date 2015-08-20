package org.molgenis.migrate.version.v1_9;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.molgenis.system.core.RuntimeProperty.ENTITY_NAME;

import org.molgenis.data.DataService;
import org.molgenis.data.support.GenomicDataSettings;
import org.molgenis.data.support.QueryImpl;
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
public class RuntimePropertyToGenomicDataSettingsMigrator implements ApplicationListener<ContextRefreshedEvent>
{
	private static final Logger LOG = LoggerFactory.getLogger(RuntimePropertyToGenomicDataSettingsMigrator.class);

	private final DataService dataService;
	private final GenomicDataSettings genomicDataSettings;

	/**
	 * Whether or not this migrator is enabled
	 */
	private boolean enabled;

	@Autowired
	public RuntimePropertyToGenomicDataSettingsMigrator(DataService dataService,
			GenomicDataSettings GenomicDataSettings)
	{
		this.dataService = checkNotNull(dataService);
		this.genomicDataSettings = checkNotNull(GenomicDataSettings);
	}

	private RuntimePropertyToGenomicDataSettingsMigrator migrateSettings()
	{

		if (enabled)
		{
			LOG.info("Migrating RuntimeProperty instances to GenomicDataSettings instance ...");

			{
				String key = "genomebrowser.data.alt";
				RuntimeProperty property = getProperty(key);
				if (property != null)
				{
					String rtpValue = property.getValue();
					rtpValue = "null".equals(rtpValue) ? null : rtpValue;
					String value = genomicDataSettings.getAttrsAlt();
					if ((rtpValue == null && value != null) || (rtpValue != null && !rtpValue.equals(value)))
					{
						LOG.info("Updating GenomicDataSettings for RuntimeProperty [" + key + "]");
						genomicDataSettings.setAttrsAlt(rtpValue);
					}
					LOG.info("Deleting RuntimeProperty [" + key + "]");
					dataService.delete(ENTITY_NAME, property.getId());
				}
			}

			{
				String key = "genomebrowser.data.chromosome";
				RuntimeProperty property = getProperty(key);
				if (property != null)
				{
					String rtpValue = property.getValue();
					rtpValue = "null".equals(rtpValue) ? null : rtpValue;
					String value = genomicDataSettings.getAttrsChrom();
					if ((rtpValue == null && value != null) || (rtpValue != null && !rtpValue.equals(value)))
					{
						LOG.info("Updating GenomicDataSettings for RuntimeProperty [" + key + "]");
						genomicDataSettings.setAttrsChrom(rtpValue);
					}
					LOG.info("Deleting RuntimeProperty [" + key + "]");
					dataService.delete(ENTITY_NAME, property.getId());
				}
			}

			{
				String key = "genomebrowser.data.desc";
				RuntimeProperty property = getProperty(key);
				if (property != null)
				{
					String rtpValue = property.getValue();
					rtpValue = "null".equals(rtpValue) ? null : rtpValue;
					String value = genomicDataSettings.getAttrsDescription();
					if ((rtpValue == null && value != null) || (rtpValue != null && !rtpValue.equals(value)))
					{
						LOG.info("Updating GenomicDataSettings for RuntimeProperty [" + key + "]");
						genomicDataSettings.setAttrsDescription(rtpValue);
					}
					LOG.info("Deleting RuntimeProperty [" + key + "]");
					dataService.delete(ENTITY_NAME, property.getId());
				}
			}

			{
				String key = "genomebrowser.data.id";
				RuntimeProperty property = getProperty(key);
				if (property != null)
				{
					String rtpValue = property.getValue();
					rtpValue = "null".equals(rtpValue) ? null : rtpValue;
					String value = genomicDataSettings.getAttrsIdentifier();
					if ((rtpValue == null && value != null) || (rtpValue != null && !rtpValue.equals(value)))
					{
						LOG.info("Updating GenomicDataSettings for RuntimeProperty [" + key + "]");
						genomicDataSettings.setAttrsIdentifier(rtpValue);
					}
					LOG.info("Deleting RuntimeProperty [" + key + "]");
					dataService.delete(ENTITY_NAME, property.getId());
				}
			}

			{
				String key = "genomebrowser.data.name";
				RuntimeProperty property = getProperty(key);
				if (property != null)
				{
					String rtpValue = property.getValue();
					rtpValue = "null".equals(rtpValue) ? null : rtpValue;
					String value = genomicDataSettings.getAttrsName();
					if ((rtpValue == null && value != null) || (rtpValue != null && !rtpValue.equals(value)))
					{
						LOG.info("Updating GenomicDataSettings for RuntimeProperty [" + key + "]");
						genomicDataSettings.setAttrsName(rtpValue);
					}
					LOG.info("Deleting RuntimeProperty [" + key + "]");
					dataService.delete(ENTITY_NAME, property.getId());
				}
			}

			{
				String key = "genomebrowser.data.linkout";
				RuntimeProperty property = getProperty(key);
				if (property != null)
				{
					String rtpValue = property.getValue();
					rtpValue = "null".equals(rtpValue) ? null : rtpValue;
					String value = genomicDataSettings.getAttrsLinkout();
					if ((rtpValue == null && value != null) || (rtpValue != null && !rtpValue.equals(value)))
					{
						LOG.info("Updating GenomicDataSettings for RuntimeProperty [" + key + "]");
						genomicDataSettings.setAttrsLinkout(rtpValue);
					}
					LOG.info("Deleting RuntimeProperty [" + key + "]");
					dataService.delete(ENTITY_NAME, property.getId());
				}
			}

			{
				String key = "genomebrowser.data.patient";
				RuntimeProperty property = getProperty(key);
				if (property != null)
				{
					String rtpValue = property.getValue();
					rtpValue = "null".equals(rtpValue) ? null : rtpValue;
					String value = genomicDataSettings.getAttrsPatientId();
					if ((rtpValue == null && value != null) || (rtpValue != null && !rtpValue.equals(value)))
					{
						LOG.info("Updating GenomicDataSettings for RuntimeProperty [" + key + "]");
						genomicDataSettings.setAttrsPatientId(rtpValue);
					}
					LOG.info("Deleting RuntimeProperty [" + key + "]");
					dataService.delete(ENTITY_NAME, property.getId());
				}
			}

			{
				String key = "genomebrowser.data.ref";
				RuntimeProperty property = getProperty(key);
				if (property != null)
				{
					String rtpValue = property.getValue();
					rtpValue = "null".equals(rtpValue) ? null : rtpValue;
					String value = genomicDataSettings.getAttrsRef();
					if ((rtpValue == null && value != null) || (rtpValue != null && !rtpValue.equals(value)))
					{
						LOG.info("Updating GenomicDataSettings for RuntimeProperty [" + key + "]");
						genomicDataSettings.setAttrsRef(rtpValue);
					}
					LOG.info("Deleting RuntimeProperty [" + key + "]");
					dataService.delete(ENTITY_NAME, property.getId());
				}
			}

			{
				String key = "genomebrowser.data.start";
				RuntimeProperty property = getProperty(key);
				if (property != null)
				{
					String rtpValue = property.getValue();
					rtpValue = "null".equals(rtpValue) ? null : rtpValue;
					String value = genomicDataSettings.getAttrsPos();
					if ((rtpValue == null && value != null) || (rtpValue != null && !rtpValue.equals(value)))
					{
						LOG.info("Updating GenomicDataSettings for RuntimeProperty [" + key + "]");
						genomicDataSettings.setAttrsPos(rtpValue);
					}
					LOG.info("Deleting RuntimeProperty [" + key + "]");
					dataService.delete(ENTITY_NAME, property.getId());
				}
			}

			{
				String key = "genomebrowser.data.stop";
				RuntimeProperty property = getProperty(key);
				if (property != null)
				{
					String rtpValue = property.getValue();
					rtpValue = "null".equals(rtpValue) ? null : rtpValue;
					String value = genomicDataSettings.getAttrsStop();
					if ((rtpValue == null && value != null) || (rtpValue != null && !rtpValue.equals(value)))
					{
						LOG.info("Updating GenomicDataSettings for RuntimeProperty [" + key + "]");
						genomicDataSettings.setAttrsStop(rtpValue);
					}
					LOG.info("Deleting RuntimeProperty [" + key + "]");
					dataService.delete(ENTITY_NAME, property.getId());
				}
			}

			LOG.info("Migrated RuntimeProperty instances to GenomicDataSettings instances");
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

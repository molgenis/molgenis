package org.molgenis.migrate.version.v1_9;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.molgenis.system.core.RuntimeProperty.ENTITY_NAME;

import org.molgenis.data.DataService;
import org.molgenis.data.settings.AppSettings;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.account.ActivationMode;
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
public class RuntimePropertyToAppSettingsMigrator implements ApplicationListener<ContextRefreshedEvent>
{
	private static final Logger LOG = LoggerFactory.getLogger(RuntimePropertyToAppSettingsMigrator.class);

	private final DataService dataService;
	private final AppSettings appSettings;

	/**
	 * Whether or not this migrator is enabled
	 */
	private boolean enabled;

	@Autowired
	public RuntimePropertyToAppSettingsMigrator(DataService dataService, AppSettings appSettings)
	{
		this.dataService = checkNotNull(dataService);
		this.appSettings = checkNotNull(appSettings);
	}

	private RuntimePropertyToAppSettingsMigrator migrateSettings()
	{
		if (enabled)
		{
			LOG.info("Migrating RuntimeProperty instances to AppSettings instance ...");

			{
				String key = "app.name";
				RuntimeProperty property = getProperty(key);
				if (property != null)
				{
					String rtpValue = property.getValue();
					rtpValue = "null".equals(rtpValue) ? null : rtpValue;
					rtpValue = "null".equals(rtpValue) ? null : rtpValue;
					String value = appSettings.getTitle();
					if ((rtpValue == null && value != null) || (rtpValue != null && !rtpValue.equals(value)))
					{
						LOG.info("Updating AppSettings for RuntimeProperty [" + key + "]");
						appSettings.setTitle(rtpValue);
					}
					LOG.info("Deleting RuntimeProperty [" + key + "]");
					dataService.delete(ENTITY_NAME, property.getId());
				}
			}

			{
				String key = "app.top.logo";
				RuntimeProperty property = getProperty(key);
				if (property != null)
				{
					String rtpValue = property.getValue();
					rtpValue = "null".equals(rtpValue) ? null : rtpValue;
					rtpValue = "null".equals(rtpValue) ? null : rtpValue;
					String value = appSettings.getLogoTopHref();
					if ((rtpValue == null && value != null) || (rtpValue != null && !rtpValue.equals(value)))
					{
						LOG.info("Updating AppSettings for RuntimeProperty [" + key + "]");
						appSettings.setLogoTopHref(rtpValue);
					}
					LOG.info("Deleting RuntimeProperty [" + key + "]");
					dataService.delete(ENTITY_NAME, property.getId());
				}
			}

			{
				String key = "app.href.logo";
				RuntimeProperty property = getProperty(key);
				if (property != null)
				{
					String rtpValue = property.getValue();
					rtpValue = "null".equals(rtpValue) ? null : rtpValue;
					String value = appSettings.getLogoNavBarHref();
					if ((rtpValue == null && value != null) || (rtpValue != null && !rtpValue.equals(value)))
					{
						LOG.info("Updating AppSettings for RuntimeProperty [" + key + "]");
						appSettings.setLogoNavBarHref(rtpValue);
					}
					LOG.info("Deleting RuntimeProperty [" + key + "]");
					dataService.delete(ENTITY_NAME, property.getId());
				}
			}

			{
				String key = "app.trackingcode.header";
				RuntimeProperty property = getProperty(key);
				if (property != null)
				{
					LOG.warn("RuntimeProperty app.trackingcode.header cannot be migrated to AppSettings automatically, please set the Google Analytics tracking id manually in Application settings and remove the RuntimeProperty.");
				}
			}

			{
				String key = "molgenis.footer";
				RuntimeProperty property = getProperty(key);
				if (property != null)
				{
					String rtpValue = property.getValue();
					rtpValue = "null".equals(rtpValue) ? null : rtpValue;
					String value = appSettings.getFooter();
					if ((rtpValue == null && value != null) || (rtpValue != null && !rtpValue.equals(value)))
					{
						LOG.info("Updating AppSettings for RuntimeProperty [" + key + "]");
						appSettings.setFooter(rtpValue);
					}
					LOG.info("Deleting RuntimeProperty [" + key + "]");
					dataService.delete(ENTITY_NAME, property.getId());
				}
			}

			{
				String key = "plugin.auth.enable_self_registration";
				RuntimeProperty property = getProperty(key);
				if (property != null)
				{
					boolean rtpValue = Boolean.parseBoolean(property.getValue());
					boolean value = appSettings.getSignUp();
					if (rtpValue != value)
					{
						LOG.info("Updating AppSettings for RuntimeProperty [" + key + "]");
						appSettings.setSignUp(rtpValue);
					}
					LOG.info("Deleting RuntimeProperty [" + key + "]");
					dataService.delete(ENTITY_NAME, property.getId());
				}
			}

			{
				String key = "plugin.auth.activation_mode";
				RuntimeProperty property = getProperty(key);
				if (property != null)
				{
					ActivationMode rtpValue = ActivationMode.from(property.getValue(), ActivationMode.ADMIN);
					boolean value = appSettings.getSignUpModeration();
					if ((value == true && rtpValue != ActivationMode.ADMIN)
							|| (value == false && rtpValue != ActivationMode.USER))
					{
						LOG.info("Updating AppSettings for RuntimeProperty [" + key + "]");
						appSettings.setSignUp(rtpValue == ActivationMode.ADMIN ? true : false);
					}
					LOG.info("Deleting RuntimeProperty [" + key + "]");
					dataService.delete(ENTITY_NAME, property.getId());
				}
			}

			{
				String key = "i18nLocale";
				RuntimeProperty property = getProperty(key);
				if (property != null)
				{
					String rtpValue = property.getValue();
					rtpValue = "null".equals(rtpValue) ? null : rtpValue;
					String value = appSettings.getLanguageCode();
					if ((rtpValue == null && value != null) || (rtpValue != null && !rtpValue.equals(value)))
					{
						LOG.info("Updating AppSettings for RuntimeProperty [" + key + "]");
						appSettings.setLanguageCode(rtpValue);
					}
					LOG.info("Deleting RuntimeProperty [" + key + "]");
					dataService.delete(ENTITY_NAME, property.getId());
				}
			}

			{
				String key = "molgenis.css.theme";
				RuntimeProperty property = getProperty(key);
				if (property != null)
				{
					String rtpValue = property.getValue();
					rtpValue = "null".equals(rtpValue) ? null : rtpValue;
					String value = appSettings.getBootstrapTheme();
					if ((rtpValue == null && value != null) || (rtpValue != null && !rtpValue.equals(value)))
					{
						LOG.info("Updating AppSettings for RuntimeProperty [" + key + "]");
						appSettings.setBootstrapTheme(rtpValue);
					}
					LOG.info("Deleting RuntimeProperty [" + key + "]");
					dataService.delete(ENTITY_NAME, property.getId());
				}
			}

			{
				String key = "app.href.css";
				RuntimeProperty property = getProperty(key);
				if (property != null)
				{
					String rtpValue = property.getValue();
					rtpValue = "null".equals(rtpValue) ? null : rtpValue;
					String value = appSettings.getCssHref();
					if ((rtpValue == null && value != null) || (rtpValue != null && !rtpValue.equals(value)))
					{
						LOG.info("Updating AppSettings for RuntimeProperty [" + key + "]");
						appSettings.setCssHref(rtpValue);
					}
					LOG.info("Deleting RuntimeProperty [" + key + "]");
					dataService.delete(ENTITY_NAME, property.getId());
				}
			}

			{
				String key = "molgenis.menu";
				RuntimeProperty property = getProperty(key);
				if (property != null)
				{
					String rtpValue = property.getValue();
					rtpValue = "null".equals(rtpValue) ? null : rtpValue;
					String value = appSettings.getMenu();
					if ((rtpValue == null && value != null) || (rtpValue != null && !rtpValue.equals(value)))
					{
						LOG.info("Updating AppSettings for RuntimeProperty [" + key + "]");
						if (rtpValue != null)
						{
							LOG.info("Adding Settings plugin to Admin menu");
							value = rtpValue
									.replace(
											"{\"type\":\"menu\",\"id\":\"admin\",\"label\":\"Admin\",\"items\":[",
											"{\"type\":\"menu\",\"id\":\"admin\",\"label\":\"Admin\",\"items\":[{\"type\":\"plugin\",\"id\":\"settings\",\"label\":\"Settings\"},");
						}
						appSettings.setMenu(value);
					}
					LOG.info("Deleting RuntimeProperty [" + key + "]");
					dataService.delete(ENTITY_NAME, property.getId());
				}
			}

			{
				String key = "app.trackingcode.header";
				RuntimeProperty property = getProperty(key);
				if (property != null)
				{
					LOG.warn("RuntimeProperty app.trackingcode.header cannot be migrated to AppSettings automatically, please set the Google Analytics tracking id manually in Application settings and remove the RuntimeProperty.");
				}
			}

			{
				String key = "app.trackingcode.footer";
				RuntimeProperty property = getProperty(key);
				if (property != null)
				{
					String rtpValue = property.getValue();
					rtpValue = "null".equals(rtpValue) ? null : rtpValue;
					String value = appSettings.getTrackingCodeFooter();
					if ((rtpValue == null && value != null) || (rtpValue != null && !rtpValue.equals(value)))
					{
						LOG.info("Updating AppSettings for RuntimeProperty [" + key + "]");
						appSettings.setTrackingCodeFooter(rtpValue);
					}
					LOG.info("Deleting RuntimeProperty [" + key + "]");
					dataService.delete(ENTITY_NAME, property.getId());
				}
			}

			{
				String key = "aggregate.anonymization.threshold";
				RuntimeProperty property = getProperty(key);
				if (property != null)
				{
					Integer rtpValue = Integer.valueOf(property.getValue());
					Integer value = appSettings.getAggregateThreshold();
					if ((rtpValue == null && value != null) || (rtpValue != null && !rtpValue.equals(value)))
					{
						LOG.info("Updating AppSettings for RuntimeProperty [" + key + "]");
						appSettings.setAggregateThreshold(rtpValue);
					}
					LOG.info("Deleting RuntimeProperty [" + key + "]");
					dataService.delete(ENTITY_NAME, property.getId());
				}
			}

			LOG.info("Migrated RuntimeProperty instances to AppSettings instances");

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

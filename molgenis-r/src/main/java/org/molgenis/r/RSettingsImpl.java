package org.molgenis.r;

import org.molgenis.data.settings.DefaultSettingsEntity;
import org.molgenis.data.settings.DefaultSettingsEntityType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static org.molgenis.data.meta.AttributeType.INT;
import static org.molgenis.r.RSettingsImpl.Meta.HOST;
import static org.molgenis.r.RSettingsImpl.Meta.PORT;

@Component
public class RSettingsImpl extends DefaultSettingsEntity implements RSettings
{
	private static final long serialVersionUID = 1L;

	private static final String ID = "RSettings";

	public RSettingsImpl()
	{
		super(ID);
	}

	@Component
	public static class Meta extends DefaultSettingsEntityType
	{
		@Value("${r.host:localhost}")
		private String defaultHost;
		@Value("${r.port:6311}")
		private String defaultPort;

		static final String HOST = "host";
		static final String PORT = "port";

		public Meta()
		{
			super(ID);
		}

		@Override
		public void init()
		{
			super.init();
			setLabel("R settings");
			setDescription("Configuration properties for R.");
			addAttribute(HOST).setDefaultValue(defaultHost).setNillable(false).setDescription("Rserve host.");
			addAttribute(PORT).setDataType(INT).setDefaultValue(defaultPort).setNillable(false)
					.setDescription("Rserve port.");
		}
	}

	@Override
	public String getHost()
	{
		return getString(HOST);
	}

	@Override
	public int getPort()
	{
		return getInt(PORT);
	}
}

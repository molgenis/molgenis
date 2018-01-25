package org.molgenis.r;

import org.molgenis.settings.DefaultSettingsEntity;
import org.molgenis.settings.DefaultSettingsEntityType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static org.molgenis.data.meta.AttributeType.INT;
import static org.molgenis.data.meta.AttributeType.STRING;
import static org.molgenis.r.OpenCpuSettingsImpl.Meta.*;

@Component
public class OpenCpuSettingsImpl extends DefaultSettingsEntity implements OpenCpuSettings
{
	private static final long serialVersionUID = 1L;

	private static final String ID = "OpenCpuSettings";

	public OpenCpuSettingsImpl()
	{
		super(ID);
	}

	@Component
	public static class Meta extends DefaultSettingsEntityType
	{
		@Value("${opencpu.uri.scheme:http}")
		private String defaultScheme;
		@Value("${opencpu.uri.host:localhost}")
		private String defaultHost;
		@Value("${opencpu.uri.port:8004}")
		private String defaultPort;
		@Value("${opencpu.uri.path:/ocpu/}")
		private String defaultRootPath;

		static final String SCHEME = "scheme";
		static final String HOST = "host";
		static final String PORT = "port";
		static final String ROOT_PATH = "rootPath";

		public Meta()
		{
			super(ID);
		}

		@Override
		public void init()
		{
			super.init();
			setLabel("OpenCPU settings");
			setDescription(
					"OpenCPU, a framework for embedded scientific computing and reproducible research, settings.");
			addAttribute(SCHEME).setDefaultValue(defaultScheme)
								.setNillable(false)
								.setLabel("URI scheme")
								.setDescription("Open CPU URI scheme (e.g. http).");
			addAttribute(HOST).setDefaultValue(defaultHost)
							  .setNillable(false)
							  .setLabel("URI host")
							  .setDescription("Open CPU URI host (e.g. localhost).");
			addAttribute(PORT).setDataType(INT)
							  .setDefaultValue(defaultPort)
							  .setNillable(false)
							  .setLabel("URI port")
							  .setDescription("Open CPU URI port (e.g. 8004).");
			addAttribute(ROOT_PATH).setDataType(STRING)
								   .setDefaultValue(defaultRootPath)
								   .setNillable(false)
								   .setLabel("URI path")
								   .setDescription("Open CPU URI root path (e.g. /ocpu/).");
		}
	}

	@Override
	public String getScheme()
	{
		return getString(SCHEME);
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

	@Override
	public String getRootPath()
	{
		return getString(ROOT_PATH);
	}
}

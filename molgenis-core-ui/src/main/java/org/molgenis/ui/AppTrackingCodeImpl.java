package org.molgenis.ui;

import org.molgenis.framework.server.MolgenisSettings;

public class AppTrackingCodeImpl implements AppTrackingCode
{
	private String header = null;
	private String footer = null;
	private String defaultAnalytics = null;
	private final static String START_TRACKINGCODE = "(function(){if('true' === $.cookie('permissionforcookies')){";
	private final static String END_TRACKINGCODE = "}})();";

	public AppTrackingCodeImpl()
	{
	}

	public AppTrackingCodeImpl(MolgenisSettings molgenisSettings)
	{
		this(molgenisSettings.getProperty(AppTrackingCode.KEY_APP_TRACKING_CODE_FOOTER), molgenisSettings
				.getProperty(AppTrackingCode.KEY_APP_TRACKING_CODE_HEADER), molgenisSettings
				.getProperty(AppTrackingCode.KEY_APP_TRACKING_CODE_DEFAULT));
	}

	public AppTrackingCodeImpl(String footer, String header, String defaultAnalytics)
	{
		if (footer != null)
		{
			this.setFooter(START_TRACKINGCODE + footer + END_TRACKINGCODE);
		}
		if (header != null)
		{
			this.setHeader(START_TRACKINGCODE + header + END_TRACKINGCODE);
		}
		if (defaultAnalytics != null)
		{
			// no cookie-permissions code is needed, the default should be privacy friendly
			this.setDefault(defaultAnalytics);
		}
	}

	/**
	 * Return the header Tracking Code from data base
	 * 
	 * @return
	 */
	@Override
	public String getHeader()
	{
		return this.header;
	}

	@Override
	public String getFooter()
	{
		return this.footer;
	}

	@Override
	public String getDefault()
	{
		return this.defaultAnalytics;
	}

	private void setHeader(String header)
	{
		this.header = header;
	}

	private void setFooter(String footer)
	{
		this.footer = footer;
	}

	private void setDefault(String defaultAnalytics)
	{
		this.defaultAnalytics = defaultAnalytics;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((defaultAnalytics == null) ? 0 : defaultAnalytics.hashCode());
		result = prime * result + ((footer == null) ? 0 : footer.hashCode());
		result = prime * result + ((header == null) ? 0 : header.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		AppTrackingCodeImpl other = (AppTrackingCodeImpl) obj;
		if (defaultAnalytics == null)
		{
			if (other.defaultAnalytics != null) return false;
		}
		else if (!defaultAnalytics.equals(other.defaultAnalytics)) return false;
		if (footer == null)
		{
			if (other.footer != null) return false;
		}
		else if (!footer.equals(other.footer)) return false;
		if (header == null)
		{
			if (other.header != null) return false;
		}
		else if (!header.equals(other.header)) return false;
		return true;
	}
}

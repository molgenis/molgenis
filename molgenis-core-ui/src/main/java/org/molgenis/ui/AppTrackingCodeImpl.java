package org.molgenis.ui;

import org.molgenis.framework.server.MolgenisSettings;

public class AppTrackingCodeImpl implements AppTrackingCode
{
	private String header = null;
	private String footer = null;
	private String gaTrackingId = null;
	private final static String START_TRACKINGCODE = "(function(){if('true' === $.cookie('permissionforcookies')){";
	private final static String END_TRACKINGCODE = "}})();";

	public AppTrackingCodeImpl()
	{
	}

	public AppTrackingCodeImpl(MolgenisSettings molgenisSettings)
	{
		this(molgenisSettings.getProperty(AppTrackingCode.KEY_APP_TRACKING_CODE_FOOTER), molgenisSettings
				.getProperty(AppTrackingCode.KEY_APP_TRACKING_CODE_HEADER), molgenisSettings
				.getProperty(AppTrackingCode.KEY_APP_GOOGLE_ANALYTICS_TRACKING_ID));
	}

	public AppTrackingCodeImpl(String footer, String header, String gaTrackingId)
	{
		if (footer != null)
		{
			this.setFooter(START_TRACKINGCODE + footer + END_TRACKINGCODE);
		}
		if (header != null)
		{
			this.setHeader(START_TRACKINGCODE + header + END_TRACKINGCODE);
		}
		if (gaTrackingId != null)
		{
			// no cookie-permissions code is needed, this id is used for privacy friendly tracking
			this.setGATrackingId(gaTrackingId);
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
	public String getGaTrackingId()
	{
		return gaTrackingId;
	}

	private void setHeader(String header)
	{
		this.header = header;
	}

	private void setFooter(String footer)
	{
		this.footer = footer;
	}

	private void setGATrackingId(String gaTrackingId)
	{
		this.gaTrackingId = gaTrackingId;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((footer == null) ? 0 : footer.hashCode());
		result = prime * result + ((gaTrackingId == null) ? 0 : gaTrackingId.hashCode());
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
		if (footer == null)
		{
			if (other.footer != null) return false;
		}
		else if (!footer.equals(other.footer)) return false;
		if (gaTrackingId == null)
		{
			if (other.gaTrackingId != null) return false;
		}
		else if (!gaTrackingId.equals(other.gaTrackingId)) return false;
		if (header == null)
		{
			if (other.header != null) return false;
		}
		else if (!header.equals(other.header)) return false;
		return true;
	}
}

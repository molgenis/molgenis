package org.molgenis.ui;

import org.molgenis.framework.server.MolgenisSettings;

public class AppTrackingCodeImpl implements AppTrackingCode
{
	private String header = null;
	private String footer = null;
	private final static String START_TRACKINGCODE = "(function(){if('true' === $.cookie('permissionforcookies')){";
	private final static String END_TRACKINGCODE = "}})();";

	public AppTrackingCodeImpl()
	{
	}

	public AppTrackingCodeImpl(MolgenisSettings molgenisSettings)
	{
		this(molgenisSettings.getProperty(AppTrackingCode.KEY_APP_TRACKING_CODE_FOOTER), molgenisSettings
				.getProperty(AppTrackingCode.KEY_APP_TRACKING_CODE_HEADER));
	}

	public AppTrackingCodeImpl(String footer, String header)
	{
		if (footer != null)
		{
			this.setFooter(START_TRACKINGCODE + footer + END_TRACKINGCODE);
		}
		if (header != null)
		{
			this.setHeader(START_TRACKINGCODE + header + END_TRACKINGCODE);
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
	
	private void setHeader(String header)
	{
		this.header = header;
	}

	private void setFooter(String footer)
	{
		this.footer = footer;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.footer == null) ? 0 : this.footer.hashCode());
		result = prime * result + ((this.header == null) ? 0 : this.header.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		AppTrackingCode other = (AppTrackingCode) obj;
		if (this.footer == null)
		{
			if (other.getFooter() != null) return false;
		}
		else if (!this.footer.equals(other.getFooter())) return false;
		if (this.header == null)
		{
			if (other.getHeader() != null) return false;
		}
		else if (!this.header.equals(other.getHeader())) return false;
		return true;
	}
}

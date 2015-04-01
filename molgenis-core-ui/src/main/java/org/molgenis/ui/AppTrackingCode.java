package org.molgenis.ui;

public interface AppTrackingCode
{
	public static final String KEY_APP_TRACKING_CODE_FOOTER = "app.trackingcode.footer";
	public static final String KEY_APP_TRACKING_CODE_HEADER = "app.trackingcode.header";

	/**
	 * Return the header Tracking Code from data base
	 * 
	 * (Google Analytics)
	 * 
	 * @return
	 */
	String getHeader();

	/**
	 * Return the footer Tracking Code from data base
	 * 
	 * (Piwik)
	 * 
	 * @return
	 */
	String getFooter();
}

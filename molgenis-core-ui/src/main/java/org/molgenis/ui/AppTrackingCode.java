package org.molgenis.ui;

public interface AppTrackingCode
{
	public static final String KEY_APP_TRACKING_CODE_PIWIK = "app.trackingcode.piwik";
	public static final String KEY_APP_TRACKING_CODE_GOOGLEANALYTICS = "app.trackingcode.googleanalytics";

	/**
	 * Return the Google Analytics Tracking Code from data base
	 * 
	 * @return
	 */
	String getGoogleAnalytics();

	/**
	 * Return the Piwik Tracking Code from data base
	 * 
	 * @return
	 */
	String getPiwik();
}

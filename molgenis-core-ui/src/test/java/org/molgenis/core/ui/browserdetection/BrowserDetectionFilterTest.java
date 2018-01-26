package org.molgenis.core.ui.browserdetection;

import org.testng.Assert;
import org.testng.annotations.Test;

public class BrowserDetectionFilterTest
{
	private static final String IE8_USER_AGENT = "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; Trident/4.0)";
	private static final String IE9_COMPATIBILTY_MODUS_USER_AGENT = "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.0; Trident/5.0; SLCC1; .NET CLR 2.0.50727; Media Center PC 5.0; .NET CLR 3.5.30729; .NET CLR 3.0.30729; FDM; .NET4.0C; .NET4.0E; chromeframe/11.0.696.57)";
	private static final String IE11_USER_AGENT = "Mozilla/5.0 (Windows NT 6.3; Trident/7.0; rv 11.0) like Gecko";

	@Test
	public void isSupported()
	{
		BrowserDetectionFilter browserDetectionFilter = new BrowserDetectionFilter();
		Assert.assertTrue(browserDetectionFilter.isSupported(IE11_USER_AGENT));
		Assert.assertTrue(browserDetectionFilter.isSupported(null));
		Assert.assertTrue(browserDetectionFilter.isSupported(""));
		Assert.assertTrue(browserDetectionFilter.isSupported("bogus"));
		Assert.assertFalse(browserDetectionFilter.isSupported(IE8_USER_AGENT));

		// We do not support IE9 in compatitiblty mode, see https://github.com/molgenis/molgenis/issues/3481
		Assert.assertFalse(browserDetectionFilter.isSupported(IE9_COMPATIBILTY_MODUS_USER_AGENT));
	}
}

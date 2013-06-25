package org.molgenis.lifelines;

import static org.testng.Assert.assertNotNull;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ProtocolViewerIT
{
	private static WebDriver driver;

	@BeforeClass
	public static void setUpBeforeClass()
	{
		driver = new HtmlUnitDriver(true);
	}

	@AfterClass
	public static void tearDownAfterClass()
	{
		driver.quit();
	}

	// TODO extend tests
	@Test
	public void openProtocolViewer()
	{
		driver.get("http://localhost:8080/");
		WebElement loginTab = driver.findElement(By.id("ProtocolViewerController_tab_button"));
		assertNotNull(loginTab);
	}
}

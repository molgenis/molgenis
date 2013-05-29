package org.molgenis.omx.plugins;

import static junit.framework.Assert.assertEquals;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.molgenis.MolgenisOptions;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

public class ProtocolViewerIntegrationTest {
  private WebDriver driver;
  
  @Before
  public void openBrowser() {
    
	MolgenisOptions mo = new MolgenisOptions();
		
	System.setProperty("webdriver.chrome.driver", mo.getChromeDriver()); 
    driver = new ChromeDriver();
   
    driver.get("http://localhost:8080/");
   
  }

  @After
  public void quit() throws IOException {
    driver.quit();
  }
  
  @Test
  public void loginClickandSearch() {

    driver.get("http://localhost:8080/molgenis.do?__target=main&select=UserLogin");
	WebElement LoginInput = driver.findElement(By.name("username"));
    LoginInput.sendKeys("admin");
	
    WebElement PassInput = driver.findElement(By.name("password"));
    PassInput.clear();  //need this because chrome remembers the password and then inserts another adminadmin as password
    PassInput.sendKeys("admin");
    
    LoginInput.submit();
    
    assertEquals("Molgenis", driver.getTitle());

    driver.get("http://localhost:8080/molgenis.do?__target=main&select=ProtocolViewer");

    try {
        Thread.sleep(40);
    } catch (Exception e) {}

	WebElement link = driver.findElement(By.linkText("Biodata_name"));
	link.click();
	
    try {
        Thread.sleep(1000);
    } catch (Exception e) {}

    
	WebElement SearchInput = driver.findElement(By.className("input-append"));
	SearchInput.click();
	
	WebElement SearchText = driver.findElement(By.id("search-text"));
	SearchText.sendKeys("Biodata");

	WebElement SearchButton = driver.findElement(By.id("search-button"));
	SearchButton.click();
	
    try {
        Thread.sleep(9000);
    } catch (Exception e) {}
    
    assertEquals("Molgenis", driver.getTitle());

  }
  
}
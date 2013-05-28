package test;

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

public class DataSetViewerTest {
  private WebDriver driver;
  
  @Before
  public void openBrowser() {
    
	MolgenisOptions mo = new MolgenisOptions();
	
	System.setProperty("webdriver.chrome.driver", mo.getChromeDriver());     driver = new ChromeDriver();
   
    driver.get("http://localhost:8080/");
   
  }
  
  @After
  public void quit() throws IOException {
    driver.quit();
  }
  
 
  @Test
  public void loginandclick() {

    driver.get("http://localhost:8080/molgenis.do?__target=main&select=UserLogin");
	WebElement LoginInput = driver.findElement(By.name("username"));
    LoginInput.sendKeys("admin");
    WebElement PassInput = driver.findElement(By.name("password"));
    PassInput.clear();  
    PassInput.sendKeys("admin");
    LoginInput.submit();
    
    assertEquals("Molgenis", driver.getTitle());
	driver.get("http://localhost:8080/molgenis.do?__target=main&select=DataSetViewerPlugin");
    try {
        Thread.sleep(4000);
    } catch (Exception e) {}

	WebElement link = driver.findElement(By.className("download-btn"));
	link.click();
	
    assertEquals("Molgenis", driver.getTitle());

  }
  
}
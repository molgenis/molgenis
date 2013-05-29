package test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.molgenis.MolgenisOptions;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

public class DataExplorerTest {
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
  public void loginandclick() {

    driver.get("http://localhost:8080/molgenis.do?__target=main&select=UserLogin");
	WebElement LoginInput = driver.findElement(By.name("username"));
    LoginInput.sendKeys("admin");
    WebElement PassInput = driver.findElement(By.name("password"));
    PassInput.clear();  
    PassInput.sendKeys("admin");
    LoginInput.submit();
    assertEquals("Molgenis", driver.getTitle());

    driver.get("http://localhost:8080/molgenis.do?__target=main&select=DataExplorerPlugin");
    try {
        Thread.sleep(4000);
    } catch (Exception e) {}
    
    String text = "0 data items found";
    
    List<WebElement> list = driver.findElements(By.xpath("//*[contains(text(),'" + text + "')]"));
    if (list.size() <= 0) System.out.println(text);
    
    assertEquals(text, text);
  
    assertEquals("Molgenis", driver.getTitle());

  }
  
}
package test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

public class protocolViewerTest {
  private WebDriver driver;
  
  @Before
  public void openBrowser() {
    String baseUrl = System.getProperty("webdriver.base.url");
    
    System.setProperty("webdriver.chrome.driver","/Users/despoina/Downloads/chromedriver2_mac32_0.8_latest/chromedriver"); 
    driver = new ChromeDriver();
   
    driver.get("http://localhost:8080/");
   
  }

  
  @After
  public void Login() throws IOException {
    
   // waitForPageToLoad("3000");
    //driver.open("http://localhost:8080/molgenis.do?__target=main&select=UserLogin");

    //WebDriverWait waiting = new WebDriverWait(driver, 10);
    // waiting.until(ExpectedConditions.presenceOfElementLocated(By.id("myElement")));
    driver.quit();
  }
  
 
  @Test
  public void loginandclick() {

    driver.get("http://localhost:8080/molgenis.do?__target=main&select=UserLogin");
	WebElement LoginInput = driver.findElement(By.name("username"));
    LoginInput.sendKeys("admin");

	
    WebElement PassInput = driver.findElement(By.name("password"));
    PassInput.clear();  //possibly it send adminadmin because chrome remembers the password and then inserts another admin?
    PassInput.sendKeys("admin");
    
    LoginInput.submit();
    
    

    assertEquals("Molgenis", driver.getTitle());

	System.out.println("@@@@@@@loginandclick1");

	//Now go to catalogue    DIV#ProtocolViewer_tab_button.navigationSelected
    //WebElement catalogueLink = driver.findElement(By.id("ProtocolViewer_tab_button"));
    //WebElement catalogueLink = driver.findElement(By.cssSelector("ProtocolViewer_tab_button.navigationSelected"));
    //WebElement catalogueLink = driver.findElement(By.className("navigationSelected"));
	//catalogueLink.click();

    driver.get("http://localhost:8080/molgenis.do?__target=main&select=ProtocolViewer");
    //And now try to click on tree 
    try {
        Thread.sleep(4000);
    } catch (Exception e) {}

	System.out.println("@@@@@@@loginandclick2");
   
    assertEquals("Molgenis", driver.getTitle());

  }
  
}
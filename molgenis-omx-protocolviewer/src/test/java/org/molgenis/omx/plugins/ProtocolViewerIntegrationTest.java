package org.molgenis.omx.plugins;

import static org.testng.Assert.assertEquals;

import java.io.IOException;

import org.molgenis.omx.plugins.ProtocolViewerIntegrationTest.ProtocolViewerIntegrationTestConfig;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;


@WebAppConfiguration
@ContextConfiguration(classes = ProtocolViewerIntegrationTestConfig.class)
public class ProtocolViewerIntegrationTest extends AbstractTestNGSpringContextTests {
  
  private WebDriver driver;
  
  @Value("${chromedriver}")
  private String chromedriver;
  
  @BeforeMethod
  public void openBrowser() {
    
	System.setProperty("webdriver.chrome.driver", chromedriver); 
    driver = new ChromeDriver();
    System.out.println(chromedriver);
	driver.get("http://localhost:8080/");
   
  }

  @AfterMethod
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
  
  @Configuration
	public static class ProtocolViewerIntegrationTestConfig extends WebMvcConfigurationSupport
	{
		@Bean
		public static PropertySourcesPlaceholderConfigurer properties()
		{
			PropertySourcesPlaceholderConfigurer pspc = new PropertySourcesPlaceholderConfigurer();
			Resource[] resources = new FileSystemResource[]
			{ new FileSystemResource(System.getProperty("user.home") + "/molgenis-server.properties") };
			pspc.setLocations(resources);
			pspc.setIgnoreUnresolvablePlaceholders(true);
			pspc.setIgnoreResourceNotFound(true);
			return pspc;
		}
	}
  
}
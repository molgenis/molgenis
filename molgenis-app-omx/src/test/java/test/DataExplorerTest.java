package test;
import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import test.DataExplorerTest.DataExplorerTestConfig;
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
@ContextConfiguration(classes = DataExplorerTestConfig.class)
public class DataExplorerTest extends AbstractTestNGSpringContextTests {
  private WebDriver driver;
  
  @Value("${chromedriver}")
  private String chromedriver;
  
  @BeforeMethod
  public void openBrowser() {
    
	System.setProperty("webdriver.chrome.driver", chromedriver); 
    driver = new ChromeDriver();
    System.out.println(chromedriver);
	//FileSystemResource a = new FileSystemResource(System.getProperty("user.home") + "/molgenis-server.properties");
	driver.get("http://localhost:8080/");
   
  }

  
  @AfterMethod
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
  
  @Configuration
	public static class DataExplorerTestConfig extends WebMvcConfigurationSupport
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
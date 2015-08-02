package co.nz.powershop.base;

/**
 * 
 * 
 * @author Yu Liu
 * 
 */

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.LocalFileDetector;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

public class TestBase {

	protected Properties configProperties;

	protected Properties accessProperties;

	protected WebDriver driver;

	protected boolean acceptNextAlert = true;

	protected boolean useRemote = false;

	protected WebDriverWait wait;

	protected String bro = "firefox";

	protected StringBuffer verificationErrors = new StringBuffer();

	protected String chromeDriver;

	protected String ieDriver;

	protected Logger Log = Logger.getLogger(this.getClass().getName());

	protected boolean iConfigured = false;

	@SuppressWarnings("rawtypes")
	private boolean isConfigured() {
		Logger rootLogger = Logger.getRootLogger();
		Enumeration appenders = rootLogger.getAllAppenders();
		return appenders.hasMoreElements();
	}

	protected void setUp() throws Exception {
		if (!isConfigured()) {
			DOMConfigurator.configure("log4j.xml");
			iConfigured = true;
		}
		configProperties = new Properties();
		try {
			configProperties.load(TestBase.class.getClassLoader().getResourceAsStream("config.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		useRemote = Boolean.parseBoolean(configProperties.getProperty("use_remote"));

		bro = configProperties.getProperty("browser");
		chromeDriver = configProperties.getProperty("chromedriver");
		ieDriver = configProperties.getProperty("iedriver");

		if (useRemote) {
			File profileDir = new File(getClass().getResource("/seleniumProfile").getFile());
			FirefoxProfile profile = new FirefoxProfile(profileDir);
			DesiredCapabilities capabilities = DesiredCapabilities.firefox();
			capabilities.setCapability(FirefoxDriver.PROFILE, profile);
			capabilities.setCapability("binary", "/usr/bin/firefox");
			String remoteUrl = configProperties.getProperty("selenium_remote_url");
			driver = new RemoteWebDriver(new URL(remoteUrl), capabilities);
			((RemoteWebDriver) driver).setFileDetector(new LocalFileDetector());
		} else {
			if (bro.toLowerCase().equals("firefox")) {

				File profileDir = null;
				profileDir = new File(getClass().getResource("/seleniumProfile").getFile());
				FirefoxProfile profile = new FirefoxProfile(profileDir);
				driver = new FirefoxDriver(profile);
			}
			if (bro.toLowerCase().equals("chrome")) {
				System.setProperty("webdriver.chrome.driver", chromeDriver);
				Map<String, Object> prefs = new HashMap<String, Object>();
				prefs.put("download.default_directory", "target");
				DesiredCapabilities caps = DesiredCapabilities.chrome();
				ChromeOptions options = new ChromeOptions();
				options.setExperimentalOption("prefs", prefs);
				caps.setCapability(ChromeOptions.CAPABILITY, options);
				driver = new ChromeDriver(caps);
			}
			if (bro.toLowerCase().equals("ie")) {
				DesiredCapabilities capabilities = DesiredCapabilities.internetExplorer();
				capabilities.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS, true);
				System.setProperty("webdriver.ie.driver", ieDriver);
				capabilities.setCapability("ignoreZoomSetting", true);
				capabilities.setCapability("nativeEvents", false);
				driver = new InternetExplorerDriver(capabilities);
			}
		}
		int timeout = Integer.parseInt(configProperties.getProperty("driver_timeout"));
		driver.manage().timeouts().implicitlyWait(timeout, TimeUnit.SECONDS);
	}

	protected void tearDown() throws Exception {
		driver.quit();
		String verificationErrorString = verificationErrors.toString();
		if (!"".equals(verificationErrorString)) {
			fail(verificationErrorString);
		}
		if (iConfigured) {
			org.apache.log4j.LogManager.shutdown();
		}
	}
	
	protected void login(String name, String password) {
		driver.findElement(By.id("email")).clear();
		driver.findElement(By.id("email")).sendKeys(name);
		driver.findElement(By.id("password")).clear();
		driver.findElement(By.id("password")).sendKeys(password);
		driver.findElement(By.id("login_button")).click();
	}

	protected String today() {
		return new SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH).format(new Date());
	}

	protected String tomorrow() {
		return new SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH).format(new Date().getTime() + 86400000);
	}

	protected String day(String date, int num) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH);
		Calendar cal = Calendar.getInstance();
		try {
			cal.setTime(dateFormat.parse(date));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		cal.add(Calendar.DATE, num);
		String convertedDate = dateFormat.format(cal.getTime());
		return convertedDate;
	}

	protected String day(int num) {
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		c.add(Calendar.DATE, num);
		return new SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH).format(c.getTime());
	}

	protected void load(int sec, By by) {
		wait = new WebDriverWait(driver, sec);
		wait.until(ExpectedConditions.presenceOfElementLocated(by));
	}

	protected boolean isElementPresent(By by) {
		try {
			driver.findElement(by);
			return true;
		} catch (NoSuchElementException e) {
			return false;
		}
	}

	protected boolean isElementPresent(WebElement we, By by) {
		try {
			we.findElement(by);
			return true;
		} catch (NoSuchElementException e) {
			return false;
		}
	}

	protected Boolean isTextPresent(String text) {
		if (driver.getPageSource().toString().contains(text))
			return true;
		else
			return false;
	}

	protected boolean isSelect(By by) {
		if (driver.findElement(by).isSelected())
			return true;
		else
			return false;
	}

	protected boolean isAlertPresent() {
		try {
			driver.switchTo().alert();
			return true;
		} catch (NoAlertPresentException e) {
			return false;
		}
	}

	protected String closeAlertAndGetItsText() {
		try {
			Alert alert = driver.switchTo().alert();
			String alertText = alert.getText();
			if (acceptNextAlert) {
				alert.accept();
			} else {
				alert.dismiss();
			}
			return alertText;
		} finally {
			acceptNextAlert = true;
		}
	}

	protected void verifyContains(By by, String text, String error) {
		try {
			assertTrue(driver.findElement(by).getText().toString().contains(text));
		} catch (Error e) {
			verificationErrors.append(error);
			Log.error(error, e);
		}
	}
	
	protected void verifyEquals(String actual, String expect, String error) {
		try {
			assertEquals(actual, expect);
		} catch (Error e) {
			verificationErrors.append(error);
			Log.error(error, e);
		}
	}
	
	protected void verifyNotEquals(String actual, String expect, String error) {
		try {
			assertFalse(expect.equals(actual));
		} catch (Error e) {
			verificationErrors.append(error);
			Log.error(error, e);
		}
	}

	protected void verifyPresent(By by, String error) {
		try {
			assertTrue(isElementPresent(by));
		} catch (Error e) {
			verificationErrors.append(error);
			Log.error(error, e);
		}
	}

	protected void verifyTrue(Boolean bool, String error) {
		try {
			assertTrue(bool);
		} catch (Error e) {
			verificationErrors.append(error);
			Log.error(error, e);
		}
	}
	
	protected void verifyFalse(Boolean bool, String error) {
		try {
			assertFalse(bool);
		} catch (Error e) {
			verificationErrors.append(error);
			Log.error(error, e);
		}
	}

	protected void click(By by) {
		driver.findElement(by).click();
	}

	protected void doubleClick(By by) {
		Actions action = new Actions(driver);
		action.moveToElement(driver.findElement(by)).doubleClick().perform();
	}

	protected void input(By by, String message) {
		driver.findElement(by).clear();
		driver.findElement(by).clear();
		driver.findElement(by).sendKeys(message);
	}

	protected String getAttributeValue(By by, String attribute) {
		return driver.findElement(by).getAttribute(attribute);
	}
	
	protected String getColor(By by) {
		return driver.findElement(by).getCssValue("color").toString();
	}
	
	protected String getBackgroudColor(By by) {
		return driver.findElement(by).getCssValue("background-color").toString();
	}

	protected void select(By by, String text) {
		new Select(driver.findElement(by)).selectByVisibleText(text);
	}

	protected void select(By by, int index) {
		new Select(driver.findElement(by)).selectByIndex(index);
	}

	protected void wait(int ms) {
		(new WebDriverWait(driver, ms)).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver d) {
				return d.findElement(By.id("mainContent")).isDisplayed();
			}
		});
	}

	protected void clickSubmenu(By menu, By submenu) {
		Actions action = new Actions(driver);
		action.moveToElement(driver.findElement(menu)).moveToElement(driver.findElement(submenu)).click().build().perform();
	}

	protected void key_action(Keys key) {
		Actions action = new Actions(driver);
		action.sendKeys(key).click().build().perform();
	}

	protected void getElement(By by) {
		driver.findElement(by);
	}

	protected String getElementText(By by) {
		return driver.findElement(by).getText().trim();
	}

	protected void nagativeToURL(String url) {
		driver.get(url);
	}

	protected String currentURL() {
		return driver.getCurrentUrl();
	}

}

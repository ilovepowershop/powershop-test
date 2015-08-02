package co.nz.powershop.testcases;

import javax.json.*;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import co.nz.powershop.base.TestBase;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;

/**
 * 
 * 
 * @author Yu Liu
 * 
 */

public class PowershopFunctionalTest extends TestBase {

	private String baseUrl;
	private String userName;
	private String userPassword;

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		baseUrl = configProperties.getProperty("baseurl");
		userName = configProperties.getProperty("username");
		userPassword = configProperties.getProperty("pwd");
		driver.get(baseUrl);
		driver.manage().window().maximize();
	}

	@Test
	public void testLoginAccount() {
		try {
			Log.info("Login powershop account");
			this.login(userName, userPassword);
			this.verifyContains(By.xpath(".//*[@id='content-inner']/div[2]/h2"), "Welcome", "Failed to login powershop account");
			Log.info("View all your previous account statements online here");
			this.click(By.linkText("View all your previous account statements online here"));
			this.verifyContains(By.xpath(".//*[@id='page-wrapper']/header/div/div[1]/div/ul/li[1]/strong"), "Welcome", "Failed to navigate to account statement");
		} catch (Exception ex) {
			verificationErrors.append(ex.toString());
			Log.error(this.getClass().getName() + " failed", ex);
		}
	}

	@Test
	public void testAverageUnitPrices() {
		try {
			Log.info("Navigate to AverageUnitPrices");
			this.login(userName, userPassword);
			this.click(By.linkText("View all your previous account statements online here"));
			this.click(By.linkText("Average Unit Prices"));
			Log.info("Verify the Average Unit Prices is highlight");
			this.verifyEquals(getColor(By.linkText("Average Unit Prices")), "rgba(188, 33, 107, 1)", "Average Unit Prices is not highlight");

			Log.info("Get data from barchart");
			List<String> chartData = this.getChartTooltips();

			Log.info("Get data from table");
			List<String> tableData = this.getPageTableData();

			Log.info("Compare barchart data and table data");
			for (int i = 0; i < chartData.size(); i++) {
				this.verifyEquals(chartData.get(i), tableData.get(i), "Barchart data " + chartData.get(i) + " is not eaqual to table data " + tableData.get(i));
			}

			Log.info("Verify averageUnitPrices value");
			double yaChart = this.getYearlyAverage();
			double yaTable = Double.parseDouble(tableData.get(tableData.size() - 1));
			this.verifyTrue(yaChart == yaTable, "Ave of Barchart data " + yaChart + "is not eaqual to ave of table data " + yaTable);

		} catch (Exception ex) {
			verificationErrors.append(ex.toString());
			Log.error(this.getClass().getName() + " failed", ex);
		}
	}

	@Test
	public void testAccountStatement() {
		try {
			Log.info("Navigate to Account Statement");
			this.login(userName, userPassword);
			this.click(By.linkText("View all your previous account statements online here"));
			this.click(By.linkText("Account Statement"));

			Log.info("Verify the Account Statement is highlight");
			this.verifyEquals(getColor(By.linkText("Account Statement")), "rgba(188, 33, 107, 1)", "Account Statement is not highlight");

			Log.info("Verify date filter");
			this.input(By.id("from"), "22/06/2015");
			this.input(By.id("to"), "22/06/2015");
			this.click(By.name("button"));
			this.verifyTrue(isTextPresent("Purchase refund"), "Date filter result is not correct");

			Log.info("Verify csv file");
			// this.click(By.name("download"));
			Log.info("Compare data between Page and CSV");
			List<String> csvData = getCSVData("my_transactions_20150622_20150622.csv");
			this.verifyEquals(getElementText(By.xpath(".//*[@id='txn_10165980']/td[4]")), csvData.get(15), "Data in the CVS " + csvData.get(15) + " does not contains "
					+ getElementText(By.xpath(".//*[@id='txn_10165980']/td[4]")) + " on the page");
			this.verifyEquals(getElementText(By.xpath(".//*[@id='txn_10165980']/td[5]")), csvData.get(16), "Data in the CVS " + csvData.get(16) + " does not contains "
					+ getElementText(By.xpath(".//*[@id='txn_10165980']/td[5]")) + " on the page");

			Log.info("Verify pdf file");
			// this.click(By.xpath(".//*[@id='txn_10165980']/td[7]/a/img"));
			String pdfData = getPDFData("Powershop_Credit_Note_10165980.pdf");
			Log.info("Compare data between Page and PDF");
			for (int i = 2; i <= 8; i++) {
				this.verifyTrue(pdfData.contains(getElementText(By.xpath(".//*[@id='txn_10165980']/td[" + i + "]"))), "Data in the PDF does not contains "
						+ getElementText(By.xpath(".//*[@id='txn_10165980']/td[" + i + "]")) + " on the page");

			}

		} catch (Exception ex) {
			verificationErrors.append(ex.toString());
			Log.error(this.getClass().getName() + " failed", ex);
		}
	}

	@Override
	@After
	public void tearDown() throws Exception {
		super.tearDown();
	}

	private List<String> getPageTableData() {
		WebElement table = null;
		table = driver.findElement(By.cssSelector("table.table-monthly-charge"));
		WebElement tbody = table.findElement(By.tagName("tbody"));
		List<WebElement> allRows = tbody.findElements(By.cssSelector("tr:nth-child(2)"));
		// List<WebElement> allRows = tbody.findElements(By.tagName("tr"));
		List<String> li1 = new ArrayList<String>();
		for (WebElement tr : allRows) {
			List<WebElement> cells = tr.findElements(By.tagName("td"));
			for (WebElement cell : cells) {
				if (cell.getText().toString().contains("-"))
					li1.add("");
				else
					li1.add(cell.getText().toString().trim());
			}
		}
		return li1;
	}

	private List<String> getChartTooltips() throws InterruptedException {
		WebElement svg = null;
		svg = driver.findElement(By.id("__svg__random___0"));
		WebElement graph = svg.findElement(By.id("graphene_data"));
		List<WebElement> bars = graph.findElements(By.cssSelector("rect.graphene_bar"));
		Actions action = new Actions(driver);
		List<String> li2 = new ArrayList<String>();
		for (WebElement b : bars) {
			action.moveToElement(b).perform();
			Thread.sleep(2000);
			li2.add(driver.findElement(By.id("graph-rollover-value")).getText().toString());
		}
		return li2;
	}

	private double getYearlyAverage() {
		double yearlyAverage = 0;
		try {
			WebElement svg = null;
			svg = driver.findElement(By.id("__svg__random___0"));
			List<WebElement> graphs = svg.findElements(By.tagName("g"));
			for (WebElement g : graphs) {
				if (g.getAttribute("id").equals("graphene_data"))
					continue;
				String data = g.getAttribute("data-data");
				JsonReader reader = Json.createReader(new StringReader(data));
				JsonArray dataArray = reader.readArray();
				JsonValue jv = ((JsonObject) dataArray.get(0)).get("y_value");
				yearlyAverage = formatDouble(Double.parseDouble(jv.toString()));
			}
		} catch (Exception e) {
			Log.error(e);
		}
		return yearlyAverage;
	}

	private List<String> getCSVData(String filename) throws IOException {
		Scanner scanner = new Scanner(new File(getClass().getResource("/" + filename).getFile()));
		scanner.useDelimiter(",");
		List<String> li3 = new ArrayList<String>();

		while (scanner.hasNext()) {
			li3.add(scanner.next().toString());
		}
		scanner.close();
		return li3;
	}

	private String getPDFData(String filename) throws IOException {
		String page = null;
		PdfReader reader = new PdfReader(filename);
		page = PdfTextExtractor.getTextFromPage(reader, 1);
		return page;
	}

	public static double formatDouble(double d) {
		BigDecimal bg = new BigDecimal(d).setScale(1, RoundingMode.UP);
		return bg.doubleValue();
	}
}

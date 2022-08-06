package com.ladbrokes;


import java.io.FileReader;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.Sleeper;
import org.testng.ITestContext;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.browserstack.local.Local;
import com.codeborne.selenide.WebDriverRunner;


public class LadbrokesProduction {
	public RemoteWebDriver driver;
	private Local l;

	public static String username;
	public static String accessKey;
	public static String sessionId;

	@BeforeMethod(alwaysRun=true)
	@Parameters(value={"config", "environment"})
	public void setUp(String config_file, String environment, ITestContext context) throws Exception {
		JSONParser parser = new JSONParser();
		JSONObject config = (JSONObject) parser.parse(new FileReader("src/test/resources/conf/" + config_file));
		JSONObject envs = (JSONObject) config.get("environments");
		JSONObject innerJson = (JSONObject) envs.get("env1");

		DesiredCapabilities capabilities = new DesiredCapabilities();
		
		HashMap<String, Boolean> networkLogsOptions = new HashMap<>();
		networkLogsOptions.put("captureContent", true);
		capabilities.setCapability("browserstack.networkLogs", true);
		capabilities.setCapability("browserstack.networkLogsOptions", networkLogsOptions);
		
		capabilities.setCapability("browserstack.maskCommands", "setValues, getValues, setCookies, getCookies");

		
		Map<String, String> envCapabilities = (Map<String, String>) innerJson;
		
		Iterator it = envCapabilities.entrySet().iterator();
    	while (it.hasNext()) {
      		Map.Entry pair = (Map.Entry)it.next();
      		//add
      		capabilities.setCapability(pair.getKey().toString(), pair.getValue().toString());
    	}

		Map<String, String> commonCapabilities= (Map<String, String>) config.get("capabilities");
		it = commonCapabilities.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry)it.next();
			if (capabilities.getCapability(pair.getKey().toString()) == null) {
				capabilities.setCapability(pair.getKey().toString(), pair.getValue().toString());
			}
		}

		username = System.getenv("rajagajula_NWM17Z");
		if (username == null) {
			username = (String) config.get("user");
		}

		accessKey = System.getenv("NX8dFKvyXN3SjyuADD4K");
		if (accessKey == null) {
			accessKey = (String) config.get("key");
		}

		if (capabilities.getCapability("browserstack.local") != null && capabilities.getCapability("browserstack.local") == "true") {
			l = new Local();
			Map<String, String> options = new HashMap<String, String>();
			options.put("key", accessKey);
			String currentTime = String.valueOf(System.currentTimeMillis());
			options.put("localIdentifier", currentTime);
			capabilities.setCapability("browserstack.localIdentifier", currentTime);
			l.start(options);
		}

		driver = new RemoteWebDriver(new URL("http://" + username + ":" + accessKey + "@" + config.get("server") + "/wd/hub"), capabilities);
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		sessionId = driver.getSessionId().toString();
		
		JavascriptExecutor jse = (JavascriptExecutor)driver;
		
		//code for to get dashboard url
		Object response = jse.executeScript("browserstack_executor: {\"action\": \"getSessionDetails\"}");
	    JSONObject json = (JSONObject) new JSONParser().parse((String) response);
	    String browserStack_dashboard_url = (String) json.get("browser_url");
		context.setAttribute("dashboard_url", browserStack_dashboard_url);
		context.setAttribute("tableName", "ladbrokesproduction");

		WebDriverRunner.setWebDriver(driver);
	}

	@AfterMethod(alwaysRun=true)
	public void tearDown() throws Exception {
		WebDriverRunner.closeWebDriver();
		if (l != null) l.stop();
	}
	
	@Test(description = "Launch sports.ladbrokes.com")
	public void ivy(ITestContext context) {
		Object executeScript;
		driver.manage().window().maximize();
		driver.manage().deleteAllCookies();
		driver.get("https://sports.ladbrokes.com/");
		
		JavascriptExecutor executor = (JavascriptExecutor) driver;
		
		
		/*
		 * WebElement element = driver.findElement(By.name("head"));
		 * element.sendKeys("BrowserStack"); element.submit();
		 */
	    // Setting the status of test as 'passed' or 'failed' based on the condition; if title of the web page matches 'BrowserStack - Google Search'
	    if (driver.getTitle().equals("Online Sports Betting & Odds at Ladbrokes Bookmakers")) {
	    	executor.executeScript("browserstack_executor: {\"action\": \"setSessionStatus\", \"arguments\": {\"status\": \"passed\", \"reason\": \"Title matched!\"}}");
	    }
	    else {
	    	executor.executeScript("browserstack_executor: {\"action\": \"setSessionStatus\", \"arguments\": {\"status\":\"failed\", \"reason\": \"Title not matched\"}}");
	    }

		
		String js = "function test() {" +
		           "const po = new PerformanceObserver(() => {});po.observe({type: 'largest-contentful-paint', buffered: true});" +
				"const lastEntry = po.takeRecords().slice(-1)[0];" +
		            "return lastEntry.renderTime || lastEntry.loadTime;" +
		           "}; return test()";
		
		executeScript = executor.executeScript(js);
		double parseDouble = Double.parseDouble(executeScript.toString());
		long lpp = (new Double(parseDouble)).longValue();
		context.setAttribute("lcp_value", lpp);
		
		driver.get("https://sports.ladbrokes.com/");
		String lcp2 = "function test1() {" +
		           "const po = new PerformanceObserver(() => {});po.observe({type: 'largest-contentful-paint', buffered: true});" +
				"const lastEntry = po.takeRecords().slice(-1)[0];" +
		            "return lastEntry.renderTime || lastEntry.loadTime;" +
		           "}; return test1()";
		
		executeScript = executor.executeScript(lcp2);
		double parsDou2 = Double.parseDouble(executeScript.toString());
		long secondlpp = (new Double(parsDou2)).longValue();
		context.setAttribute("lcp_value2", secondlpp);
		
//		String firstPaint = "function showPaintTimings() {"
//				+ "	var finalPaint = {}; var firstPaint; var contentful;"rom 
//				+ "  if (window.performance) {"
//				+ "    let performance = window.performance; "
//				+ "    let performanceEntries = performance.getEntriesByType('paint');"
//				+ "    performanceEntries.forEach( (performanceEntry, i, entries) => {"
//				+ "		if(performanceEntry.name === \"first-paint\") {"
//				+ "			firstPaint = performanceEntry.startTime;"
//				+ "		}"
//				+ " 	if(performanceEntry.name === \"first-contentful-paint\") {"
//				+ "	   		contentful = performanceEntry.startTime;"
//				+ "		}"
//				+ "    });"
//				+ "  }"
//				+ " finalPaint = {\"finalPaint\" : firstPaint, \"contentful\" : contentful};"
//				+ "  return finalPaint;"
//				+ "}; return showPaintTimings()";
//		Object executeFirstPaint = executor.executeScript(firstPaint);
		
		//long lcpSeconds = (lpp / 1000) % 60;
		/*
		 * df.format(lcpSeconds); System.out.println(df.format(lcpSeconds));
		 */
		
		/*long longValue = ((Double) executeScript).longValue();
		((Double) executeScript) / 1000) % 60;*/
		
		
		
		/*
		 * long seconds = (milliseconds / 1000) % 60;
 
		 * String pageSource = driver.getPageSource();
		 * 
		 * String addr = "function test1() {" + "return document" + "}; return test1()";
		 * executor.executeScript(addr);
		 */
		
		
	}
}

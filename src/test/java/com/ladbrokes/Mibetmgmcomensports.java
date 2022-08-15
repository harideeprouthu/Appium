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
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.ITestContext;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.browserstack.local.Local;
import com.codeborne.selenide.WebDriverRunner;
import com.ladbrokes.utils.Constants;

public class Mibetmgmcomensports {
	public RemoteWebDriver driver;
	private Local l;

	public static String username;
	public static String accessKey;
	public static String sessionId;

	@SuppressWarnings("unchecked")
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
		capabilities.setCapability("browserstack.geoLocation", "GB");
		capabilities.setCapability("browserstack.networkProfile", "4g-lte-good");
		capabilities.setCapability("name", "Mibetmgm");

		
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

		username = System.getenv(Constants.USERNAME);
		if (username == null) {
			username = (String) config.get("user");
		}

		accessKey = System.getenv(Constants.PASSWORD);
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
		context.setAttribute("tableName", "mibetmgmcomensports");

		WebDriverRunner.setWebDriver(driver);
	}

	@AfterMethod(alwaysRun=true)
	public void tearDown() throws Exception {
		WebDriverRunner.closeWebDriver();
		if (l != null) l.stop();
	}
	
	@Test(description = "Launch sports.coral.co.uk")
	public void coralProduction(ITestContext context) {
		Object executeScript;
		driver.manage().window().maximize();
		driver.manage().deleteAllCookies();
		driver.get("https://sports.mi.betmgm.com/en/sports");
		
		JavascriptExecutor executor = (JavascriptExecutor) driver;
		if (driver.getTitle().equals("Online Sportsbook | Bet Online | Online Sports Betting | BetMGM")) {
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
		
//		System.out.println("");
//		System.out.println("");
//		driver.get("https://sports.mi.betmgm.com/en/sports");
//		String lcp2 = "function test1() {" +
//		           "const po = new PerformanceObserver(() => {});po.observe({type: 'largest-contentful-paint', buffered: true});" +
//				"const lastEntry = po.takeRecords().slice(-1)[0];" +
//		            "return lastEntry.renderTime || lastEntry.loadTime;" +
//		           "}; return test1()";
//		if(lcp2 != null) {
//			executeScript = executor.executeScript(lcp2);
//			double parsDou2 = Double.parseDouble(executeScript.toString());
//			long secondlpp = (new Double(parsDou2)).longValue();
//			context.setAttribute("lcp_value2", secondlpp);
//		}
	}
}

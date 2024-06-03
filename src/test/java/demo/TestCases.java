package demo;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import io.github.bonigarcia.wdm.WebDriverManager;

public class TestCases {
    private WebDriver driver;
    private WebDriverWait wait;
    private static final int PAGES_TO_SCRAPE = 4;

    @BeforeSuite(alwaysRun = true)
    public void startTest() {
        System.out.println("TestCases Started:");
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        driver.manage().window().maximize();

        wait = new WebDriverWait(driver, Duration.ofSeconds(20));

    }

    @Test(priority = 1, enabled = true)
    public void testCase_01() {
        List<HashMap<String, Object>> teamData = new ArrayList<>();

        try {
            System.out.println("Start Test case: Scrape hockey team data");
            driver.get("https://www.scrapethissite.com/pages/");
            wait.until(ExpectedConditions
                    .elementToBeClickable(By.linkText("Hockey Teams: Forms, Searching and Pagination"))).click();
            Thread.sleep(2000);
            for (int i = 0; i <= PAGES_TO_SCRAPE; i++) {
                if (i > 1) {
                    wait.until(ExpectedConditions
                            .elementToBeClickable(By.xpath("//*[@id='hockey']/div/div[5]/div[1]/ul/li[25]/a"))).click();
                }

                wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath("//table/tbody/tr")));
                List<WebElement> rows = driver.findElements(By.xpath("//table/tbody/tr"));

                for (WebElement row : rows) {
                    List<WebElement> columns = row.findElements(By.tagName("td"));
                    if (columns.size() > 0) {
                        String teamName = columns.get(0).getText();
                        String year = columns.get(1).getText();
                        String pctText = columns.get(5).getText();
                        double winPercentage = pctText.isEmpty() ? 0.0 : Double.parseDouble(pctText);

                        if (winPercentage < 0.40) {
                            HashMap<String, Object> team = new HashMap<>();
                            team.put("EpochTime", System.currentTimeMillis());
                            team.put("TeamName", teamName);
                            team.put("Year", year);
                            team.put("WinPercentage", winPercentage);
                            teamData.add(team);
                        }
                    }
                }
            }

            saveToJson(teamData, "output/hockey-team-data.json");
            System.out.println("End Test case: Hockey team data scraped and saved successfully");
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Test case 01 failed due to an exception: " + e.getMessage());
        }
    }

    @Test(priority = 2, enabled = true)
    public void testCase_02() {
        List<HashMap<String, Object>> filmData = new ArrayList<>();

        try {
            System.out.println("Start Test case: Scrape Oscar winning films data");
            driver.get("https://www.scrapethissite.com/pages/");
            WebElement oscarLink = wait.until(
                    ExpectedConditions.elementToBeClickable(By.linkText("Oscar Winning Films: AJAX and Javascript")));
            oscarLink.click();

            List<WebElement> years = driver.findElements(By.xpath("//a[@href='#']"));

            for (int yearIndex = 0; yearIndex < years.size(); yearIndex++) {
                WebElement yearElement = years.get(yearIndex);
                String year = yearElement.getText();
                System.out.println("Scraping data for year: " + year);
                yearElement.click();
                Thread.sleep(3000);
                // Wait for the movie table to be present
                WebElement movieTable = wait
                        .until(ExpectedConditions.presenceOfElementLocated(By.xpath("(//div[@class='col-md-12'])[4]")));

                List<WebElement> rows = movieTable.findElements(By.xpath(".//tbody/tr"));

                // Scraping the top 5 movies for each year
                int count = 0;
                for (int i = 0; i < rows.size() && count < 5; i++) {
                    WebElement row = rows.get(i);
                    List<WebElement> columns = row.findElements(By.tagName("td"));

                    if (columns.size() > 0) {
                        String title = columns.get(0).getText();
                        String nomination = columns.get(1).getText();
                        String awards = columns.get(2).getText();
                        boolean isWinner = !row.findElements(By.xpath(".//i[contains(@class, 'glyphicon-flag')]"))
                                .isEmpty();

                        if (!title.isEmpty() && !nomination.isEmpty() && !awards.isEmpty()) {
                            HashMap<String, Object> film = new HashMap<>();
                            film.put("EpochTime", System.currentTimeMillis());
                            film.put("Year", year);
                            film.put("Title", title);
                            film.put("Nomination", nomination);
                            film.put("Awards", awards);
                            film.put("isWinner", isWinner);
                            filmData.add(film);
                            count++;
                        }
                    }
                }
            }

            saveToJson(filmData, "output/oscar-winner-data.json");
            System.out.println("End Test case: Oscar winning films data scraped and saved successfully");
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Test case 02 failed due to an exception: " + e.getMessage());
        }
    }

    private static void saveToJson(List<HashMap<String, Object>> data, String filePath) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        try {
            File outputDir = new File("Output");
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }
            mapper.writeValue(new File(filePath), data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @AfterSuite(alwaysRun = true)
    public void endTest() {
        System.out.println("End Test: TestCases");
        if (driver != null) {
            driver.quit();
        }
    }
}

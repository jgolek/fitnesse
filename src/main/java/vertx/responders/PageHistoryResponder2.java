package vertx.responders;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;

import fitnesse.html.template.HtmlPage;
import fitnesse.html.template.PageFactory;
import fitnesse.html.template.PageTitle;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.Response.Format;
import fitnesse.http.SimpleResponse;
import fitnesse.reporting.history.ExecutionReport;
import fitnesse.reporting.history.PageHistory;
import fitnesse.reporting.history.SuiteExecutionReport;
import fitnesse.reporting.history.TestExecutionReport;
import fitnesse.reporting.history.TestHistory;
import fitnesse.reporting.history.TestResultRecord;
import fitnesse.testsystems.ExecutionResult;
import fitnesse.wiki.PathParser;
import util.FileUtil;

public class PageHistoryResponder2 {
    private SimpleDateFormat dateFormat = new SimpleDateFormat(PageHistory.TEST_RESULT_FILE_DATE_PATTERN);
    private SimpleResponse response;
    private PageHistory pageHistory;
    private HtmlPage page;

    public String makeHtml(PageFactory pageFactory, File testHistoryDirectory, String pageName, String requestResource,
                    String resultDate) throws Exception {

        TestHistory history = new TestHistory(testHistoryDirectory, pageName);
        pageHistory = history.getPageHistory(pageName);
        page = pageFactory.newPage();
        PageTitle pageTitle = new PageTitle("Test History", PathParser.parse(requestResource), "");
        page.setPageTitle(pageTitle);

        if (resultDate != null) {
            return tryToMakeTestExecutionReport(requestResource, resultDate);
        }

        page.setTitle("Page History");
        page.put("pageHistory", pageHistory);
        page.setNavTemplate("viewNav");
        page.put("viewLocation", requestResource);
        page.setMainTemplate("pageHistory");

        return page.html();
    }

    private String makePageHistoryResponse(String requestResource) throws UnsupportedEncodingException {
        page.setTitle("Page History");
        page.put("pageHistory", pageHistory);
        page.setNavTemplate("viewNav");
        page.put("viewLocation", requestResource);
        page.setMainTemplate("pageHistory");
        return page.html();
    }

    private boolean formatIsXML(Request request) {
        String format = request.getInput("format");
        return "xml".equalsIgnoreCase(format);
    }

    private String tryToMakeTestExecutionReport(String requestResource, String date) throws Exception {
        Date resultDate;
        if ("latest".equals(date)) {
            resultDate = pageHistory.getLatestDate();
        } else {
            resultDate = dateFormat.parse(date);
        }
        TestResultRecord testResultRecord = pageHistory.get(resultDate);

        return makeTestExecutionReportResponse(requestResource, resultDate, testResultRecord);

    }

    private String makeTestExecutionReportResponse(String requestResource, Date resultDate,
                    TestResultRecord testResultRecord) throws Exception {

        ExecutionReport report;

        String content = FileUtil.getFileContent(testResultRecord.getFile());
        report = ExecutionReport.makeReport(content);
        if (report instanceof TestExecutionReport) {
            report.setDate(resultDate);
            return generateHtmlTestExecutionResponse(requestResource, (TestExecutionReport) report);
        } else if (report instanceof SuiteExecutionReport) {
            return generateHtmlSuiteExecutionResponse(requestResource, (SuiteExecutionReport) report);
        } else
            return null;
    }

    private String generateHtmlSuiteExecutionResponse(String requestResource, SuiteExecutionReport report)
                    throws Exception {
        page.setTitle("Suite Execution Report");
        page.setNavTemplate("viewNav");
        page.put("viewLocation", requestResource);
        page.put("suiteExecutionReport", report);
        page.put("resultDate", dateFormat.format(report.getDate()));
        page.put("ExecutionResult", ExecutionResult.class);
        page.setMainTemplate("suiteExecutionReport");
        PageTitle pageTitle = new PageTitle("Suite History", PathParser.parse(requestResource), "");
        page.setPageTitle(pageTitle);

        return page.html();
    }

    private String generateHtmlTestExecutionResponse(String requestResource, TestExecutionReport report)
                    throws Exception {
        page.setTitle("Test Execution Report");
        page.setNavTemplate("viewNav");
        page.put("viewLocation", requestResource);
        page.put("testExecutionReport", report);
        if (!report.getExecutionLogs().isEmpty()) {
            page.put("resultDate", dateFormat.format(report.getDate()));
        }
        page.put("ExecutionResult", ExecutionResult.class);
        page.setMainTemplate("testExecutionReport");
        page.setErrorNavTemplate("errorNavigator");
        String tags = report.getResults().get(0).getTags();
        PageTitle pageTitle = new PageTitle("Test History", PathParser.parse(requestResource), tags);
        page.setPageTitle(pageTitle);

        return page.html();
    }

    private Response generateXMLResponse(File file) throws UnsupportedEncodingException {
        try {
            response.setContent(FileUtil.getFileContent(file));
        } catch (IOException e) {
            response.setContent("Error: Unable to read file '" + file.getName() + "'\n");
        }
        response.setContentType(Format.XML);
        return response;
    }

}

package vertx.responders;

import static fitnesse.wiki.WikiPageUtil.isTestPage;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import fitnesse.FitNesseContext;
import fitnesse.reporting.BaseFormatter;
import fitnesse.reporting.history.TestXmlFormatter;
import fitnesse.testrunner.MultipleTestsRunner;
import fitnesse.testsystems.TestSummary;
import fitnesse.wiki.WikiPage;

public class TestResponder2 extends SuiteResponder2 {

  @Override
  protected List<WikiPage> getPagesToRun() {
    if (isTestPage(page)) {
      return singletonList(page);
    } else {
      return emptyList();
    }
  }

  @Override
  protected void addHistoryFormatter(MultipleTestsRunner runner) {
    HistoryWriterFactory source = new HistoryWriterFactory();
    TestXmlFormatter testXmlFormatter = new TestXmlFormatter(context, page, source);
    runner.addTestSystemListener(testXmlFormatter);
    runner.addExecutionLogListener(testXmlFormatter);
  }

  @Override
  protected BaseFormatter newXmlFormatter() {
    return new TestXmlFormatter(context, page, new TestXmlFormatter.WriterFactory() {
      @Override
      public Writer getWriter(FitNesseContext context, WikiPage page, TestSummary counts, long time) throws IOException {
        return response.getWriter();
      }
    });
  }

}

package vertx;

import java.io.File;
import java.io.StringWriter;
import java.util.Map;

import fitnesse.html.template.HtmlPage;
import fitnesse.html.template.PageFactory;
import fitnesse.html.template.PageTitle;
import fitnesse.reporting.SuiteHtmlFormatter;
import fitnesse.responders.WikiPageActions;
import fitnesse.testrunner.WikiPageDescriptor;
import fitnesse.testrunner.WikiTestPage;
import fitnesse.testsystems.slim.CustomComparatorRegistry;
import fitnesse.testsystems.slim.HtmlSlimTestSystem;
import fitnesse.testsystems.slim.InProcessSlimClientBuilder;
import fitnesse.testsystems.slim.SlimClient;
import fitnesse.testsystems.slim.SlimTestSystem;
import fitnesse.testsystems.slim.tables.SlimTableFactory;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.SystemVariableSource;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;
import fitnesse.wiki.WikiPageUtil;
import fitnesse.wiki.fs.DiskFileSystem;
import fitnesse.wiki.fs.FileSystemPageFactory;
import fitnesse.wiki.fs.ZipFileVersionsController;
import vertx.reporting.history.TestXmlFormatter2;
import vertx.responders.AddChildPageResponder2;
import vertx.responders.EditResponder2;
import vertx.responders.NewPageResponder2;
import vertx.responders.SaveResponder2;
import vertx.responders.SuiteResponder2.HistoryWriterFactory;
import vertx.responders.TestResponder2;
import vertx.responders.WikiPageResponder;

public class PageWorkflow {

  public String runTest(String qualifiedPageName, Map<String, String> params) throws Exception {

    WikiPage wikiPage = loadPageFromFileSystem(qualifiedPageName);

    // testResponder.runTest();
    // MultipleTestSystemFactory. run test.
    // wiki format to slim instructuions.
    WikiTestPage testPage = new WikiTestPage(wikiPage);
    WikiPageDescriptor descriptor = new WikiPageDescriptor(wikiPage, true, false, "");

    // start slim service
    SlimClient slimClient = new InProcessSlimClientBuilder(descriptor).build();
    CustomComparatorRegistry customComparatorRegistry = new CustomComparatorRegistry();
    // slimClient = new SlimClientBuilder(getDescriptor()).build();
    SlimTestSystem testSystem = new HtmlSlimTestSystem("slim", slimClient, new SlimTableFactory(), customComparatorRegistry);
    // testSystem.addTestSystemListener(this);

    // run test.
    StringWriter stringWriter = new StringWriter();
    SuiteHtmlFormatter htmlFormatter = new SuiteHtmlFormatter(wikiPage, stringWriter);
    testSystem.addTestSystemListener(htmlFormatter);
    
    HistoryWriterFactory source = new HistoryWriterFactory();
    
    File testHistoryDirectory = new File("./FitNesseRoot/files/testResults/");
    
    PageFactory pageFactory = new PageFactory(new File("."), "./");
    TestXmlFormatter2 testXmlFormatter = new TestXmlFormatter2(testHistoryDirectory, pageFactory, wikiPage, source);
    testSystem.addTestSystemListener(testXmlFormatter);
    
    testSystem.start();
    testSystem.runTests(testPage);
    testXmlFormatter.close();
    testSystem.bye();
    
    System.out.println(qualifiedPageName);
    HtmlPage htmlPage = pageFactory.newPage();
    htmlPage.setTitle(testPage.getName() + ": " + testPage);
    htmlPage.setPageTitle(new PageTitle(qualifiedPageName, qualifiedPageName));
    htmlPage.setNavTemplate("testNav.vm");
    htmlPage.put("actions", new WikiPageActions(wikiPage));
    htmlPage.setMainTemplate("testPage");
    htmlPage.put("testExecutor", new TestExecutor());
    htmlPage.setFooterTemplate("wikiFooter.vm");
    htmlPage.put("headerContent", WikiPageUtil.getHeaderPageHtml(wikiPage));
    htmlPage.put("footerContent", WikiPageUtil.getFooterPageHtml(wikiPage));
    htmlPage.setErrorNavTemplate("errorNavigator");
    
    // save test results?
    // render page / build html

    return htmlPage.html() + stringWriter.toString();
  }
  
  public class TestExecutor {
    public void execute() {
    }
  }

  public WikiPage createPage(String qualifiedPageName, AddChildPageResponder2 pageResponder, Map<String, String> params)
      throws Exception {

    WikiPage rootPage = loadRootPageFromFileSystem();
    WikiPage wikiPage = pageResponder.createPage(rootPage.getPageCrawler(), qualifiedPageName, params);

    return wikiPage;
  }

  public String showPage(String qualifiedPageName, WikiPageResponder pageResponder) {

    WikiPage page = loadPageFromFileSystem(qualifiedPageName);
    PageFactory pageFactory = new PageFactory(new File("."), "./");

    String html = pageResponder.makeHtml(pageFactory, page);

    return html;
  }

  public String showEditPage(String qualifiedPageName, EditResponder2 pageResponder) {

    WikiPage currentPage = loadPageFromFileSystem(qualifiedPageName);

    PageFactory pageFactory = new PageFactory(new File("."), "./");

    String html = pageResponder.makeHtml(currentPage, pageFactory);
    return html;
  }

  public String showCreatePage(String qualifiedPageName, NewPageResponder2 pageResponder, Map<String, String> params) {

    WikiPage rootPage = loadRootPageFromFileSystem();

    PageFactory pageFactory = new PageFactory(new File("."), "./");

    String html = pageResponder.makeHtml(pageFactory, qualifiedPageName, rootPage.getPageCrawler(), params);
    return html;
  }

  public WikiPage updatePage(String qualifiedPageName, SaveResponder2 saveResponder2, Map<String, String> params) {

    WikiPage currentPage = loadPageFromFileSystem(qualifiedPageName);

    saveResponder2.updatePage(currentPage, params);
    return currentPage;
  }

  private WikiPage loadPageFromFileSystem(String qualifiedPageName) {
    WikiPage rootPage = loadRootPageFromFileSystem();

    WikiPagePath currentPagePath = PathParser.parse(qualifiedPageName);
    WikiPage currentPage = rootPage.getPageCrawler().getPage(currentPagePath);
    return currentPage;
  }

  private WikiPage loadRootPageFromFileSystem() {
    FileSystemPageFactory wikiPageFactory = new FileSystemPageFactory(new DiskFileSystem(),
        new ZipFileVersionsController());

    SystemVariableSource variableSource = new SystemVariableSource();
    WikiPage rootPage = wikiPageFactory.makePage(new File(".", "FitNesseRoot"), "FitNesseRoot", null, variableSource);
    return rootPage;
  }

}

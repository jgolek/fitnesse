package vertx;

import java.io.File;
import java.util.Map;

import fitnesse.html.template.PageFactory;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.SystemVariableSource;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;
import fitnesse.wiki.fs.DiskFileSystem;
import fitnesse.wiki.fs.FileSystemPageFactory;
import fitnesse.wiki.fs.ZipFileVersionsController;

public class PageWorkflow {
    
    public WikiPage createPage(String qualifiedPageName, AddChildPageResponder2 pageResponder, Map<String, String> params) throws Exception {
        
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
      FileSystemPageFactory wikiPageFactory = new FileSystemPageFactory(new DiskFileSystem(), new ZipFileVersionsController());
    
      SystemVariableSource variableSource = new SystemVariableSource();
      WikiPage rootPage = wikiPageFactory.makePage(new File(".", "FitNesseRoot"), "FitNesseRoot", null, variableSource);
      return rootPage;
    }

    
}

package vertx;

import java.io.File;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import fitnesse.html.template.PageFactory;
import fitnesse.wiki.SystemVariableSource;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.fs.DiskFileSystem;
import fitnesse.wiki.fs.FileSystemPageFactory;
import fitnesse.wiki.fs.ZipFileVersionsController;

public class PageWorkflow {
    
    public WikiPage createPage(String qualifiedPageName, AddChildPageResponder2 pageResponder, Map<String, String> params) throws Exception {
        
        FileSystemPageFactory wikiPageFactory = new FileSystemPageFactory(new DiskFileSystem(), new ZipFileVersionsController());
       
        SystemVariableSource variableSource = new SystemVariableSource();
        WikiPage rootPage = wikiPageFactory.makePage(new File(".", "FitNesseRoot"), "FitNesseRoot", null, variableSource );
        
        WikiPage wikiPage = pageResponder.createPage(rootPage.getPageCrawler(), qualifiedPageName, params);

        return wikiPage;
    }
    

    public String run(String qualifiedPageName, WikiPageResponder pageResponder) {
        
        FileSystemPageFactory wikiPageFactory = new FileSystemPageFactory(new DiskFileSystem(), new ZipFileVersionsController());
       
        SystemVariableSource variableSource = new SystemVariableSource();
        WikiPage rootPage = wikiPageFactory.makePage(new File(".", "FitNesseRoot"), "FitNesseRoot", null, variableSource );
        
        String[] split = StringUtils.split(qualifiedPageName, ".");
        WikiPage page = null;
        WikiPage parentPage = rootPage;
        String parentFile = "./"+parentPage.getName();
        for (String name : split) {
          page = wikiPageFactory.makePage(new File(parentFile, name), name, parentPage, variableSource);
          parentPage = page;
          parentFile = parentFile + "/" + name;
        }
        
        PageFactory pageFactory = new PageFactory(new File("."), "./");
        
        
        String html = pageResponder.makeHtml(pageFactory, page);

        return html;
    }

    public String run(String qualifiedPageName, EditResponder2 pageResponder) {
        
        FileSystemPageFactory wikiPageFactory = new FileSystemPageFactory(new DiskFileSystem(), new ZipFileVersionsController());
       
        SystemVariableSource variableSource = new SystemVariableSource();
        WikiPage rootPage = wikiPageFactory.makePage(new File(".", "FitNesseRoot"), "FitNesseRoot", null, variableSource );
        
        String[] split = StringUtils.split(qualifiedPageName, ".");
        WikiPage page = null;
        WikiPage parentPage = rootPage;
        String parentFile = "./"+parentPage.getName();
        for (String name : split) {
          page = wikiPageFactory.makePage(new File(parentFile, name), name, parentPage, variableSource);
          parentPage = page;
          parentFile = parentFile + "/" + name;
        }
        
        PageFactory pageFactory = new PageFactory(new File("."), "./");
     
        String html = pageResponder.makeHtml(page, pageFactory);
        return html;
    }
    
    public String run(String qualifiedPageName, NewPageResponder2 pageResponder, Map<String, String> params) {
        
        FileSystemPageFactory wikiPageFactory = new FileSystemPageFactory(new DiskFileSystem(), new ZipFileVersionsController());
       
        SystemVariableSource variableSource = new SystemVariableSource();
        WikiPage rootPage = wikiPageFactory.makePage(new File(".", "FitNesseRoot"), "FitNesseRoot", null, variableSource );
        
        
        PageFactory pageFactory = new PageFactory(new File("."), "./");
     
        String html = pageResponder.makeHtml(pageFactory, qualifiedPageName, rootPage.getPageCrawler(), params);
        return html;
    }

    
}

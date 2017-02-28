package vertx;

import java.io.File;

import org.apache.commons.lang.StringUtils;

import fitnesse.html.template.PageFactory;
import fitnesse.wiki.SystemVariableSource;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.fs.DiskFileSystem;
import fitnesse.wiki.fs.FileSystemPageFactory;
import fitnesse.wiki.fs.ZipFileVersionsController;

public class PageWorkflow {

    public String run(String qualifiedPageName) {
        
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
        
        
        WikiPageResponder pageResponder = new WikiPageResponder();
        String html = pageResponder.makeHtml(pageFactory, page);

        return html;
    }

}

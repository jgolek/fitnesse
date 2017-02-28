package vertx;

import java.io.File;

import fitnesse.html.template.PageFactory;
import fitnesse.wiki.SystemVariableSource;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.fs.DiskFileSystem;
import fitnesse.wiki.fs.FileSystemPageFactory;
import fitnesse.wiki.fs.ZipFileVersionsController;

public class PageWorkflow {

    public String run() {
        
        FileSystemPageFactory wikiPageFactory = new FileSystemPageFactory(new DiskFileSystem(), new ZipFileVersionsController());
       
        SystemVariableSource variableSource = new SystemVariableSource();
        WikiPage rootPage = wikiPageFactory.makePage(new File(".", "FitNesseRoot"), "FitNesseRoot", null, variableSource );
        WikiPage page = wikiPageFactory.makePage(new File("./FitNesseRoot", "FrontPage"), "FrontPage", rootPage, variableSource);
        
        PageFactory pageFactory = new PageFactory(new File("."), "./");
        
        WikiPageResponder pageResponder = new WikiPageResponder();
        String html = pageResponder.makeHtml(pageFactory, page);

        return html;
    }

}

package vertx;

import java.io.File;

import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PageCrawlerImpl;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;
import fitnesse.wiki.fs.FileSystemPageFactory;

public class PageWorkflow {

    public Page run() {
        
        FileSystemPageFactory pageFactory = new FileSystemPageFactory();
        WikiPage rootPage = pageFactory.makePage(new File("./"), "rootPage", null, null);
        

        String pageName = "FrontPage";
        // page workflow
        WikiPagePath path = PathParser.parse(pageName);
        PageCrawler crawler = new PageCrawlerImpl(rootPage);
        crawler.getPage(path);
        WikiPage page = crawler.getPage(path);
        
        
        
        
        
        return null;
    }

}

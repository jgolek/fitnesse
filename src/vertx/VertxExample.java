package vertx;

import fitnesse.html.template.PageFactory;
import fitnesse.http.Request;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PageCrawlerImpl;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageDummy;
import fitnesse.wiki.WikiPagePath;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.StaticHandler;

public class VertxExample {

    private static WikiPage page2;

public static void main(String[] args) {

        Vertx vertx = Vertx.vertx();

        HttpServer server = vertx.createHttpServer();

        Router router = Router.router(vertx);
        router.route("/static/*").handler(StaticHandler.create());
        
        router.route("/").handler(context -> {

            // This handler gets called for each request that arrives on the server
            HttpServerResponse response = context.response();
            response.putHeader("content-type", "text/plain");
            
            String pageName = "FrontPage";
            // page workflow
            WikiPagePath path = PathParser.parse(pageName);
            PageCrawler crawler = new PageCrawlerImpl(new WikiPageDummy());
            crawler.getPage(path);
            page2 = crawler.getPage(path);
            
            WikiPageResponder pageResponder = new WikiPageResponder();
            
            RoutingContext context2 = context;
            WikiPage page;
            PageFactory pageFactory = new PageFactory(null);
            
            pageResponder.makeHtml(pageFactory, page);

            // Write to the response and end it
            response.end("Hello World");
        });

        server.requestHandler(router::accept).listen(8080);

    }

}

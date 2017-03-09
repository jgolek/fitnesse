package vertx;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;

import vertx.responders.AddChildPageResponder2;
import vertx.responders.EditResponder2;
import vertx.responders.NewPageResponder2;
import vertx.responders.SaveResponder2;
import vertx.responders.WikiPageResponder;

import com.google.common.collect.Maps;

import fitnesse.wiki.WikiPage;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;

public class VertxExample {


    public static void main(String[] args) {

        Vertx vertx = Vertx.vertx();

        HttpServer server = vertx.createHttpServer();

        Router router = Router.router(vertx);
        router.route("/files/*").handler(StaticHandler.create("./FitNesseRoot/files/"));

        router.route("/").handler(context -> {
            context.reroute("/FrontPage");
        });
        
        router.route(HttpMethod.POST, "/*").handler(BodyHandler.create());
        
        router.route(HttpMethod.POST, "/:rootPageName").handler(context -> {

            System.out.println(context.request().method());
            System.out.println(context.request().absoluteURI());
            String qualifiedPageName = context.request().getParam("rootPageName");
            System.out.println(qualifiedPageName);
            
            Map<String, String> parameters = toQueryMap(context.request().query());
            parameters.putAll(toQueryMap(context.getBodyAsString()));
            String bodyAsString = context.getBodyAsString();
            System.out.println(bodyAsString);
            
            PageWorkflow pageWorkflow = new PageWorkflow();

            String responderClass = parameters.get("responder");
            if ("addChild".equals(responderClass) || parameters.containsKey("addChild")) {
                AddChildPageResponder2 pageResponder = new AddChildPageResponder2();
                try {
                    WikiPage newPage = pageWorkflow.createPage(qualifiedPageName, pageResponder, parameters);

                    context.reroute(HttpMethod.GET, "/" +qualifiedPageName+"." + newPage.getName());

                } catch (Exception e) {
                    System.out.println("exc r");
                    throw new RuntimeException(e);
                }
            }else
            if ("saveData".equals(responderClass) || parameters.containsKey("saveData")) {    
                SaveResponder2 saveResonder = new SaveResponder2();
                 pageWorkflow.updatePage(qualifiedPageName, saveResonder, parameters);
                
                context.reroute(HttpMethod.GET, "/" +qualifiedPageName);
            } else {
                System.out.println("exc");
                throw new UnsupportedOperationException("Unsupported post request");
            }

        });


        router.route(HttpMethod.GET, "/:pageName").handler(context -> {

            System.out.println(context.request().method());
            System.out.println(context.request().absoluteURI());

            String qualifiedPageName = context.request().getParam("pageName");

            Map<String, String> queryParameters = toQueryMap(context.request().query());

              
            PageWorkflow pageWorkflow = new PageWorkflow();
            String html = null;
            if (queryParameters.containsKey("new")) {
                NewPageResponder2 pageResponder = new NewPageResponder2();
                html = pageWorkflow.showCreatePage(qualifiedPageName, pageResponder, queryParameters);
            } else 
            if (queryParameters.containsKey("edit")) {
                EditResponder2 pageResponder = new EditResponder2();
                html = pageWorkflow.showEditPage(qualifiedPageName, pageResponder);
            } else 
            if (queryParameters.containsKey("test")) {
              try {
                html = pageWorkflow.runTest(qualifiedPageName, queryParameters);
              } catch (Exception e) {
                e.printStackTrace();
                html = e.getMessage();
              }
            } 
            else {              
                WikiPageResponder pageResponder = new WikiPageResponder();
                html = pageWorkflow.showPage(qualifiedPageName, pageResponder);
            }


            HttpServerResponse response = context.response();
            response.putHeader("content-type", "text/html");
            response.end(html);
        });

        server.requestHandler(router::accept).listen(8082);
    }

    private static Map<String, String> toQueryMap(String queryString) {
        if (queryString == null) {
            return Maps.newHashMap();
        }

        Map<String, String> queryPairs = Maps.newHashMap();

        String[] pairs = queryString.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            if (idx == -1) {
                queryPairs.put(pair, "");
                System.out.println(pair);
            } else {
                try {
                    String key = URLDecoder.decode(pair.substring(0, idx), "UTF-8");
                    String value = URLDecoder.decode(pair.substring(idx + 1), "UTF-8");
                    System.out.println(key + " " + value);
                    queryPairs.put(key, value);
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return queryPairs;
    }

}

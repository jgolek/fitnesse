package vertx;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;

import com.google.common.collect.Maps;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;

public class VertxExample {


  public static void main(String[] args) {

    Vertx vertx = Vertx.vertx();

    HttpServer server = vertx.createHttpServer();

    Router router = Router.router(vertx);
    router.route("/files/*").handler(StaticHandler.create("./FitNesseRoot/files/"));

    router.route("/").handler(context ->{
        context.reroute("/FrontPage");
    });
    
    router.route(HttpMethod.POST, "/:pageName").handler(context -> {
        
        System.out.println(context.request().method());
        System.out.println(context.request().absoluteURI());
        


        HttpServerResponse response = context.response();
        response.putHeader("content-type", "text/html");
        response.end("demo");
      });

    
    router.route(HttpMethod.GET, "/:pageName").handler(context -> {
        
        System.out.println(context.request().method());
        System.out.println(context.request().absoluteURI());
        
        String qualifiedPageName = context.request().getParam("pageName");
        
        Map<String, String> queryParameters = toQueryMap(context.request().query());

        PageWorkflow pageWorkflow = new PageWorkflow();
        String html = null;
        if(queryParameters.containsKey("new")){
            NewPageResponder2 pageResponder = new NewPageResponder2();
            html = pageWorkflow.run(qualifiedPageName, pageResponder, queryParameters);            
        }else
        if(queryParameters.containsKey("edit")){
            EditResponder2 pageResponder = new EditResponder2();
            html = pageWorkflow.run(qualifiedPageName, pageResponder);            
        }else{
            WikiPageResponder pageResponder = new WikiPageResponder();
            html = pageWorkflow.run(qualifiedPageName, pageResponder);
        }
        

        HttpServerResponse response = context.response();
        response.putHeader("content-type", "text/html");
        response.end(html);
      });

    server.requestHandler(router::accept).listen(8080);
  }

  private static Map<String, String> toQueryMap(String queryString) {
        if(queryString == null){
            return Maps.newHashMap();
        }
        
        Map<String, String> queryPairs = Maps.newHashMap();
        
        String[] pairs = queryString.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            if(idx == -1){
                queryPairs.put(pair, "");
            }else{
                try {
                    queryPairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return queryPairs;
 }

}

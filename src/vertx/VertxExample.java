package vertx;

import io.vertx.core.Vertx;
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

    router.route("/").handler(context -> {

      // This handler gets called for each request that arrives on the server
        HttpServerResponse response = context.response();
        response.putHeader("content-type", "text/html");

        String html = new PageWorkflow().run();

        // Write to the response and end it
        response.end(html);
      });

    server.requestHandler(router::accept).listen(8080);

  }

}

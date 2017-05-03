package vertx;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.templ.FreeMarkerTemplateEngine;

public class Main {

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        HttpServer server = vertx.createHttpServer();
        Router router = Router.router(vertx);

        FreeMarkerTemplateEngine engine = FreeMarkerTemplateEngine.create();
        engine.setMaxCacheSize(0);

        router.route("/").handler(ctx -> {

            engine.render(ctx, "templates/index.ftl", res -> {
                if (res.succeeded()) {
                    HttpServerResponse response = ctx.response();
                    response.putHeader("content-type", "text/html");
                    response.end(res.result());
                } else {
                    ctx.fail(res.cause());
                }
            });
        });

        server.requestHandler(router::accept).listen(8080);
        System.out.println("Server is running!");
    }
    
    
}

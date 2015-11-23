package com.jimwang.webserver;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * A typical verticle to be executed on the event loop.  We are advised to never block
 * the event loop
 *
 * Created by jimwang on 17/10/2015.
 */
public class Server extends AbstractVerticle {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private final int port = 10000;

    @Override
    public void start() throws Exception {

        Vertx vertx = Vertx.vertx();
        Router router = Router.router(vertx);

        StaticHandler staticHandler = StaticHandler.create();
        staticHandler.setWebRoot("src/main/webapp");

        router.route().path("/").handler(staticHandler);

        router.route().path("/hello").
                handler(routingContext -> {
                    routingContext.response().
                            putHeader("content-type", "text/plain").
                            end("Hello");
                });

        /** REST handlers that use the EventBus **/
        EventBus bus = vertx.eventBus();

        router.route().path("/eventbus").handler(routingContect -> {
            bus.send("webapp.eventbus", "Some data from the REST request", reply -> {
                routingContect.response().end(reply.result().body().toString());
            });
        });

        bus.consumer("webapp.eventbus", message -> {
            logger.info("Received message off bus: {}", message.body().toString());
            // Doing something with the message body
            message.reply("Hello from the eventBus handler!");
        });

        HttpServer httpServer = vertx.createHttpServer();
        httpServer.requestHandler(router::accept).listen(port);
        logger.info("Listening on port {} ...", port);
    }

    public static void main(String[] args) {
        Server server = new Server();
        try {
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}

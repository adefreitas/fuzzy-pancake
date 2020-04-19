package se.kry.codetest;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;

import java.util.List;

public class MainVerticle extends AbstractVerticle {
  private DBConnector connector;
  private BackgroundPoller poller = new BackgroundPoller();

  @Override
  public void start(Future<Void> startFuture) {
    connector = new DBConnector(vertx);
    Router router = Router.router(vertx);
    router.route().handler(BodyHandler.create());
    connector.create("https://www.kry.se", "UNKNOWN", null);
    connector.create("https://www.google.com/", "UNKNOWN", null);
    vertx.setPeriodic(1000 * 60, timerId -> poller.pollServices(connector));
    setRoutes(router);
    vertx
            .createHttpServer()
            .requestHandler(router)
            .listen(8080, result -> {
              if (result.succeeded()) {
                System.out.println("KRY code test service started");
                startFuture.complete();
              } else {
                startFuture.fail(result.cause());
              }
            });
  }

  private void setRoutes(Router router){
    router.errorHandler(500, rc -> {
      System.err.println("Handling failure");
      Throwable failure = rc.failure();
      if (failure != null) {
        failure.printStackTrace();
      }
    });

    router.route("/*").handler(StaticHandler.create());

    router.get("/service").handler(req -> {
      connector.getAll().setHandler(handler -> {
        if (handler.succeeded()) {
          List<JsonObject> jsonServices = handler.result().getRows();
          req.response()
                  .putHeader("content-type", "application/json")
                  .end(new JsonArray(jsonServices).encode());
        } else {
          req.fail(500);
        }
      });
    });

    router.post("/service").handler(req -> {
      JsonObject jsonBody = req.getBodyAsJson();
      connector.create(jsonBody.getString("url"), "UNKNOWN", jsonBody.getString("name")).setHandler(handler -> {
        if (handler.succeeded()) {
          req.response()
                  .putHeader("content-type", "text/plain")
                  .end("OK");
        } else {
          req.fail(500);
        }
      });
    });

    router.delete("/service").handler(req -> {
      JsonObject jsonBody = req.getBodyAsJson();
      connector.deleteByUrl(jsonBody.getString("url")).setHandler(handler -> {
        if (handler.succeeded()) {
          req.response()
                  .putHeader("content-type", "text/plain")
                  .end("OK");
        } else {
          req.fail(500);
        }
      });
    });
  }

}




package se.kry.codetest;

import io.vertx.core.json.JsonObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.List;

public class BackgroundPoller {

  public void pollServices(DBConnector connector) {
    connector.getAll().setHandler(handler -> {
      if (handler.succeeded()) {
        List<JsonObject> jsonServices = handler.result().getRows();
        jsonServices.forEach((service) -> {
          String serviceUrl = service.getString("url");
          String status = "UNKNOWN";
          try {
            URL serviceAddress = new URL(serviceUrl);
            HttpURLConnection connection = (HttpURLConnection) serviceAddress.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.connect();
            status = connection.getResponseCode() < 300 ? "AVAILABLE" : "UNAVAILABLE";
          } catch (MalformedURLException e) {
            e.printStackTrace();
          } catch (ProtocolException e) {
            e.printStackTrace();
          } catch (IOException e) {
            e.printStackTrace();
          }
          connector.updateStatusByUrl(serviceUrl, status);
        });
      } else {
        System.out.println("BackgroundPoller: Couldn't connect to the DB");
      }
    });
  }
}

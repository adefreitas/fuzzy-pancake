package se.kry.codetest;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLClient;

import java.util.Date;


public class DBConnector {

  private final String DB_PATH = "poller.db";
  private final SQLClient client;

  public DBConnector(Vertx vertx){
    JsonObject config = new JsonObject()
            .put("url", "jdbc:sqlite:" + DB_PATH)
            .put("driver_class", "org.sqlite.JDBC")
            .put("max_pool_size", 30);

    client = JDBCClient.createShared(vertx, config);
  }

  public Future<ResultSet> query(String query) {
    return query(query, new JsonArray());
  }


  public Future<ResultSet> query(String query, JsonArray params) {
    if(query == null || query.isEmpty()) {
      return Future.failedFuture("Query is null or empty");
    }
    if(!query.endsWith(";")) {
      query = query + ";";
    }

    Future<ResultSet> queryResultFuture = Future.future();

    client.queryWithParams(query, params, result -> {
      if(result.failed()){
        System.err.println("Error executing query");
        System.err.println(result.cause().toString());

        queryResultFuture.fail(result.cause());
      } else {
        System.out.println("Success fetching results");
        queryResultFuture.complete(result.result());
      }
    });
    return queryResultFuture;
  }

  public Future<ResultSet> deleteById(Integer id) {
    if(id == null ) {
      return Future.failedFuture("ID is null");
    }
    return this.query("DELETE FROM service WHERE id = ?;", new JsonArray().add(id));
  }

  public Future<ResultSet> getById(Integer id) {
    if(id == null) {
      return Future.failedFuture("id is null or empty");
    }
    return this.query("SELECT TOP 1 * FROM service WHERE id = ?;", new JsonArray().add(id));
  }

  public Future<ResultSet> updateById(Integer id, String name, String url, String status) {
    if (id == null) {
      return Future.failedFuture("ID is null");
    }
    if(name == null || name.isEmpty()) {
      return Future.failedFuture("Name is null or empty");
    }
    if(url == null || url.isEmpty()) {
      return Future.failedFuture("URL is null or empty");
    }
    if (status == null || status.isEmpty()) {
      return Future.failedFuture("Status is null or empty");
    }
    return this.query("UPDATE service SET url = ?, status = ?, name = ? WHERE id = ?;", new JsonArray().add(url).add(status).add(name).add(id));
  }

  public Future<ResultSet> create(String url, String status, String name) {
    if(url == null || url.isEmpty()) {
      System.err.println("Error creating");
      return Future.failedFuture("URL is null or empty");
    }
    System.out.println("Creating new service");
    return this.query(
            "INSERT INTO service (url, name, status, added_at) values (?, ?, ?, ?);",
            new JsonArray()
                    .add(url)
                    .add(name)
                    .add(status)
                    .add(new Date().toString()));
  }

  public Future<ResultSet> getAll() {
    Future<ResultSet> queryResultFuture = Future.future();

    client.query("SELECT * FROM service;", result -> {
      if(result.failed()){
        System.err.println("Error getting all services");
        System.err.println(result.cause().toString());

        queryResultFuture.fail(result.cause());
      } else {
        System.out.println("Success getting all services");
        queryResultFuture.complete(result.result());
      }
    });
    return queryResultFuture;
  }
}

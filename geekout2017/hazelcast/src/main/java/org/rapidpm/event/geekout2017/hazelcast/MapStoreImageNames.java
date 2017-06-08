package org.rapidpm.event.geekout2017.hazelcast;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.hazelcast.core.MapStore;
import com.zaxxer.hikari.HikariDataSource;

/**
 *
 */
public class MapStoreImageNames implements MapStore<String, String> {

  private final HikariDataSource db;

  public MapStoreImageNames(HikariDataSource db) {
    this.db = db;
  }

  @Override
  public void store(String key, String value) {
    //(username, activated, passwd)
//    VALUES ('sven.ruppert', TRUE, 'passwd');
    try {
      final Connection connection = db.getConnection();
      connection.createStatement().executeUpdate("INSERT INTO geekoutdb.imagenames (filename) VALUES ('" + value + "')  ");
      db.evictConnection(connection);
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void storeAll(Map<String, String> map) {
    map.forEach(this::store);
  }

  @Override
  public void delete(String key) {
    System.out.println("delete.key = " + key);
  }

  @Override
  public void deleteAll(Collection<String> keys) {
    System.out.println("deleteAll.keys = " + keys);
  }

  @Override
  public String load(String key) {
    System.out.println("load.key = " + key);
    try {
      final Connection connection = db.getConnection();
      ResultSet resultSet = connection.createStatement().executeQuery("SELECT filename FROM geekoutdb.imagenames WHERE filename='" + key + "'");
      db.evictConnection(connection);

      while (resultSet.next()) {
        return resultSet.getString("filename");
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return null;
  }

  @Override
  public Map<String, String> loadAll(Collection<String> keys) {
    return keys.stream()
               .map(this::load)
               .filter(Objects::nonNull)
               .collect(Collectors.toMap(e -> e, e -> e));
  }

  @Override
  public Iterable<String> loadAllKeys() {
    try {
      final Connection connection = db.getConnection();
      ResultSet resultSet = connection.createStatement().executeQuery("SELECT filename FROM geekoutdb.imagenames");
      db.evictConnection(connection);
      List<String> result = new ArrayList<>();
      while (resultSet.next()) {
        result.add(resultSet.getString("filename"));
      }
      return result;
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return Collections.emptyList();
  }
}

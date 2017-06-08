package org.rapidpm.event.geekout2017.hazelcast;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.rapidpm.frp.model.Pair;
import com.hazelcast.core.MapStore;
import com.zaxxer.hikari.HikariDataSource;

/**
 *
 */
class MapStoreImages implements MapStore<String, byte[]> {

  private final char[] hexArray;
  private final HikariDataSource db;

  public MapStoreImages(HikariDataSource db) {
    this.db = db;
    hexArray = "0123456789ABCDEF".toCharArray();
  }

  private String bytesToHex(byte[] bytes) {
    StringBuilder sb = new StringBuilder();
    for (byte aByte : bytes) {
      int v = aByte & 0xFF;
      sb.append("\\x")
        .append(hexArray[v >>> 4])
        .append(hexArray[v & 0x0F]);
    }
    return sb.toString();
  }

  @Override
  public byte[] load(String key) {
    System.out.println("load.key = " + key);
    try {

      final Connection connection = db.getConnection();
      ResultSet resultSet = connection.createStatement().executeQuery("SELECT * FROM geekoutdb.images \n"
                                                                              + "WHERE filename = '" + key + "'\n"
                                                                              + "ORDER BY partID;");

      db.evictConnection(connection);

      List<byte[]> imagesParts = new ArrayList<>();
      while (resultSet.next()) {
        byte[] parts = resultSet.getBytes("part");
        imagesParts.add(parts);
      }

      return Main.mergeImage(imagesParts).toByteArray();
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return new byte[0];
  }

  @Override
  public Map<String, byte[]> loadAll(Collection<String> keys) {
    System.out.println("loadAll.keys = " + keys);
    return keys.parallelStream()
               .map((String key) -> new Pair<>(key, load(key)))
               .collect(Collectors.toMap(Pair::getT1, Pair::getT2));
  }

  @Override
  public Iterable<String> loadAllKeys() {

//    try {
//      ResultSet resultSet = db.createStatement().executeQuery("SELECT DISTINCT filename FROM geekoutdb.images ");
//      List<String> result = new ArrayList<>();
//      while (resultSet.next()) {
//        result.add(resultSet.getString("filename"));
//      }
//      return result;
//    } catch (SQLException e) {
//      e.printStackTrace();
//    }
//    return Collections.emptyList();
    return null; // lazy start
  }

  @Override
  public void store(String key, byte[] value) {
    System.out.println("key = " + key);
    System.out.println("value.length = " + value.length);
    List<byte[]> bytes = Main.splitImage(value, Main.BLOCK_SIZE);

    for (int i = 0; i < bytes.size(); i++) {
      byte[] aByte = bytes.get(i);
      String partAsHex = bytesToHex(aByte);

      try {
        final Connection connection = db.getConnection();
        connection.createStatement().execute("INSERT INTO geekoutdb.images\n"
                                     + "(filename, partID, part)\n"
                                     + "    VALUES ('" + key + "' , " + i + ", b'" + partAsHex + "');");
        db.evictConnection(connection);
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }

  }

  @Override
  public void storeAll(Map<String, byte[]> map) {
    map.forEach((key, value) -> {
      System.out.println("key = " + key);
      System.out.println("value.length = " + value.length);
      store(key, value);
    });
  }

  @Override
  public void delete(String key) {
    System.out.println("delete.key = " + key);
    try {
      final Connection connection = db.getConnection();
      connection.createStatement().execute("DELETE FROM geekoutdb.images WHERE filename = '" + key + "'");
      db.evictConnection(connection);

    } catch (SQLException e) {
      e.printStackTrace();
    }


  }

  @Override
  public void deleteAll(Collection<String> keys) {
    keys.forEach(this::delete);
  }
}

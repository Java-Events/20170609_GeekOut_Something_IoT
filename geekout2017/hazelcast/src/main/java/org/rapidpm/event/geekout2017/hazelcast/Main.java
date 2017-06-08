package org.rapidpm.event.geekout2017.hazelcast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.rapidpm.event.geekout2017.api.model.tinkerforge.SensorData;
import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MapStoreConfig;
import com.hazelcast.config.MulticastConfig;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.config.TcpIpConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.ITopic;
import com.zaxxer.hikari.HikariDataSource;

/**
 * easy file system , one folder for images -> HZDFS
 */
public class Main {

  public static final int BLOCK_SIZE = (62 * 1024);
//  public static final String IP = "localhost";
//  public static final String IP = "51.15.47.148";

  public static void main(String[] args) throws ClassNotFoundException, SQLException {

    System.out.println("args[0] = IP Address from a CockroachDB Node");

    // start JDBC for CockRoachDB
    // MapStore is single threaded per node


    Class.forName("org.postgresql.Driver");

    // Connect to the "bank" database.
    //jdbc:postgresql://localhost:26257/geekoutdb
//    final Connection db = DriverManager.getConnection("jdbc:postgresql://" + IP + ":26257/geekoutdb?sslmode=disable",
//                                                      "root", "");

    final HikariDataSource ds = new HikariDataSource();
    ds.setJdbcUrl("jdbc:postgresql://" + args[0] + ":26257/geekoutdb?sslmode=disable");
    ds.setUsername("root");
    ds.setPassword("");
    ds.setAutoCommit(true);

    Config cfg = new Config();
    cfg.addMapConfig(createMapConfigForImages(ds));
    cfg.addMapConfig(createMapConfigForImageNames(ds));


    NetworkConfig networkConfig = new NetworkConfig();
    JoinConfig joinConfig = new JoinConfig();
    TcpIpConfig tcpIpConfig = new TcpIpConfig();
    tcpIpConfig.setEnabled(true);
    tcpIpConfig.addMember("10.8.6.89");
    tcpIpConfig.addMember("10.8.38.139");
    tcpIpConfig.addMember("10.8.114.203");
    tcpIpConfig.addMember("10.8.32.29");
    joinConfig.setTcpIpConfig(tcpIpConfig);
    MulticastConfig multicastConfig = new MulticastConfig();
    multicastConfig.setEnabled(false);
    joinConfig.setMulticastConfig(multicastConfig);

    networkConfig.setJoin(joinConfig);
    cfg.setNetworkConfig(networkConfig);


    HazelcastInstance instance = Hazelcast.newHazelcastInstance(cfg);
    final ITopic<SensorData> tinkerforge = instance.getReliableTopic("tinkerforge");
    final ITopic<SensorData> reverse = instance.getReliableTopic("tinkerforge-reverse");

    tinkerforge.addMessageListener(message ->

                                   {
                                     if (message.getMessageObject() != null)
                                       reverse.publish(message.getMessageObject());
                                   });


    IMap<String, byte[]> images = instance.getMap("images");


  }

  private static MapConfig createMapConfigForImages(HikariDataSource db) {
    MapConfig mapConfig = new MapConfig();
    mapConfig.setName("images");
    mapConfig.setBackupCount(2);
    MapStoreConfig mapStoreConfig = new MapStoreConfig();
    mapStoreConfig.setEnabled(true);
    mapStoreConfig.setInitialLoadMode(MapStoreConfig.InitialLoadMode.LAZY);
    mapStoreConfig.setImplementation(new MapStoreImages(db));
    mapConfig.setMapStoreConfig(mapStoreConfig);
    return mapConfig;
  }

  private static MapConfig createMapConfigForImageNames(HikariDataSource db) {
    MapConfig mapConfig = new MapConfig();
    mapConfig.setName("images-names");
    mapConfig.setBackupCount(2);
    MapStoreConfig mapStoreConfig = new MapStoreConfig();
    mapStoreConfig.setEnabled(true);
    mapStoreConfig.setInitialLoadMode(MapStoreConfig.InitialLoadMode.LAZY);
    mapStoreConfig.setImplementation(new MapStoreImageNames(db));
    mapConfig.setMapStoreConfig(mapStoreConfig);
    return mapConfig;
  }

  //TODO refactor to functions

  public static ByteArrayOutputStream mergeImage(List<byte[]> chunkList) {
    final ByteArrayOutputStream target = new ByteArrayOutputStream();
    for (byte[] b : chunkList) {
      try {
        target.write(b);
      } catch (IOException e) {
        e.printStackTrace();
        return new ByteArrayOutputStream();
      }
    }
    return target;
  }

  //TODO refactor to functions
  public static List<byte[]> splitImage(byte[] data, int blockSize) {
    int blockCount = (data.length + blockSize - 1) / blockSize;

    List<byte[]> chunkList = new ArrayList<>();

    for (int i = 1; i < blockCount; i++) {
      int idx = (i - 1) * blockSize;
      byte[] chunk = Arrays.copyOfRange(data, idx, idx + blockSize);
      chunkList.add(chunk);
    }

    // Last chunk
    int end = - 1;
    if (data.length % blockSize == 0) {
      end = data.length;
    }
    else {
      end = data.length % blockSize + blockSize * (blockCount - 1);
    }

    byte[] chunk = Arrays.copyOfRange(data, (blockCount - 1) * blockSize, end);
    chunkList.add(chunk);
    return chunkList;
  }

}

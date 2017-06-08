package org.rapidpm.event.geekout2017.iot.tinkerforge;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;

/**
 *  Main to collect the weather data and write it into HZ cluster
 */
public class Main {


  public static void main(String[] args) {

    final HazelcastInstance hazelcastInstance = connectToHazelcast();

    //TODO constant "tinkerforge" extracting into API
    final ITopic<Object> tinkerforge = hazelcastInstance.getTopic("tinkerforge");

    //ToDo Message Class -> API
    //tinkerforge.publish();


    // connect to weather station



  }

  private static HazelcastInstance connectToHazelcast() {
    ClientConfig clientConfig = new ClientConfig();
    clientConfig.getGroupConfig().setName("dev").setPassword("dev-pass");
    clientConfig.getNetworkConfig().addAddress("10.90.0.1", "10.90.0.2:5702"); // TODO static IP´s

    final HazelcastInstance hazelcastInstance = HazelcastClient.newHazelcastClient(clientConfig);

    Runtime.getRuntime().addShutdownHook(new Thread(){
      @Override
      public void run() {
        hazelcastInstance.shutdown();
      }
    });

    return hazelcastInstance;
  }

}

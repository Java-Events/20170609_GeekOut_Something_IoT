package junit.org.rapidpm.event.geekout2017.hazelcast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.rapidpm.event.geekout2017.api.model.tinkerforge.SensorData;
import org.rapidpm.event.geekout2017.hazelcast.Main;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientNetworkConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.ITopic;

/**
 *
 */
@Ignore
public class Main001Test {

  @Rule public TestName testMethodName = new TestName();

  private HazelcastInstance client;

  @Before
  public void setUp() throws Exception {
    // startup of HZ cluster needed

    ClientConfig clientConfig = new ClientConfig();
    ClientNetworkConfig clientNetworkConfig = new ClientNetworkConfig();
    clientNetworkConfig.addAddress("127.0.0.1:5701");
    clientConfig.setNetworkConfig(clientNetworkConfig);
    client = HazelcastClient.newHazelcastClient(clientConfig);
  }

  @After
  public void tearDown() throws Exception {
    client.shutdown();
  }

  @Test
  public void test001() throws Exception {

    final SensorData messageOrig = new SensorData("master",
                                                  "brick",
                                                  - 1,
                                                  ZonedDateTime.now(),
                                                  100);

    final ITopic<SensorData> reverse = client.getReliableTopic("tinkerforge-reverse");
    reverse.addMessageListener(message -> Assert.assertEquals(messageOrig, message.getMessageObject()));

    final ITopic<SensorData> tinkerforge = client.getReliableTopic("tinkerforge");
    tinkerforge.publish(messageOrig);

  }


  @Test
  public void test002() throws Exception {
    final IMap<String, byte[]> images = client.getMap("images");
    String key = testMethodName.getMethodName();
    images.put(key, key.getBytes());
    byte[] bytes = images.get(key);
    Assert.assertEquals(key, new String(bytes).intern());
  }


  @Test
  public void test003() throws Exception {
    final IMap<String, byte[]> images = client.getMap("images");
    String key = testMethodName.getMethodName();


    ByteArrayOutputStream baos = readImage();

    images.put(key, baos.toByteArray());
    byte[] bytes = images.get(key);

    Assert.assertEquals(4808258, bytes.length);
    Assert.assertTrue(Arrays.equals(bytes, baos.toByteArray()));
  }

  private ByteArrayOutputStream readImage() throws IOException {
    ClassLoader classLoader = getClass().getClassLoader();
    InputStream is = classLoader.getResourceAsStream("data/images/expedition-50-soyuz-ms-02-landing-nhq201704100022_33588341010_o.jpg");
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    int reads = is.read();
    while (reads != - 1) {
      baos.write(reads);
      reads = is.read();
    }
    return baos;
  }

  private byte[] readImage(File image) throws IOException {
    return Files.readAllBytes(image.toPath());
  }


  @Test
  public void test004() throws Exception {

    ByteArrayOutputStream baos = readImage();
    byte[] data = baos.toByteArray();

    int blockSize = 64 * 1024; // 64kByte
    List<byte[]> chunkList = Main.splitImage(data, blockSize);

    Assert.assertEquals(74, chunkList.size());
    Assert.assertTrue(73 < (float) data.length / blockSize);
    Assert.assertTrue(74 > (float) data.length / blockSize);

    System.out.println("chunkList.size() = " + chunkList.size());
    System.out.println("data.length / blockSize = " + (float) data.length / blockSize);

    ByteArrayOutputStream target = Main.mergeImage(chunkList);

    byte[] bytes = target.toByteArray();
    Assert.assertEquals(data.length, bytes.length);
    Assert.assertTrue(Arrays.equals(data, bytes));

  }


  @Test
  public void test005() throws Exception {
    final IMap<String, byte[]> images = client.getMap("images");
    byte[] nothing = images.get("nothing");
    Assert.assertTrue(nothing != null);


    byte[] image = images.get("expedition-50-soyuz-ms-02-landing-nhq201704100022_33588341010_o.jpg");

    ByteArrayOutputStream baos = readImage();
    Assert.assertEquals(4808258, baos.size());
    byte[] a2 = baos.toByteArray();
    Assert.assertTrue(Arrays.equals(image, a2));
  }

  @Test
  @Ignore
  public void test006() throws Exception {
    String key = "expedition-50-soyuz-ms-02-landing-nhq201704100022_33588341010_o.jpg";

    final IMap<String, byte[]> images = client.getMap("images");

    ByteArrayOutputStream baos = readImage();
    images.put(key, baos.toByteArray());

    byte[] image = images.get(key);

    Assert.assertEquals(4808258, baos.size());
    Assert.assertTrue(Arrays.equals(image, baos.toByteArray()));
  }


  @Test
  public void test007() throws Exception {
    // load images into DB

    final IMap<String, byte[]> images = client.getMap("images");
    File imageDirectory = new File("./_data/images");
    File[] imageArray = imageDirectory.listFiles();

    for (final File image : imageArray) {
      byte[] imageFromDisc = readImage(image);
//      images.put(image.getName(), Files.readAllBytes(image.toPath()));
      byte[] bytes = images.get(image.getName());
      Assert.assertTrue(bytes.length > 0);
      Assert.assertEquals(imageFromDisc.length, bytes.length);
      Assert.assertTrue(Arrays.equals(imageFromDisc, bytes));
    }
  }

  @Test
  public void test008() throws Exception {
    // load images into DB

    final IMap<String, byte[]> images = client.getMap("images");
    final IMap<String, String> imageNames = client.getMap("images-names");

    images.keySet().forEach(key -> imageNames.put(key,key));
//    images.keySet().forEach(System.out::println);

    Assert.assertEquals(images.size(), imageNames.size());


  }
}

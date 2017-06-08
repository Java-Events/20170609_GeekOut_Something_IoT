package org.rapidpm.event.geekout2017.vaadin;

import static io.undertow.Handlers.redirect;
import static io.undertow.servlet.Servlets.servlet;

import java.io.ByteArrayInputStream;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientNetworkConfig;
import com.hazelcast.core.HazelcastInstance;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.event.selection.SingleSelectionListener;
import com.vaadin.server.StreamResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.handlers.PathHandler;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;

/**
 *
 */
public class Main {

  private static HazelcastInstance client;

  public static final String CONTEXT_PATH = "/";

  @WebServlet
  @VaadinServletConfiguration(productionMode = false, ui = MyUI.class)
  public static class MyProjectServlet extends VaadinServlet {}


  public static class MyUI extends UI {
    @Override
    protected void init(VaadinRequest request) {

      final HorizontalLayout mainLayout = new HorizontalLayout();
      mainLayout.setSizeFull();

      final TabSheet mainTabSheet = new TabSheet();
      mainTabSheet.setSizeFull();

      createWeatherTab(mainTabSheet);
      createImageOverviewTab(mainTabSheet);
      createImageUploadTab(mainTabSheet);
      mainLayout.addComponents(mainTabSheet);
      setContent(mainLayout);   // Attach to the UI
    }

    private void createWeatherTab(TabSheet mainTabSheet) {
      final VerticalLayout tabWeather = new VerticalLayout();
      tabWeather.setSizeFull();
      mainTabSheet.addTab(tabWeather, "Weatherstation");
    }

    private void createImageOverviewTab(TabSheet mainTabSheet) {
      final VerticalLayout tabImageOverview = new VerticalLayout();
      mainTabSheet.addTab(tabImageOverview, "Image Overview");

      Image image = new Image();
      ComboBox<String> filenames = new ComboBox<>();
      filenames.setHeight(30, Unit.PIXELS);

      Set<String> keySet = client.<String, String>getMap("images-names").keySet();
      filenames.setItems(keySet);
      filenames.addSelectionListener((SingleSelectionListener<String>) event ->
          event
              .getSelectedItem()
              .ifPresent((filename) -> {
                byte[] imageBytes = client
                    .<String, byte[]>getMap("images")
                    .get(filename);
                StreamResource.StreamSource streamSource = (StreamResource.StreamSource) () -> new ByteArrayInputStream(imageBytes);
                image.setSource(new StreamResource(streamSource, filename));
                image.markAsDirty();
              }));

      tabImageOverview.addComponent(filenames);
      Panel imagePanel = new Panel();
      imagePanel.setSizeFull();
      imagePanel.setContent(image);
      tabImageOverview.addComponent(imagePanel);
    }

    private void createImageUploadTab(TabSheet mainTabSheet) {
      final VerticalLayout tabImageUpload = new VerticalLayout();
      tabImageUpload.setSizeFull();
      mainTabSheet.addTab(tabImageUpload, "Image Upload");

    }


  }

  public static void main(String[] args) throws ServletException {

    ClientConfig clientConfig = new ClientConfig();
    ClientNetworkConfig clientNetworkConfig = new ClientNetworkConfig();
//    clientNetworkConfig.addAddress("127.0.0.1:5701");
    clientNetworkConfig.addAddress("51.15.79.35:5701");
    clientConfig.setNetworkConfig(clientNetworkConfig);
    client = HazelcastClient.newHazelcastClient(clientConfig);


    // grap it here
    // http://bit.ly/undertow-servlet-deploy
    DeploymentInfo servletBuilder = Servlets.deployment()
                                            .setClassLoader(Main.class.getClassLoader())
                                            .setContextPath(CONTEXT_PATH)
                                            .setDeploymentName("ROOT.war")
                                            .setDefaultEncoding("UTF-8")
                                            .addServlets(
                                                servlet(
                                                    MyProjectServlet.class.getSimpleName(),
                                                    MyProjectServlet.class)
                                                    .addMapping("/*"));

    DeploymentManager manager = Servlets
        .defaultContainer()
        .addDeployment(servletBuilder);

    manager.deploy();

    PathHandler path = Handlers.path(redirect(CONTEXT_PATH))
                               .addPrefixPath(CONTEXT_PATH, manager.start());

    Undertow undertowServer = Undertow.builder()
                                      .addHttpListener(8080, "0.0.0.0")
                                      .setHandler(path)
                                      .build();
    undertowServer.start();

  }


}

package org.jvnkr.blogbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BlogBackendApplication {

  public static void main(String[] args) {
    SpringApplication app = new SpringApplication(BlogBackendApplication.class);

    // Add a listener to set the server address dynamically
//    app.addListeners((ApplicationListener<ApplicationEnvironmentPreparedEvent>) event -> {
//      ConfigurableEnvironment environment = event.getEnvironment();
//      AppEnvironments appEnvironment = AppEnvironments.fromString(environment.getProperty("app.environment", "dev"));
//      String localAddress;
//
//      try {
//        if (appEnvironment.equals(AppEnvironments.PROD)) {
//          localAddress = NetworkAddressUtil.getLocalHostName(); // IPv4 Address
//        } else {
//          localAddress = "localhost";
//        }
//      } catch (UnknownHostException | SocketException e) {
//        throw new RuntimeException(e);
//      }
//      environment.getSystemProperties().put("server.address", localAddress);
//      System.out.println("Server will run on: " + localAddress);
//    });

    app.run(args);
  }
}
package simultan.team.cucumber.runner;

import io.netty.handler.codec.http.HttpScheme;
import java.io.Serializable;
import java.net.URI;
import java.util.Objects;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.http.HttpMethod;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.util.UriComponentsBuilder;

@Getter
@Validated
@ConfigurationProperties(prefix = "simultan.team.rest")
public class RestProperties {


  @NestedConfigurationProperty
  private Endpoint mongoEndpoint;

  @NestedConfigurationProperty
  private Endpoint rabbitEndpoint;

  public RestProperties() {
    this.mongoEndpoint = Endpoint.builder()
        .host("localhost")
        .path("get-data-mongo")
        .scheme(HttpScheme.HTTP)
        .method(HttpMethod.GET)
        .build();
    this.rabbitEndpoint = Endpoint.builder()
        .host("localhost")
        .path("get-data-rabbit")
        .scheme(HttpScheme.HTTP)
        .method(HttpMethod.GET)
        .build();
  }

  @Getter
  public static class Endpoint implements Serializable {
    HttpScheme scheme;
    HttpMethod method;
    String host;
    String path;
    String port;

    @lombok.Builder(builderClassName = "Builder", toBuilder = true)
    public Endpoint(HttpScheme scheme, HttpMethod method, String host, String path, String port) {
      this.scheme = scheme;
      this.method = method;
      this.host = host;
      this.path = path;
      this.port = port;
    }
  }


}

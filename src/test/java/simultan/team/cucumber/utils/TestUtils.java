package simultan.team.cucumber.utils;

import java.net.URI;
import java.util.Objects;
import org.springframework.web.util.UriComponentsBuilder;
import simultan.team.cucumber.runner.RestProperties.Endpoint;

public class TestUtils {

  public static URI setURI(Endpoint endpoint) {
    UriComponentsBuilder builder = UriComponentsBuilder.newInstance()
        .scheme(endpoint.getScheme().toString().toLowerCase())
        .host(endpoint.getHost())
        .path(endpoint.getPath());

    if(Objects.nonNull(endpoint.getPort())) {
      builder.port(endpoint.getPort());
    }

    return builder
        .build().toUri();
  }
}

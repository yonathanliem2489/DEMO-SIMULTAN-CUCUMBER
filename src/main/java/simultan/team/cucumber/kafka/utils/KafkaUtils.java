package simultan.team.cucumber.kafka.utils;

import java.nio.ByteBuffer;
import java.util.function.Function;

public class KafkaUtils {

  public static Function<String, ByteBuffer> cacheKey = request ->
      ByteBuffer.wrap("kafka.employee.".concat(request).getBytes());

}

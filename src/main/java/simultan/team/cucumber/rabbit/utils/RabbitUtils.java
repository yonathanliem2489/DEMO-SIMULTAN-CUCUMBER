package simultan.team.cucumber.rabbit.utils;

import java.nio.ByteBuffer;
import java.util.function.Function;

public class RabbitUtils {

  public static Function<String, ByteBuffer> cacheKey = request ->
      ByteBuffer.wrap("rabbit.employee.".concat(request).getBytes());

}

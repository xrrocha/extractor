package bvc.extractor;

import java.util.Map;
import java.util.stream.Stream;

public interface Extractor {
    Stream<Map<String, Object>> extraer();
}

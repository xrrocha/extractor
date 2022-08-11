package bvc.extractor;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.text.DecimalFormat;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ExtraccionTest {
    @Test
    public void extraeCorrectamente() {

        final Extractor extractor = () -> Stream.of(
                Map.of(
                    "id", 123,
                    "valor", 1_234_567_890.12
                ),
                Map.of(
                        "id", 234,
                        "valor", 2_345_678_901.23
                )
        );

        final var formatos = Map.of(
                "id", new DecimalFormat("###,###"),
                "valor", new DecimalFormat("###,###,###,###.##")
        );
        final Formateador formateador = registro ->
            Stream.of("id", "valor")
                    .map(nombre -> formatos.get(nombre).format(registro.get(nombre)))
                    .collect(Collectors.joining("\t"));

        final var resultado = new StringBuilder();
        final Diseminador diseminador = linea -> {
            resultado.append(linea);
            resultado.append("\n");
        };

        final var extraccion = new Extraccion(extractor, formateador, diseminador);
        extraccion.ejecutar();

        Assertions.assertEquals("""
                123\t1,234,567,890.12
                234\t2,345,678,901.23
                """.stripIndent(),
                resultado.toString());
    }
}

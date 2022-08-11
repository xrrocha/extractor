package bvc.extractor;

import java.util.stream.Stream;

record Extraccion(Extractor extractor, Formateador formateador, Diseminador diseminador) {
    public void ejecutar() {
        try {
            abrir();
            extractor.extraer()
                    .map(formateador::formatear)
                    .forEach(diseminador::grabar);
        } finally {
            cerrar();
        }
    }

    private void abrir() {
        Stream.of(extractor, diseminador).forEach(obj -> {
            if (obj instanceof CicloVida cv) {
                cv.abrir();
            }
        });
    }

    private void cerrar() {
        Stream.of(extractor, diseminador).forEach(obj -> {
            if (obj instanceof CicloVida cv) {
                try {
                    cv.cerrar();
                } catch (Exception ignored) {
                }
            }
        });
    }
}

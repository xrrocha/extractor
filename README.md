# Extractor SQL

_Extractor_ es una herramienta para:

- Ejecutar _consultas_ SQL
- Serializar los resultados en múltiples _formatos_
- Enviar los resultados formateados a un _destino_ dado

Este proyecto implementa esta funcionalidad de una forma minimalista e independiente de su contexto de uso (local,  
cliente/servidor, microservicios en la nube de cómputo, etc.)

## Objetivos de Diseño

Este ejercicio busca:

- Satisfacer los requerimientos del dominio de la forma más simple y con un mínimo conjunto de "partes móviles"
- Maximizar el aprovechamiento de la plataforma Java (18+)
- Minimizar las dependencias de librerías o frameworks de terceras partes
- Posibilitar el uso de la librería en un ambiente distribuído con escalamiento horizontal elástico

## Objetivos Estratégicos

Desde una perspectiva didáctica, este ejercicio también ejercita los siguientes lineamientos metodológicos:

- Simplicidad, simplicidad, simplicidad
- Máximo énfasis en la funcionalidad de negocio
- Mínima dependencia del uso de herramientas, librerías y frameworks específicas
- Desarrollo orgánico en el que la solución _emerge_ de una búsqueda metódica pero también exploratoria del espacio de 
  soluciones
- Aplicación de disciplinas y estrategias de desarrollo ágiles:
  - Domain-driven design
  - Walking skeleton
  - Test-driven development
  - Code reviews
  - Continuous integration, continuous deployment 

Estas consideraciones responden a la necesidad de evitar la complejidad accidental introducida por:

- La falta de precisión en la enunciación de requerimientos
- La interferencia creada por la aplicación ritualística y burocrática de procesos ágiles
- El desconocimiento de la plataforma y el _stack_ tecnológico por parte de los desarrolladores
- La pretensión de formular una solución "final" directamente en términos de una librería o tecnología particular sin 
  haber formulado primero "en abstracto" el dominio de negocio

## Requerimiento General

El requerimiento que origina este proyecto es el de permitir a un suscriptor de un servicio el obtener constancia de la 
ejecución de un proceso mediante un archivo que contiene los datos pertinentes y que le es enviado a un destino de 
su selección. 

Un ejemplo simple de este requerimiento sería el envío de una "papeleta de liquidación" a un afiliado como un archivo 
CSV mediante un servicio de _managed file transfer_.

En este ejemplo, el afiliado selecciona una transacción histórica particular para iniciar un proceso (no interactivo)
en el que se extraen los datos de la transacción desde una base datos relacional, se los formatea como un archivo CSV 
y se envía al suscriptor el archivo resultante a través a un servicio de diseminación de archivos.

Como tecnologías de implementación de una solución se requieren:

- Java 17+
- Spring
- RDBMS (via JDBC)
- Interfaz web

## Advertencia

La forma predominante en que se intenta satisfacer este género de requerimientos es lanzarse a la construcción de un 
proyecto específico en el que las abstracciones dominantes serían entidades tales como "papeleta", "afiliado", 
"archivo CSV" y "servidor de transferencia". 

Estas entidades se representarían de manera concreta como tablas relacionales representadas en  clases JPA dentro de 
una aplicación Spring Boot completa con repositorios, servicios y controladores para REST endpoints a los que se 
accedería desde una aplicación React o Angular.

Con frecuencia los desarrolladores que siguen este ritual repetitivo de implementación no se encuentran aun 
familiarizados con la plataforma Java "moderna" posterior a Java 11 o, incluso, a Java 8.

También con frecuencia, estos desarrolladores no conocen suficientemente Spring en cuanto framework y lo utilizan 
siguiendo una fórmula repetitiva en la que el uso de anotaciones o de interfaces se sigue pero no se comprende.

La combinación de una comprensión incompleta y excesivamente concreta del dominio de aplicación con un conocimiento 
parcial e insuficiente de la tecnología de implementación es la causa más frecuente de que los proyectos de desarrollo 
se tarden mucho más de lo esperado e implementen la funcionalidad requerida de manera incompleta e inconveniente 
para el usuario final.

A lo anterior se suman falencias tales como la escasez (o completa ausencia) de pruebas unitarias y de integración 
lo que causa que muchos defectos de la aplicación solo se revelen cuando esta entra en producción.

Finalmente, estos mismos desarrolladores rotan con gran frecuencia lo que hace que los esfuerzos por capacitarlos 
generalmente aceleren aun más la rotación al hacerlos "apetecibles" a otras organizaciones.

Como ya lo notara hace más de 35 años Fred Brooks en su 
[No Silver Bullet](https://en.wikipedia.org/wiki/No_Silver_Bullet) 
no existe una única respuesta metodológica, administrativa o técnica para eliminar 
mágicamente estos problemas.

Durante este largo periodo, sin embargo, se han ido acumulando experiencias y avances metodológicos y de 
herramientas que posibilitan una administración racional del proceso de desarrollo de software. Esto va desde la simple 
aplicación del sentido común hasta el seguimiento y soporte _permanentes_ (no solo iniciales!) por parte de los 
arquitectos de software como mentores y responsables de la formulación y desarrollo de patrones, herramientas y 
lineamientos metodológicos.

## Requerimiento Inicial

Una arma básica en el arsenal del arquitecto de software es la satisfacción de requerimientos concretos mediante 
el uso o desarrollo de herramientas _genéricas_.

En nuestro caso de ejemplo, una herramienta genérica (y, por tanto, reutilizable por múltiples requerimientos 
concretos de aplicación) para la extracción de datos desde una base de datos relacional, su serialización en una 
variedad de formatos y su diseminación mediante una variedad de mecanismos de distribución.

Este es el primer paso (exploratorio) implementado en este proyecto: una herramienta que consuma una _descripción 
declarativa_ del proceso de extracción, formateo y diseminación de los datos para instanciar dicha descripción 
declarativa de forma genérica, 100% basada en metadatos.

Para ilustrar, prototípicamente, esta herramienta imaginemos un _script_ de extracción de datos formulado en YAML 
que luciría como:

```yaml
--- !!bvc.extractor.Extraccion
extractor: !!bvc.extractor.extraccion.ExtractorSQL
  conexion:
    clase: org.postgresql.Driver
    url: 'jdbc:postgres:localhost:5432:custodia'
    usuario: tandem
    clave: DontStopTillYouGetEnough
  consulta: |
    SELECT  fecha, valor
    FROM    liquidacion
    WHERE   usuario = :usuario
      AND   id = :id
formateador: !!bvc.extractor.formateo.ArchivoCSV
  columnas:
    fecha:
      tipo: FECHA
      formato: yyyy/dd/mm
    valor:
      tipo: DINERO
      formato: '$###,###,###.##'
diseminador: !!bvc.extractor.diseminacion.ServidorFTP
  servidor: localhost
  puerto: 22
  usuario: admin
  clave: SuperAgente86
  directorio: /papeletas
  archivo: 'papeleta_liquidacion_${id}.csv'
```

Este sería el grafo de objetos que describe una instacia concreta de extracción, formateo y diseminación de datos.

Una formulación (también, necesariamente, prototípica) de la implementación de esta herramienta sería:

```java
interface CicloVida {
    void abrir();
    void cerrar();
}

interface Extractor extends CicloVida {
    Stream<Map<String, Object>> extraer();
}
class ExtractorSQL implements Extractor { /* Implementación... */ }

interface Formateador {
    String formatear(Map<String, Object> registro);
}
class ArchivoCSV implements Formateador { /* Implementación... */ }

interface Diseminador extends CicloVida {
    void grabar(String linea);
}
class ServidorFTP implements Diseminador { /* Implementación... */ }

record Extraccion(Extractor extractor, 
                  Formateador formateador, 
                  Diseminador diseminador) {
  public void ejecutar() {
    try {
      extractor.abrir();
      diseminador.abrir();
      extractor.extraer()
              .map(formateador::formatear)
              .forEach(diseminador::grabar);
    } finally {
      extractor.cerrar();
      diseminador.cerrar();
    }
  }
}
```

Esta es la formulación "esquelética" de lo que nos proponemos desarrollar como primer paso de este ejercicio.





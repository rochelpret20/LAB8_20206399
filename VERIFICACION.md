# Verificación de la entrega

Fecha: 5 de noviembre de 2026, America/Lima.

- `mvn verify`: **BUILD SUCCESS**. 16 pruebas, 0 fallos, 0 errores, 0 omitidas. Se repitió la verificación después de ajustar las columnas de texto para admitir caracteres multibyte sin el límite de bytes de TINYTEXT.
- Entorno anfitrión: JDK 26, Maven 3.9.16, destino de compilación Java 17. El Dockerfile fija JDK/JRE 17. No se verificó Java 17 en ejecución local.
- Integración: Spring Boot, MockMvc, filtros Spring Security, repositorios JPA y sesiones JDBC con H2 en modo MySQL. Se verifica cookie real SESSION y eliminación del registro persistido al salir.
- Prueba HTTP adicional: se inició el JAR con H2 y Tomcat en el puerto 18080. Acceso de tres cuentas, creación de post, escape HTML, envío y aceptación de solicitud, prohibición de borrado user, borrado admin y logout: satisfactorios. El servidor temporal se detuvo al terminar.
- `docker compose config --quiet`: correcto.
- Word: renderizado y revisado visualmente en sus seis páginas.

## Verificación pendiente

No se ejecutaron `docker build` ni `docker compose up`: el daemon estaba detenido y la instalación de Docker Desktop carecía del ejecutable para iniciarlo. No se ha validado el SQL con un MySQL 8.4 real, la compatibilidad del esquema con Hibernate en ese motor, ni la persistencia del volumen tras reinicios. H2 no sustituye esas verificaciones.

La clave única y los bloqueos para solicitudes concurrentes están implementados. La suite cubre duplicados secuenciales y recíprocos; no incluye una prueba de carga o concurrencia real. No se midió el vencimiento de 30 minutos; el valor está configurado.

## Reproducir

En un equipo con JDK 17 y Maven: `mvn clean verify`.
En un equipo con Docker operativo: seguir README.md, ejecutar `docker compose up --build -d` y realizar el recorrido manual allí indicado. La construcción de la imagen ejecuta la suite antes de crear el JAR final.

Los resúmenes de pruebas, compilación y HTTP están en `verificacion/`.

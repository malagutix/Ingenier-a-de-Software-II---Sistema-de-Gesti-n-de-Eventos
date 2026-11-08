package modelo.persistencia;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Estructura de la base de datos: tablas, claves y restricciones (CDU 007,
 * paso 3). Se crea con CREATE TABLE IF NOT EXISTS, así que se puede ejecutar
 * en cada inicio sin perder datos.
 */
final class Esquema {

    static final int VERSION = 1;

    /** Tablas con datos, en el orden en que se pueden cargar (respetando las claves foráneas). */
    static final List<String> TABLAS = Collections.unmodifiableList(Arrays.asList(
            "usuarios", "notificaciones", "tokens_recuperacion", "tipos_evento", "locaciones",
            "bloqueos_locacion", "solicitudes", "planificaciones", "eventos", "hojas_servicio"));

    /** Tablas cuyo id es serial (para reajustar la secuencia al restaurar). */
    static final List<String> CON_SECUENCIA = Collections.unmodifiableList(Arrays.asList(
            "usuarios", "notificaciones", "tipos_evento", "locaciones", "bloqueos_locacion",
            "solicitudes", "planificaciones", "eventos", "hojas_servicio"));

    static final List<String> SENTENCIAS = Collections.unmodifiableList(Arrays.asList(
            "CREATE TABLE IF NOT EXISTS control ("
                    + " id integer PRIMARY KEY CHECK (id = 1),"
                    + " version bigint NOT NULL,"
                    + " esquema integer NOT NULL,"
                    + " datos_ejemplo boolean NOT NULL DEFAULT false,"
                    + " ultimo_respaldo timestamp)",
            "INSERT INTO control (id, version, esquema) VALUES (1, 1, " + VERSION + ") ON CONFLICT (id) DO NOTHING",

            "CREATE TABLE IF NOT EXISTS usuarios ("
                    + " id serial PRIMARY KEY,"
                    + " nombre varchar(100) NOT NULL,"
                    + " apellido varchar(100) NOT NULL,"
                    + " dni varchar(20) NOT NULL UNIQUE,"
                    + " correo varchar(254) NOT NULL UNIQUE,"
                    + " contrasena_hash varchar(255) NOT NULL,"
                    + " rol varchar(20) NOT NULL"
                    + "   CHECK (rol IN ('ADMINISTRADOR', 'ORGANIZADOR', 'CLIENTE', 'PENDIENTE')))",

            "CREATE TABLE IF NOT EXISTS notificaciones ("
                    + " id serial PRIMARY KEY,"
                    + " id_usuario integer NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,"
                    + " mensaje text NOT NULL,"
                    + " creada timestamp NOT NULL,"
                    + " leida boolean NOT NULL DEFAULT false)",
            // Bases creadas antes de poder marcar notificaciones como leídas
            "ALTER TABLE notificaciones ADD COLUMN IF NOT EXISTS leida boolean NOT NULL DEFAULT false",

            "CREATE TABLE IF NOT EXISTS tokens_recuperacion ("
                    + " codigo varchar(20) PRIMARY KEY,"
                    + " id_usuario integer NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,"
                    + " vence timestamp NOT NULL,"
                    + " usado boolean NOT NULL DEFAULT false)",

            "CREATE TABLE IF NOT EXISTS tipos_evento ("
                    + " id serial PRIMARY KEY,"
                    + " nombre varchar(60) NOT NULL UNIQUE,"
                    + " duracion_sugerida integer NOT NULL CHECK (duracion_sugerida BETWEEN 1 AND 24),"
                    + " hora_inicio_sugerida time NOT NULL,"
                    + " requerimientos_sugeridos text NOT NULL DEFAULT '',"
                    + " activo boolean NOT NULL DEFAULT true)",

            "CREATE TABLE IF NOT EXISTS locaciones ("
                    + " id serial PRIMARY KEY,"
                    + " nombre varchar(80) NOT NULL UNIQUE,"
                    + " direccion varchar(200) NOT NULL,"
                    + " tipo varchar(30) NOT NULL,"
                    + " capacidad integer NOT NULL CHECK (capacidad > 0),"
                    + " distancia_km numeric(9,2) NOT NULL CHECK (distancia_km >= 0),"
                    + " tiempo_traslado_min integer NOT NULL CHECK (tiempo_traslado_min >= 0),"
                    + " tiempo_montaje_min integer NOT NULL CHECK (tiempo_montaje_min >= 0),"
                    + " vehiculos_requeridos integer NOT NULL CHECK (vehiculos_requeridos >= 0),"
                    + " requiere_camara_frio boolean NOT NULL,"
                    + " otros_requerimientos text NOT NULL DEFAULT '',"
                    + " habilitada boolean NOT NULL DEFAULT true)",

            "CREATE TABLE IF NOT EXISTS bloqueos_locacion ("
                    + " id serial PRIMARY KEY,"
                    + " id_locacion integer NOT NULL REFERENCES locaciones(id) ON DELETE CASCADE,"
                    + " desde date NOT NULL,"
                    + " hasta date NOT NULL,"
                    + " motivo varchar(200) NOT NULL,"
                    + " CHECK (hasta >= desde))",

            "CREATE TABLE IF NOT EXISTS solicitudes ("
                    + " id serial PRIMARY KEY,"
                    + " id_cliente integer NOT NULL REFERENCES usuarios(id),"
                    + " fecha_solicitada date NOT NULL,"
                    + " id_tipo_evento integer NOT NULL REFERENCES tipos_evento(id),"
                    + " cantidad_invitados integer NOT NULL CHECK (cantidad_invitados > 0),"
                    + " id_locacion integer NOT NULL REFERENCES locaciones(id),"
                    + " menu varchar(30) NOT NULL,"
                    + " estado varchar(20) NOT NULL CHECK (estado IN ('PENDIENTE', 'ACEPTADA', 'RECHAZADA', 'ELIMINADA')))",
            // Bases creadas antes de los estados PENDIENTE y ELIMINADA: se reemplaza la restricción
            "ALTER TABLE solicitudes DROP CONSTRAINT IF EXISTS solicitudes_estado_check",
            "ALTER TABLE solicitudes ADD CONSTRAINT solicitudes_estado_check"
                    + " CHECK (estado IN ('PENDIENTE', 'ACEPTADA', 'RECHAZADA', 'ELIMINADA'))",

            "CREATE TABLE IF NOT EXISTS planificaciones ("
                    + " id serial PRIMARY KEY,"
                    + " nombre varchar(80) NOT NULL,"
                    + " id_solicitud integer NOT NULL UNIQUE REFERENCES solicitudes(id))",

            "CREATE TABLE IF NOT EXISTS eventos ("
                    + " id serial PRIMARY KEY,"
                    + " id_planificacion integer NOT NULL REFERENCES planificaciones(id),"
                    + " fecha date NOT NULL,"
                    + " hora_inicio time NOT NULL,"
                    + " duracion_horas integer NOT NULL CHECK (duracion_horas BETWEEN 1 AND 24),"
                    + " cantidad_asistentes integer NOT NULL CHECK (cantidad_asistentes > 0),"
                    + " requerimientos_especificos text NOT NULL DEFAULT '',"
                    + " id_tipo_evento integer NOT NULL REFERENCES tipos_evento(id),"
                    + " id_locacion integer NOT NULL REFERENCES locaciones(id),"
                    + " id_organizador integer NOT NULL REFERENCES usuarios(id),"
                    + " estado varchar(30) NOT NULL,"
                    + " fue_aceptado boolean NOT NULL DEFAULT false)",
            // Bases creadas antes de recordar si un evento estuvo aceptado: se completa con su estado
            "ALTER TABLE eventos ADD COLUMN IF NOT EXISTS fue_aceptado boolean NOT NULL DEFAULT false",
            "UPDATE eventos SET fue_aceptado = true"
                    + " WHERE NOT fue_aceptado AND estado IN ('ACEPTADO', 'EN PROGRESO', 'FINALIZADO')",

            "CREATE TABLE IF NOT EXISTS hojas_servicio ("
                    + " id serial PRIMARY KEY,"
                    + " id_evento integer NOT NULL REFERENCES eventos(id) ON DELETE CASCADE,"
                    + " generada timestamp NOT NULL,"
                    + " contenido text NOT NULL)",

            "CREATE INDEX IF NOT EXISTS ix_notificaciones_usuario ON notificaciones (id_usuario)",
            "CREATE INDEX IF NOT EXISTS ix_solicitudes_locacion_fecha ON solicitudes (id_locacion, fecha_solicitada)",
            "CREATE INDEX IF NOT EXISTS ix_eventos_locacion_fecha ON eventos (id_locacion, fecha)",
            "CREATE INDEX IF NOT EXISTS ix_eventos_planificacion ON eventos (id_planificacion)"));

    private Esquema() {
    }
}

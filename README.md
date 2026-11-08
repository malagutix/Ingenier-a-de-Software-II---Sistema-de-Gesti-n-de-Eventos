# Subsistema 1 – Gestión de Eventos y Planificación

Implementación en Java de los CDU 001 a 015 con arquitectura **MVC**, interfaz gráfica en Swing y
base de datos **PostgreSQL**. Funciona con **Java 8 o superior**. La única librería externa es el driver JDBC
de PostgreSQL, que ya viene en `lib/`.

## Documentación (SRS)

La especificación de requisitos de software (SRS, IEEE 830) del Subsistema 1 está en
**[docs/SRS-Subsistema1-v1.1.4.pdf](docs/SRS-Subsistema1-v1.1.4.pdf)**.

- **Materia:** Ingeniería de Software II – Trabajo Práctico Anual
- **Versión:** 1.1.4 · **Fecha de entrega:** 29/06/2026
- **Profesores:** Jose Luis Oemig y Gonzalo Amigo
- **Integrantes:** Giovannelli, Luis · Montiel, Ignacio · Rial, Santiago · Saraví, Lucas

Contenido del documento:

1. Introducción (propósito, alcance, definiciones)
2. Descripción general (perspectiva, funciones, usuarios, restricciones)
3. Requerimientos específicos: no funcionales (seguridad, rendimiento, usabilidad, mantenimiento,
   escalabilidad) y funcionales RF001 a RF015
4. Casos de uso CDU 001 a CDU 015
5. Diseño de clases UML
6. Diagramas de colaboración de objetos (DCO)
7. Diagramas de secuencia (DDS)

| CDU | Caso de uso | Requisito |
|---|---|---|
| 001 | Registro de usuario | RF001 |
| 002 | Iniciar sesión | RF002 |
| 003 | Restablecer contraseña | RF003 |
| 004 | Gestionar usuario y contraseña | RF004 |
| 005 | Asignar roles | RF005 |
| 006 | Modificar usuario | RF006 |
| 007 | Gestionar base de datos | RF007 |
| 008 | Crear solicitud | RF008 |
| 009 | Modificar solicitud | RF009 |
| 010 | Eliminar solicitud | RF010 |
| 011 | Crear evento | RF011 |
| 012 | Eliminar evento | RF012 |
| 013 | Validar evento | RF013 |
| 014 | Modificar evento | RF014 |
| 015 | Generar hoja de servicio | RF015 |

## Antes de empezar: PostgreSQL

1. Instalá PostgreSQL (12 o superior) desde https://www.postgresql.org/download/. Durante la
   instalación te pide una contraseña para el usuario `postgres`: anotala.
2. Abrí `basedatos.properties` (en la carpeta del proyecto) y poné esa contraseña en `contrasena=`.
3. Listo. **No hace falta crear la base ni las tablas**: al iniciar, el sistema verifica si la base
   `gestion_eventos` existe, la crea si falta y genera las tablas (CDU 007).

La primera vez, como la base está vacía, aparece la **Configuración inicial** para crear el
administrador. Ahí podés tildar "Cargar datos de ejemplo" para tener usuarios, locaciones,
solicitudes y eventos de prueba.

Si querés probar sin PostgreSQL, poné `habilitado=false` en `basedatos.properties`: el sistema
arranca en **modo demostración**, con los datos en memoria (se pierden al cerrar). Si PostgreSQL
no responde al iniciar, el sistema ofrece reintentar, usar el modo demostración o salir.

## Cómo abrirlo en IntelliJ IDEA

1. Descomprimí el zip en una **carpeta nueva y vacía**.
2. **File > Open** y elegí la carpeta `sistema-eventos` (la que contiene `src`). Elegí **Trust Project**.
3. Si aparece **"Project JDK is not defined"**, hacé clic en **Setup SDK** y elegí tu Java.
4. Arriba a la derecha elegí **"Sistema de eventos"** y tocá ▶.

El driver de PostgreSQL ya está configurado como librería del módulo (`lib/postgresql-42.7.4.jar`).
Si IntelliJ marca en rojo los `import java.sql` o `org.postgresql`, andá a **File > Project Structure
> Modules > Dependencies**, tocá **+ > JARs or Directories** y elegí ese archivo.

Sin IDE: doble clic en `ejecutar.bat` (Windows) o `./ejecutar.sh` (Linux/macOS).

## Usuarios de prueba

Con PostgreSQL, el administrador es el que creás en la configuración inicial; si cargaste los datos de
ejemplo, además están maría, juan, lucía y sofía. En modo demostración también existe el admin de abajo.

| Rol           | Correo             | Contraseña | Qué ve |
|---------------|--------------------|------------|--------|
| Administrador | admin@eventos.com  | Admin2026  | Todo, incluida la pestaña Base de datos (solo en modo demostración) |
| Organizador   | maria@mail.com     | Clave2026  | Planificaciones, Eventos, Calendario |
| Cliente       | juan@mail.com      | Clave2026  | Calendario (solo sus eventos) |
| Cliente       | lucia@mail.com     | Clave2026  | Calendario (solo sus eventos) |
| Sin rol       | sofia@mail.com     | Clave2026  | No puede ingresar |

Los datos de ejemplo son 5 tipos de evento (los del enunciado), 5 locaciones (una con fechas
bloqueadas), 4 solicitudes y 2 planificaciones: un casamiento y un congreso de 3 jornadas.

## Base de datos (CDU 007)

- **Tablas:** `usuarios`, `notificaciones`, `tokens_recuperacion`, `tipos_evento`, `locaciones`,
  `bloqueos_locacion`, `solicitudes`, `planificaciones`, `eventos`, `hojas_servicio` y `control`.
  Tienen claves primarias, foráneas, valores únicos y controles (por ejemplo, capacidad > 0).
  El script completo está en `basedatos/esquema.sql` (es documentación, no hace falta ejecutarlo).
- **Pestaña Base de datos** (administrador): muestra la conexión y cuántos registros tiene cada
  tabla, permite ver los registros (sin mostrar contraseñas), hacer un respaldo, restaurarlo y
  verificar la integridad. Las altas, bajas y modificaciones se hacen desde cada pestaña, que
  valida las reglas del negocio antes de guardar.
- **Respaldo automático (RNF 003):** una vez por día se guarda un respaldo en la carpeta
  `respaldos/` y se conservan los 7 más recientes.
- **Transacciones:** cada operación se guarda completa o no se guarda (por ejemplo, planificar un
  congreso de 3 días). Si se corta la conexión, no queda nada a medias.
- **Varias computadoras a la vez (RNF 006):** todas pueden usar la misma base. Antes de guardar, el
  sistema bloquea la fila de control y vuelve a leer lo que cambiaron los demás, así dos personas no
  pueden reservar la misma locación al mismo tiempo. Lo que cargan otros aparece al refrescar.
- **Seguridad:** todas las consultas usan parámetros (`PreparedStatement`), así que no hay
  inyección SQL; las contraseñas se guardan con hash PBKDF2; los respaldos se restauran validando
  tablas y columnas, sin ejecutar SQL del archivo.

## Funcionalidades del Subsistema 1

- **Gestión de eventos, tipos y estados.** Los tipos de evento se administran desde el sistema
  (alta, modificación, activar/desactivar, baja), con hora y duración sugeridas. Los estados
  usan el patrón State.
- **Gestión de locaciones.** Tipo (salón propio, espacio alquilado, domicilio particular,
  espacio público), capacidad máxima, distancia a la sede, tiempo de traslado y requerimientos
  logísticos (vehículos, cámara de frío, tiempo de montaje, otros). Disponibilidad: se pueden
  inhabilitar y bloquear rangos de fechas.
- **Gestión de solicitudes** (CDU 008 a 010). Una solicitud nace **PENDIENTE**; el administrador la
  **acepta** o la **rechaza**. Al aceptarla se crea su evento (PENDIENTE DE RESERVA) en la fecha
  solicitada, con la hora y duración sugeridas del tipo, y aparece en Eventos y Calendario. Una
  solicitud pendiente ya reserva su día en la locación.
- **Planificación de múltiples eventos**: una solicitud genera uno o varios eventos agrupados
  en una planificación, que se puede reprogramar o cancelar en bloque.
- **Hoja de servicio** con datos del evento, horarios de inicio y fin, locación, recursos
  logísticos (salida desde la sede, montaje, desmontaje), menú y consumos. Exporta a PDF,
  Excel (.csv) y Word (.rtf).
- **Reglas de planificación** (ver abajo).
- **Coordinación general**: usuarios, roles, permisos y recuperación de contraseña por correo.
- **Persistencia en PostgreSQL** con creación automática de la base, respaldos y verificación de
  integridad (CDU 007).

## Reglas de planificación

- Un **evento** reserva su locación desde el **inicio del montaje** hasta el **fin del desmontaje**
  (su horario más el tiempo de montaje de la locación, antes y después). Dos eventos en el mismo
  lugar no pueden superponerse; el mismo día puede haber más de uno si los horarios no se pisan.
- Una **solicitud sin planificar** reserva su **día**: ese día no puede empezar otro evento ni haber
  otra solicitud en esa locación. Al planificarla, la reserva pasa a sus eventos.
- Las fechas tienen que ser futuras, la cantidad de personas no puede superar la capacidad, y la
  locación tiene que estar habilitada y sin la fecha bloqueada.
- No se puede bajar la capacidad de una locación por debajo de una reserva, ni aumentar el tiempo
  de montaje si eso hace chocar eventos, ni bloquear fechas que tienen reservas.
- Lo que está en uso (locaciones, tipos) no se elimina: se inhabilita o se desactiva.
- Si un evento se reprograma o se cancela, su horario anterior queda libre.

## Planificación de múltiples eventos

Relación: `SolicitudEvento 1 -- 0..1 Planificacion 1 -- 1..* Evento`.

- Al aceptar una solicitud se crea la planificación con su primer evento. Desde Planificaciones se le
  agregan más eventos en días consecutivos, a la misma hora.
- Es todo o nada: si alguna fecha no está disponible, no se crea ningún evento.
- Reprogramar todo: mueve los eventos activos la misma cantidad de días, manteniendo horarios.
- Cancelar todo: cancela los eventos activos. Ninguna de las dos se permite con eventos EN PROGRESO.
- Una solicitud que forma parte de una planificación no se puede modificar ni rechazar. Al
  eliminarla (CDU 010, paso 2) el sistema pregunta si también se eliminan sus eventos:
  - **Sí:** se eliminan la solicitud, su planificación y todos sus eventos (salvo que alguno esté
    ACEPTADO o EN PROGRESO: en ese caso no se elimina nada).
  - **No:** la solicitud sale de la lista (queda en estado ELIMINADA, que no se muestra) y sus
    eventos se conservan, porque siguen usando sus datos (cliente, menú, invitados).

## Estados del evento (patrón State)

| Estado actual        | Transiciones posibles                  | ¿Se puede eliminar? |
|----------------------|----------------------------------------|---------------------|
| PENDIENTE DE RESERVA | Aceptar, Reprogramar, Cancelar         | Sí                  |
| ACEPTADO             | Iniciar, Reprogramar, Cancelar         | No (se cancela)     |
| REPROGRAMADO         | Aceptar, Reprogramar, Cancelar         | Solo si nunca estuvo aceptado |
| EN PROGRESO          | Finalizar                              | No                  |
| FINALIZADO           | Ninguna                                | Sí                  |
| CANCELADO            | Ninguna                                | Sí                  |

## Correo real (recuperación de contraseña)

Por defecto los correos van a una **bandeja simulada** que se abre desde el login. Para enviarlos
de verdad, copiá `correo.properties.ejemplo` como `correo.properties` (en la misma carpeta),
completá los datos y poné `habilitado=true`. El archivo de ejemplo explica cómo hacerlo con Gmail.

El correo trae un **código de recuperación** (por ejemplo `K7MP-4QZX`) que se ingresa en la
aplicación junto con la contraseña nueva. Vence a los 30 minutos y sirve una sola vez.

## Arquitectura MVC (modelo – vista – controlador)

El código está separado en tres capas, cada una en su paquete:

- **`modelo`**: entidades, reglas del negocio, repositorios y persistencia en PostgreSQL. No sabe que
  existe una interfaz gráfica: no importa nada de Swing, de `vista` ni de `controlador`.
- **`vista`**: pantallas Swing (`Vista...`). Solo muestran datos y avisan qué hace el usuario
  (`alNueva`, `alEliminar`, ...). Pueden leer entidades para mostrarlas, pero nunca llaman al modelo.
- **`controlador`**: un controlador por pantalla (`Controlador...`). Recibe la acción de su vista, le
  pide al modelo que la realice, decide qué mostrar y, si el modelo la rechaza, muestra el motivo.

| Pantalla | Vista | Controlador | Modelo que usa |
|---|---|---|---|
| Arranque | `VistaArranque` | `ControladorAplicacion` | `Modelo`, `BaseDeDatos` |
| Navegación | `VistaPrincipal` | `ControladorPrincipal` | `Modelo` |
| Configuración inicial | `VistaConfiguracionInicial` | `ControladorConfiguracionInicial` | `GestorUsuarios`, `DatosDePrueba` |
| Login (CDU 001 a 003) | `VistaLogin` | `ControladorLogin` | `GestorUsuarios` |
| Inicio y pestañas | `VistaInicio` | `ControladorInicio` | `Modelo` |
| Usuarios (CDU 004 a 006) | `VistaUsuarios` | `ControladorUsuarios` | `GestorUsuarios` |
| Solicitudes (CDU 008 a 010) | `VistaSolicitudes` | `ControladorSolicitudes` | `SistemaEventos` |
| Planificaciones | `VistaPlanificaciones` | `ControladorPlanificaciones` | `SistemaEventos` |
| Eventos (CDU 011 a 015) | `VistaEventos` | `ControladorEventos` | `SistemaEventos`, exportadores |
| Calendario | `VistaCalendario` | `ControladorCalendario` | `SistemaEventos` |
| Locaciones | `VistaLocaciones` | `ControladorLocaciones` | `GestorCatalogos` |
| Tipos de evento | `VistaTiposEvento` | `ControladorTiposEvento` | `GestorCatalogos` |
| Base de datos (CDU 007) | `VistaBaseDatos` | `ControladorBaseDatos` | `GestorBaseDatos` |
| Notificaciones | `VistaNotificaciones` | `ControladorNotificaciones` | `GestorUsuarios` |

**Ejemplo, eliminar una solicitud (CDU 010):** el usuario toca *Eliminar* → `VistaSolicitudes` avisa a
`ControladorSolicitudes` → el controlador le pide a la vista que confirme → llama a
`SistemaEventos.eliminarSolicitud(...)` → si el modelo lo rechaza (por ejemplo, "un
evento está en progreso"), le pide a la vista que muestre el error; si sale bien, vuelve a leer las solicitudes del
modelo y se las pasa a la vista con el mensaje «Solicitud eliminada».

## Estructura y patrones

```
src/
├── app/            Main (arranca la aplicación) y DemoConsola
├── modelo/         M de MVC
│   ├── Modelo          arma el modelo con PostgreSQL o en memoria (único que conoce las implementaciones)
│   ├── DatosDePrueba   datos de ejemplo
│   ├── usuarios/       CDU 001 a 006
│   │   ├── entidades/      Usuario (Observer), Permiso, TokenRecuperacion, rol/ (Strategy + Factory Method)
│   │   ├── repositorio/    UsuarioRepository y TokenRepository (Repository)
│   │   ├── servicio/       GestorUsuarios (Facade), correo (SMTP real, bandeja simulada)
│   │   └── seguridad/      Hash de contraseñas (PBKDF2)
│   ├── eventos/        CDU 008 a 015
│   │   ├── entidades/      Planificacion, Evento, SolicitudEvento, HojaServicio, Locacion,
│   │   │                   BloqueoFecha, TipoEvento, EventoFactory (Factory Method)
│   │   ├── entidades/estado/  EstadoEvento y sus 6 estados (State)
│   │   ├── exportacion/    Exportador PDF / Excel / Word (Strategy)
│   │   ├── repositorio/    Repositorios de cada entidad (Repository)
│   │   └── servicio/       SistemaEventos y GestorCatalogos (Facade), ReglasPlanificacion
│   └── persistencia/   PostgreSQL: BaseDeDatos, Esquema, repositorios *Postgres, ContextoPersistencia
│                       (transacciones y mapa de identidades), respaldos y GestorBaseDatos (Facade del CDU 007)
├── vista/          V de MVC: Vista (interfaz común), Vista* (una por pantalla), formularios y diálogos
└── controlador/    C de MVC: Controlador (base), Controlador* (uno por pantalla), PlanificadorEventos
```

## Límites con otros subsistemas

- Subsistema 2: proveedores, chefs, personal, catálogo de menús y costos. La hoja de servicio deja
  esas secciones como "Pendiente de asignación (Subsistema 2)" y no incluye precios.
- Subsistema 3: las notificaciones quedan pendientes de definir con ese grupo. Por ahora `Evento`
  avisa a sus observadores, `Usuario` guarda los avisos y se persisten en la tabla `notificaciones`.

## Qué probar

1. **Horarios.** Como organizadora (maria), en Eventos, modificar el evento de la feria o del congreso
   y cambiar la hora: si el montaje se pisa con otro evento del mismo lugar, lo rechaza y dice con cuál.
2. **Aceptar y planificar.** En Solicitudes, aceptar la #3: su evento aparece en Eventos y Calendario.
   Después, en Planificaciones, "Planificar eventos" le agrega más días.
3. **Locaciones.** Como admin: crear una, bloquear fechas, inhabilitarla y ver que ya no aparece en
   solicitudes nuevas. Intentar bajar la capacidad del Salón Costa Azul a 100 (no deja: el casamiento
   tiene 150). Intentar crear una solicitud en el Espacio Terraza en sus fechas de mantenimiento.
4. **Tipos de evento.** Crear uno nuevo y usarlo en una solicitud. Desactivar "Boda" e intentar
   eliminarlo (no deja porque está en uso).
5. **Hoja de servicio.** Generarla para el casamiento y exportarla: muestra horario, montaje,
   salida desde la sede y recursos logísticos.
6. **Base de datos.** Cerrá y volvé a abrir la aplicación: todo sigue ahí. En la pestaña Base de
   datos tocá "Verificar integridad", mirá los registros de `eventos` y hacé un respaldo.
7. **Dos computadoras.** Abrí la aplicación dos veces (o en dos computadoras apuntando al mismo
   servidor) y creá una solicitud en una: la otra la ve al cambiar de pestaña.
8. **Recuperar contraseña.** "Olvidé mi contraseña" > ver el correo en la bandeja simulada > copiar
   el código > poner la nueva contraseña. Con `correo.properties` configurado llega a tu correo real.

## Si aparecen errores

| Error | Causa | Solución |
|-------|-------|----------|
| `package ... does not exist`, clases duplicadas o que no existen | Quedaron archivos de un zip anterior | Descomprimir en una carpeta vacía |
| `Could not find or load main class` | Configuración de ejecución vieja | La clase principal es `app.Main` |
| "Java file outside source root" | `src` no está marcada como fuentes | Clic derecho en `src` > Mark Directory as > Sources Root |
| "The output path is not specified" | Falta la carpeta de compilación | File > Project Structure > Project > Compiler output: `...\sistema-eventos\out` |
| Acentos raros (Ã±, Ã¡) | El proyecto no está en UTF-8 | File > Settings > Editor > File Encodings: UTF-8 |
| "No se pudo enviar el correo" | Datos de `correo.properties` incorrectos | Revisar servidor, puerto y contraseña de aplicación |
| La pestaña Base de datos dice "modo demostración" | No encontró o no pudo usar `basedatos.properties` | Debajo del login y en esa pestaña figura el motivo. El archivo tiene que estar en la carpeta del proyecto (junto a `src`) con `habilitado=true` |
| "PostgreSQL rechazó el usuario o la contraseña" | La contraseña de `basedatos.properties` no es la de la instalación | Corregirla en `basedatos.properties` |
| "No se pudo conectar con la base de datos" | PostgreSQL no está iniciado o el puerto no es 5432 | Windows: Servicios > postgresql-x64 > Iniciar. Revisar el puerto |
| "package org.postgresql does not exist" o `No suitable driver` | Falta el driver en el classpath | Agregar `lib/postgresql-42.7.4.jar` como dependencia del módulo |
| "no tiene permisos para verificar la base de datos" | El usuario de PostgreSQL no puede crear bases | Crear la base a mano (`CREATE DATABASE gestion_eventos;`) o usar el usuario `postgres` |

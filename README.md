# Ingeniería de Software II – Trabajo Práctico: Subsistema 1 – Gestión de Eventos y Planificación

**Documentación de especificación de requisitos de software (SRS)**

- **Versión:** 1.1.4
- **Fecha de entrega:** 29/06/2026
- **Profesores:** Jose Luis Oemig y Gonzalo Amigo
- **Integrantes:** Giovannelli, Luis · Montiel, Ignacio · Rial, Santiago · Saraví, Lucas

El documento original, con el diagrama de clases UML y los diagramas de colaboración y de secuencia,
está en **[docs/SRS-Subsistema1-v1.1.4.pdf](docs/SRS-Subsistema1-v1.1.4.pdf)**.

## Índice

- [Historial de versiones](#historial-de-versiones)
- [1. Introducción](#1-introducción)
- [2. Descripción general](#2-descripción-general)
- [3. Requerimientos específicos del sistema](#3-requerimientos-específicos-del-sistema)
- [4. Casos de uso](#4-casos-de-uso)
- [5. Diseño de clases UML](#5-diseño-de-clases-uml)
- [6. Diagramas de colaboración de objetos](#6-diagramas-de-colaboración-de-objetos)
- [7. Diagramas de secuencia](#7-diagramas-de-secuencia)
- [Usuarios de prueba en la base de datos](#usuarios-de-prueba-en-la-base-de-datos)

## Historial de versiones

| Fecha | Motivo de el/los cambios | Versión |
|---|---|---|
| 4/05 | Creación de la documentación y especificación de requerimientos | 1.0 |
| 11/05 | Arreglar requerimientos funcionales y creación de casos de uso | 1.1 |
| 18/05 | Agregar diseño de clases UML | 1.1.1 |
| 21/05 | Reestructuración del documento, cambio en requerimientos no funcionales | 1.1.2 |
| 1/06 | Reestructuración del diseño de clases UML | 1.1.3 |
| 8/06 | Creación de diagramas de colaboración de objetos y diagramas de secuencia con pseudocódigo | 1.1.4 |

---

## 1. Introducción

### 1.1 Propósito

La presente Especificación de Requisitos de Software (SRS) tiene como objetivo definir los requisitos
del sistema de gestión de eventos, el cual permitirá organizar, planificar y coordinar eventos de
manera integral.

El sistema busca reemplazar métodos manuales o desorganizados mediante una plataforma digital
centralizada que optimice la planificación, control y ejecución de eventos.

### 1.2 Alcance

El sistema será una aplicación que permitirá gestionar todo el ciclo de vida de los eventos, desde la
solicitud hasta su finalización.

Las funcionalidades principales incluyen:

- Gestión de eventos, tipos y estados.
- Gestión de solicitudes.
- Planificación de múltiples eventos.
- Gestión de hoja de servicio.
- Reglas de planificación (fechas, conflictos, reprogramaciones).
- Coordinación general del sistema.

### 1.3 Definiciones, acrónimos y abreviaturas

- **SRS:** Especificación de Requisitos del Sistema
- **RF:** Requisitos funcionales
- **RFN:** Requisitos no funcionales
- **CDU:** Casos de uso
- **DCO:** Diagramas de colaboración de objetos
- **DDB:** Diagrama de Bloques
- **DDS:** Diagrama de Secuencia

**Referencias:** el formato del documento es Standard IEEE 830, que provee una visión global de la SRS.

### 1.4 Overview

Estructura del documento:

- **Descripción General del Sistema:** esta sección proporciona una visión panorámica del sistema,
  abarcando la perspectiva del producto, las funciones que éste cumplirá, las características de los
  usuarios implicados, las restricciones generales a considerar y las dependencias externas relevantes.
- **Requisitos Específicos del Sistema:** aquí se detallan tanto los requisitos funcionales como los no
  funcionales, estableciendo las capacidades y características específicas que debe cumplir el sistema.
- **Casos de Uso y Gráficos:** en esta sección se presentan los casos de uso que describen las
  interacciones entre usuarios y el sistema, acompañados de gráficos que ilustran visualmente estas
  interacciones y flujos de trabajo.
- **Diagramas de colaboración de objetos y Gráficos:** en esta sección se presentan los diagramas de
  colaboración de objetos, los cuales describen cómo interactúan los objetos entre sí dentro de un
  escenario particular del sistema. Estos diagramas muestran las relaciones, mensajes y la colaboración
  necesaria entre diferentes instancias de clases u objetos para cumplir con un flujo específico.
- **Diagramas de bloques:** en esta sección se incluyen los diagramas de bloques, que representan la
  arquitectura estática del sistema, mostrando cómo se organizan sus componentes o módulos principales
  y cómo se relacionan entre sí.
- **Diagrama de Secuencia:** en esta sección se muestran los diagramas de secuencia, que describen el
  orden cronológico de los mensajes e interacciones entre actores y componentes del sistema. Permiten
  visualizar cómo se desarrolla un proceso paso a paso en el tiempo, desde la acción inicial de un
  actor hasta la respuesta final del sistema.

---

## 2. Descripción general

### 2.1 Perspectiva del producto

El sistema consistirá en una aplicación centralizada orientada a la coordinación eficiente de
eventos, integrando las funcionalidades principales definidas en el alcance.

### 2.2 Funciones del producto

- **Registro de usuario:** el sistema permite registrar a los usuarios ingresando sus datos (nombre,
  apellido, dni, mail y contraseña) en la aplicación.
- **Iniciar sesión:** el sistema permite iniciar sesión a aquellas personas que se hayan registrado
  previamente, ingresando correctamente su email y contraseña. Los usuarios podrán cambiar su
  contraseña si lo desean ingresando su mail y siguiendo los pasos correspondientes.
- **Gestionar usuario y contraseña:** el administrador podrá eliminar o editar los datos de los usuarios.
- **Asignar roles:** el administrador podrá asignar los roles una vez que el usuario se registre. Los
  roles que podrá asignar son: organizador, administrador, cliente.
- **Gestionar base de datos:** el sistema debe permitir la creación, acceso y administración de la base
  de datos necesaria para almacenar la información de usuarios, eventos, locaciones, solicitudes y
  calendarios.
- **Crear solicitud:** el administrador o el organizador puede crear una solicitud indicando fecha,
  tipo de evento, número de invitados, lugar y menús requeridos. El sistema debe filtrar lugares y
  fechas en base a disponibilidad.
- **Modificar solicitud:** el administrador o el organizador puede modificar una solicitud.
- **Eliminar solicitud:** el administrador o el organizador puede eliminar una solicitud.
- **Crear evento:** el administrador o el organizador puede crear un evento una vez creada la
  solicitud, ingresando tipo, fecha, duración, ubicación, cantidad estimada de asistentes y
  requerimientos específicos.
- **Modificar evento:** el administrador o el organizador puede modificar el evento.
- **Eliminar evento:** el administrador o el organizador puede eliminar un evento.
- **Validar evento:** el administrador o el organizador podrá validar el evento indicando su estado:
  finalizado - aceptado - en progreso - reprogramado - cancelado.
- **Generar hoja de servicio:** el sistema genera una hoja de servicio según la información ingresada
  previamente.

### 2.3 Características del usuario

Los usuarios abarcan diferentes roles dentro del sistema como Administrador, Organizador y Cliente. Se
establecerán restricciones de acceso adaptadas a las responsabilidades y privilegios específicos de
cada tipo de usuario.

### 2.4 Restricciones generales

- **Dependencia de datos previos:** no se podrán crear eventos sin tener previamente registradas
  locaciones y usuarios.
- **Gestión manual de datos críticos:** la creación y modificación de eventos, solicitudes, locaciones
  y calendario será realizada manualmente por los usuarios autorizados.
- **Limitación de automatización:** el sistema no realizará planificación automática inteligente; la
  organización de eventos dependerá del usuario.
- **Persistencia de datos:** toda la información será almacenada en una base de datos centralizada.

### 2.5 Supuestos y dependencias

- Los usuarios estarán previamente registrados.
- Los usuarios deberán tener un rol asignado para el uso de la aplicación.
- La planificación depende de la correcta carga de datos.
- El sistema requerirá que cada usuario tenga un rol asignado, el cual determinará sus permisos de
  acceso y las funcionalidades disponibles.

---

## 3. Requerimientos específicos del sistema

### 3.1 Requerimientos no funcionales

#### 3.1.1 RNF referidos a la seguridad

| Código | Nombre | Prioridad | Descripción |
|---|---|---|---|
| RNF 001 | Autenticación de los usuarios | Esencial | El sistema deberá garantizar que solo los usuarios autenticados puedan acceder a las funcionalidades según su rol (Administrador, Organizador y Personal). |
| RNF 002 | Seguridad de la información | Necesario | Toda comunicación entre usuarios debe realizarse mediante el protocolo HTTPS y los datos almacenados deben cifrarse en reposo mediante el AES-256. |
| RNF 003 | Respaldo y recuperación de datos | Necesario | El sistema debe realizar respaldos automáticos de la base de datos diariamente y permitir la restauración de datos en caso de fallos o pérdidas. |
| RNF 004 | Protección contra ataques comunes | Necesario | El sistema debe implementar medidas de seguridad siguiendo los estándares de OWASP Top 10 para prevenir ataques como SQL injection, XSS y CSRF. |

#### 3.1.2 RNF referidos al rendimiento

| Código | Nombre | Prioridad | Descripción |
|---|---|---|---|
| RNF 005 | Compatibilidad con otros sistemas | Deseable | La aplicación debe ser compatible con los sistemas operativos de Windows y Linux. |
| RNF 006 | Concurrencia de usuarios | Esencial | El sistema debe poder soportar simultáneamente al menos 50 usuarios activos sin degradación significativa del rendimiento. |
| RNF 007 | Tiempo de respuesta | Deseable | Las páginas de la plataforma deben cargarse en un máximo de 3 segundos. |

#### 3.1.3 RNF referidos a la usabilidad

| Código | Nombre | Prioridad | Descripción |
|---|---|---|---|
| RNF 008 | Usabilidad de la interfaz | Necesario | La interfaz deberá ser intuitiva y fácil de utilizar para cualquier usuario. Los mensajes de error deben ser claros. |
| RNF 009 | Disponibilidad del servicio | Necesario | La plataforma debe estar disponible el 99,5% del tiempo. |

#### 3.1.4 RNF referidos al mantenimiento

| Código | Nombre | Prioridad | Descripción |
|---|---|---|---|
| RNF 010 | Mantenimiento continuo | Necesario | El sistema debe permitir realizar actualizaciones y mantenimiento sin interrumpir el servicio más de 1 hora al mes. |

#### 3.1.5 RNF referidos a la escalabilidad

| Código | Nombre | Prioridad | Descripción |
|---|---|---|---|
| RNF 011 | Escalabilidad del sistema | Esencial | El sistema deberá permitir agregar nuevas funcionalidades sin afectar el funcionamiento actual. El sistema deberá permitir agregar nuevos módulos. |

### 3.2 Requerimientos funcionales

> La prioridad (Alta / Media / Baja) de cada requerimiento funcional está marcada en el
> [documento PDF](docs/SRS-Subsistema1-v1.1.4.pdf).

#### 3.2.1 Requerimientos de sesión

##### RF001 – Registro de usuario

| Campo | Contenido |
|---|---|
| Caso de uso asociado | CDU001 |
| Historial de usuario | Registrarse dentro de la plataforma. |
| Descripción | El sistema deberá permitir el registro de nuevos usuarios a través de un formulario, solicitando información personal y credenciales de acceso. |
| Entradas | El usuario ingresa los siguientes datos para registrarse en el sistema: nombre, apellido, DNI, correo electrónico y contraseña. |
| Proceso | Se verifica que los datos sean correctos. |
| Salidas | Confirmación de registro exitoso. Mensajes de error en caso de datos inválidos o incompletos. |
| Restricciones | El correo electrónico debe ser único en el sistema. El DNI no debe estar previamente registrado. La contraseña debe cumplir criterios mínimos de seguridad. Todos los campos son obligatorios. |
| Dependencias | Disponibilidad de la base de datos. |

##### RF002 – Iniciar sesión

| Campo | Contenido |
|---|---|
| Caso de uso asociado | CDU002 |
| Historial de usuario | Ingreso al sistema. |
| Descripción | El sistema deberá permitir a los usuarios previamente registrados iniciar sesión mediante el ingreso de su correo electrónico y contraseña. Asimismo, el sistema permitirá la recuperación o cambio de contraseña a través del correo electrónico, siguiendo un proceso de verificación. |
| Entradas | El usuario ingresa los siguientes datos para ingresar al sistema: correo electrónico y contraseña. |
| Proceso | Se verifica si el usuario está registrado en la aplicación. |
| Salidas | Si el usuario está registrado en la aplicación, lo lleva a la ventana principal del programa. El usuario podrá restablecer su contraseña a través de su email. |
| Restricciones | El usuario debe estar previamente registrado en el sistema. El correo electrónico debe existir en la base de datos. La contraseña debe coincidir con la registrada. El proceso de recuperación de contraseña debe realizarse mediante el correo electrónico registrado. |
| Dependencias | Disponibilidad de la base de datos. Usuario previamente registrado. |

##### RF003 – Restablecer contraseña

| Campo | Contenido |
|---|---|
| Caso de uso asociado | CDU003 |
| Historial de usuario | El usuario restablece su contraseña para recuperar el acceso al sistema. |
| Descripción | El sistema deberá permitir al usuario restablecer su contraseña mediante un enlace de recuperación y la validación de una nueva contraseña. |
| Entradas | El usuario ingresa: nueva contraseña y confirmación de nueva contraseña. |
| Proceso | Si las contraseñas ingresadas coinciden, el sistema enviará un correo electrónico con un enlace de activación para confirmar el cambio de contraseña. Una vez activado el enlace, la contraseña será actualizada exitosamente en el sistema. |
| Salidas | Confirmación de actualización exitosa de contraseña. Mensaje de error: "Contraseñas no coinciden". |
| Restricciones | El usuario debe estar previamente registrado en el sistema. Solo usuarios con rol Personal u Organizador podrán realizar esta acción. Las contraseñas ingresadas deben coincidir. |
| Dependencias | Disponibilidad de la base de datos. Servicio de correo electrónico activo. Usuario previamente registrado en el sistema. |

##### RF004 – Gestionar usuario y contraseña

| Campo | Contenido |
|---|---|
| Caso de uso asociado | CDU004 |
| Historial de usuario | El administrador modifica los datos de los usuarios. |
| Descripción | El sistema deberá permitir al administrador modificar y eliminar los datos de los usuarios registrados, así como también gestionar el cambio de sus contraseñas. |
| Entradas | El administrador ingresa algunos de los datos para buscar el usuario en el sistema: DNI, rol, correo electrónico. |
| Proceso | Si los datos ingresados son correctos, el administrador podrá modificar los datos o eliminar el usuario. |
| Salidas | Confirmación de modificación de dato exitosa. Confirmación de eliminación de usuario exitosa. El usuario no fue encontrado en la lista. |
| Restricciones | Solo el administrador podrá realizar estas acciones. El usuario debe existir en la lista para su modificación o eliminación. |
| Dependencias | Disponibilidad de la base de datos. Usuario con rol administrador autenticado. Usuario previamente registrado. |

##### RF005 – Asignar roles

| Campo | Contenido |
|---|---|
| Caso de uso asociado | CDU005 |
| Historial de usuario | Gestionar los diferentes roles de usuario. |
| Descripción | El sistema deberá permitir al administrador asignar un rol a cada usuario registrado. Los roles disponibles serán Personal y Organizador, los cuales determinarán los permisos y funcionalidades accesibles dentro del sistema. |
| Entradas | El administrador ingresa algunos de los datos para buscar el usuario en el sistema (DNI, rol, correo electrónico) y se le asigna un rol (Personal/Organizador). |
| Proceso | El administrador le asigna un rol de personal o organizador al usuario. |
| Salidas | Confirmación de asignación de rol exitosa. |
| Restricciones | Solo el administrador podrá asignar roles. Cada usuario deberá tener un único rol activo. El rol debe ser asignado obligatoriamente para acceder al sistema. |
| Dependencias | Disponibilidad de la base de datos. Usuario previamente registrado. |

##### RF006 – Modificar usuario

| Campo | Contenido |
|---|---|
| Caso de uso asociado | CDU006 |
| Historial de usuario | El administrador modifica los datos de un usuario registrado en el sistema. |
| Descripción | El sistema deberá permitir al administrador editar los datos de los usuarios registrados, incluyendo nombre, apellido, DNI, contraseña y correo electrónico. |
| Entradas | El administrador ingresa: DNI del usuario o correo electrónico para realizar la búsqueda. Nuevos datos del usuario: nombre, apellido, DNI, contraseña, correo electrónico. |
| Proceso | El administrador busca un usuario registrado en el sistema. Si el usuario existe, el sistema mostrará sus datos actuales y permitirá modificar la información correspondiente. Una vez confirmados los cambios, el sistema actualizará los datos del usuario. |
| Salidas | Confirmación de modificación exitosa. Mensaje: "Usuario no encontrado". Mensaje de error en caso de datos inválidos. |
| Restricciones | Solo el administrador podrá realizar modificaciones de usuarios. El usuario debe existir en el sistema. El correo electrónico no debe estar registrado por otro usuario. |
| Dependencias | Disponibilidad de la base de datos. Administrador autenticado en el sistema. Usuario previamente registrado. |

#### 3.2.2 Requerimientos de base de datos

##### RF007 – Gestionar base de datos

| Campo | Contenido |
|---|---|
| Caso de uso asociado | CDU007 |
| Historial de usuario | Administrar la información del sistema. |
| Descripción | El sistema deberá permitir la creación, acceso y administración de una base de datos que almacene toda la información necesaria para el funcionamiento del sistema, incluyendo usuarios, eventos y solicitudes. Esta base de datos deberá garantizar la persistencia, integridad y disponibilidad de los datos. |
| Entradas | Datos a almacenar: usuarios, eventos, solicitudes. |
| Proceso | 1. El administrador ingresa los datos tanto de usuario, evento y solicitudes. 2. El sistema crea la base de datos correspondiente, en caso de que no exista. 3. Se establecen las estructuras necesarias (tablas, relaciones). 4. El sistema permite operaciones de acceso. 5. Se valida la integridad de los datos. |
| Salidas | Base de datos creada correctamente. Acceso exitoso a la información almacenada. |
| Restricciones | Debe garantizarse la integridad y consistencia de los datos. |
| Dependencias | Disponibilidad del servidor de base de datos. Configuración del entorno del sistema. Conectividad del sistema. |

#### 3.2.3 Requerimientos de solicitud

##### RF008 – Crear solicitud

| Campo | Contenido |
|---|---|
| Caso de uso asociado | CDU008 |
| Historial de usuario | Registrar una nueva solicitud. |
| Descripción | El sistema debe registrar los requerimientos iniciales para un evento futuro. |
| Entradas | Fecha solicitada, tipo de evento, número de invitados, lugar preferido, menús requeridos. |
| Proceso | 1. El usuario ingresa los requerimientos. 2. El sistema filtra y valida lugares y fechas según disponibilidad. 3. Se guarda la solicitud con un estado inicial. |
| Salidas | Solicitud creada con ID único. Listado de opciones de locación disponibles. |
| Restricciones | La fecha de la solicitud debe ser posterior a la fecha actual. |
| Dependencias | Las locaciones y menús deben estar previamente cargados en el sistema. |

##### RF009 – Modificar solicitud

| Campo | Contenido |
|---|---|
| Caso de uso asociado | CDU009 |
| Historial de usuario | Actualiza los datos de una solicitud existente. |
| Descripción | El sistema debe permitir editar cualquier atributo de una solicitud previamente registrada. |
| Entradas | ID solicitud. Nueva/o: fecha, tipo de evento, número de invitados, lugar y/o menús requeridos. |
| Proceso | 1. El usuario selecciona 1 o más solicitudes. 2. El sistema filtra y valida los nuevos valores para encontrar incompatibilidad con otras solicitudes. 3. Se actualiza la base de datos. 4. Se guarda la solicitud modificada. |
| Salidas | Confirmación de modificación exitosa. Mensaje de error si no cumple la capacidad o si la fecha y/o el lugar ya están ocupados. |
| Restricciones | Solo usuarios con el rol "Administrador" pueden modificar. |
| Dependencias | Las locaciones y menús deben estar previamente cargados en el sistema. |

##### RF010 – Eliminar solicitud

| Campo | Contenido |
|---|---|
| Caso de uso asociado | CDU010 |
| Historial de usuario | Eliminar una solicitud del sistema. |
| Descripción | El sistema debe permitir eliminar la lógica de una solicitud del sistema. |
| Entradas | ID de la solicitud. |
| Proceso | 1. El usuario selecciona una o más solicitudes. 2. Se verifica si es/es parte de una planificación general. 3. Se muestra un mensaje de confirmación y una vez aceptado se procede a la eliminación. |
| Salidas | Confirmación de modificación exitosa. |
| Restricciones | La fecha de la solicitud debe ser posterior a la fecha actual. |
| Dependencias | La solicitud debe haber sido creada previamente. |

#### 3.2.5 Requerimientos de evento

##### RF011 – Crear evento

| Campo | Contenido |
|---|---|
| Caso de uso asociado | CDU011 |
| Historial de usuario | Registrar un nuevo evento. |
| Descripción | El sistema debe registrar los requerimientos iniciales de un cliente para un evento futuro. |
| Entradas | Fecha solicitada, tipo de evento, número de invitados, lugar preferido, menús requeridos. |
| Proceso | El usuario ingresa los requerimientos del cliente. El sistema filtra y valida lugares y fechas según disponibilidad. Se guarda la solicitud con un estado inicial. |
| Salidas | Solicitud creada con ID único. Listado de opciones de locación disponibles. |
| Restricciones | La fecha de la solicitud debe ser posterior a la fecha actual. |
| Dependencias | Las locaciones y menús deben estar previamente cargados en el sistema. |

##### RF012 – Eliminar evento

| Campo | Contenido |
|---|---|
| Caso de uso asociado | CDU012 |
| Historial de usuario | El organizador podrá eliminar un evento. |
| Descripción | El sistema deberá permitir al organizador eliminar un evento de la lista de eventos. Ya sea por cancelación, reprogramación o algún error en la solicitud. |
| Entradas | Lista de eventos. |
| Proceso | El organizador selecciona el evento a eliminar. |
| Salidas | El sistema elimina el evento de la lista y de la base de datos. |
| Restricciones | Ninguna. |
| Dependencias | La solicitud del evento debe haber sido aprobada previamente. |

##### RF013 – Validar evento

| Campo | Contenido |
|---|---|
| Caso de uso asociado | CDU013 |
| Historial de usuario | El organizador podrá validar si los datos de un evento son correctos. |
| Descripción | El sistema deberá permitir al organizador acceder a la hoja de servicio del evento para poder validar si los datos del mismo son correctos. |
| Entradas | Lista de eventos. |
| Proceso | El organizador podrá validar el evento indicando su estado: Pendiente de Reserva - Finalizado - Aceptado - En Progreso - Reprogramado - Cancelado. |
| Salidas | El sistema refleja el nuevo estado del evento. |
| Restricciones | Ninguna. |
| Dependencias | El evento debe estar ya creado. |

##### RF014 – Modificar evento

| Campo | Contenido |
|---|---|
| Caso de uso asociado | CDU014 |
| Historial de usuario | El organizador podrá eliminar un evento. |
| Descripción | El sistema deberá permitir al organizador realizar cambios en un evento de la lista de eventos. |
| Entradas | Lista de eventos. |
| Proceso | El organizador selecciona el evento y realiza las modificaciones necesarias. |
| Salidas | El sistema modifica los datos del evento de la lista y de la base de datos. |
| Restricciones | Ninguna. |
| Dependencias | La solicitud del evento debe haber sido aprobada previamente. |

#### 3.2.6 Requerimientos de web

##### RF015 – Generar hoja de servicio

| Campo | Contenido |
|---|---|
| Caso de uso asociado | CDU015 |
| Historial de usuario | El usuario deberá poder acceder a la hoja de servicio. |
| Descripción | El sistema deberá permitir al organizador acceder a la hoja de servicio del evento. |
| Entradas | Todos los datos del evento: tipo de evento, fecha, duración, ubicación, cantidad de asistentes, requerimientos específicos. |
| Proceso | El organizador decide generar la hoja de servicio. |
| Salidas | El sistema genera la hoja de servicio. |
| Restricciones | Ninguna. |
| Dependencias | El evento debe estar cargado en la base de datos. |

### 3.3 Requerimientos de implantación

- El sistema deberá poder instalarse en computadoras con sistema operativo Windows 10 o superior.
- El sistema requerirá una base de datos centralizada para almacenar usuarios, eventos y solicitudes.
- El sistema deberá contar con conexión a internet para permitir el acceso multiusuario y el envío de
  correos electrónicos.
- El sistema deberá permitir la creación inicial de un usuario administrador durante la primera
  ejecución.

---

## 4. Casos de uso

### 4.1 CDU 001 – Registro de usuario – RF001

- **Requerimiento asociado:** RF001
- **Precondición:** ingresar al sistema.
- **Actor:** Usuario. Este usuario tendrá el rol de Organizador.
- **Caso de uso relacionado:** CDU 002

| Curso normal | Alternativas |
|---|---|
| 1) El usuario completa un formulario con los siguientes datos: nombre, apellido, dni, contraseña y mail. | – |
| 2) El sistema verifica la validez de los datos del usuario. | – |
| 3) Si las credenciales son correctas, entonces el usuario se registrará con éxito. | 3.1) Si las credenciales son incorrectas, se muestra un mensaje de error en el campo correspondiente. |

### 4.2 CDU 002 – Iniciar sesión – RF002

- **Requerimiento asociado:** RF002
- **Precondición:** el usuario debe estar previamente registrado en el sistema.
- **Actor:** Usuario. Este usuario tendrá el rol de Personal u Organizador.
- **Caso de uso relacionado:** CDU 002

| Curso normal | Alternativas |
|---|---|
| 1) El usuario ingresa su email y contraseña. | – |
| 2) El sistema verifica si el usuario está registrado. | – |
| 3) Si el usuario está registrado, lo lleva a la ventana principal. | 3.1) Si el usuario no está registrado, se muestra un mensaje de error: "El usuario no está registrado en el sistema". |

### 4.3 CDU 003 – Restablecer contraseña – RF003

- **Requerimiento asociado:** RF003
- **Precondición:** el usuario debe estar previamente registrado en el sistema.
- **Actor:** Usuario. Este usuario tendrá un rol de Organizador.
- **Caso de uso relacionado:** CDU 002

| Curso normal | Alternativas |
|---|---|
| 1) El usuario presiona un enlace llamado "Restablecer contraseña". | – |
| 2) El sistema lo redirige a una pantalla donde puede establecer una nueva contraseña. | – |
| 3) El usuario ingresa su nueva contraseña en dos campos: uno para escribirla y otro para confirmarla. | – |
| 4) Si ambas contraseñas coinciden, el sistema envía un correo electrónico al usuario con un enlace para activar la nueva contraseña. | 4.1) Si las contraseñas no coinciden se mostrará un mensaje de error "Contraseñas no coinciden". |
| 5) Una vez activada, la contraseña se actualiza exitosamente en el sistema. | – |

### 4.4 CDU 004 – Gestionar usuario y contraseña – RF004

- **Requerimiento asociado:** RF004
- **Precondición:** el usuario debe estar previamente registrado en el sistema.
- **Actor:** Administrador
- **Caso de uso relacionado:** CDU 006

| Curso normal | Alternativas |
|---|---|
| 1) El administrador ingresa los datos de usuario para buscarlo en el sistema (dni, rol y correo electrónico). | 1.1) Si los datos ingresados por el administrador son incorrectos, se mostrará un mensaje de error "Usuario ingresado no encontrado". |
| 2) El sistema muestra en la interfaz la información del usuario. | – |

### 4.5 CDU 005 – Asignar roles – RF005

- **Requerimiento asociado:** RF005
- **Precondición:** el usuario debe estar previamente registrado en el sistema.
- **Actor:** Administrador
- **Caso de uso relacionado:** CDU 006

| Curso normal | Alternativas |
|---|---|
| 1) El administrador ingresa los datos de usuario para buscarlo en el sistema (dni, rol y correo electrónico). | 1.1) Si los datos ingresados por el administrador son incorrectos, se mostrará un mensaje de error "Usuario ingresado no encontrado". |
| 2) El sistema muestra en la interfaz la información del usuario. | – |
| 3) El administrador podrá asignar un rol de personal u organizador al usuario. | – |

### 4.6 CDU 006 – Modificar usuario – RF006

- **Requerimiento asociado:** RF006
- **Precondición:** el administrador debe haber iniciado sesión en el sistema y el usuario debe estar
  previamente registrado.
- **Actor:** Administrador
- **Caso de uso relacionado:** CDU 004

| Curso normal | Alternativas |
|---|---|
| 1) El administrador accede al módulo de gestión de usuarios. | 1.1) Si los datos ingresados por el administrador son incorrectos, se mostrará un mensaje de error "Usuario ingresado no encontrado". |
| 2) El sistema muestra una pantalla de búsqueda de usuarios. | – |
| 3) El administrador ingresa el DNI o correo electrónico del usuario. | – |
| 4) El sistema busca al usuario y muestra sus datos actuales. | 4.1) Si el usuario no existe, el sistema mostrará el mensaje: "Usuario no encontrado". |
| 5) El administrador modifica uno o varios de los siguientes datos: nombre, apellido, DNI, contraseña, correo electrónico. | – |
| 6) El administrador confirma la modificación de los datos. | 6.1) Si algún dato ingresado es inválido o incompleto, el sistema mostrará un mensaje de error solicitando corregir la información. |
| 7) El sistema valida la información ingresada y actualiza los datos del usuario correctamente. | – |
| 8) El sistema muestra el mensaje: "Modificación de usuario exitosa". | – |

### 4.7 CDU 007 – Gestionar base de datos – RF007

- **Requerimiento asociado:** RF007
- **Precondición:** debe existir conexión con la base de datos.
- **Actor:** Administrador
- **Caso de uso relacionado:** CDU 004

| Curso normal | Alternativas |
|---|---|
| 1) El sistema verifica si la base de datos existe. | – |
| 2) El sistema crea la base de datos si no existe. | 2.1) Si la base de datos ya existe, el sistema omite la creación y permite el acceso directamente. |
| 3) El sistema genera las estructuras necesarias (tablas y relaciones). | – |
| 4) El administrador accede a la base de datos. | – |
| 5) El sistema permite realizar operaciones (alta, baja, modificación y consulta). | – |
| 6) El sistema guarda los cambios realizados. | – |
| 7) El sistema muestra un mensaje de confirmación de operación exitosa. | 7.1) Si ocurre un error de conexión con el servidor, el sistema muestra un mensaje de error y no permite continuar con la operación. |

### 4.8 CDU 008 – Crear solicitud – RF008

- **Requerimiento asociado:** RF008
- **Precondición:** ingresar al sistema.
- **Actor:** Administrador
- **Caso de uso relacionado:** –

| Curso normal | Alternativas |
|---|---|
| 1) El actor inicia una nueva solicitud de cliente. | – |
| 2) El actor ingresa: fecha solicitada, tipo de evento, número de invitados, lugar preferido y menús. | – |
| 3.1) El sistema filtra las locaciones y fechas según la disponibilidad registrada. | 3.2) Si no hay disponibilidad para la fecha o capacidad solicitada, el sistema sugiere alternativas. |
| 4) El sistema asigna un ID único a la solicitud y la guarda con un estado inicial "Aceptada". | – |
| 5) El sistema muestra un mensaje de solicitud creada exitosamente y lista las opciones disponibles. | – |

### 4.9 CDU 009 – Modificar solicitud – RF009

- **Requerimiento asociado:** RF009
- **Precondición:** deben existir solicitudes previamente registradas en el sistema.
- **Actor:** Administrador
- **Caso de uso relacionado:** CDU 008

| Curso normal | Alternativas |
|---|---|
| 1) El usuario selecciona 1 o más solicitudes. | – |
| 2) El sistema filtra y valida los nuevos valores para encontrar incompatibilidad con otras solicitudes. | – |
| 3.1) Se actualiza la base de datos. | 3.2) Si hay incompatibilidad se muestra mensaje de error. |
| 4) Se guarda la solicitud modificada. | – |

### 4.10 CDU 010 – Eliminar solicitud – RF010

- **Requerimiento asociado:** RF010
- **Precondición:** deben existir solicitudes previamente registradas en el sistema.
- **Actor:** Administrador
- **Caso de uso relacionado:** CDU 008

| Curso normal | Alternativas |
|---|---|
| 1) El usuario selecciona una o más solicitudes. | – |
| 2) Se verifica si es/es parte de una planificación general. | – |
| 3.1) Se muestra un mensaje de confirmación y una vez aceptado se procede a la eliminación. | – |

### 4.11 CDU 011 – Crear evento – RF011

- **Requerimiento asociado:** RF011
- **Precondición:** el organizador debe haber ingresado previamente al sistema.
- **Actor:** Organizador
- **Caso de uso relacionado:** –

| Curso normal | Alternativas |
|---|---|
| 1) El usuario selecciona 1 o más solicitudes. | – |
| 2) El sistema filtra y valida los nuevos valores para encontrar incompatibilidad con otros eventos. | – |
| 3) Se actualiza la base de datos y se guarda la fecha en el calendario. | – |
| 4) Se guarda el evento. | – |

### 4.12 CDU 012 – Eliminar evento – RF012

- **Requerimiento asociado:** RF012
- **Precondición:** el evento debe estar guardado en la base de datos.
- **Actor:** Organizador
- **Caso de uso relacionado:** CDU 011

| Curso normal | Alternativas |
|---|---|
| 1) El organizador selecciona el evento a eliminar. | – |
| 2) El organizador selecciona el botón "eliminar". | – |
| 3) El sistema pide confirmación para eliminar. | – |
| 4) El evento es eliminado del sistema y de la base de datos. | – |

### 4.13 CDU 013 – Validar evento – RF013

- **Requerimiento asociado:** RF013
- **Precondición:** todos los datos del evento deben estar guardados en la base de datos.
- **Actor:** Organizador
- **Caso de uso relacionado:** CDU 011

| Curso normal | Alternativas |
|---|---|
| 1) El organizador selecciona el evento que quiere validar. | 1.1) El organizador selecciona un evento que ya fue realizado. |
| 2) El organizador selecciona el botón con el nuevo estado del evento. | 1.2) El sistema imprime el mensaje "Evento ya finalizado". |
| 3) El sistema muestra por pantalla el nuevo estado del evento. | – |

### 4.14 CDU 014 – Modificar evento – RF014

- **Requerimiento asociado:** RF014
- **Precondición:** todos los datos del evento deben estar guardados en la base de datos.
- **Actor:** Organizador
- **Caso de uso relacionado:** CDU 011

| Curso normal | Alternativas |
|---|---|
| 1) El organizador selecciona el evento que quiere modificar. | 1.1) El organizador selecciona un evento que ya fue realizado. |
| 2) El organizador realiza las modificaciones necesarias. | 1.2) El sistema imprime el mensaje "No se puede modificar un evento ya finalizado". |
| 3) El sistema muestra por pantalla el evento con los datos modificados. | – |

### 4.15 CDU 015 – Generar hoja de servicio – RF015

- **Requerimiento asociado:** RF015
- **Precondición:** todos los datos del evento deben estar guardados en la base de datos.
- **Actor:** Organizador
- **Caso de uso relacionado:** –

| Curso normal | Alternativas |
|---|---|
| 1) El organizador selecciona el evento del que quiere generar la hoja de servicio. | – |
| 2) El organizador selecciona el botón "Generar Hoja de Servicio". | – |
| 3) El sistema muestra por pantalla la hoja de servicio en formato PDF con todos los datos relevantes del evento. | – |

---

## 5. Diseño de clases UML

En esta fase se realizará el diseño conceptual del sistema mediante un diagrama de clases UML en nivel
de análisis. Se identificarán los principales conceptos del dominio, sus atributos relevantes y las
relaciones existentes entre ellos, permitiendo representar la estructura general del sistema sin
incluir detalles de implementación.

El diagrama está en el [documento PDF](docs/SRS-Subsistema1-v1.1.4.pdf).

---

## 6. Diagramas de colaboración de objetos

Los diagramas están en el [documento PDF](docs/SRS-Subsistema1-v1.1.4.pdf).

| DCO | Nombre | Casos de uso |
|---|---|---|
| 6.1 DCO 001 | Registrar usuario | CDU 001 |
| 6.2 DCO 002 | Iniciar sesión | CDU 002 – CDU 003 |
| 6.3 DCO 003 | Gestionar usuarios | CDU 004 – CDU 005 – CDU 006 |
| 6.4 DCO 004 | Gestionar base de datos | CDU 007 |
| 6.5 DCO 005 | Crear solicitud | CDU 008 |
| 6.6 DCO 006 | Modificar solicitud | CDU 009 |
| 6.7 DCO 007 | Eliminar solicitud | CDU 010 |
| 6.8 DCO 008 | Crear evento | CDU 011 |
| 6.9 DCO 009 | Eliminar evento | CDU 012 |
| 6.10 DCO 010 | Validar evento | CDU 013 |
| 6.11 DCO 011 | Modificar evento | CDU 014 |
| 6.12 DCO 012 | Generar hoja de servicio | CDU 015 |

---

## 7. Diagramas de secuencia

Los diagramas están en el [documento PDF](docs/SRS-Subsistema1-v1.1.4.pdf). Debajo, el pseudocódigo de
cada uno.

### 7.1 DDS 001 – Registrar usuario – CDU 001

```
INICIO
Ingresar nombre
Ingresar apellido
Ingresar dni
Ingresar contraseña
Ingresar mail
SI VerificarCampos(nombre, apellido, dni, contraseña, mail) ENTONCES
    GuardarUsuario(nombre, apellido, dni, contraseña, mail)
    Mostrar "Usuario registrado correctamente"
SINO
    Mostrar "Error en los datos ingresados"
FIN SI
FIN
```

### 7.2 DDS 002 – Iniciar sesión – CDU 002

```
INICIO
Ingresar usuario
Ingresar contraseña
SI ValidarDatos(usuario, contraseña) ENTONCES
    Mostrar ("Sesión iniciada")
    AbrirInterfazUsuario()
SINO
    Mostrar "Error de autenticación"
FIN SI
FIN
```

### 7.3 DDS 003 – Restablecer contraseña – CDU 003

```
INICIO
Solicitar correo electrónico al usuario
Leer mail
SI validarMail(mail) = VERDADERO ENTONCES
    enviarMailRecuperacion(mail)
    Mostrar "Se envió un enlace de recuperación"
    Esperar que el usuario acceda al enlace
    Solicitar nueva contraseña
    Leer nuevaContraseña
    SI comprobarContraseña(nuevaContraseña) = VERDADERO ENTONCES
        actualizarContraseña(mail, nuevaContraseña)
        Mostrar "Contraseña actualizada correctamente"
    SINO
        Mostrar "La contraseña no cumple los requisitos"
    FIN SI
SINO
    Mostrar "El correo ingresado no existe"
FIN SI
FIN ALGORITMO
```

### 7.4 DDS 004 – Gestionar usuario y contraseña – CDU 004

```
INICIO
El administrador ingresa los datos de un usuario
GestorUsuarios.buscarUsuario(datosUsuario)
SI el usuario existe ENTONCES
    datosUsuarioEncontrado ← obtenerDatosUsuario()
    Mostrar datosUsuarioEncontrado al administrador
SINO
    Mostrar "Usuario no encontrado"
FIN SI
FIN ALGORITMO
```

### 7.5 DDS 005 – Asignar roles – CDU 005

```
INICIO
El administrador ingresa los datos del usuario al que se le asignará un rol
GestorUsuarios recibe los datos del usuario
GestorUsuarios solicita a Usuarios buscarUsuario(datosUsuario)
Usuarios busca el usuario solicitado
SI el usuario existe ENTONCES
    Usuarios devuelve los datos del usuario a GestorUsuarios
    GestorUsuarios muestra los datos del usuario al administrador
    El administrador asigna el rol al usuario
    GestorUsuarios solicita a Usuarios modificarRol(usuario, rol)
    Usuarios modifica el rol del usuario
    Usuarios devuelve mensaje de modificación exitosa
    GestorUsuarios muestra al administrador:
        "El rol fue modificado correctamente"
SINO
    GestorUsuarios muestra al administrador:
        "Usuario no encontrado"
FIN SI
FIN ALGORITMO
```

### 7.6 DDS 006 – Modificar usuario – CDU 006

```
INICIO
El administrador ingresa los datos de un usuario
GestorUsuarios recibe los datos ingresados
GestorUsuarios solicita a Usuarios buscarUsuario(datosUsuario)
Usuarios busca el usuario solicitado
SI el usuario existe ENTONCES
    Usuarios devuelve los datos del usuario a GestorUsuarios
    GestorUsuarios muestra los datos del usuario al administrador
    El administrador modifica los datos del usuario
    GestorUsuarios solicita a Usuarios modificarDatos(usuario, nuevosDatos)
    Usuarios modifica los datos del usuario
    Usuarios devuelve mensaje de modificación exitosa
    GestorUsuarios muestra al administrador:
        "Los datos del usuario fueron modificados correctamente"
SINO
    GestorUsuarios muestra al administrador:
        "Usuario no encontrado"
FIN SI
FIN ALGORITMO
```

### 7.7 DDS 007 – Gestionar base de datos – CDU 007

```
INICIO
SI verificaEstructuraBD {
    inicializaTablaEventos();
    inicializaTablaSolicitudEvento();
    inicializaTablaUsuario();
    inicializaTablaLocación();
}
SINO {
    creaTablas();
    creaRelaciones();
}
...
FIN
```

### 7.8 DDS 008 – Crear solicitud – CDU 008

```
Ingresar fecha
Ingresar tipoEvento
Ingresar cantidadInvitados
Ingresar lugarPreferido
Ingresar menu
SI existeDisponibilidad(fecha, cantidadInvitados) ENTONCES
    id ← generarID()
    crear Solicitud()
    Solicitud.id ← id
    Solicitud.estado ← "Aceptada"
    guardarSolicitud()
    mostrar("Solicitud creada exitosamente")
SINO
    mostrar("Error al crear la solicitud")
FIN SI
FIN
```

### 7.9 DDS 009 – Modificar solicitud – CDU 009

```
Seleccionar solicitud
Ingresar nuevos datos
SI verificarCampos() ENTONCES
    actualizarSolicitud()
    guardarCambios()
    mostrar("Solicitud modificada")
SINO
    mostrar("Error al modificar la solicitud")
FIN SI
FIN
```

### 7.10 DDS 010 – Eliminar solicitud – CDU 010

```
INICIO
Seleccionar solicitud
SI Eliminar solicitud() ENTONCES
    BorrarSolicitud()
    guardarCambios()
    mostrar("Solicitud eliminada")
SINO
    mostrar("Error: solicitud no eliminada")
FIN SI
FIN
```

### 7.11 DDS 011 – Crear evento – CDU 011

```
INICIO
Seleccionar solicitud
SI verificarFecha Y obtener solicitud ENTONCES
    crearEvento()
    registrarFechaCalendario()
    guardarEvento()
    mostrar("Evento creado correctamente")
SINO
    mostrar("No se pudo crear el evento")
FIN SI
FIN
```

### 7.12 DDS 012 – Eliminar evento – CDU 012

```
INICIO
Seleccionar Evento
SI Eliminar evento() ENTONCES
    EliminarEvento()
    guardarCambios()
    mostrar("Evento eliminado")
SINO
    mostrar("Error: evento no eliminado")
FIN SI
FIN
```

### 7.13 DDS 013 – Validar evento – CDU 013

```
INICIO
Seleccionar Evento
SI evento() ENTONCES
    validarEvento()
    validarFecha()
    mostrar("Evento validado con exito")
SINO
    mostrar("Error: Evento no validado")
FIN SI
FIN
```

### 7.14 DDS 014 – Modificar evento – CDU 014

```
INICIO
SeleccionarEvento()
BuscarEvento(idEvento)
SI ObtenerEvento() = TRUE ENTONCES
    IngresarModificaciones()
    SI ValidarDatos() = TRUE ENTONCES
        ModificarDatos()
        Mostrar("Evento modificado correctamente")
    SINO
        Mostrar("Error: Los datos ingresados no son válidos")
    FIN SI
SINO
    Mostrar("Error: El evento no existe")
FIN SI
FIN
```

### 7.15 DDS 015 – Generar hoja de servicio – CDU 015

```
INICIO
Seleccionar Evento
SI evento() ENTONCES
    generarHojadeServicio()
    calcularCostes()
    mostrar("Hoja de Servicio")
SINO
    mostrar("Error: Hoja no generada")
FIN SI
FIN
```

---

## Usuarios de prueba en la base de datos

La base de datos (`gestion_eventos` en PostgreSQL) se crea sola la primera vez que se ejecuta el
programa, junto con todas sus tablas. Como al principio está vacía, aparece la **Configuración
inicial**:

1. Se crea el **administrador** con los datos que elijas (nombre, apellido, DNI, correo y
   contraseña). No hay un administrador fijo: es el que creás en ese paso.
2. La casilla **"Cargar datos de ejemplo para probar el sistema"** viene marcada. Si la dejás
   marcada, se guardan en la base los usuarios de prueba de la tabla de abajo, junto con 5 tipos de
   evento, 5 locaciones (una con fechas bloqueadas), 4 solicitudes y 2 planificaciones (un casamiento
   y un congreso de 3 jornadas).

Esta configuración se hace una sola vez: después los datos quedan guardados en la base y los usuarios
de prueba se muestran también en la pantalla de inicio de sesión.

| Rol | Correo | Contraseña | Qué puede hacer |
|---|---|---|---|
| Organizador | maria@mail.com | Clave2026 | Planificaciones, Eventos, Calendario |
| Cliente | juan@mail.com | Clave2026 | Calendario (solo sus eventos) |
| Cliente | lucia@mail.com | Clave2026 | Calendario (solo sus eventos) |
| Sin rol | sofia@mail.com | Clave2026 | No puede ingresar hasta que el administrador le asigne un rol |

Si el programa funciona sin PostgreSQL (modo demostración, con `habilitado=false` en
`basedatos.properties`), los mismos datos se cargan en memoria y además existe un administrador fijo:
**admin@eventos.com** con contraseña **Admin2026**. En ese modo los datos se pierden al cerrar el
programa.

# Pedidos360 - Sistema Cloud Native (Duoc UC)

Este proyecto es la solución integral del sistema de gestión de pedidos **Pedidos360** para la asignatura de Desarrollo Cloud Native I. Integra frontend en Angular con Azure AD (MSAL), backend en Spring Boot con PostgreSQL, mensajería transaccional asíncrona con **RabbitMQ** (Evaluación 3) y streaming distribuido de alto rendimiento con **Apache Kafka** en clúster multi-broker (Evaluación 5).

---

## Como levantar todo rapido con Docker

La forma mas sencilla de correr todo el sistema (12 contenedores: clúster Kafka de 3 brokers, ZooKeeper, RabbitMQ, PostgreSQL, backend, consumidores, administradores y frontend) es con Docker Compose:

```bash
docker compose up --build
```

Una vez que termine de compilar y levantar los contenedores:
- Frontend: http://localhost:4200
- Backend API: http://localhost:8080/api/health
- Kafka UI (Consola Clúster): http://localhost:8085
- Consola RabbitMQ: http://localhost:15672
- Administrador Kafka REST: http://localhost:8083
- Administrador RabbitMQ REST: http://localhost:8081
- Base de datos PostgreSQL: puerto 5432

Para detener los servicios y limpiar volúmenes:
```bash
docker compose down -v
```

---
## Evaluacion Parcial 3: RabbitMQ

La solucion mantiene Pedidos360 como productor autenticado con JWT/IDaaS y Angular como frontend. Se agregaron dos aplicaciones Spring Boot independientes: `rabbit-consumer/` (consumidores por dominio) y `rabbit-admin/` (administracion REST). El demo React de `rabbit-lab/` no forma parte del despliegue principal.

> **Nota de Diseño Arquitectónico (RabbitMQ Single-Node):**
> Por decisión de diseño y alcance operativo, RabbitMQ se implementa como un nodo único altamente confiable para la gestión de mensajería transaccional puntual (notificaciones, tickets y emisión de documentos), reservando la complejidad de un clúster distribuido multi-broker (con 3 réplicas, quorum y replicación en sincronía) a Apache Kafka, donde el caso de uso de streaming masivo de eventos y analítica continua lo justifica plenamente.

### Levantar todo con Docker

Requisitos: Docker Desktop iniciado y Docker Compose v2.

Desde la carpeta raiz del proyecto:

```powershell
docker compose up --build
```

Compose levanta PostgreSQL, RabbitMQ, el backend productor, los consumidores y el frontend Angular. Las URLs locales son:

- Angular: http://localhost:4200
- API Pedidos360: http://localhost:8080/api/health
- API administrador RabbitMQ: http://localhost:8081
- Consola RabbitMQ: http://localhost:15672 (usuario y clave locales por defecto: `pedidos360`)

Los puertos RabbitMQ y administrador se publican solo en `127.0.0.1`. Las credenciales locales por defecto son para desarrollo; se pueden cambiar con `RABBITMQ_USERNAME` y `RABBITMQ_PASSWORD` antes de ejecutar Compose.

Para autenticar contra Azure, copia `.env.example` a `.env` (`Copy-Item .env.example .env` en PowerShell) y completa `AZURE_TENANT_ID`, `AZURE_CLIENT_ID` y `AZURE_APP_ID_URI` antes de iniciar los contenedores. Sin esas variables, el backend conserva su modo de simulacion local. No se deben guardar IDs, secretos ni tokens en `application.yml` ni en Git.

Para seguir los mensajes y detener el entorno:

```powershell
docker compose logs -f backend rabbit-consumer rabbit-admin
docker compose down
```

Los datos PostgreSQL quedan en el volumen `postgres_data`; para borrarlo deliberadamente, ejecutar `docker compose down -v`.

### Ejecucion local por Maven

Requisitos: Java 17, Maven y Docker con RabbitMQ y PostgreSQL iniciados. Desde la raiz, inicia la infraestructura y ejecuta cada servicio en una terminal distinta:

```powershell
docker compose up -d rabbitmq postgres-db
mvn -f backend/pom.xml spring-boot:run
mvn -f rabbit-consumer/pom.xml spring-boot:run
mvn -f rabbit-admin/pom.xml spring-boot:run
npm --prefix frontend install
npm --prefix frontend start
```

Ejecuta cada comando Maven y los comandos npm en su propia terminal; el frontend Angular queda disponible en `http://localhost:4200`. Para el modo Azure real, define las tres variables indicadas antes de abrir la terminal del backend.

### Endpoints del administrador

- `POST /queues` y `DELETE /queues/{name}`
- `POST /exchanges` y `DELETE /exchanges/{name}`
- `POST /bindings` y `DELETE /bindings`

Ejemplo para crear una cola:

```json
{"name":"pedidos.prueba","durable":true}
```

El administrador no implementa autenticacion propia y debe mantenerse restringido a la red local/de confianza; no se debe publicar directamente en Internet.

### Pruebas

```powershell
mvn -f backend/pom.xml test
mvn -f rabbit-consumer/pom.xml test
mvn -f rabbit-admin/pom.xml test
```

## Evaluacion Parcial 5: Apache Kafka (Streaming, Analitica y Administracion)

Esta etapa complementa la arquitectura Cloud Native integrando **Apache Kafka** para streaming de eventos y analitica en tiempo real, coexistiendo armonicamente con **RabbitMQ** (comunicacion transaccional puntual) sin afectar sus integraciones previas.

### Arquitectura de Mensajeria Dual (RabbitMQ + Kafka)

```
                              ┌───────────────────────────────────┐
                              │        pedidos360-backend         │
                              │    (Productor Dual / Spring Boot) │
                              └─────────┬───────────────┬─────────┘
                                        │               │
            [RabbitMQ: Tareas Discretas]│               │[Kafka: Streaming y Analitica]
                                        ▼               ▼
                       ┌──────────────────────┐   ┌──────────────────────────────┐
                       │ Exchange:            │   │ Topico 1:                    │
                       │  pedidos.events      │   │  pedidos.streaming.eventos   │
                       └────────┬─────────────┘   │  (3 particiones, orden key)  │
                                │                 ├──────────────────────────────┤
               ┌────────────────┼────────────┐    │ Topico 2:                    │
               │                │            │    │  pedidos.auditoria.log       │
               ▼                ▼            ▼    │  (retencion 30d, metricas)   │
           pedidos.         pedidos.     pedidos. └──────────────┬───────────────┘
         notifications       tickets     documents               │
               │                │            │                   │
               └────────────────┼────────────┘                   ├──────────────────────────────┐
                                ▼                                ▼                              ▼
                     ┌──────────────────────┐         ┌──────────────────────┐      ┌──────────────────────┐
                     │   rabbit-consumer    │         │    kafka-consumer    │      │    kafka-consumer    │
                     │  (Notif/Ticket/Docs) │         │(pedidos360-analytics)│      │  (pedidos360-audit)  │
                     └──────────────────────┘         └──────────────────────┘      └──────────────────────┘
```

### Tabla de Microservicios y Puertos del Ecosistema

| Componente / Servicio | Tipo | Puerto Host | Descripcion |
| :--- | :--- | :--- | :--- |
| **`frontend`** | Angular | `4200` | UI Web con login IDaaS (Azure AD / MSAL). |
| **`backend`** | Spring Boot | `8080` | API REST pedidos, productor RabbitMQ y productor Kafka. |
| **`rabbit-admin`** | Spring Boot | `8081` | Microservicio de administracion REST para colas y exchanges RabbitMQ. |
| **`rabbit-consumer`** | Spring Boot | `8082` | Consumidor AMQP de notificaciones, tickets y documentos. |
| **`kafka-admin`** | Spring Boot | `8083` | Microservicio de administracion REST para topicos y consumer groups Kafka. |
| **`kafka-consumer`** | Spring Boot | `8084` | Consumidor Kafka con grupos `pedidos360-analytics-group` y `pedidos360-audit-group` (concurrencia 3). |
| **`kafka-ui`** | Web Console (v0.7.2) | `8085` | Consola web de observabilidad de Kafka (brokers, topicos, mensajes y offsets). |
| **`rabbitmq`** | Infraestructura | `5672` / `15672` | Broker y consola web de administracion RabbitMQ (nodo único transaccional). |
| **`kafka-1`** | Broker Streaming | `9092` / `29092` | Broker 1 del clúster Apache Kafka (Replicación 3, ISR 2). |
| **`kafka-2`** | Broker Streaming | `9093` / `29092` | Broker 2 del clúster Apache Kafka (Replicación 3, ISR 2). |
| **`kafka-3`** | Broker Streaming | `9094` / `29092` | Broker 3 del clúster Apache Kafka (Replicación 3, ISR 2). |
| **`zookeeper`** | Coordinador | `2181` | Coordinador de cluster Kafka. |
| **`postgres-db`** | Base de Datos | `5432` | PostgreSQL para persistencia transaccional del backend. |

---

### Especificacion de Topicos Kafka y Parametros de Administracion

El sistema declara y gestiona dos topicos principales y sus correspondientes colas de descarte (DLT) con configuraciones ajustadas a las necesidades del negocio (con factor de replicación 3 en el clúster):

| Topico | Particiones | Replicas | Configuraciones (`retention.ms`, `cleanup.policy`, `min.insync.replicas`) | Clave de Particionado (`Key`) | Proposito |
| :--- | :---: | :---: | :--- | :--- | :--- |
| **`pedidos.streaming.eventos`** | **3** | **3** | `retention.ms=604800000` (7 dias)<br>`cleanup.policy=delete`<br>`min.insync.replicas=2` | `numeroPedido` (ej: `PED-2025-004`) | Ingesta de ciclo de vida (`PEDIDO_CREADO`, `ESTADO_ACTUALIZADO`). Garantiza orden estricto por pedido mediante Sticky Partitioning. |
| **`pedidos.auditoria.log`** | **3** | **3** | `retention.ms=2592000000` (30 dias)<br>`cleanup.policy=delete`<br>`min.insync.replicas=2` | `canalVenta` / `id` (ej: `PORTAL_B2B`) | Trazabilidad y auditoria de acciones (`CREACION_PEDIDO`, `CAMBIO_ESTADO`, usuario, montos). |
| **`pedidos.streaming.eventos.DLT`** | **3** | **3** | `retention.ms=1209600000` (14 dias)<br>`cleanup.policy=delete` | Mantiene clave original (soporte raw bytes) | Dead Letter Topic para eventos de streaming con errores irrecuperables. |
| **`pedidos.auditoria.log.DLT`** | **3** | **3** | `retention.ms=1209600000` (14 dias)<br>`cleanup.policy=delete` | Mantiene clave original (soporte raw bytes) | Dead Letter Topic para registros de auditoria fallidos. |

---

### Grupos de Consumidores (Consumer Groups)

Se implementaron grupos de consumidores diferenciados que permiten balanceo de carga, resiliencia e independencia de procesamiento (Publish/Subscribe):

1. **`pedidos360-analytics-group`**:
   - Suscrito a: `pedidos.streaming.eventos`.
   - Delegacion: `AnalyticsProcessingService` para procesar metricas continuas (volumen, montos acumulados, conteos por estado).
2. **`pedidos360-audit-group`**:
   - Suscrito a: `pedidos.auditoria.log`.
   - Delegacion: `AuditProcessingService` para almacenar traza historica de operaciones.
3. **`pedidos360-dlt-monitor-group`**:
   - Suscrito a los topicos `.DLT` para monitoreo de anomalias operacionales.

---

### Resiliencia y Manejo de Errores

* **Deserializacion Segura:** Uso de `ErrorHandlingDeserializer` tanto en Key como en Value para impedir bucles infinitos causados por "poison pills" (mensajes con JSON malformado).
* **Control de Reintentos:** `DefaultErrorHandler` con `FixedBackOff` configurado a 3 reintentos antes del descarte.
* **Dead Letter Topic (DLT):** `DeadLetterPublishingRecoverer` desvia automaticamente los mensajes fallidos a `<topic>.DLT` registrando particion, offset y causa del error.
* **Separacion de Capas:** Los listeners (`@KafkaListener`) estan desacoplados de los servicios de dominio de negocio (`AnalyticsProcessingService` y `AuditProcessingService`).

---

### Endpoints del Microservicio Administrador Kafka (`kafka-admin` en puerto 8083)

El microservicio expone operaciones REST completas sobre Kafka utilizando el `AdminClient` oficial:

#### 1. Listar topicos
```bash
curl http://localhost:8083/topics
```

#### 2. Consultar detalle y configuraciones de un topico (particiones, retencion, ISR)
```bash
curl http://localhost:8083/topics/pedidos.streaming.eventos
```

#### 3. Crear nuevo topico con particiones y retencion personalizada
```bash
curl -X POST http://localhost:8083/topics \
  -H "Content-Type: application/json" \
  -d '{
    "name": "pedidos.reportes.especiales",
    "partitions": 3,
    "replicationFactor": 1,
    "configs": {
      "retention.ms": "86400000",
      "cleanup.policy": "delete"
    }
  }'
```

#### 4. Modificar parametros de configuracion de un topico (ej: cambiar retencion)
```bash
curl -X PUT http://localhost:8083/topics/pedidos.reportes.especiales/configs \
  -H "Content-Type: application/json" \
  -d '{
    "configs": {
      "retention.ms": "172800000"
    }
  }'
```

#### 5. Incrementar particiones de un topico
```bash
curl -X PUT http://localhost:8083/topics/pedidos.reportes.especiales/partitions \
  -H "Content-Type: application/json" \
  -d '{
    "totalPartitions": 6
  }'
```

#### 6. Eliminar un topico
```bash
curl -X DELETE http://localhost:8083/topics/pedidos.reportes.especiales
```

#### 7. Listar grupos de consumidores activos
```bash
curl http://localhost:8083/consumer-groups
```

#### 8. Inspeccionar detalle de un grupo de consumidores (miembros, asignaciones, offsets)
```bash
curl http://localhost:8083/consumer-groups/pedidos360-analytics-group
```

---

### Monitoreo en Kafka UI

Ingresa en tu navegador a:
```
http://localhost:8085
```
Encontraras el cluster `pedidos360-kafka` donde podras:
- Ver la lista de topicos y sus particiones (0, 1, 2).
- Inspeccionar mensajes en tiempo real con sus claves (`Key`), particion y `Offset`.
- Revisar el estado `STABLE` y asignacion de los consumer groups.

---

### Ejecucion de Pruebas Automatizadas

Para validar las pruebas unitarias de todos los componentes:

```powershell
mvn -f backend/pom.xml test
mvn -f kafka-consumer/pom.xml test
mvn -f kafka-admin/pom.xml test
mvn -f rabbit-consumer/pom.xml test
mvn -f rabbit-admin/pom.xml test
```

---

## Estructura del proyecto

- `frontend/`: Aplicacion en Angular con MSAL, guards, interceptores y vistas del sistema.
- `backend/`: Microservicio en Spring Boot con JPA, autenticacion JWT, productor RabbitMQ y productor Kafka.
- `rabbit-consumer/`: Microservicio Spring Boot consumidor de colas RabbitMQ.
- `rabbit-admin/`: Microservicio Spring Boot para administracion REST de colas y exchanges RabbitMQ.
- `kafka-consumer/`: Microservicio Spring Boot consumidor Kafka con grupos de analitica, auditoria y resiliencia DLT.
- `kafka-admin/`: Microservicio Spring Boot para administracion REST de topicos, particiones, retencion y consumer groups Kafka.
- `kafka-tutorial/`: Laboratorio educativo de demostracion basica de particiones y offsets.
- `docker-compose.yml`: Orquestacion completa de los 10 contenedores (PostgreSQL, RabbitMQ, ZooKeeper, Kafka Broker, Kafka UI, backend, consumidores y administradores).
- `GUIA_CONFIGURACION_AZURE.md`: Guia paso a paso para la configuracion en Azure Portal.

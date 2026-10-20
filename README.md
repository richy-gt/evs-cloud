# Pedidos360 - Evaluacion Cloud Native I (Duoc UC)

Este proyecto es la solucion para la Evaluacion Parcial N° 1 de Cloud Native. Es un sistema de gestion de pedidos llamado Pedidos360, compuesto por un frontend en Angular con autenticacion MSAL (Azure AD) y un backend en Spring Boot (Java) protegido con OAuth2 Resource Server y base de datos relacional.

---

## Como levantar todo rapido con Docker

La forma mas sencilla de correr todo el sistema (base de datos PostgreSQL, backend y frontend) es con Docker Compose:

```bash
docker compose up --build
```

Una vez que termine de compilar y levantar los contenedores:
- Frontend: http://localhost:4200
- Backend API: http://localhost:8080/api/health
- Base de datos PostgreSQL: puerto 5432

Para detener los servicios:
```bash
docker compose down
```

---
## Evaluacion Parcial 3: RabbitMQ

La solucion mantiene Pedidos360 como productor autenticado con JWT/IDaaS y Angular como frontend. Se agregaron dos aplicaciones Spring Boot independientes: `rabbit-consumer/` (consumidores por dominio) y `rabbit-admin/` (administracion REST). El demo React de `rabbit-lab/` no forma parte del despliegue principal.

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

## Estructura del proyecto

- `frontend/`: Aplicacion en Angular con MSAL, guards, interceptores y vistas del sistema.
- `backend/`: Microservicio en Java Spring Boot con Spring Security, JPA y validacion de tokens JWT.
- `rabbit-consumer/`: Microservicio Spring Boot consumidor de notificaciones, tickets y documentos.
- `rabbit-admin/`: Microservicio Spring Boot para administrar recursos RabbitMQ mediante REST.
- `docker-compose.yml`: Archivo para levantar base de datos, backend y frontend juntos.
- `GUIA_CONFIGURACION_AZURE.md`: Guia paso a paso para la configuracion en Azure Portal.

# Actividad 3.2.1: Apache Kafka - Topics, Productor, Consumidor, Particiones y Offsets

Este módulo implementa el laboratorio práctico de **Apache Kafka**, diseñado para ejecutarse sin alterar el proyecto principal de Pedidos360/RabbitMQ, funcionando tanto en **entorno local** como en una **Máquina Virtual de Microsoft Azure**.

---

## 🏗️ Arquitectura del Laboratorio

- **ZooKeeper**: Coordinador del cluster Kafka (`puerto 2181`).
- **Kafka Broker**: Servidor de mensajería distribuida (`puerto 9092` / interno `29092`).
- **Kafka UI**: Interfaz gráfica para observar topics, particiones, offsets y mensajes en tiempo real (`puerto 8085`).
- **Spring Boot App (`kafka-tutorial`)**: Aplicación que contiene el Productor REST y el Consumidor con `@KafkaListener` (`puerto 8888`).

---

## 🚀 Despliegue en Máquina Virtual de Azure

En tu VM de Azure, el proyecto está completamente preparado para levantarse con un solo comando Docker Compose sin requerir instalar Java ni Maven manualmente en el sistema operativo:

### 1. Variables de Entorno (`.env`)
En la carpeta `kafka-tutorial/`:
```bash
cp .env.example .env
```
- Si vas a acceder a la API y a Kafka UI a través de la IP pública de la VM, los puertos por defecto son:
  - `KAFKA_UI_PORT=8085`
  - `KAFKA_APP_PORT=8888`
- Si deseas conectar productores/consumidores externos (ej. desde tu laptop) al broker de Kafka en Azure, edita en `.env`:
  ```dotenv
  KAFKA_HOST=<IP_PUBLICA_O_DNS_DE_TU_VM_AZURE>
  ```

### 2. Reglas de Red en Azure (Network Security Group - NSG)
Para acceder desde tu navegador o Postman a los servicios en la VM de Azure, asegúrate de que en el **Portal de Azure -> Virtual Machine -> Networking** tengas reglas de entrada (Inbound security rules) para:
- **Puerto 8085** (Kafka UI)
- **Puerto 8888** (Spring Boot API)
- **Puerto 9092** (Opcional, solo si conectas clientes Kafka externos fuera de la VM)

### 3. Iniciar todo el stack
Desde la carpeta `kafka-tutorial/`:
```bash
docker compose up -d --build
```

Para seguir los logs del consumidor en tiempo real:
```bash
docker compose logs -f kafka-app
```

Para detener los servicios:
```bash
docker compose down
```

---

## 💻 Despliegue en Local

### Opción A: Todo en Docker
```powershell
cd kafka-tutorial
docker compose up -d --build
docker compose logs -f kafka-app
```

### Opción B: Infraestructura en Docker y App en Maven local
1. Inicia solo ZooKeeper, Kafka y Kafka UI:
   ```powershell
   cd kafka-tutorial
   docker compose up -d zookeeper kafka kafka-ui
   ```
2. Ejecuta la aplicación Spring Boot:
   ```powershell
   mvn spring-boot:run
   ```

---

## 📋 Paso a Paso de la Guía

### Paso 1: Crear el Topic `tutorial-particiones`

#### Por Kafka UI:
1. Abre tu navegador en:
   - Local: `http://localhost:8085`
   - En Azure VM: `http://<IP_PUBLICA_AZURE>:8085`
2. Ve a **Topics** en el menú izquierdo.
3. Haz clic en **+ Add a Topic**:
   - **Topic Name:** `tutorial-particiones`
   - **Partitions:** `3` (esencial para observar distribución)
   - **Replication Factor:** `1`
4. Clic en **Create topic**.

#### Alternativa por CLI (terminal de la VM o local):
```bash
docker exec -it kafka-tutorial-kafka kafka-topics --create \
  --bootstrap-server localhost:9092 \
  --topic tutorial-particiones \
  --partitions 3 \
  --replication-factor 1
```

Verificar:
```bash
docker exec -it kafka-tutorial-kafka kafka-topics --describe \
  --bootstrap-server localhost:9092 \
  --topic tutorial-particiones
```

---

### Paso 2: Verificar Salud de la Aplicación

Ejecuta en terminal o navegador:
```bash
curl http://localhost:8888/api/kafka/health
```
*(En Azure: sustituye `localhost` por la IP pública de la VM)*

**Respuesta esperada:**
```
Aplicación de Kafka Tutorial está corriendo en puerto 8888
```

---

### Paso 3: PRUEBA 1 - Round Robin (Sin Key)

**Objetivo:** Observar cómo Kafka reparte equitativamente los mensajes entre las 3 particiones (0, 1 y 2).

Ejecuta 3 peticiones en orden (por navegador, curl o Postman):
```bash
curl "http://localhost:8888/api/kafka/send?message=Mensaje1"
curl "http://localhost:8888/api/kafka/send?message=Mensaje2"
curl "http://localhost:8888/api/kafka/send?message=Mensaje3"
```

**Salida en consola (`docker compose logs -f kafka-app`):**
```text
====================================================================================================
 MENSAJE RECIBIDO
 📍 Partición: 0
 Offset: 0
 Key: (sin key)
 Mensaje: Mensaje1
====================================================================================================

====================================================================================================
 MENSAJE RECIBIDO
 📍 Partición: 1
 Offset: 0
 Key: (sin key)
 Mensaje: Mensaje2
====================================================================================================

====================================================================================================
 MENSAJE RECIBIDO
 📍 Partición: 2
 Offset: 0
 Key: (sin key)
 Mensaje: Mensaje3
====================================================================================================
```
> **Observación:** Cada mensaje fue asignado a una partición distinta (0, 1, 2) y el offset comienza en 0 de forma independiente.

---

### Paso 4: PRUEBA 2 - Sticky Partitioning (Con Key)

**Objetivo:** Garantizar que los eventos del ciclo de vida de una misma entidad (ej. `orden-555`) vayan siempre a la misma partición para asegurar su orden secuencial.

Ejecuta las 3 peticiones con la MISMA `key=orden-555`:
```bash
curl "http://localhost:8888/api/kafka/send?message=Paso1-Creada&key=orden-555"
curl "http://localhost:8888/api/kafka/send?message=Paso2-Pagada&key=orden-555"
curl "http://localhost:8888/api/kafka/send?message=Paso3-Enviada&key=orden-555"
```

**Salida en consola (`docker compose logs -f kafka-app`):**
```text
====================================================================================================
 MENSAJE RECIBIDO
 📍 Partición: 1
 Offset: 1
 Key: orden-555
 Mensaje: Paso1-Creada
====================================================================================================

====================================================================================================
 MENSAJE RECIBIDO
 📍 Partición: 1
 Offset: 2
 Key: orden-555
 Mensaje: Paso2-Pagada
====================================================================================================

====================================================================================================
 MENSAJE RECIBIDO
 📍 Partición: 1
 Offset: 3
 Key: orden-555
 Mensaje: Paso3-Enviada
====================================================================================================
```
> **Observación:** Todos los mensajes fueron dirigidos a la **misma partición** (partición 1 en este ejemplo) y los offsets se incrementaron secuencialmente (1, 2, 3), garantizando que el consumidor procese `Paso1 -> Paso2 -> Paso3` en estricto orden.

---

### Paso 5: Monitoreo en Kafka UI

1. Ingresa a `http://localhost:8085` (o IP de tu Azure VM en el puerto `8085`).
2. Entra a **Topics** -> `tutorial-particiones`.
3. Ve a la pestaña **Messages**:
   - Observarás las columnas `Partition`, `Offset`, `Key` y `Value`.
4. **Comprobación de Persistencia:**
   - Si detienes y reinicias el contenedor (`docker compose restart kafka-app`), el consumidor **no** vuelve a leer los mensajes antiguos.
   - **¿Por qué?** Porque Kafka almacena el offset del grupo de consumidores (`mi-grupo-educativo`). Al reconectarse, la aplicación sabe que ya procesó hasta ese offset y espera nuevos mensajes.

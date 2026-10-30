# Docker Commands Cheat Sheet

Most commonly used Docker commands for Java/Spring Boot backend development.

## 1. Docker basics

| Command | Use case |
|---|---|
| `docker --version` | Check Docker version |
| `docker info` | View Docker Engine details |
| `docker ps` | List running containers |
| `docker ps -a` | List all containers (running and stopped) |
| `docker images` | List downloaded images |
| `docker pull mysql:8` | Download an image |
| `docker search mysql` | Search Docker Hub |
| `docker stats` | View container CPU and memory usage |
| `docker system df` | Check Docker disk usage |

## 2. Container lifecycle

| Command | Use case |
|---|---|
| `docker run nginx` | Create and run a container |
| `docker run -d nginx` | Run a container in the background |
| `docker run -it ubuntu bash` | Start an interactive terminal |
| `docker stop <container>` | Stop a container |
| `docker start <container>` | Start a stopped container |
| `docker restart <container>` | Restart a container |
| `docker rm <container>` | Remove a stopped container |
| `docker rm -f <container>` | Force-remove a container |
| `docker rename <old> <new>` | Rename a container |
| `docker pause <container>` | Pause a container |
| `docker unpause <container>` | Resume a paused container |

**Remember:**
- `docker stop` stops a container; it does not delete it.
- `docker rm` removes a container.
- `docker rmi` removes an image.

## 3. Ports, names, environment variables and volumes

Example: run MySQL with a named container, host port mapping and database configuration.

```bash
docker run -d \
  --name mysql-db \
  -p 3306:3306 \
  -e MYSQL_ROOT_PASSWORD=root \
  -e MYSQL_DATABASE=hackethon \
  mysql:8
```

| Option | Meaning |
|---|---|
| `-d` | Run in the background |
| `--name` | Set the container name |
| `-p host:container` | Map a host port to a container port |
| `-e` | Set an environment variable |
| `-v` | Mount a volume or directory |
| `--network` | Attach the container to a Docker network |

For `-p 8080:80`, `8080` is the port on your Mac (host), and `80` is the port inside the container.

## 4. Logs and debugging

| Command | Use case |
|---|---|
| `docker logs <container>` | View container logs |
| `docker logs -f <container>` | Follow logs live |
| `docker logs --tail 100 <container>` | View the last 100 log lines |
| `docker inspect <container>` | View detailed container configuration |
| `docker exec -it <container> bash` | Open Bash inside a container |
| `docker exec -it <container> sh` | Open a shell inside a container |
| `docker top <container>` | View processes running in a container |
| `docker port <container>` | View published port mappings |

Example:

```bash
docker exec -it mysql-db bash
```

If the MySQL client is installed in the container, connect with:

```bash
mysql -u root -p
```

Some minimal images do not include Bash; use `sh` instead.

## 5. Image commands

| Command | Use case |
|---|---|
| `docker images` | List images |
| `docker pull mysql:8` | Download an image |
| `docker build -t my-app .` | Build an image from a Dockerfile in the current directory |
| `docker rmi mysql:8` | Remove an image |
| `docker tag my-app my-app:v1` | Add a tag to an image |
| `docker history my-app` | View image layers |
| `docker image prune` | Remove dangling images |

Build and run a Spring Boot image:

```bash
docker build -t transaction-app:v1 .
docker run -d -p 8080:8080 --name transaction-app transaction-app:v1
```

## 6. Docker volumes

Volumes persist data independently of a container's lifecycle. They are especially useful for databases.

| Command | Use case |
|---|---|
| `docker volume ls` | List volumes |
| `docker volume create mysql-data` | Create a volume |
| `docker volume inspect mysql-data` | Inspect a volume |
| `docker volume rm mysql-data` | Remove a volume |
| `docker volume prune` | Remove unused volumes |

Example: run MySQL with persistent storage.

```bash
docker run -d \
  --name mysql-db \
  -p 3306:3306 \
  -v mysql-data:/var/lib/mysql \
  -e MYSQL_ROOT_PASSWORD=root \
  mysql:8
```

The named volume `mysql-data` stores MySQL data outside the container's writable layer. Do not remove the volume if you need its data.

## 7. Docker networks

Containers on the same user-defined Docker network can communicate using container names.

| Command | Use case |
|---|---|
| `docker network ls` | List networks |
| `docker network create app-network` | Create a network |
| `docker network inspect app-network` | Inspect a network |
| `docker network connect app-network mysql-db` | Connect a container to a network |
| `docker network disconnect app-network mysql-db` | Disconnect a container |
| `docker network rm app-network` | Remove a network |

Example:

```bash
docker network create app-network

docker run -d \
  --name mysql-db \
  --network app-network \
  -e MYSQL_ROOT_PASSWORD=root \
  mysql:8
```

Another container attached to `app-network` can connect to MySQL using `mysql-db:3306`. From your Mac, use the published host port (if one was published).

## 8. Docker Compose

Docker Compose manages multiple services using a `compose.yaml` or `docker-compose.yml` file.

| Command | Use case |
|---|---|
| `docker compose up` | Create and start services |
| `docker compose up -d` | Start services in the background |
| `docker compose down` | Stop and remove containers and the Compose network |
| `docker compose stop` | Stop services without removing containers |
| `docker compose start` | Start existing stopped services |
| `docker compose restart` | Restart services |
| `docker compose ps` | List Compose services |
| `docker compose logs` | View service logs |
| `docker compose logs -f` | Follow service logs live |
| `docker compose build` | Build or rebuild service images |
| `docker compose pull` | Pull service images |
| `docker compose exec <service> sh` | Run a command inside a service container |

Common workflow:

```bash
docker compose up -d
docker compose ps
docker compose logs -f
docker compose down
```

## 9. Quick daily-use revision

```bash
# List running containers
docker ps

# List all containers
docker ps -a

# List images
docker images

# Start, stop or restart a container
docker start mysql-db
docker stop mysql-db
docker restart mysql-db

# Follow container logs
docker logs -f mysql-db

# Open a shell inside a container
docker exec -it mysql-db bash

# Check resource usage
docker stats

# Check disk usage
docker system df

# Remove a container (forcefully, if needed)
docker rm -f mysql-db
```

## 10. Cleanup commands — use carefully

| Command | What it removes |
|---|---|
| `docker container prune` | All stopped containers |
| `docker image prune` | Dangling images |
| `docker image prune -a` | Images not used by any container |
| `docker volume prune` | Unused volumes |
| `docker network prune` | Unused networks |
| `docker system prune` | Unused containers, networks, dangling images and build cache |

**Warning:** `docker system prune -a --volumes` can remove unused images and volumes. A volume may contain database data you still need. Inspect resources before pruning.

## 11. Suggested hands-on practice

For a local Java backend project:

1. Start a MySQL container.
2. Run `docker ps` to verify that it is running.
3. Use `docker logs -f mysql-db` to inspect its logs.
4. Use `docker exec -it mysql-db bash` to open a shell (or `sh` if Bash is unavailable).
5. Run `docker stats` to observe resource usage.
6. Practise starting and stopping the container without removing it.

**Next step:** Learn Docker Compose by running Spring Boot, MySQL and Kafka from one Compose file.

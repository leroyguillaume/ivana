# Ivana
Photos manager.

## Installation
### Prerequisites
* Java 11
* PostgreSQL 11

### Steps
- Download latest release
- Unarchive the tarball
- Run `./ivana-<version>/bin/ivana`

You can customize   configuration. See [Spring Boot documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/spring-boot-features.html#boot-features-external-config) for full details.

## API Documentation
[Documentation](https://web.postman.co/collections/9866325-e816b796-823f-4e98-8e9d-a11beceaf95c?version=latest)

## Development
### Prerequisites
* Java 11
* Docker 19.03
* Docker Compose 1.24
* NodeJS 12.16
* NPM 6.13
* Angular CLI 9.1

### Steps
#### Backend
```bash
docker-compose -f api/docker-compose.yml up -d
./gradlew bootRun
```

#### Frontend
```bash
cd webapp
npm i
ng serve
```

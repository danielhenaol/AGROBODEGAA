# AGROBODEGAA

![CI](https://github.com/danielhenaol/AGROBODEGAA/actions/workflows/ci.yml/badge.svg)

Sistema de gestión de bodega agrícola: entradas y salidas de producto, lotes,
agricultores, comerciantes y alertas de inventario.

## Estructura

| Carpeta | Descripción |
|---|---|
| `api-gateway/` | Punto de entrada único: enrutamiento, CORS y cabeceras de seguridad (Spring Cloud Gateway) |
| `backend/` | API de negocio (Java 21, Spring Boot, PostgreSQL/Supabase) |
| `frontend/` | Aplicación web (React + Vite) |
| `notification-service/` | Gateway de notificaciones (Java, plantillas en Supabase, envío con Resend) |
| `render.yaml` | Despliegue en Render (Blueprint) de los 3 servicios Java |
| `infra/` | Configuración local con Docker (Kong, Redis, Vault). Será retirada |
| `.github/workflows/` | Integración continua (GitHub Actions) |

## Compilar el backend

```
mvn clean verify
```

> Documentación de arquitectura y despliegue: en construcción.

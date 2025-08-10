# 📸 Artiles Photography Backend

<div align="center">

![Photography](https://img.shields.io/badge/Photography-Portfolio-ff6b6b?style=for-the-badge&logo=camera&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.0+-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![Java](https://img.shields.io/badge/Java-17+-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8.0+-4479A1?style=for-the-badge&logo=mysql&logoColor=white)

*API RESTful moderna para el sitio web de Artiles Photography*

[🚀 Instalación](#-instalación) • [📖 API Docs](#-documentación-de-la-api) • [🛠️ Tecnologías](#️-tecnologías) • [🤝 Contribuir](#-contribuir)

</div>

---

## ✨ Características Principales

- 🔐 **API RESTful** completa para gestión de contenido
- 📷 **Gestión de servicios** fotográficos y paquetes
- 🖼️ **Galería dinámica** con soporte para múltiples álbumes
- 💬 **Sistema de testimonios** de clientes
- ⚙️ **Configuración flexible** del sitio
- 📄 **Gestión legal** (términos y políticas)
- 🌐 **CORS configurado** para integración frontend

## 🏗️ Arquitectura del Proyecto

```
src/main/java/com/artiles_photography_backend/
├── 📁 models/           # Entidades JPA
├── 📁 repositories/     # Acceso a datos
├── 📁 services/         # Lógica de negocio
├── 📁 controllers/      # Endpoints REST
└── 📁 configurations/   # Configuración y inicialización
```

## 🛠️ Tecnologías

<table>
<tr>
<td align="center" width="96">
<img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/java/java-original.svg" width="48" height="48" />
<br><strong>Java 17+</strong>
</td>
<td align="center" width="96">
<img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/spring/spring-original.svg" width="48" height="48" />
<br><strong>Spring Boot 3</strong>
</td>
<td align="center" width="96">
<img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/mysql/mysql-original.svg" width="48" height="48" />
<br><strong>MySQL</strong>
</td>
<td align="center" width="96">
<img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/apache/apache-original.svg" width="48" height="48" />
<br><strong>Maven</strong>
</td>
</tr>
</table>

### Stack Completo

- **Spring Data JPA** - Persistencia de datos
- **Hibernate** - ORM
- **RESTful APIs** - Arquitectura REST
- **Maven** - Gestión de dependencias

## 🚀 Instalación

### Prerrequisitos

```bash
Java 17+
MySQL 8.0+
Maven 3.6+
```

### 1️⃣ Clonar el repositorio

```bash
git clone https://github.com/AntRed1/artiles-photography-backend.git
cd artiles-photography-backend
```

### 2️⃣ Configurar base de datos

```sql
CREATE DATABASE artiles_photography;
CREATE USER 'artiles_user'@'localhost' IDENTIFIED BY 'tu_password';
GRANT ALL PRIVILEGES ON artiles_photography.* TO 'artiles_user'@'localhost';
FLUSH PRIVILEGES;
```

### 3️⃣ Configurar variables de entorno

```bash
# application.properties
spring.datasource.url=jdbc:mysql://localhost:3306/artiles_photography
spring.datasource.username=artiles_user
spring.datasource.password=tu_password
spring.jpa.hibernate.ddl-auto=update
```

### 4️⃣ Ejecutar la aplicación

```bash
mvn clean install
mvn spring-boot:run
```

🎉 **¡Listo!** La API estará disponible en `http://localhost:8080`

## 📖 Documentación de la API

### 📋 Endpoints Principales

| Recurso | Endpoint | Descripción |
|---------|----------|-------------|
| 📷 **Servicios** | `/api/photography-services` | Gestión de servicios fotográficos |
| ℹ️ **Sobre Nosotros** | `/api/about-us` | Información de la empresa |
| 📞 **Contacto** | `/api/contact-info` | Datos de contacto |
| 🖼️ **Galería** | `/api/gallery` | Gestión de galería e imágenes |
| 📦 **Paquetes** | `/api/photography-packages` | Paquetes fotográficos |
| ⭐ **Testimonios** | `/api/testimonials` | Opiniones de clientes |
| ⚖️ **Legal** | `/api/legal` | Términos y políticas |
| ⚙️ **Configuración** | `/api/configuration` | Configuraciones del sitio |

### 🔍 Ejemplos de Uso

#### Obtener todos los servicios

```http
GET /api/photography-services
Content-Type: application/json
```

#### Crear un nuevo testimonio

```http
POST /api/testimonials
Content-Type: application/json

{
  "clientName": "María García",
  "rating": 5,
  "comment": "Excelente trabajo, muy profesional",
  "serviceType": "Bodas"
}
```

## 🗄️ Modelos de Datos

<details>
<summary><strong>📷 PhotographyService</strong></summary>

```java
- id: Long
- name: String
- description: String
- basePrice: BigDecimal
- duration: String
- featured: Boolean
```

</details>

<details>
<summary><strong>📦 PhotographyPackage</strong></summary>

```java
- id: Long
- name: String
- description: String
- price: BigDecimal
- features: List<String>
- popular: Boolean
```

</details>

<details>
<summary><strong>🖼️ Gallery</strong></summary>

```java
- id: Long
- title: String
- imageUrl: String
- category: String
- featured: Boolean
- uploadDate: LocalDateTime
```

</details>

<details>
<summary><strong>⭐ Testimonial</strong></summary>

```java
- id: Long
- clientName: String
- rating: Integer
- comment: String
- serviceType: String
- date: LocalDateTime
```

</details>

## 🔧 Configuración de Desarrollo

### Variables de Entorno

```bash
# Database
DB_HOST=localhost
DB_PORT=3306
DB_NAME=artiles_photography
DB_USERNAME=artiles_user
DB_PASSWORD=your_password

# Server
SERVER_PORT=8080
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:5173
```

### Perfil de Desarrollo

```properties
spring.profiles.active=dev
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
logging.level.com.artiles_photography_backend=DEBUG
```

## 🧪 Testing

```bash
# Ejecutar todos los tests
mvn test

# Ejecutar tests específicos
mvn test -Dtest=PhotographyServiceControllerTest

# Generar reporte de cobertura
mvn jacoco:report
```

## 📦 Build y Deployment

### Desarrollo Local

```bash
mvn spring-boot:run
```

### Producción

```bash
mvn clean package
java -jar target/artiles-photography-backend-1.0.0.jar
```

### Docker (Opcional)

```dockerfile
FROM openjdk:17-jdk-slim
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]
```

## 🤝 Contribuir

¡Las contribuciones son bienvenidas! Por favor:

1. 🍴 Fork el repositorio
2. 🌱 Crea una rama feature (`git checkout -b feature/AmazingFeature`)
3. 💾 Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. 📤 Push a la rama (`git push origin feature/AmazingFeature`)
5. 🔄 Abre un Pull Request

### 📋 Guías de Contribución

- Seguir las convenciones de código Java
- Añadir tests para nuevas funcionalidades
- Actualizar documentación cuando sea necesario
- Usar mensajes de commit descriptivos

## 📝 Roadmap

- [ ] 🔐 Sistema de autenticación JWT
- [ ] 📧 Integración con servicio de email
- [ ] 🌩️ Subida de imágenes a la nube
- [ ] 📊 Analytics y métricas
- [ ] 🔍 Búsqueda avanzada en galería
- [ ] 📱 Optimización para móviles
- [ ] 🌍 Internacionalización (i18n)

## 📞 Soporte

¿Tienes preguntas o problemas?

- 📧 **Email**: [Crear issue](https://github.com/AntRed1/artiles-photography-backend/issues)
- 🐛 **Bug Reports**: [Reportar bug](https://github.com/AntRed1/artiles-photography-backend/issues/new?template=bug_report.md)
- 💡 **Feature Requests**: [Solicitar feature](https://github.com/AntRed1/artiles-photography-backend/issues/new?template=feature_request.md)

## 📄 Licencia

Este proyecto está bajo la Licencia MIT. Ver [LICENSE](LICENSE) para más detalles.

---

<div align="center">

**Hecho con ❤️ para Artiles Photography Studio**

⭐ ¡Dale una estrella si te gusta el proyecto! ⭐

</div>

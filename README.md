# Despliegue Seguro de una Aplicación Web en AWS con Apache, Spring Boot y Let's Encrypt

Este documento describe los pasos para desplegar una aplicación segura y escalable en AWS utilizando Apache, Spring Boot y Let's Encrypt.

## Requisitos Previos
- Cuenta en AWS con permisos para crear instancias EC2.
- Clave privada para conectarse a la instancia.
- Dominio registrado (opcional pero recomendado).
- Java y Maven instalados para compilar la aplicación Spring Boot.

## Paso 1: Configurar la Instancia EC2

Conecta tu instancia EC2 y actualiza los paquetes:

```bash
sudo yum update -y
```

## Paso 2: Configurar Apache con TLS

### 2.1 Instalar Apache
```bash
sudo yum install httpd  # Para Amazon Linux
sudo systemctl start httpd
sudo systemctl enable httpd
```

### 2.2 Configurar TLS con Let's Encrypt

Instala Certbot:
```bash
sudo yum install certbot python3-certbot-apache
```

Genera un certificado TLS:
```bash
sudo certbot --apache -d tu-dominio.com
```
> **Nota:** Reemplaza `tu-dominio.com` con tu dominio real.

Certbot configurará automáticamente Apache para usar HTTPS y renovará el certificado periódicamente.

## Paso 3: Desplegar el Cliente HTML+JavaScript

Copia los archivos del frontend al servidor Apache:
```bash
sudo cp -r frontend/* /var/www/html/
```

Asegúrate de que el frontend use HTTPS en las peticiones al backend:

```javascript
fetch('https://tu-dominio.com:8080/api', {
    method: 'GET',
    headers: {
        'Authorization': 'Basic ' + btoa('usuario:contraseña')
    }
})
.then(response => response.json())
.then(data => console.log(data));
```

## Paso 4: Configurar el Backend con Spring Boot

### 4.1 Generar el Certificado TLS para Spring Boot

```bash
sudo openssl pkcs12 -export -in /etc/letsencrypt/live/tu-dominio.com/fullchain.pem \
                     -inkey /etc/letsencrypt/live/tu-dominio.com/privkey.pem \
                     -out certificado.p12 \
                     -name "tu-dominio"
```
> **Nota:** Reemplaza `tu-dominio.com` con tu dominio real.

Coloca el archivo `certificado.p12` en `src/main/resources/keystore/`.

### 4.2 Configurar Spring Boot para HTTPS

Edita `application.properties`:
```properties
server.ssl.enabled=true
server.ssl.key-store-type=PKCS12
server.ssl.key-store=classpath:keystore/certificado.p12
server.ssl.key-store-password=tu_contraseña
server.ssl.key-alias=tu-dominio
```

### 4.3 Implementar Seguridad con Spring Security

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .cors().and()
            .csrf().disable()
            .authorizeRequests()
            .anyRequest().authenticated()
            .and()
            .httpBasic();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

### 4.4 Desplegar la Aplicación en EC2

Compila la aplicación:
```bash
./mvnw clean package
```

Copia el `.jar` a la instancia EC2 del backend:
```bash
scp -i clave.pem target/aplicacion.jar ec2-user@ip-publica-backend:/home/ec2-user/
```

Ejecuta la aplicación:
```bash
java -jar aplicacion.jar
```

## Paso 5: Integración del Frontend y Backend

Asegúrate de que el frontend se comunique con el backend de forma segura:
```javascript
fetch('https://tu-dominio.com:8080/api', {
    method: 'GET',
    headers: {
        'Authorization': 'Basic ' + btoa('usuario:contraseña')
    }
})
.then(response => response.json())
.then(data => console.log(data));
```

## Paso 6: Seguridad Adicional y Monitoreo

### 6.1 Almacenamiento Seguro de Contraseñas
Utiliza `BCryptPasswordEncoder` en Spring Boot para almacenar contraseñas de forma segura.

### 6.2 Monitoreo y Escalabilidad
- Usa **AWS CloudWatch** para monitorear el rendimiento de las instancias EC2.
- Configura un **balanceador de carga (ELB)** para escalar la aplicación según la demanda.

### 6.3 Renovación Automática del Certificado TLS
Ejecuta:
```bash
sudo certbot renew --dry-run
```
Esto asegurará que el certificado se renueve automáticamente antes de expirar.

### Video de funcionamiento

https://github.com/user-attachments/assets/d1ac8254-046e-460f-b2be-2f8f4746cef9

## Conclusión
Este laboratorio cubre:
- Configuración de servidores en AWS.
- Implementación de TLS para seguridad.
- Desarrollo de un frontend asíncrono HTML+JavaScript.
- Autenticación segura con Spring Security.
- Integración y despliegue de frontend y backend.

Ahora tienes una aplicación lista para producción con un enfoque en la seguridad. 🚀






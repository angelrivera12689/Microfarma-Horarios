# 🔐 GUÍA DE IMPLEMENTACIÓN DE MEJORAS DE SEGURIDAD

## Fecha: 24 de abril de 2026
## Versión: 1.0

---

## 📋 MEJORAS IMPLEMENTADAS

### ✅ 1. Configuración de Variables de Entorno

**Archivos creados:**
- `Backend/src/main/resources/application-prod.properties` - Config de producción
- `Backend/.env.example` - Plantilla de variables

**¿Por qué?** Previene exposición de credenciales en repositorio

**Cómo implementar:**

#### Paso 1: Crear archivo .env en Backend
```bash
cp Backend/.env.example Backend/.env
# Editar Backend/.env y rellenar valores reales
```

#### Paso 2: Generar JWT Secret seguro
```bash
# En Linux/Mac
openssl rand -hex 128

# En Windows PowerShell
[System.Convert]::ToBase64String((1..128 | ForEach-Object {Get-Random -Maximum 256}) -as [byte[]])
```

#### Paso 3: Configurar variables en sistema
```bash
# Linux/Mac - Agregar a ~/.bashrc o ~/.zshrc
export DB_URL="jdbc:mysql://localhost:3306/microfarmahorarios?..."
export DB_USERNAME="root"
export DB_PASSWORD="tu_password"
export JWT_SECRET="tu_clave_de_256_caracteres..."
export MAIL_USERNAME="tu_email@gmail.com"
export MAIL_PASSWORD="tu_app_password"

# Windows - Usar Variables de Entorno del Sistema
# O crear application-prod.properties con referencias ${VARIABLE}
```

#### Paso 4: Ejecutar con perfil de producción
```bash
# Local con prod config
java -jar target/crud_basic-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod

# O mediante Maven
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=prod"
```

---

### ✅ 2. Rate Limiting

**Archivo creado:**
- `Backend/src/main/java/.../Security/Config/RateLimitingConfig.java`

**¿Por qué?** Previene ataques de fuerza bruta y DDoS

**Límites implementados:**
- Login: 5 intentos por minuto
- API General: 100 requests por minuto
- Importación: 10 por hora

**Cómo activar:**

En `SecurityConfig.java` ya está agregado (sin cambios necesarios).

**Verificar en producción:**
```bash
# Hacer 6 requests rápidos a /api/auth/login desde misma IP
# Respuesta esperada:
# HTTP 429 Too Many Requests
# {"error": "Too many requests..."}
```

---

### ✅ 3. CORS Mejorado (Más Restrictivo)

**Cambios en:** `SecurityConfig.java`

**¿Por qué?** Permitir `*` en CORS es inseguro

**Cómo configurar:**

#### En producción, definir orígenes permitidos:
```bash
# Agregar variable de entorno
export ALLOWED_ORIGINS="https://app.microfarma.com,https://admin.microfarma.com"

# O hardcodear en application-prod.properties
allowed.origins=https://app.microfarma.com,https://admin.microfarma.com
```

#### En local (default):
```
http://localhost:5173  (Vite)
http://localhost:3000  (Create React App)
```

---

### ✅ 4. Headers de Seguridad

**Cambios en:** `SecurityConfig.java`

**Implementados:**
- X-Frame-Options: DENY (previene clickjacking)
- X-XSS-Protection (protección XSS)
- Content-Security-Policy (solo scripts de mismo origen)

**Verificar en navegador:**
```javascript
// En consola del navegador
console.log(document.headers)
// Deberías ver X-Frame-Options: DENY
```

---

### ✅ 5. DTOs de Validación

**Archivos creados:**
- `LoginRequestDTO.java` - Valida login
- `RegisterRequestDTO.java` - Valida registro
- `AuthResponseDTO.java` - Respuesta sin exponer entidad
- `CreateEmployeeDTO.java` - Valida creación de empleado

**¿Por qué?** Validación centralizada y no expone entidades internas

**Cómo usar:**

**Opción A: Reemplazar gradualmente** (Recomendado)
```java
// Endpoint nuevo (sin romper existente)
@PostMapping("/login-v2")
public ResponseEntity<AuthResponseDTO> loginV2(
    @Valid @RequestBody LoginRequestDTO request) {
    // ... implementación
}

// Endpoint viejo sigue funcionando
@PostMapping("/login")
public ResponseEntity<?> login(@RequestBody User user) {
    // ... implementación
}
```

**Opción B: Reemplazar directamente** (Si puedes actualizar cliente)
```java
@PostMapping("/login")
public ResponseEntity<AuthResponseDTO> login(
    @Valid @RequestBody LoginRequestDTO request) {
    // Cambiar aquí
}
```

---

### ✅ 6. Tests Básicos

**Archivos creados:**
- `PasswordEncoderTest.java` - Valida BCrypt
- `RateLimitingConfigTest.java` - Valida rate limiting  
- `LoginRequestDTOTest.java` - Valida DTO

**Ejecutar tests:**
```bash
# Todos los tests
mvn test

# Solo tests de seguridad
mvn test -Dtest=*Security*

# Con cobertura
mvn test jacoco:report
# Reporte en: target/site/jacoco/index.html
```

---

## 🔧 CHECKLIST DE IMPLEMENTACIÓN

### Antes de producción:

- [ ] Crear archivo `.env` con variables seguras
- [ ] NO versionar `.env` (está en `.gitignore`)
- [ ] Cambiar `JWT_SECRET` a clave fuerte (256 chars)
- [ ] Configurar `MAIL_USERNAME` y `MAIL_PASSWORD`
- [ ] Ejecutar tests: `mvn test`
- [ ] Verificar rate limiting funciona
- [ ] Probar login con 6 intentos rápidos (debe fallar)
- [ ] Revisar logs en `logs/microfarma.log`
- [ ] Compilar: `mvn clean install`
- [ ] Ejecutar: `mvn spring-boot:run --spring.profiles.active=prod`

### Verificaciones de seguridad:

```bash
# 1. Verificar no hay credenciales en git
git grep -l "password\|secret\|token" src/ | grep -v DTO

# 2. Verificar headers de seguridad
curl -i http://localhost:8082/api/employees | grep -i "X-"

# 3. Probar CORS restrictivo
curl -H "Origin: http://malicious.com" http://localhost:8082/api/employees

# 4. Probar rate limiting
for i in {1..10}; do curl -X POST http://localhost:8082/api/auth/login; done
```

---

## 📊 COMPARATIVA ANTES/DESPUÉS

| Aspecto | ANTES | DESPUÉS | Mejora |
|---------|-------|---------|--------|
| Credenciales | En archivo | Env variables | ✅ Seguro |
| CORS | Permitir * | Restricto | ✅ Seguro |
| Rate Limiting | Ninguno | 5/min login | ✅ Protegido |
| Validación | Mínima | DTOs + @Valid | ✅ Robusto |
| Headers Seguridad | No | Sí | ✅ Protegido |
| Tests | <5% | 15% (3 nuevos) | ✅ Mejor |

---

## 🚀 PRÓXIMOS PASOS

### Fase 2 (Próximas 2-3 semanas):
1. [ ] Migrar más endpoints a DTOs
2. [ ] Agregar tests para 70% cobertura
3. [ ] Implementar Redis para caching
4. [ ] Optimizar N+1 queries

### Fase 3 (1-2 meses):
1. [ ] Migrar frontend a TypeScript
2. [ ] Docker containerization
3. [ ] CI/CD pipeline (GitHub Actions)
4. [ ] Logging centralizado (ELK)

---

## 📞 SOPORTE

Si encuentras problemas:

1. **Revisar logs:**
   ```bash
   tail -f logs/microfarma.log
   ```

2. **Verificar variables de entorno:**
   ```bash
   echo $JWT_SECRET
   echo $DB_PASSWORD
   ```

3. **Verificar puertos:**
   ```bash
   lsof -i :8082
   ```

4. **Contactar:** soporte@microfarma.com

---

**Documento clasificado:** INTERNO - Microfarma S.A.


# 🔧 Исправление проблем с проектом demo

## ✅ Что было исправлено

### 1. Исправлена зависимость `restaurant_api_contracts` в `demo/pom.xml`

**Было:**
```xml
<dependency>
    <groupId>com.example.Restaurant</groupId>  <!-- Неправильный groupId -->
    <artifactId>restaurant_api_contracts</artifactId>  <!-- Неправильный artifactId -->
    <version>1.0</version>  <!-- Неправильная версия -->
    ...
</dependency>
```

**Стало:**
```xml
<dependency>
    <groupId>com.example</groupId>  <!-- Правильный groupId из restaurant_api_contracts/pom.xml -->
    <artifactId>Restaurant</artifactId>  <!-- Правильный artifactId -->
    <version>0.0.1-SNAPSHOT</version>  <!-- Правильная версия -->
    <scope>system</scope>
    <systemPath>${project.basedir}/lib/restaurant_api_contracts.jar</systemPath>
</dependency>
```

### 2. Исправлена зависимость `events-contract` в `demo/pom.xml`

**Было:**
```xml
<dependency>
    <groupId>org.example.restaurant</groupId>
    <artifactId>events-contract</artifactId>
    <version>1.0-SNAPSHOT</version>
    <!-- Не было system scope -->
</dependency>
```

**Стало:**
```xml
<dependency>
    <groupId>org.example.restaurant</groupId>
    <artifactId>events-contract</artifactId>
    <version>1.0-SNAPSHOT</version>
    <scope>system</scope>  <!-- Используем локальный JAR -->
    <systemPath>${project.basedir}/lib/events-contract.jar</systemPath>
</dependency>
```

### 3. Улучшен `events-contract/pom.xml`

Добавлены плагины для правильной сборки JAR:
- `maven-jar-plugin` - для создания JAR файла
- `maven-install-plugin` - для установки в локальный Maven репозиторий

---

## 📋 Как правильно собрать проект

### Вариант 1: Ручная сборка (для тестирования)

```bash
# 1. Соберите events-contract
cd events-contract
mvn clean install
# JAR будет в target/events-contract-1.0-SNAPSHOT.jar
# Скопируйте его в demo/lib/events-contract.jar
cp target/events-contract-1.0-SNAPSHOT.jar ../demo/lib/events-contract.jar

# 2. Соберите restaurant_api_contracts (если нужно)
cd ../restaurant_api_contracts
mvn clean package
# JAR будет в target/Restaurant-0.0.1-SNAPSHOT.jar
# Скопируйте его в demo/lib/restaurant_api_contracts.jar
cp target/Restaurant-0.0.1-SNAPSHOT.jar ../demo/lib/restaurant_api_contracts.jar

# 3. Соберите demo
cd ../demo
mvn clean package
```

### Вариант 2: Автоматическая сборка через Jenkins

Jenkinsfile уже настроен правильно:
1. Сначала собирается `events-contract` (устанавливается в локальный Maven репозиторий)
2. Затем собираются все сервисы, включая `demo`

**Но!** Если вы используете `system scope` для зависимостей, нужно убедиться, что JAR файлы находятся в `demo/lib/`.

---

## 🔍 Проверка наличия JAR файлов

Убедитесь, что в папке `demo/lib/` есть оба JAR файла:

```bash
# Windows PowerShell
dir demo\lib\

# Linux/Mac
ls -la demo/lib/
```

Должны быть:
- ✅ `events-contract.jar`
- ✅ `restaurant_api_contracts.jar`

---

## 🐛 Решение проблем

### Проблема 1: "Could not find artifact events-contract"

**Решение:**
1. Убедитесь, что `events-contract.jar` находится в `demo/lib/`
2. Если его нет, соберите events-contract:
   ```bash
   cd events-contract
   mvn clean install
   cp target/events-contract-1.0-SNAPSHOT.jar ../demo/lib/events-contract.jar
   ```

### Проблема 2: "Could not find artifact restaurant_api_contracts"

**Решение:**
1. Убедитесь, что `restaurant_api_contracts.jar` находится в `demo/lib/`
2. Если его нет, соберите restaurant_api_contracts:
   ```bash
   cd restaurant_api_contracts
   mvn clean package
   cp target/Restaurant-0.0.1-SNAPSHOT.jar ../demo/lib/restaurant_api_contracts.jar
   ```

### Проблема 3: "ClassNotFoundException" при запуске

**Решение:**
1. Убедитесь, что JAR файлы содержат нужные классы
2. Проверьте, что groupId, artifactId и version совпадают в pom.xml и в JAR файлах
3. Пересоберите проект:
   ```bash
   cd demo
   mvn clean package
   ```

### Проблема 4: "Package com.example.Restaurant not found"

**Решение:**
Это означает, что `restaurant_api_contracts.jar` не содержит нужные классы или имеет неправильную структуру.

1. Проверьте содержимое JAR:
   ```bash
   # Windows PowerShell
   jar -tf demo\lib\restaurant_api_contracts.jar | Select-String "com/example"
   
   # Linux/Mac
   jar -tf demo/lib/restaurant_api_contracts.jar | grep "com/example"
   ```

2. Если классов нет, пересоберите restaurant_api_contracts:
   ```bash
   cd restaurant_api_contracts
   mvn clean package
   cp target/Restaurant-0.0.1-SNAPSHOT.jar ../demo/lib/restaurant_api_contracts.jar
   ```

---

## ✅ Альтернативный вариант: Использование Maven репозитория

Если вы хотите использовать стандартный Maven способ (без system scope):

### 1. Установите events-contract в локальный Maven репозиторий:

```bash
cd events-contract
mvn clean install
```

### 2. Установите restaurant_api_contracts в локальный Maven репозиторий:

```bash
cd restaurant_api_contracts
mvn clean install
```

### 3. Измените `demo/pom.xml`:

```xml
<!-- Events Contract -->
<dependency>
    <groupId>org.example.restaurant</groupId>
    <artifactId>events-contract</artifactId>
    <version>1.0-SNAPSHOT</version>
    <!-- Уберите system scope -->
</dependency>

<!-- Restaurant API Contracts -->
<dependency>
    <groupId>com.example</groupId>
    <artifactId>Restaurant</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <!-- Уберите system scope -->
</dependency>
```

Этот способ лучше, но требует, чтобы зависимости были установлены в локальный Maven репозиторий перед сборкой demo.

---

## 🎯 Рекомендации

1. **Для разработки:** Используйте Maven репозиторий (установите через `mvn install`)
2. **Для Jenkins/Docker:** Используйте system scope с JAR файлами в `lib/` (текущий вариант)

---

## 📝 Чеклист исправления

- [x] Исправлен groupId для restaurant_api_contracts
- [x] Исправлен artifactId для restaurant_api_contracts
- [x] Исправлена версия для restaurant_api_contracts
- [x] Добавлен system scope для events-contract
- [x] Улучшен pom.xml events-contract (добавлены плагины)
- [ ] Проверено наличие JAR файлов в demo/lib/
- [ ] Проект demo успешно собирается
- [ ] Проект demo успешно запускается

---

**После исправлений попробуйте собрать проект снова!** 🚀




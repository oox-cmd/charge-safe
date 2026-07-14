# ========== 阶段1: 构建 ==========
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app

# 先复制 pom.xml，利用 Docker 缓存加速依赖下载
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 复制源码并打包
COPY src ./src
RUN mvn clean package -DskipTests -B

# ========== 阶段2: 运行 ==========
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# 从构建阶段复制 jar 包
COPY --from=builder /app/target/*.jar app.jar

# 暴露端口
EXPOSE 8080

# 启动命令，支持通过环境变量覆盖配置
ENTRYPOINT ["java", "-jar", "app.jar"]

plugins {
    `java-library`
}

dependencies {
    api("io.jsonwebtoken:jjwt-api:0.12.6")
    compileOnly("jakarta.servlet:jakarta.servlet-api:6.0.0")
    compileOnly("org.springframework.security:spring-security-core:6.3.4")
    compileOnly("org.springframework.security:spring-security-web:6.3.4")
    compileOnly("org.springframework:spring-web:6.1.14")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")
}

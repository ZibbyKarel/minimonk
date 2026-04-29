plugins {
    `java-library`
}

dependencies {
    api("com.fasterxml.jackson.core:jackson-annotations:2.17.2")
    api("org.springframework.amqp:spring-amqp:3.1.7")
    implementation("org.springframework.amqp:spring-rabbit:3.1.7")
}

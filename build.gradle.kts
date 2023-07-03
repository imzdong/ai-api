plugins {
    id("java")
    id("checkstyle")
    id("io.spring.javaformat") version "0.0.39"
    id("org.springframework.boot") version "3.1.1"
    id("io.spring.dependency-management") version "1.1.0"
}

group = "org.imzdong"
version = "1.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
}

//ext["micrometer.version"] = "1.10.0-SNAPSHOT"

dependencies {

    implementation("com.fasterxml.jackson.core:jackson-annotations:2.15.0-rc1")
    compileOnly("org.projectlombok:lombok:1.18.26")
    annotationProcessor("org.projectlombok:lombok:1.18.26")

    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")

    //api 'io.github.openfeign:feign-core:12.2'
    implementation("io.github.openfeign:feign-okhttp:12.2")
    implementation("io.github.openfeign:feign-jackson:12.2")
    // 计算token https://github.com/knuddelsgmbh/jtokkit
    implementation("com.knuddels:jtokkit:0.5.1")

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.springframework.boot:spring-boot-starter-test")

    checkstyle("io.spring.javaformat:spring-javaformat-checkstyle:0.0.39")

}

tasks.test {
    useJUnitPlatform()
}

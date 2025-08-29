plugins {
   id("java")
}

group = "de.sko.dev"
version = "1.0-SNAPSHOT"

repositories {
   mavenCentral()
}

dependencies {
   testImplementation(platform("org.junit:junit-bom:5.10.0"))
   testImplementation(platform("com.fasterxml.jackson:jackson-bom:2.16.1"))
   testImplementation("org.junit.jupiter:junit-jupiter")
   testImplementation("org.wiremock:wiremock:3.13.1")
   testImplementation("com.fasterxml.jackson.core:jackson-core")
   testImplementation("org.assertj:assertj-core:3.27.3")
   testImplementation("net.javacrumbs.json-unit:json-unit-assertj:3.5.0")
   testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
   useJUnitPlatform()
}

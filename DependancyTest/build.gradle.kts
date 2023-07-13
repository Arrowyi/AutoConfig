plugins {
    id("java")
}

group = "indi.arrowyi"
version = "unspecified"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")

    implementation(project(":ConfigManager"))
    annotationProcessor  (project(":ConfigManager"))
    implementation(project(":Tester"))

}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}
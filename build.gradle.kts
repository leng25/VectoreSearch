plugins {
    id("application")
}

application {
    mainClass.set("org.example.Main")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.apache.lucene:lucene-core:10.3.2")
    implementation("org.apache.lucene:lucene-queryparser:10.3.2")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.named<JavaExec>("run") {
    if (project.hasProperty("nojit")) {
        jvmArgs("-Xint")
    }
}


tasks.test {
    useJUnitPlatform()
}
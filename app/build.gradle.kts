plugins {
    application
    java
}

group = "br.gov.sso"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("junit:junit:4.13.2")
    implementation("com.google.guava:guava:30.1.1-jre")
    implementation("com.google.code.gson:gson:2.10.1")
}

tasks.named<JavaExec>("run") {
    environment("GOVBR_API_URL", System.getenv("GOVBR_API_URL") ?: "")
    environment("GOVBR_CLIENTID", System.getenv("GOVBR_CLIENTID") ?: "")
    environment("GOVBR_SECRET", System.getenv("GOVBR_SECRET") ?: "")
    environment("GOVBR_BASIC", System.getenv("GOVBR_BASIC") ?: "")
    environment("GOVBR_CODEVERIFY", System.getenv("GOVBR_CODEVERIFY") ?: "")
    environment("GOVBR_SCOPE", System.getenv("GOVBR_SCOPE") ?: "")
    environment("GOVBR_CODE", System.getenv("GOVBR_CODE") ?: "")
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "br.gov.sso.App"
    }

    archiveFileName.set("govbrsso-1.0.jar")
}

plugins {
    `java-library`
    `maven-publish`
}

group = "org.leycm.frames"
version = "1.2.1"
description = "the-frame"

val targetJavaVersion = 17
val targetLombokVersion = "1.18.38"

repositories {
    mavenLocal()
    mavenCentral()

    maven("https://repo.dmulloy2.net/repository/public/")

    maven("https://maven.google.com")
}

dependencies {
    compileOnly("org.projectlombok:lombok:$targetLombokVersion")
    annotationProcessor("org.projectlombok:lombok:$targetLombokVersion")
    testCompileOnly("org.projectlombok:lombok:$targetLombokVersion")
    testAnnotationProcessor("org.projectlombok:lombok:$targetLombokVersion")

    compileOnly("org.jetbrains:annotations:24.0.1")

    implementation("com.google.code.gson:gson:2.11.0")
    implementation("com.google.guava:guava:33.3.1-jre")
    implementation ("com.moandjiezana.toml:toml4j:0.7.2")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.16.1")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])

        groupId = group.toString()
        artifactId = description
        version = version
    }

}

java { toolchain.languageVersion.set(JavaLanguageVersion.of(targetJavaVersion)) }

tasks.withType<JavaCompile>() { options.encoding = "UTF-8" }
tasks.withType<Javadoc>() { options.encoding = "UTF-8" }
tasks.test { useJUnitPlatform() }

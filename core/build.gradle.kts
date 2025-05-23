plugins {
    id("java")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("net.minestom:minestom-snapshots:1_21_5-aa17002536")
    implementation("com.ecwid.consul:consul-api:1.4.5")
    implementation("io.lettuce:lettuce-core:6.6.0.RELEASE")
    implementation("org.jetbrains:annotations:26.0.2")
    implementation("io.projectreactor:reactor-core:3.8.0-M3")
    compileOnly("org.projectlombok:lombok:1.18.38")
    implementation("org.hibernate.orm:hibernate-core:7.0.0.Final")
}

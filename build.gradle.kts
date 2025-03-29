plugins {
    id("fabric-loom") version "1.10-SNAPSHOT"
    id("maven-publish")
}

group = property("maven_group")!!
version = property("mod_version") as String + "+mc" + property("minecraft_version") as String
val baseName = property("archives_base_name") as String


repositories {
    mavenCentral()
    exclusiveContent {
        forRepository {
            maven {
                name = "Modrinth"
                url = uri("https://api.modrinth.com/maven")
            }
        }
        filter {
            includeGroup("maven.modrinth")
        }
    }
    exclusiveContent {
        forRepository {
            maven {
                name = "Terraformers"
                url = uri("https://maven.terraformersmc.com/")
            }
        }
        filter {
            includeGroup("com.terraformersmc")
        }
    }
}

base {
    archivesName.set(property("mod_name") as String)
}

loom {
    accessWidenerPath = file("src/main/resources/amecs.accesswidener")
}


dependencies {
    //to change the versions see the gradle.properties file
    minecraft("com.mojang:minecraft:${property("minecraft_version")}")
    mappings("net.fabricmc:yarn:${property("yarn_mappings")}:v2")
    modImplementation("net.fabricmc:fabric-loader:${property("loader_version")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${property("fabric_version")}")
    include(implementation("com.moulberry:mixinconstraints:${(property("mcon_version"))}")!!)
    include(modImplementation("wtf.cheeze:platformlanguageloader-fabric:${property("pll_version")}")!!)

    modImplementation("maven.modrinth:controlling:${property("controlling_version")}")
    modImplementation("maven.modrinth:searchables:${property("searchables_version")}")
    modImplementation("com.terraformersmc:modmenu:${property("modmenu_version")}")
}


tasks.processResources {
    inputs.property("version", project.version)

    filesMatching("fabric.mod.json") {
        expand("version" to project.version)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(21)
}


java {
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}


//sourceSets {
//    create("testmod") {
//        compileClasspath += sourceSets["main"].compileClasspath
//        runtimeClasspath += sourceSets["main"].runtimeClasspath
//    }
//}

tasks.named<Jar>("jar") {

    from("LICENSE") {
        rename { "${it}_${project.base.archivesName.get()}" }
    }
}


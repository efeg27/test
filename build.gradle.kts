import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.shadow)
}

group = "org.template.efeg27"
version = "1.0.0"

val currentDateString: String
    get() = OffsetDateTime.now(ZoneOffset.UTC).toLocalDate().format(DateTimeFormatter.ISO_DATE)

kotlin {
    jvmToolchain(21)
}

dependencies {
    compileOnly(libs.paper)
    implementation("org.yaml:snakeyaml:2.3")
}

tasks {
    wrapper {
        gradleVersion = "9.0.0"
        distributionType = Wrapper.DistributionType.ALL
    }

    processResources {
        val placeholders = mapOf(
            "version" to version,
            "apiVersion" to libs.versions.mcApi.get(),
            "kotlinVersion" to libs.versions.kotlin.get(),
        )

        filesMatching("plugin.yml") {
            expand(placeholders)
        }

        // create an "offline" copy/variant of the plugin.yml with `libraries` omitted
        doLast {
            val resourcesDir = sourceSets.main.get().output.resourcesDir
            val yamlDumpOptions =
                // make it pretty for the people
                DumperOptions().also {
                    it.defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
                    it.isPrettyFlow = true
                }
            val yaml = Yaml(yamlDumpOptions)
            val pluginYml: Map<String, Any> = yaml.load(file("$resourcesDir/plugin.yml").inputStream())
            yaml.dump(pluginYml.filterKeys { it != "libraries" }, file("$resourcesDir/offline-plugin.yml").writer())
        }
    }

    jar {
        exclude("offline-plugin.yml")
    }

    // offline jar should be ready to go with all dependencies
    shadowJar {
        minimize()
        archiveClassifier.set("offline")
        exclude("plugin.yml")
        rename("offline-plugin.yml", "plugin.yml")

        // avoid classpath conflicts/pollution via relocation
        enableAutoRelocation = true
        relocationPrefix = "${project.group}.${project.name.lowercase()}.libraries"
    }
}

package mutual.aid

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.attributes.Category
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

/**
 * Relevant docs:
 *
 * 1. https://docs.gradle.org/8.4/release-notes.html
 * 2. https://docs.gradle.org/current/userguide/cross_project_publications.html#sec:variant-aware-sharing
 * 3. https://docs.gradle.org/current/userguide/java_library_plugin.html#sec:java_library_configurations_graph
 */
@Suppress("unused", "UnstableApiUsage")
class ConfigurationRolesPlugin : Plugin<Project> {

  internal companion object {
    const val TASK_GROUP = "Configuration roles demo"
  }

  override fun apply(project: Project): Unit = project.run {
    val kotlin = extensions.getByType(KotlinJvmProjectExtension::class.java)

    // src/main
    val mainSource = kotlin.sourceSets.named("main")
    // src/main/{kotlin,java} as a FileTree
    val mainKotlinSource = mainSource.map { it.kotlin }
    // src/main/{kotlin,java} as a FileCollection
    val mainKotlinSourceFiles = mainKotlinSource.map { it.sourceDirectories }

    // Register a task to produce a custom output for consumption by other projects
    val producer = tasks.register("collectSources", ProducerTask::class.java) { t ->
      t.source.from(mainKotlinSourceFiles)
      t.output.set(layout.buildDirectory.file("reports/configuration-roles/sources.txt"))
    }

    // Following the naming pattern established by the Java Library plugin. See
    // https://docs.gradle.org/current/userguide/java_library_plugin.html#sec:java_library_configurations_graph
    val declarableName = "sourceFiles"
    val internalName = "${declarableName}Classpath"
    val externalName = "${declarableName}Elements"

    // Dependencies are declared on this configuration
    val declarable = configurations.dependencyScope(declarableName)
      // The new APIs return the new configuration wrapped in a lazy Provider, for consistency with
      // other Gradle APIs. However, there is no value in having a lazy Configuration, since we use
      // it immediately anyway. So, call get() to realize it, and call it a day.
      .get()

    // The plugin will resolve dependencies against this internal configuration, which extends from
    // the declared dependencies
    val internal = configurations.resolvable(internalName) { c ->
      c.extendsFrom(declarable)
      c.attributes { a ->
        a.attribute(
          // This attribute is identical to what is set on the external/consumable configuration
          Category.CATEGORY_ATTRIBUTE, objects.named(Category::class.java, Category.DOCUMENTATION)
        )
      }
    }

    // The plugin will expose dependencies on this configuration, which extends from the declared
    // dependencies
    configurations.consumable(externalName) { c ->
      c.extendsFrom(declarable)
      c.attributes { a ->
        a.attribute(
          // This attribute is identical to what is set on the internal/resolvable configuration
          Category.CATEGORY_ATTRIBUTE, objects.named(Category::class.java, Category.DOCUMENTATION)
        )
      }

      // Teach Gradle which task produces the artifact associated with this external/consumable
      // configuration
      c.outgoing.artifact(producer.flatMap { it.output })
    }

    // Register a task to consume the custom outputs produced by other projects
    tasks.register("printDependencySources", ConsumerTask::class.java) { t ->
      t.reports.setFrom(internal)
    }
  }
}

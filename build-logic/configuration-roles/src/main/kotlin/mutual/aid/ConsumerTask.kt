package mutual.aid

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.UntrackedTask

/**
 * Imagine this task registered on the root project, which will have a dependency on all
 * subprojects. This is an idiomatic way to collect global information in a "safe" way, that
 * respects project boundaries. Projects must only be coupled together using the concept of
 * dependencies.
 */
@UntrackedTask(because = "This task always prints to console")
abstract class ConsumerTask : DefaultTask() {

  init {
    group = ConfigurationRolesPlugin.TASK_GROUP
    description = "Prints all the source files in dependency projects"
  }

  @get:PathSensitive(PathSensitivity.RELATIVE)
  @get:InputFiles
  abstract val reports: ConfigurableFileCollection

  @TaskAction fun action() {
    val globalSources = reports.joinToString(separator = "\n") { it.readText() }
    logger.quiet(globalSources)
  }
}
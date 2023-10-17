package mutual.aid

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

/**
 * Producer a report of all source files in this project. This is for some imaginary report we want
 * to generate.
 */
@CacheableTask
abstract class ProducerTask : DefaultTask() {

  init {
    group = ConfigurationRolesPlugin.TASK_GROUP
    description = "Write the name of each source file into output.txt"
  }

  /** The files in `src/kotlin/main` in this project. */
  @get:PathSensitive(PathSensitivity.RELATIVE)
  @get:InputFiles
  abstract val source: ConfigurableFileCollection

  @get:OutputFile
  abstract val output: RegularFileProperty

  @TaskAction fun action() {
    // It is best practice to delete any prior output, in case the task fails.
    // This prevents stale outputs.
    val output = output.get().asFile
    output.delete()

    // Collect the name of all source files in `src/kotlin/main`.
    val files = source
      .map { it.name }
      // The order of the files in a FileTree is not stable, even on a single computer
      .sorted()
      .joinToString(separator = "\n")

    output.writeText(files)
  }
}
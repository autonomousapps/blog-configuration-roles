package mutual.aid.rules

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.attributes.AttributeCompatibilityRule
import org.gradle.api.attributes.CompatibilityCheckDetails

/**
 * This proof-of-concept explores how one might use the concept of Gradle variant attributes to
 * enforce module structure rules without resorting to out-of-band checks (that is, a failure to
 * follow the rules results in immediate failure). The primary drawback to this approach is probably
 * that failure to follow the rules results in an extremely verbose error. Steps would have to be
 * taken to make the error more legible.
 *
 * Further investigation is needed to see how this might be expanded to enforce rules that aren't
 * based on project type but perhaps project location (although I wonder if that might be an anti-
 * pattern).
 *
 * @see <a href="https://docs.gradle.org/current/userguide/cross_project_publications.html#sec:variant-aware-sharing">Variant-aware sharing of artifacts between projects</a>
 * @see <a href="https://docs.gradle.org/current/userguide/java_library_plugin.html#sec:java_library_configurations_graph">The Java Library plugin configurations</a>
 */
@Suppress("unused")
class ModuleRules {

  private companion object {
    /** These configurations are exposed to consumer projects. */
    val consumables = listOf("runtimeElements", "apiElements")

    /** These configurations are used within a project to resolve dependencies. */
    val resolvables = listOf(
      "compileClasspath", "runtimeClasspath",
      "testCompileClasspath", "testRuntimeClasspath"
    )

    fun Project.role(of: String): (Configuration) -> Unit = { c ->
      c.attributes.attribute(Role.ROLE_ATTRIBUTE, objects.named(Role::class.java, of))
    }

    fun Project.onEachConsumable(configureAction: (Configuration) -> Unit) {
      consumables.forEach { configurations.named(it, configureAction) }
    }

    fun Project.onEachResolvable(configureAction: (Configuration) -> Unit) {
      resolvables.forEach { configurations.named(it, configureAction) }
    }

    fun Project.setCompatibilityRules(rule: Class<out AttributeCompatibilityRule<Role>>) {
      dependencies.run {
        attributesSchema { schema ->
          schema.attribute(Role.ROLE_ATTRIBUTE) { matchingStrategy ->
            matchingStrategy.compatibilityRules.add(rule)
          }
        }
      }
    }
  }

  /**
   * Apply this plugin to app projects:
   *
   * ```
   * plugins {
   *   id 'mutual.aid.rules.app'
   * }
   * ```
   */
  class AppRulesPlugin : Plugin<Project> {

    override fun apply(target: Project): Unit = target.run {
      // outgoing variants: this is an app project
      onEachConsumable(role(Role.APP))
      // resolvable dependencies: only resolve library-like things, never apps
      onEachResolvable(role(Role.LIB))

      setCompatibilityRules(AppRule::class.java)
    }

    abstract class AppRule : AttributeCompatibilityRule<Role> {

      private companion object {
        val COMPATIBLE_DEP_ROLES = listOf(Role.LIB, Role.PROTO)
      }

      override fun execute(details: CompatibilityCheckDetails<Role>) = details.run {
        if (consumerValue?.name == Role.LIB && producerValue?.name in COMPATIBLE_DEP_ROLES) {
          compatible()
        }
      }
    }
  }

  /**
   * Apply this plugin to library projects:
   *
   * ```
   * plugins {
   *   id 'mutual.aid.rules.lib'
   * }
   * ```
   */
  class LibRulesPlugin : Plugin<Project> {

    override fun apply(target: Project): Unit = target.run {
      role(Role.LIB).let { lib ->
        // outgoing variants: this is a library project
        onEachConsumable(lib)
        // resolvable dependencies: only resolve library-like things, never apps
        onEachResolvable(lib)
      }

      // libs can resolve libs or protos
      setCompatibilityRules(LibRule::class.java)
    }

    /** Libs can resolve other libs and protos. */
    abstract class LibRule : AttributeCompatibilityRule<Role> {

      private companion object {
        val COMPATIBLE_DEP_ROLES = listOf(Role.LIB, Role.PROTO)
      }

      override fun execute(details: CompatibilityCheckDetails<Role>) = details.run {
        if (consumerValue?.name == Role.LIB && producerValue?.name in COMPATIBLE_DEP_ROLES) {
          compatible()
        }
      }
    }
  }

  /**
   * Apply this plugin to proto projects:
   *
   * ```
   * plugins {
   *   id 'mutual.aid.rules.proto'
   * }
   * ```
   */
  class ProtoRulesPlugin : Plugin<Project> {

    override fun apply(target: Project): Unit = target.run {
      role(Role.PROTO).let { proto ->
        // outgoing variants: this is a proto project
        onEachConsumable(proto)
        // resolvable dependencies: only resolve protos, nothing else
        onEachResolvable(proto)
      }
    }
  }
}

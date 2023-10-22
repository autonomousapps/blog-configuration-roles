package mutual.aid.rules

import org.gradle.api.Named
import org.gradle.api.attributes.Attribute

@Suppress("HasPlatformType", "MayBeConstant")
interface Role : Named {

  companion object {
    @JvmField val ROLE_ATTRIBUTE = Attribute.of("mutual.aid.role", Role::class.java)

    /** Apps may depend on libs or on protos. */
    @JvmField val APP = "app"

    /** Libs may depend on other libs or on protos. */
    @JvmField val LIB = "lib"

    /** Anything may depend on protos. Protos may only depend on other protos. */
    @JvmField val PROTO = "proto"
  }
}
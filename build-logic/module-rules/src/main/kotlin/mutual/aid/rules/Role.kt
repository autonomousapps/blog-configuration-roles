package mutual.aid.rules

import org.gradle.api.Named
import org.gradle.api.attributes.Attribute

interface Role : Named {

  companion object {
    @JvmField val ROLE_ATTRIBUTE = Attribute.of("mutual.aid.role", Role::class.java)
    @JvmField val APP = "app"
    @JvmField val LIB = "lib"
  }
}
/*
 * The contents of this file is free and unencumbered software released into the
 * public domain. For more information, please refer to <http://unlicense.org/>
 */

package gragas.commands

import dev.kord.rest.builder.message.modify.InteractionResponseModifyBuilder

class CommandScope {
  fun fail(text: String): Nothing {
    fail {
      content = text
    }
  }

  fun fail(builder: InteractionResponseModifyBuilder.() -> Unit): Nothing {
    throw CommandFailure(builder)
  }
}

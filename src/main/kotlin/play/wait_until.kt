/*
 * The contents of this file is free and unencumbered software released into the
 * public domain. For more information, please refer to <http://unlicense.org/>
 */

package gragas.play

inline fun waitUntil(f: () -> Boolean) {
  while (!f()) {
    //
  }
}

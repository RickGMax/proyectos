package cl.uchile.dcc.model.actions

import cl.uchile.dcc.model.units.{Character, Enemy}

/** Base implementation shared by game actions. */
abstract class Action(initialName: String) {

  private val storedName: String = initialName

  /** Returns this action's name. */
  def name: String = storedName

  /** Rejects execution from enemies unless the action supports it. */
  def executeFromEnemy(source: Enemy, target: Target): Unit = {
    throw new UnsupportedOperationException(
      s"$name does not support execution by an enemy"
    )
  }

  /** Rejects execution from characters unless the action supports it. */
  def executeFromCharacter(source: Character, target: Target): Unit = {
    throw new UnsupportedOperationException(
      s"$name does not support execution by a character."
    )
  }
}

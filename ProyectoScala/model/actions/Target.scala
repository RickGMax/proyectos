package cl.uchile.dcc.model.actions

import cl.uchile.dcc.model.units.{Character, Enemy}


/** Represents an entity that can receive game actions. */
trait Target {
  /** Rejects enemy attacks unless this target supports them. */
  def receiveEnemyAttack(source: Enemy, action: Attack):Unit = {
    throw new UnsupportedOperationException(
      "this target cannot receive an enemy attack"
    )
  }

  /** Rejects character attacks unless this target supports them. */
  def receiveCharacterAttack(source: Character, action: Attack): Unit = {
    throw new UnsupportedOperationException(
      "This target cannot receive a character attack."
    )
  }
}

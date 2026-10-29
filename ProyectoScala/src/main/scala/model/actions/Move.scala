package cl.uchile.dcc.model.actions

import cl.uchile.dcc.model.units.{Character, Enemy}

/** Represents movement to an adjacent panel. */
class Move extends Action("Move") {
  /** Dispatches a character's movement to the selected target. */
  override def executeFromCharacter(
                                     source: Character,
                                     target: Target
                                   ): Unit = {
    target.receiveMove(source, this)
  }

  /** Dispatches an enemy's movement to the selected target. */
  override def executeFromEnemy(
                                 source: Enemy,
                                 target: Target
                               ): Unit = {
    target.receiveMove(source, this)
  }
}
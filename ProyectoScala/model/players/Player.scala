package cl.uchile.dcc.model.players

import cl.uchile.dcc.model.units.GameUnit

/**
 * Represents a player and the units under their control.
 *
 * @param initialUnits the player's starting units
 */
class Player(initialUnits: Seq[GameUnit]) {
  private val playerUnits: Vector[GameUnit] = initialUnits.toVector

  /** Returns the player's units without exposing a mutable collection. */
  def units: Seq[GameUnit] = playerUnits

  /** Returns true when no controlled unit remains alive. */
  def defeated: Boolean =
    playerUnits.forall(_.lifePoints <= 0.0)
}

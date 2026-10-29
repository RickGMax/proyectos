package cl.uchile.dcc.model.players

import cl.uchile.dcc.model.units.GameUnit

/**
 * Represents a player and the units under their control.
 *
 * @param initialUnits the player's starting units
 * @throws IllegalArgumentException if a unit already belongs to another player
 */
class Player(initialUnits: Seq[GameUnit]) {
  private val playerUnits: Vector[GameUnit] = initialUnits.toVector

  require(
    playerUnits.forall(_.player.isEmpty),
    "A unit cannot belong to more than one player."
  )

  playerUnits.foreach(_.assignPlayer(this))

  /** Returns the player's units without exposing a mutable collection. */
  def units: Seq[GameUnit] = playerUnits

  /** Returns true when no controlled unit remains alive. */
  def defeated: Boolean =
    playerUnits.forall(_.lifePoints <= 0.0)
}
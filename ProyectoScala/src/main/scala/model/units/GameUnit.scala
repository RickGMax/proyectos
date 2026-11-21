package cl.uchile.dcc.model.units

import cl.uchile.dcc.model.actions.{Action, Attack, Meteorite, Target}
import cl.uchile.dcc.model.map.Panel
import cl.uchile.dcc.model.players.Player

/** Common contract implemented by playable characters and enemies. */
trait GameUnit extends Target {

  /** Returns this unit's name. */
  def name: String

  /** Returns this unit's remaining life points. */
  def lifePoints: Double

  /** Returns this unit's maximum life points. */
  def maxLifePoints: Double

  /**
   * Restores life points up to the maximum without reviving defeated units.
   *
   * @param amount the finite, non-negative amount to restore
   * @throws IllegalArgumentException if defeated or the amount is invalid
   */
  def restoreLifePoints(amount: Double): Unit

  /** Returns this unit's physical defense. */
  def defense: Double

  /**
   * Increases a living unit's current defense by a percentage.
   *
   * @param percentage the finite, non-negative percentage to apply
   * @throws IllegalArgumentException if defeated or the percentage is invalid
   */
  def increaseDefense(percentage: Double): Unit

  /** Returns this unit's own weight. */
  def weight: Double

  /** Returns this unit's total physical weight. */
  def totalWeight: Double

  /** Returns the maximum value of this unit's action bar. */
  def actionBarMaximum: Double

  /** Returns the unit's current panel, if placed on the map. */
  def currentPanel: Option[Panel]

  /** Updates the panel reference from model operations. */
  private[model] def updatePanel(panel: Option[Panel]): Unit

  /**
   * Moves this unit to an adjacent panel.
   *
   * @param destination the panel receiving the unit
   * @throws IllegalArgumentException if defeated, unplaced or the destination is invalid
   */
  def moveTo(destination: Panel): Unit

  /** Captures mutable state and returns an operation that restores it. */
  def captureState(): () => Unit

  /** Returns the player controlling this unit, if assigned. */
  def player: Option[Player]

  /** Assigns this unit to a player during model setup. */
  private[model] def assignPlayer(owner: Player): Unit

  /** Receives Meteorite damage delivered through a panel. */
  def receiveMeteoriteDamage(
                              source: BlackMage,
                              action: Meteorite,
                              damage: Double
                            ): Unit

}

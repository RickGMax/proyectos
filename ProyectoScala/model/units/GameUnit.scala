package cl.uchile.dcc.model.units

import cl.uchile.dcc.model.actions.{Action, Attack, Target}

/** Common contract implemented by playable characters and enemies. */
trait GameUnit extends Target {

  /** Returns this unit's name. */
  def name: String

  /** Returns this unit's remaining life points. */
  def lifePoints: Double

  /** Returns this unit's physical defense. */
  def defense: Int

  /** Returns this unit's own weight. */
  def weight: Double

  /** Returns this unit's total physical weight. */
  def totalWeight: Double

  /** Returns the maximum value of this unit's action bar. */
  def actionBarMaximum: Double
}

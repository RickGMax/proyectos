package cl.uchile.dcc.model.units

import cl.uchile.dcc.model.actions.Source
import cl.uchile.dcc.model.actions.{Action, Attack, Purification, Target}


/** Represents an enemy capable of performing physical attacks. */
class Enemy(
             name: String,
             initialLifePoints: Double,
             initialAttackPoints: Int,
             defense: Int,
             weight: Double
           ) extends AbstractUnit(
  name, initialLifePoints, defense, weight
) with Source {

  private val storedAttackPoints: Int = initialAttackPoints

  /** Returns this enemy's physical attack points. */
  def attackPoints: Int = storedAttackPoints

  private val attackAction: Action = new Attack

  /** Receives damage based on current health and the caster's magical damage. */
  override def receivePurification(
                                    source: WhiteMage,
                                    action: Purification,
                                    magicDamage: Double
                                  ): Unit = {
    require(
      magicDamage.isFinite && magicDamage >= 0.0,
      "Magical damage must be finite and non-negative."
    )

    receiveMagicalDamage(lifePoints * 0.15 + magicDamage / 2.0)
  }

  /** Returns attack and movement actions currently available to this enemy. */
  override def availableActions: Seq[Action] =
    if (lifePoints > 0.0) Seq(attackAction) ++ movementActions
    else Seq.empty


  /**
   * Executes an action using enemy-specific dispatch.
   *
   * @param action the action to perform
   * @param target the entity receiving the action
   * @throws IllegalArgumentException if this enemy is defeated
   */
  override def doAction(action: Action, target: Target): Unit = {
    require(
      lifePoints > 0.0,
      "A defeated enemy cannot perform actions."
    )

    action.executeFromEnemy(this, target)
  }
}
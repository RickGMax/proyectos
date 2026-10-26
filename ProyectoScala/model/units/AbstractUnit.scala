package cl.uchile.dcc.model.units
import cl.uchile.dcc.model.actions.{Action, Attack, Target}


/** Shared implementation for playable characters and enemies. */
abstract class AbstractUnit(
                             initialName: String,
                             initialLifePoints: Double,
                             initialDefense: Int,
                             initialWeight: Double
                           ) extends GameUnit {

  private val storedName: String = initialName
  private var storedLifePoints: Double = initialLifePoints
  private val storedDefense: Int = initialDefense
  private val storedWeight: Double = initialWeight

  /** Returns this unit's name. */
  override def name: String = storedName

  /** Returns this unit's remaining life points. */
  override def lifePoints: Double = storedLifePoints

  /** Returns this unit's physical defense. */
  override def defense: Int = storedDefense

  /** Returns this unit's own weight. */
  override def weight: Double = storedWeight

  /** Applies physical damage without reducing health below zero. */
  private def receivePhysicalAttack(attackPoints: Int): Unit = {
    require(
      lifePoints > 0.0,
      "A defeated unit cannot receive an attack."
    )

    val damage = math.max(0.0, attackPoints.toDouble - defense.toDouble)
    storedLifePoints = math.max(0.0, storedLifePoints - damage)
  }

  /** Receives a physical attack from a character. */
  override def receiveCharacterAttack(
                                       source: Character,
                                       action: Attack
                                     ): Unit = {
    receivePhysicalAttack(source.attackPoints)
  }


  /** Returns this unit's total physical weight. */
  override def totalWeight: Double = weight

  /** Returns the maximum value of this unit's action bar. */
  override def actionBarMaximum: Double = weight

  /** Receives a physical attack from an enemy. */
  override def receiveEnemyAttack(
                                   source: Enemy,
                                   action: Attack
                                 ): Unit = {
    receivePhysicalAttack(source.attackPoints)
  }
}
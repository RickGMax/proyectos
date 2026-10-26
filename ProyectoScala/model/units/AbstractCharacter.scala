package cl.uchile.dcc.model.units

import cl.uchile.dcc.model.actions.{Action, Attack, Target}
import cl.uchile.dcc.model.items.Usable
import cl.uchile.dcc.model.weapons.Weapon

/** Base implementation shared by playable characters. */
abstract class AbstractCharacter(
                                  name: String,
                                  initialLifePoints: Double,
                                  defense: Int,
                                  initialWeight: Double,
                                  initialWeapon: Option[Weapon],
                                  initialInventory: Array[Usable]
                                ) extends AbstractUnit(
  name, initialLifePoints, defense, initialWeight
) with Character {

  private var storedInventory: Array[Usable] = initialInventory.clone()

  /** Returns a copy of this character's inventory. */
  override def inventory: Array[Usable] = storedInventory.clone()

  /** Adds an item to this character's inventory. */
  override def addToInventory(item: Usable): Unit = {
    storedInventory = storedInventory :+ item
  }

  private var storedWeapon: Option[Weapon] = initialWeapon

  /** Returns this character's equipped weapon, if any. */
  override def weapon: Option[Weapon] = storedWeapon

  /** Equips the given weapon. */
  override def equipWeapon(newWeapon: Weapon): Unit = {
    storedWeapon = Some(newWeapon)
  }

  /** Removes the currently equipped weapon. */
  override def unequipWeapon(): Unit = {
    storedWeapon = None
  }
  private val attackAction: Action = new Attack

  /** Returns the equipped weapon's physical attack points. */
  def attackPoints: Int =
    weapon match {
      case Some(equippedWeapon) => equippedWeapon.attackPoints
      case None =>
        throw new IllegalArgumentException(
          "An unarmed character cannot attack."
        )
    }

  /** Returns the physical attack when this character is alive and armed. */
  override def availableActions: Seq[Action] =
    if (lifePoints > 0.0 && weapon.nonEmpty) Seq(attackAction)
    else Seq.empty

  /** Executes an action using character-specific dispatch. */
  override def doAction(action: Action, target: Target): Unit = {
    require(
      lifePoints > 0.0,
      "A defeated character cannot perform actions."
    )
    action.executeFromCharacter(this, target)
  }

  /** Returns the character's weight plus the equipped weapon's full weight. */
  override def totalWeight: Double =
    weapon match {
      case Some(equippedWeapon) => weight + equippedWeapon.weight
      case None => weight
    }

  /** Returns the character's weight plus half the equipped weapon's weight. */
  override def actionBarMaximum: Double =
    weapon match {
      case Some(equippedWeapon) => weight + 0.5 * equippedWeapon.weight
      case None => weight
    }

}
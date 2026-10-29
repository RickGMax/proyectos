package cl.uchile.dcc.model.units

import scala.collection.mutable.ArrayBuffer
import cl.uchile.dcc.model.actions.{
  Action, Attack, ConsumePotion, EquipWeapon, Healing, Target
}
import cl.uchile.dcc.model.items.{Usable, UsableVisitor}
import cl.uchile.dcc.model.potions.Potion
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

  /** Captures the unit state, equipped weapon and inventory. */
  override def captureState(): () => Unit = {
    val restoreUnit = super.captureState()
    val savedWeapon = storedWeapon
    val savedInventory = storedInventory.clone()

    () => {
      restoreUnit()
      storedWeapon = savedWeapon
      storedInventory = savedInventory.clone()
    }
  }

  /** Restores health using the target's maximum health and magical damage. */
  override def receiveHealing(
                               source: WhiteMage,
                               action: Healing,
                               magicDamage: Double
                             ): Unit = {
    require(
      magicDamage.isFinite && magicDamage >= 0.0,
      "Magical damage must be finite and non-negative."
    )

    restoreLifePoints(maxLifePoints * 0.2 + magicDamage / 4.0)
  }

  /** Returns a copy of this character's inventory. */
  override def inventory: Array[Usable] = storedInventory.clone()

  /** Adds an item to this character's inventory. */
  override def addToInventory(item: Usable): Unit = {
    storedInventory = storedInventory :+ item
  }

  /** Consumes one owned potion when this character is alive. */
  override def consumePotion(potion: Potion): Unit = {
    require(
      lifePoints > 0.0,
      "A defeated character cannot consume potions."
    )

    val index = storedInventory.indexWhere(_ eq potion)

    require(
      index >= 0,
      "The potion must belong to this character's inventory."
    )

    potion.applyTo(this)
    storedInventory =
      storedInventory.patch(index, Array.empty[Usable], 1)
  }

  private var storedWeapon: Option[Weapon] = initialWeapon
  require(
    initialWeapon.forall(_.canBeEquippedBy(this)),
    "The initial weapon is not compatible with this character."
  )

  /** Returns this character's equipped weapon, if any. */
  override def weapon: Option[Weapon] = storedWeapon

  /** Equips a compatible weapon when this character is alive. */
  override def equipWeapon(newWeapon: Weapon): Unit = {
    require(
      lifePoints > 0.0,
      "A defeated character cannot equip a weapon."
    )
    require(
      newWeapon.canBeEquippedBy(this),
      s"Weapon '${newWeapon.name}' is not compatible with this character."
    )

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

  /** Returns the actions available from this character's state and inventory. */
  override def availableActions: Seq[Action] = {
    if (lifePoints > 0.0) {
      val weapons = ArrayBuffer.empty[Usable]
      val potions = ArrayBuffer.empty[Usable]

      val collector = new UsableVisitor {
        override def visitWeapon(weapon: Weapon): Unit = {
          if (weapon.canBeEquippedBy(AbstractCharacter.this)) {
            weapons += weapon
          }
        }

        override def visitPotion(potion: Potion): Unit = {
          potions += potion
        }
      }

      storedInventory.foreach(_.accept(collector))

      val attackOptions: Seq[Action] =
        if (weapon.nonEmpty) Seq(attackAction)
        else Seq.empty

      val equipmentOptions: Seq[Action] =
        if (weapons.nonEmpty) Seq(new EquipWeapon(weapons.toArray))
        else Seq.empty

      val consumptionOptions: Seq[Action] =
        if (potions.nonEmpty) Seq(new ConsumePotion(potions.toArray))
        else Seq.empty

      attackOptions ++ equipmentOptions ++ consumptionOptions ++ movementActions
    } else {
      Seq.empty
    }
  }

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
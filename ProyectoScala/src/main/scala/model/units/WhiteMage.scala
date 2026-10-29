package cl.uchile.dcc.model.units

import cl.uchile.dcc.model.weapons.{Bow, Staff, Wand, Weapon}
import cl.uchile.dcc.model.items.Usable
import cl.uchile.dcc.model.actions.{Action, Healing, Purification, Target}

/** Represents a white mage capable of casting white magic. */
class WhiteMage(
                 name: String,
                 lifePoints: Double,
                 defense: Int,
                 weight: Double,
                 manaPoints: Double,
                 weapon: Option[Weapon] = None,
                 inventory: Array[Usable] = Array.empty[Usable]
               ) extends MagicCharacter(
  name, lifePoints, defense, weight, manaPoints, weapon, inventory
) {
  private val healingAction: Healing = new Healing
  private val purificationAction: Purification = new Purification

  /** Allows equipping bows. */
  override def canEquip(bow: Bow): Boolean = true

  /** Allows equipping wands. */
  override def canEquip(wand: Wand): Boolean = true

  /** Allows equipping staffs. */
  override def canEquip(staff: Staff): Boolean = true

  /** Returns ordinary actions and currently available white spells. */
  override def availableActions: Seq[Action] = {
    val healingOptions: Seq[Action] =
      if (canCastSpell(healingAction.manaCost)) Seq(healingAction)
      else Seq.empty

    val purificationOptions: Seq[Action] =
      if (canCastSpell(purificationAction.manaCost)) Seq(purificationAction)
      else Seq.empty

    super.availableActions ++ healingOptions ++ purificationOptions
  }

  /** Casts Healing and charges its resources after success. */
  override def castHealing(action: Healing, target: Target): Unit = {
    castSpell(action.manaCost) { damage =>
      target.receiveHealing(this, action, damage)
    }
  }

  /** Casts Purification and charges its resources after success. */
  override def castPurification(
                                 action: Purification,
                                 target: Target
                               ): Unit = {
    castSpell(action.manaCost) { damage =>
      target.receivePurification(this, action, damage)
    }
  }
}
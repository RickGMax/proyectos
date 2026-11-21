package cl.uchile.dcc.model.units

import cl.uchile.dcc.model.weapons.{Dagger, Staff, Wand, Weapon}
import cl.uchile.dcc.model.items.Usable
import cl.uchile.dcc.model.actions.{Action, Meteorite, Target, Thunder}

/** Represents a black mage capable of casting dark magic. */
class BlackMage(
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
  private val thunderAction: Thunder = new Thunder
  private val meteoriteAction: Meteorite = new Meteorite

  /** Allows equipping daggers. */
  override def canEquip(dagger: Dagger): Boolean = true

  /** Allows equipping wands. */
  override def canEquip(wand: Wand): Boolean = true

  /** Allows equipping staffs. */
  override def canEquip(staff: Staff): Boolean = true

  /** Returns ordinary actions and currently available dark spells. */
  override def availableActions: Seq[Action] = {
    val thunderOptions: Seq[Action] =
      if (canCastSpell(thunderAction.manaCost)) Seq(thunderAction)
      else Seq.empty

    val meteoriteOptions: Seq[Action] =
      if (canCastSpell(meteoriteAction.manaCost)) Seq(meteoriteAction)
      else Seq.empty

    super.availableActions ++ thunderOptions ++ meteoriteOptions
  }

  /** Casts Thunder and charges its resources after success. */
  override def castThunder(action: Thunder, target: Target): Unit = {
    castSpell(action.manaCost) { damage =>
      target.receiveThunder(this, action, damage)
    }
  }

  /** Casts Meteorite and charges its resources after success. */
  override def castMeteorite(action: Meteorite, target: Target): Unit = {
    castSpell(action.manaCost) { damage =>
      target.receiveMeteorite(this, action, damage)
    }
  }
}
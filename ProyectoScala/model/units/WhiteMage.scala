package cl.uchile.dcc.model.units
import cl.uchile.dcc.model.weapons.Weapon
import cl.uchile.dcc.model.items.Usable

/** Represents a white mage. */
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
)
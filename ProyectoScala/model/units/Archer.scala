package cl.uchile.dcc.model.units
import cl.uchile.dcc.model.weapons.Weapon
import cl.uchile.dcc.model.items.Usable
/** Represents an archer. */
class Archer(
              name: String,
              lifePoints: Double,
              defense: Int,
              weight: Double,
              weapon: Option[Weapon] = None,
              inventory: Array[Usable] = Array.empty[Usable]
            ) extends AbstractCharacter(
  name, lifePoints, defense, weight, weapon, inventory
)
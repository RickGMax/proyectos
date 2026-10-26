package cl.uchile.dcc.model.units
import cl.uchile.dcc.model.weapons.Weapon
import cl.uchile.dcc.model.items.Usable

/** Base implementation shared by characters with mana. */
abstract class MagicCharacter(
                               name: String,
                               lifePoints: Double,
                               defense: Int,
                               weight: Double,
                               initialManaPoints: Double,
                               weapon: Option[Weapon],
                               inventory: Array[Usable]
                             ) extends AbstractCharacter(
  name, lifePoints, defense, weight, weapon, inventory
) {
  private val storedManaPoints: Double = initialManaPoints

  /** Returns the character's mana points. */
  def manaPoints: Double = storedManaPoints
}
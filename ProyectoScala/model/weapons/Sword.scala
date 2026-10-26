package cl.uchile.dcc.model.weapons
import cl.uchile.dcc.model.units.Character

/** Represents a sword owned by a playable character. */
class Sword(
             name: String,
             attackPoints: Int,
             weight: Double,
             owner: Character
           ) extends AbstractWeapon(name, attackPoints, weight, owner, WeaponType.Sword)
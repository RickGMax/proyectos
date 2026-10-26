package cl.uchile.dcc.model.actions
import cl.uchile.dcc.model.items.Usable

/** Represents the weapon equipment action. */
class EquipWeapon(
                   possibleUsables: Array[Usable]
                 ) extends UsableAction("Equip a weapon",possibleUsables)

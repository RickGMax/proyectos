package cl.uchile.dcc.model.actions

import cl.uchile.dcc.model.units.{
  BlackMage, Character, Enemy, GameUnit, WhiteMage
}


/** Represents an entity that can receive game actions. */
trait Target {

  /** Rejects movement unless this target supports receiving a unit. */
  def receiveMove(source: GameUnit, action: Move): Unit = {
    throw new UnsupportedOperationException(
      "This target cannot receive a moving unit."
    )
  }

  /** Rejects Meteorite unless this target supports receiving it. */
  def receiveMeteorite(
                        source: BlackMage,
                        action: Meteorite,
                        damage: Double
                      ): Unit = {
    throw new UnsupportedOperationException(
      "This target cannot receive Meteorite."
    )
  }

  /** Rejects Purification unless this target supports receiving it. */
  def receivePurification(
                           source: WhiteMage,
                           action: Purification,
                           magicDamage: Double
                         ): Unit = {
    throw new UnsupportedOperationException(
      "This target cannot receive Purification."
    )
  }

  /** Rejects Healing unless this target supports receiving it. */
  def receiveHealing(
                      source: WhiteMage,
                      action: Healing,
                      magicDamage: Double
                    ): Unit = {
    throw new UnsupportedOperationException(
      "This target cannot receive Healing."
    )
  }

  /** Rejects Thunder unless this target supports receiving it. */
  def receiveThunder(
                      source: BlackMage,
                      action: Thunder,
                      damage: Double
                    ): Unit = {
    throw new UnsupportedOperationException(
      "This target cannot receive Thunder."
    )
  }

  /** Rejects consumption unless this target is a potion. */
  def receiveConsumePotion(
                            source: Character,
                            action: ConsumePotion
                          ): Unit = {
    throw new UnsupportedOperationException(
      "This target cannot be consumed as a potion."
    )
  }
  /** Rejects equipment unless this target supports it. */
  def receiveEquipWeapon(
                          source: Character,
                          action: EquipWeapon
                        ): Unit = {
    throw new UnsupportedOperationException(
      "This target cannot be equipped as a weapon."
    )
  }

  /** Rejects enemy attacks unless this target supports them. */
  def receiveEnemyAttack(source: Enemy, action: Attack):Unit = {
    throw new UnsupportedOperationException(
      "this target cannot receive an enemy attack"
    )
  }

  /** Rejects character attacks unless this target supports them. */
  def receiveCharacterAttack(source: Character, action: Attack): Unit = {
    throw new UnsupportedOperationException(
      "This target cannot receive a character attack."
    )
  }
}

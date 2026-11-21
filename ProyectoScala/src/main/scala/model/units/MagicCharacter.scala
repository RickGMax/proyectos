package cl.uchile.dcc.model.units

import cl.uchile.dcc.model.weapons.Weapon
import cl.uchile.dcc.model.items.Usable
import cl.uchile.dcc.model.potions.{MagicForcePotion, ManaPotion}

/** Base implementation shared by characters with mana. */
abstract class MagicCharacter(
                               name: String,
                               initialLifePoints: Double,
                               defense: Int,
                               weight: Double,
                               initialManaPoints: Double,
                               initialWeapon: Option[Weapon],
                               inventory: Array[Usable]
                             ) extends AbstractCharacter(
  name, initialLifePoints, defense, weight, initialWeapon, inventory
) {
  private var storedManaPoints: Double = initialManaPoints
  private val storedMaxManaPoints: Double = initialManaPoints
  private var storedMagicDamageMultiplier: Double = 1.0

  /** Captures the character state, remaining mana and magical bonus. */
  override def captureState(): () => Unit = {
    val restoreCharacter = super.captureState()
    val savedManaPoints = storedManaPoints
    val savedMagicDamageMultiplier = storedMagicDamageMultiplier

    () => {
      restoreCharacter()
      storedManaPoints = savedManaPoints
      storedMagicDamageMultiplier = savedMagicDamageMultiplier
    }
  }


  /** Returns whether the character currently meets a spell's requirements. */
  protected def canCastSpell(manaCost: Double): Boolean =
    lifePoints > 0.0 &&
      manaCost.isFinite &&
      manaCost >= 0.0 &&
      manaPoints >= manaCost &&
      magicDamage.exists(damage => damage.isFinite && damage >= 0.0)

  /**
   * Applies a spell effect and charges its resources after success.
   *
   * @param manaCost the spell's mana cost
   * @param effect   the effect receiving the calculated magical damage
   * @throws IllegalArgumentException if casting requirements are not met
   */
  protected def castSpell(manaCost: Double)(effect: Double => Unit): Unit = {
    require(
      lifePoints > 0.0,
      "A defeated character cannot cast spells."
    )
    require(
      manaCost.isFinite && manaCost >= 0.0,
      "The spell's mana cost must be finite and non-negative."
    )
    require(
      manaPoints >= manaCost,
      s"Not enough mana: required $manaCost, available $manaPoints."
    )

    val damage = magicDamage.getOrElse(
      throw new IllegalArgumentException(
        "An equipped magical weapon is required to cast spells."
      )
    )

    require(
      damage.isFinite && damage >= 0.0,
      "Spell damage must be finite and non-negative."
    )

    effect(damage)

    storedManaPoints -= manaCost
    storedMagicDamageMultiplier = 1.0
  }

  /** Returns the character's remaining mana points. */
  def manaPoints: Double = storedManaPoints

  /** Returns boosted spell damage, or None without a magical weapon. */
  def magicDamage: Option[Double] =
    weapon.flatMap(_.spellPower).map(_ * magicDamageMultiplier)

  /** Returns the character's maximum mana points. */
  def maxManaPoints: Double = storedMaxManaPoints

  /**
   * Spends mana without allowing a negative balance.
   *
   * @param amount the finite, non-negative amount to spend
   * @throws IllegalArgumentException if defeated, invalid or insufficient
   */
  def spendManaPoints(amount: Double): Unit = {
    require(
      lifePoints > 0.0,
      "A defeated character cannot spend mana."
    )
    require(
      amount.isFinite && amount >= 0.0,
      "Spent mana must be finite and non-negative."
    )
    require(
      amount <= manaPoints,
      "The character does not have enough mana."
    )

    storedManaPoints -= amount
  }

  /**
   * Restores mana without exceeding its maximum.
   *
   * @param amount the finite, non-negative amount to restore
   * @throws IllegalArgumentException if defeated or the amount is invalid
   */
  def restoreManaPoints(amount: Double): Unit = {
    require(
      lifePoints > 0.0,
      "A defeated character cannot recover mana."
    )
    require(
      amount.isFinite && amount >= 0.0,
      "Restored mana must be finite and non-negative."
    )

    storedManaPoints = math.min(maxManaPoints, storedManaPoints + amount)
  }

  /** Restores 30 percent of maximum mana without exceeding it. */
  override def receiveManaPotion(potion: ManaPotion): Unit = {
    restoreManaPoints(maxManaPoints * 0.3)
  }

  /** Returns the multiplier prepared for the next successful spell. */
  def magicDamageMultiplier: Double = storedMagicDamageMultiplier

  /** Prepares a 50 percent bonus for the next successful spell. */
  override def receiveMagicForcePotion(potion: MagicForcePotion): Unit = {
    require(
      lifePoints > 0.0,
      "A defeated character cannot receive a magic force bonus."
    )

    storedMagicDamageMultiplier = 1.5
  }
}
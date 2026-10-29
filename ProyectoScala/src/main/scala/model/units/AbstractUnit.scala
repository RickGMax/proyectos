package cl.uchile.dcc.model.units
import cl.uchile.dcc.model.actions.{
  Action, Attack, Meteorite, Move, Target, Thunder
}
import cl.uchile.dcc.model.map.Panel
import cl.uchile.dcc.model.players.Player


/** Shared implementation for playable characters and enemies. */
abstract class AbstractUnit(
                             initialName: String,
                             initialLifePoints: Double,
                             initialDefense: Int,
                             initialWeight: Double
                           ) extends GameUnit {

  private val storedName: String = initialName
  private var storedLifePoints: Double = initialLifePoints
  private val storedMaxLifePoints: Double = initialLifePoints
  private var storedDefense: Double = initialDefense.toDouble
  private val storedWeight: Double = initialWeight
  private var storedPanel: Option[Panel] = None
  private val moveAction: Action = new Move
  private var storedPlayer: Option[Player] = None

  /** Returns the player controlling this unit. */
  override def player: Option[Player] = storedPlayer

  /** Assigns a player without allowing conflicting ownership. */
  private[model] override def assignPlayer(owner: Player): Unit = {
    require(
      storedPlayer.forall(_ eq owner),
      "A unit cannot belong to more than one player."
    )

    storedPlayer = Some(owner)
  }

  /** Rejects physical attacks against the attacker or its allies. */
  private def validateAttackFrom(source: GameUnit): Unit = {
    require(
      source ne this,
      "A unit cannot attack itself."
    )
    require(
      !player.exists(owner => source.player.exists(_ eq owner)),
      "A unit cannot attack another unit controlled by the same player."
    )
  }

  /** Captures health, defense and position for later restoration. */
  override def captureState(): () => Unit = {
    val savedLifePoints = storedLifePoints
    val savedDefense = storedDefense
    val savedPanel = storedPanel

    () => {
      storedLifePoints = savedLifePoints
      storedDefense = savedDefense

      savedPanel match {
        case Some(panel) =>
          panel.addUnit(this)

        case None =>
          storedPanel.foreach(_.removeUnit(this))
      }
    }
  }

  /** Returns movement for a living unit with an adjacent destination. */
  protected def movementActions: Seq[Action] = {
    val hasDestination = currentPanel.exists { origin =>
      origin.adjacent.exists(destination => destination ne origin)
    }

    if (lifePoints > 0.0 && hasDestination) Seq(moveAction)
    else Seq.empty
  }

  /** Moves this living unit after validating its origin and destination. */
  override def moveTo(destination: Panel): Unit = {
    require(
      lifePoints > 0.0,
      "A defeated unit cannot move."
    )

    val origin = currentPanel.getOrElse(
      throw new IllegalArgumentException(
        "The unit must be placed on a panel before moving."
      )
    )

    require(
      origin ne destination,
      "The destination must differ from the current panel."
    )
    require(
      origin.adjacent.exists(_ eq destination),
      "The destination must be adjacent to the current panel."
    )

    destination.addUnit(this)
  }

  
  /** Returns the unit's current panel. */
  override def currentPanel: Option[Panel] = storedPanel

  /** Updates the panel reference from model operations. */
  private[model] override def updatePanel(panel: Option[Panel]): Unit = {
    storedPanel = panel
  }

  /** Removes this unit from its panel when defeated. */
  private def removeFromPanelIfDefeated(): Unit = {
    if (lifePoints <= 0.0) {
      storedPanel.foreach(_.removeUnit(this))
    }
  }

  /** Applies Meteorite damage received through a panel. */
  override def receiveMeteoriteDamage(
                                       source: BlackMage,
                                       action: Meteorite,
                                       damage: Double
                                     ): Unit = {
    receiveMagicalDamage(damage)
  }

  /** Applies magical damage without using physical defense. */
  protected def receiveMagicalDamage(damage: Double): Unit = {
    require(
      lifePoints > 0.0,
      "A defeated unit cannot receive magical damage."
    )
    require(
      damage.isFinite && damage >= 0.0,
      "Magical damage must be finite and non-negative."
    )

    storedLifePoints = math.max(0.0, storedLifePoints - damage)
    removeFromPanelIfDefeated()
  }


  /** Receives Thunder without applying physical defense. */
  override def receiveThunder(
                               source: BlackMage,
                               action: Thunder,
                               damage: Double
                             ): Unit = {
    receiveMagicalDamage(damage)
  }

  /** Returns this unit's name. */
  override def name: String = storedName

  /** Returns this unit's remaining life points. */
  override def lifePoints: Double = storedLifePoints

  /** Returns this unit's maximum life points. */
  override def maxLifePoints: Double = storedMaxLifePoints

  /** Restores a living unit's health without exceeding its maximum. */
  override def restoreLifePoints(amount: Double): Unit = {
    require(
      lifePoints > 0.0,
      "A defeated unit cannot recover life points."
    )
    require(
      amount.isFinite && amount >= 0.0,
      "Restored life points must be finite and non-negative."
    )

    storedLifePoints = math.min(maxLifePoints, storedLifePoints + amount)
  }

  /** Returns this unit's physical defense. */
  override def defense: Double = storedDefense

  /** Increases the current defense of a living unit. */
  override def increaseDefense(percentage: Double): Unit = {
    require(
      lifePoints > 0.0,
      "A defeated unit cannot increase its defense."
    )
    require(
      percentage.isFinite && percentage >= 0.0,
      "The defense increase must be finite and non-negative."
    )

    storedDefense += storedDefense * percentage / 100.0
  }

  /** Returns this unit's own weight. */
  override def weight: Double = storedWeight

  /** Receives a physical attack from a character after validating allegiance. */
  override def receiveCharacterAttack(
                                       source: Character,
                                       action: Attack
                                     ): Unit = {
    validateAttackFrom(source)
    receivePhysicalAttack(source.attackPoints)
  }

  /** Receives a physical attack from an enemy after validating allegiance. */
  override def receiveEnemyAttack(
                                   source: Enemy,
                                   action: Attack
                                 ): Unit = {
    validateAttackFrom(source)
    receivePhysicalAttack(source.attackPoints)
  }


  /** Returns this unit's total physical weight. */
  override def totalWeight: Double = weight

  /** Returns the maximum value of this unit's action bar. */
  override def actionBarMaximum: Double = weight

  /** Applies physical damage without reducing health below zero. */
  private def receivePhysicalAttack(attackPoints: Int): Unit = {
    require(
      lifePoints > 0.0,
      "A defeated unit cannot receive physical damage."
    )

    val damage = math.max(0.0, attackPoints.toDouble - defense)
    storedLifePoints = math.max(0.0, storedLifePoints - damage)
    removeFromPanelIfDefeated()
  }
}
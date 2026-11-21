package cl.uchile.dcc.controller

import cl.uchile.dcc.model.actions.{Action, Attack, Source, Target}
import cl.uchile.dcc.model.items.ItemFactory
import cl.uchile.dcc.model.map.Panel
import cl.uchile.dcc.model.players.Player
import cl.uchile.dcc.model.potions.PotionType
import cl.uchile.dcc.model.turns.TurnScheduler
import cl.uchile.dcc.model.units.{Character, CharacterFactory, CharacterType, GameUnit, MagicCharacter}
import cl.uchile.dcc.model.weapons.WeaponType
import scala.util.Random

/**
 * Coordinates battle turns and action execution.
 *
 * @param initialUnits the units participating in the battle
 * @param panels the panels created for an initialized battle
 */
class GameController private (initialUnits: Seq[GameUnit], val panels: Seq[Panel]) {
  private val scheduler = new TurnScheduler
  private var activeUnit: Option[GameUnit] = None

  private var battlePlayers: Vector[Player] = Vector.empty
  private var battleEndMessage: Option[String] = None

  /** Creates a controller for units supplied directly by the caller. */
  def this(initialUnits: Seq[GameUnit]) = this(initialUnits, Vector.empty)

  private def this(firstPlayer: Player, secondPlayer: Player, panels: Seq[Panel]) = {
    this(firstPlayer.units ++ secondPlayer.units, panels)

    require(
      firstPlayer ne secondPlayer,
      "A battle requires two different players."
    )

    battlePlayers = Vector(firstPlayer, secondPlayer)
    battlePlayers.foreach(_.addDefeatObserver(() => endBattle()))
    if (battlePlayers.exists(_.defeated)) endBattle()
  }

  /**
   * Creates a battle involving two different players and all their units.
   *
   * @param firstPlayer  the player controlling the first side
   * @param secondPlayer the player controlling the second side
   * @throws IllegalArgumentException if both players are the same instance
   */
  def this(firstPlayer: Player, secondPlayer: Player) =
    this(firstPlayer, secondPlayer, Vector.empty)

  /** Returns the players registered in this battle. */
  def players: Seq[Player] = battlePlayers

  /**
   * Returns whether a player in this battle has no living units.
   * Controllers created with a unit sequence do not track player defeat.
   */
  def isGameOver: Boolean =
    battleEndMessage.nonEmpty

  /** Returns the message recorded when a player loses the last unit. */
  def gameOverMessage: Option[String] = battleEndMessage

  /** Records the first defeat notification and clears the current turn. */
  private def endBattle(): Unit = {
    if (!isGameOver) {
      battleEndMessage = Some("Game over.")
      activeUnit = None
    }
  }

  private val initialUnitStates: Seq[(GameUnit, () => Unit)] =
    initialUnits.distinct.map(unit => (unit, unit.captureState()))

  initialUnitStates.foreach { case (unit, _) =>
    scheduler.addUnit(unit)
  }

  /**
   * Returns the actions currently available to a source.
   *
   * @param source the entity whose actions are requested
   * @return the source's available actions
   */
  def availableActions(source: Source): Seq[Action] =
    if (isGameOver) Seq.empty else source.availableActions

  /**
   * Executes a named action and advances the turn if the battle continues.
   *
   * @param source     the unit performing the action
   * @param actionName the name of the action to execute
   * @param target     the entity receiving the action
   * @return the action confirmation or the game-over message
   * @throws IllegalArgumentException      if the battle has ended, the source is
   *                                       not active, the action is unavailable or a requirement is not met
   *
   * @throws UnsupportedOperationException if the target does not support the action
   */
  def executeAction(
                     source: Source,
                     actionName: String,
                     target: Target
                   ): String = {

    require(!isGameOver, "The battle has already ended.")

    require(
      currentUnit.contains(source),
      "Only the active unit can perform an action."
    )

    source.findAction(actionName) match {
      case Some(action) =>
        if (action.isInstanceOf[Attack] && battlePlayers.nonEmpty) {
          val targetInBattle = target match {
            case unit: GameUnit => battlePlayers.exists(_.units.exists(_ eq unit))
            case _ => false
          }
          require(targetInBattle, "The attack target must participate in this battle.")
        }

        source.doAction(action, target)
        gameOverMessage.getOrElse {
          nextTurn()
          s"Action '$actionName' executed."
        }

      case None =>
        throw new IllegalArgumentException(
          s"Action '$actionName' is not available."
        )
    }
  }

  /**
   * Returns the unit currently playing its turn.
   *
   * @return the active unit, or None when no turn is active
   */
  def currentUnit: Option[GameUnit] =
    activeUnit

  /**
   * Removes defeated units and selects the next living unit.
   *
   * @return the next active unit, or None when the battle has ended or no units remain
   * @throws IllegalArgumentException if a living unit has invalid action bar values
   */
  def nextTurn(): Option[GameUnit] = {
    if (isGameOver) return None
    val registeredUnits = scheduler.units.toVector
    val livingUnits = registeredUnits.filter(_.lifePoints > 0.0)

    livingUnits.foreach { unit =>
      val maximum = unit.actionBarMaximum

      require(
        maximum.isFinite &&
          maximum > 0.0 &&
          unit.totalWeight > 0.0,
        "A unit must have positive total weight and a positive, finite action bar maximum"
      )
    }

    registeredUnits
      .filterNot(_.lifePoints > 0.0)
      .foreach(scheduler.removeUnit)

    activeUnit.foreach(scheduler.resetActionBar)
    activeUnit = None

    if (scheduler.units.nonEmpty) {
      while (scheduler.nextUnit.isEmpty) {
        scheduler.increaseActionBars(1.0)
      }

      activeUnit = scheduler.nextUnit
    }

    activeUnit
  }

  /** Restores the unit states captured when the controller was created. */
  def resetGame(): Unit = {
    activeUnit = None

    scheduler.units.toVector.foreach(scheduler.removeUnit)

    initialUnitStates.foreach { case (unit, restore) =>
      restore()
      scheduler.addUnit(unit)
    }
    battleEndMessage = None
    if (battlePlayers.exists(_.defeated)) endBattle()
  }
}
/** Initializes battles using the character and item factories. */
object GameController {

  /** Creates two randomly generated sides and places them on a three-column grid. */
  def initializeGame(random: Random = new Random()): GameController = {
    val teamSize = 3
    val characterTypes = CharacterType.values

    val characters = Vector.tabulate(teamSize) { index =>
      val kind = characterTypes(random.nextInt(characterTypes.length))
      val character = CharacterFactory.createCharacter(
        kind, s"$kind ${index + 1}", random
      )
      giveStartingItems(character, random)
      character
    }

    val enemies = Vector.tabulate(teamSize) { index =>
      CharacterFactory.createEnemy(s"Enemy ${index + 1}", random)
    }

    val grid = Vector.tabulate(3, teamSize) { (x, y) =>
      new Panel(x + 1, y + 1, Array.empty, Array.empty)
    }
    for {
      x <- grid.indices
      y <- grid(x).indices
    } {
      val adjacent = Vector((x - 1, y), (x + 1, y), (x, y - 1), (x, y + 1))
        .flatMap { case (column, row) => grid.lift(column).flatMap(_.lift(row)) }
      grid(x)(y).setAdjacentPanels(adjacent.toArray)
    }

    grid.head.zip(characters).foreach { case (panel, character) => panel.addUnit(character) }
    grid.last.zip(enemies).foreach { case (panel, enemy) => panel.addUnit(enemy) }

    new GameController(new Player(characters), new Player(enemies), grid.flatten)
  }


  /** Equips a compatible weapon and adds a usable potion. */
  private def giveStartingItems(character: Character, random: Random): Unit = {
    val isMage = character.isInstanceOf[MagicCharacter]
    val weaponTypes =
      if (isMage) Vector(WeaponType.Wand, WeaponType.Staff)
      else WeaponType.values.toVector

    val weapon = random.shuffle(weaponTypes).iterator
      .map(kind => ItemFactory.createWeapon(kind, character, random))
      .find(_.canBeEquippedBy(character))
      .getOrElse {
        throw new IllegalStateException(s"No compatible weapon for ${character.name}.")
      }

    character.addToInventory(weapon)
    character.equipWeapon(weapon)

    val potionTypes =
      if (isMage) PotionType.values.toVector
      else Vector(PotionType.Heal, PotionType.Strength)

    val potionType = potionTypes(random.nextInt(potionTypes.length))
    character.addToInventory(ItemFactory.createPotion(potionType))
  }
}

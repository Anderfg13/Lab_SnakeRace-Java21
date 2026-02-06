package co.eci.snake.concurrency;

import co.eci.snake.core.Board;
import co.eci.snake.core.Direction;
import co.eci.snake.core.Snake;
import co.eci.snake.core.GameState;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.BrokenBarrierException;

/**
 * Clase que controla el movimiento de una serpiente individual en un hilo separado.
 * 
 * Implementa Runnable para ser ejecutada en un hilo virtual de Java 21.
 * Cada instancia maneja una serpiente específica, verificando el estado del juego
 * y sincronizándose con otras serpientes mediante un CyclicBarrier durante
 * las pausas para garantizar una visualización consistente.
 * 
 * El runner soporta modo turbo que aumenta temporalmente la velocidad de la serpiente
 * cuando consume un power-up de turbo en el tablero.
 * 
 * @author Anderson Fabian Garcia Nieto
 * @author Juana Lozano Chaves
 * @version 1.0
 */
public final class SnakeRunner implements Runnable {

  /** La serpiente controlada por este runner */
  private final Snake snake;
  
  /** El tablero de juego donde se mueve la serpiente */
  private final Board board;
  
  /** Estado compartido del juego (STOPPED, RUNNING, PAUSED) */
  private AtomicReference<GameState> gameState = new AtomicReference<>(GameState.STOPPED);
  
  /** Barrera cíclica para sincronizar todas las serpientes durante pausa */
  private final CyclicBarrier pauseBarrier;
  
  /** Tiempo de espera base entre movimientos (ms) */
  private final int baseSleepMs = 80;
  
  /** Tiempo de espera en modo turbo (ms) */
  private final int turboSleepMs = 40;
  
  /** Contador de ticks restantes en modo turbo */
  private int turboTicks = 0;

  /** Indica si esta serpiente es controlada por un jugador (sin giros aleatorios) */
  private final boolean isPlayerControlled;

  /**
   * Constructor del runner de serpiente.
   * 
   * @param snake        La serpiente a controlar
   * @param board        El tablero de juego
   * @param gameState    Referencia atómica al estado del juego compartido
   * @param pauseBarrier Barrera para sincronización durante pausas
   */
  public SnakeRunner(Snake snake, Board board, AtomicReference<GameState> gameState, CyclicBarrier pauseBarrier) {
    this(snake, board, gameState, pauseBarrier, false);
  }

  /**
   * Constructor del runner de serpiente con opción de control por jugador.
   * 
   * @param snake              La serpiente a controlar
   * @param board              El tablero de juego
   * @param gameState          Referencia atómica al estado del juego compartido
   * @param pauseBarrier       Barrera para sincronización durante pausas
   * @param isPlayerControlled true si la serpiente es controlada por un jugador (sin giros aleatorios)
   */
  public SnakeRunner(Snake snake, Board board, AtomicReference<GameState> gameState, CyclicBarrier pauseBarrier, boolean isPlayerControlled) {
    this.snake = snake;
    this.board = board;
    this.gameState = gameState;
    this.pauseBarrier = pauseBarrier;
    this.isPlayerControlled = isPlayerControlled;
  }

  /**
   * Bucle principal de ejecución del runner.
   * 
   * Mientras el hilo no sea interrumpido:
   * - Si el juego está RUNNING: mueve la serpiente y procesa colisiones
   * - Si el juego está PAUSED: espera en la barrera y luego hace spin-wait
   * 
   * La velocidad de movimiento depende del estado de turbo.
   */
  @Override
  public void run() {
    try {
      while (!Thread.currentThread().isInterrupted()) {
        if (gameState.get() == GameState.RUNNING) {
            // Solo hacer giros aleatorios si NO es controlada por jugador
            if (!isPlayerControlled) {
              maybeTurn();
            }
          var res = board.step(snake);
        if (res == Board.MoveResult.HIT_OBSTACLE) {
          // Siempre rebotar al chocar con obstáculo (incluso serpiente del jugador)
          randomTurn();
        } else if (res == Board.MoveResult.ATE_TURBO) {
          turboTicks = 100;
        }
        int sleep = (turboTicks > 0) ? turboSleepMs : baseSleepMs;
        if (turboTicks > 0) turboTicks--;
        Thread.sleep(sleep);
        } else if (gameState.get() == GameState.PAUSED) {
          pauseBarrier.await();
          while (gameState.get() == GameState.PAUSED){
            Thread.sleep(50);
          }
        }
        }
    } catch (InterruptedException | BrokenBarrierException ie) {
      Thread.currentThread().interrupt();
    }
  }

  /**
   * Decide aleatoriamente si la serpiente debe girar.
   * 
   * En modo turbo, la probabilidad de giro es menor (5% vs 10%)
   * para mantener una trayectoria más recta a alta velocidad.
   */
  private void maybeTurn() {
    double p = (turboTicks > 0) ? 0.05 : 0.10;
    if (ThreadLocalRandom.current().nextDouble() < p) randomTurn();
  }

  /**
   * Gira la serpiente hacia una dirección aleatoria.
   * 
   * Selecciona una de las cuatro direcciones disponibles al azar.
   * La serpiente ignorará giros de 180° según la lógica en Snake.turn().
   */
  private void randomTurn() {
    var dirs = Direction.values();
    snake.turn(dirs[ThreadLocalRandom.current().nextInt(dirs.length)]);
  }

}

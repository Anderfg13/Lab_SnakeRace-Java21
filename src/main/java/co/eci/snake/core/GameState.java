package co.eci.snake.core;

/**
 * Enumeración que representa los posibles estados del juego Snake Race.
 * 
 * Esta enumeración es utilizada junto con AtomicReference para manejar de forma
 * segura el estado compartido entre múltiples hilos, permitiendo iniciar, pausar
 * y reanudar el juego de manera sincronizada.
 * 
 * Estados disponibles:
 * - STOPPED: El juego no ha iniciado o fue detenido
 * - RUNNING: El juego está en ejecución activa
 * - PAUSED: El juego está temporalmente pausado
 * 
 * @author Anderson Fabian Garcia Nieto
 * @author Juana Lozano Chaves
 * @version 1.0
 */
public enum GameState {
  /** El juego no ha iniciado o fue detenido completamente */
  STOPPED,
  /** El juego está en ejecución y las serpientes se mueven normalmente */
  RUNNING,
  /** El juego está pausado y las serpientes esperan para reanudar */
  PAUSED
}

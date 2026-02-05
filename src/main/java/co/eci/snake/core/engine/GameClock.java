package co.eci.snake.core.engine;

import co.eci.snake.core.GameState;

import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Clase que maneja el reloj del juego para actualizaciones periódicas de la UI.
 * 
 * Utiliza un ScheduledExecutorService para ejecutar un callback (tick)
 * a intervalos regulares. El reloj respeta el estado del juego y solo ejecuta
 * el tick cuando está en estado RUNNING.
 * 
 * Implementa AutoCloseable para liberación automática de recursos
 * con try-with-resources.
 * 
 * @author Anderson Fabian Garcia Nieto
 * @author Juana Lozano Chaves
 * @version 1.0
 */
public final class GameClock implements AutoCloseable {

  /** Scheduler de un solo hilo para ejecución periódica */
  private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
  
  /** Período entre ejecuciones del tick en milisegundos */
  private final long periodMillis;
  
  /** Callback a ejecutar en cada tick (típicamente repaint de UI) */
  private final Runnable tick;
  
  /** Estado actual del reloj (STOPPED, RUNNING, PAUSED) */
  private final AtomicReference<GameState> state = new AtomicReference<>(GameState.STOPPED);

  /**
   * Constructor del reloj del juego.
   * 
   * @param periodMillis Período entre ticks en milisegundos (debe ser positivo)
   * @param tick         Callback a ejecutar en cada tick (no puede ser null)
   * @throws IllegalArgumentException si periodMillis <= 0
   * @throws NullPointerException si tick es null
   */
  public GameClock(long periodMillis, Runnable tick) {
    if (periodMillis <= 0) throw new IllegalArgumentException("periodMillis must be > 0");
    this.periodMillis = periodMillis;
    this.tick = Objects.requireNonNull(tick, "tick");
  }

  /**
   * Inicia el reloj del juego.
   * 
   * Solo inicia si el estado actual es STOPPED. Una vez iniciado,
   * el tick se ejecutará periódicamente mientras el estado sea RUNNING.
   */
  public void start() {
    if (state.compareAndSet(GameState.STOPPED, GameState.RUNNING)) {
      scheduler.scheduleAtFixedRate(() -> {
        if (state.get() == GameState.RUNNING) tick.run();
      }, 0, periodMillis, TimeUnit.MILLISECONDS);
    }
  }

  /**
   * Pausa el reloj del juego.
   * El scheduler sigue ejecutándose pero el tick no se llama.
   */
  public void pause()  { state.set(GameState.PAUSED); }

  /**
   * Reanuda el reloj del juego después de una pausa.
   */
  public void resume() { state.set(GameState.RUNNING); }

  /**
   * Detiene completamente el reloj del juego.
   */
  public void stop()   { state.set(GameState.STOPPED); }

  /**
   * Cierra y libera los recursos del scheduler.
   * Llamado automáticamente cuando se usa try-with-resources.
   */
  @Override
  public void close() { scheduler.shutdownNow(); }
}

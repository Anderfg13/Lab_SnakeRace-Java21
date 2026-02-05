package co.eci.snake.core;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Clase que representa una serpiente en el juego Snake Race.
 * 
 * Cada serpiente tiene un cuerpo compuesto por posiciones, una dirección de movimiento,
 * y una longitud máxima que puede crecer al comer ratones. Esta clase es thread-safe
 * ya que los métodos que acceden al cuerpo están sincronizados para evitar condiciones
 * de carrera cuando múltiples hilos (UI y SnakeRunner) acceden simultáneamente.
 * 
 * La dirección se declara como volatile para garantizar visibilidad
 * entre hilos cuando se cambia con las teclas de dirección.
 * 
 * @author Anderson Fabian Garcia Nieto
 * @author Juana Lozano Chaves
 * @version 1.0
 */
public final class Snake {

  /** Cola que almacena las posiciones del cuerpo (la cabeza está al frente) */
  private final Deque<Position> body = new ArrayDeque<>();
  
  /** Dirección actual de movimiento (volatile para visibilidad entre hilos) */
  private volatile Direction direction;
  
  /** Longitud máxima actual del cuerpo */
  private int maxLength = 5;

  /**
   * Constructor privado de la serpiente.
   * 
   * @param start Posición inicial de la cabeza
   * @param dir   Dirección inicial de movimiento
   */
  private Snake(Position start, Direction dir) {
    body.addFirst(start);
    this.direction = dir;
  }

  /**
   * Método factory para crear una nueva serpiente.
   * 
   * @param x   Coordenada X inicial
   * @param y   Coordenada Y inicial
   * @param dir Dirección inicial de movimiento
   * @return Una nueva instancia de Snake
   */
  public static Snake of(int x, int y, Direction dir) {
    return new Snake(new Position(x, y), dir);
  }

  /**
   * Obtiene la dirección actual de movimiento.
   * 
   * @return La dirección actual ({@link Direction})
   */
  public Direction direction() { return direction; }

  /**
   * Cambia la dirección de movimiento de la serpiente.
   * 
   * Ignora giros de 180 grados (no se puede ir directamente en dirección opuesta).
   * Por ejemplo, si va hacia arriba, no puede girar directamente hacia abajo.
   * 
   * @param dir La nueva dirección deseada
   */
  public void turn(Direction dir) {
    if ((direction == Direction.UP && dir == Direction.DOWN) ||
        (direction == Direction.DOWN && dir == Direction.UP) ||
        (direction == Direction.LEFT && dir == Direction.RIGHT) ||
        (direction == Direction.RIGHT && dir == Direction.LEFT)) {
      return;
    }
    this.direction = dir;
  }

  /**
   * Obtiene la posición de la cabeza de la serpiente.
   * 
   * Método sincronizado para evitar condiciones de carrera cuando
   * el hilo de UI lee mientras SnakeRunner modifica el cuerpo.
   * 
   * @return La posición de la cabeza
   */
  public synchronized Position head() { return body.peekFirst(); }

  /**
   * Crea una copia instantánea del cuerpo de la serpiente.
   * 
   * Método sincronizado que retorna una copia defensiva del cuerpo,
   * permitiendo que la UI dibuje sin interferir con las modificaciones
   * del hilo de movimiento.
   * 
   * @return Una nueva Deque con las posiciones actuales del cuerpo
   */
  public synchronized Deque<Position> snapshot() { return new ArrayDeque<>(body); }

  /**
   * Avanza la serpiente a una nueva posición de cabeza.
   * 
   * Método sincronizado que agrega la nueva cabeza y opcionalmente
   * incrementa la longitud máxima si la serpiente creció (comió ratón).
   * Mantiene el cuerpo dentro de la longitud máxima permitida.
   * 
   * @param newHead La nueva posición para la cabeza
   * @param grow    true si la serpiente debe crecer (comió ratón)
   */
  public synchronized void advance(Position newHead, boolean grow) {
    body.addFirst(newHead);
    if (grow) maxLength++;
    while (body.size() > maxLength) body.removeLast();
  }
}

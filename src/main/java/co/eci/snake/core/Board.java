package co.eci.snake.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Clase que representa el tablero de juego de Snake Race.
 * 
 * El tablero contiene:
 * - Ratones: Al comerlos, la serpiente crece
 * - Obstáculos: Bloquean el movimiento, la serpiente rebota
 * - Turbo: Aumenta temporalmente la velocidad de la serpiente
 * - Teleports: Pares de posiciones que transportan la serpiente
 * 
 * Esta clase es thread-safe: los métodos de acceso a colecciones están sincronizados
 * y retornan copias defensivas para evitar modificaciones externas no controladas.
 * El método step() tiene una región crítica minimizada para optimizar
 * el rendimiento en escenarios de alta concurrencia.
 * 
 * @author Anderson Fabian Garcia Nieto
 * @author Juana Lozano Chaves
 * @version 1.0
 */
public final class Board {

  /** Ancho del tablero en celdas */
  private final int width;
  
  /** Alto del tablero en celdas */
  private final int height;

  /** Conjunto de posiciones con ratones */
  private final Set<Position> mice = new HashSet<>();
  
  /** Conjunto de posiciones con obstáculos */
  private final Set<Position> obstacles = new HashSet<>();
  
  /** Conjunto de posiciones con power-up de turbo */
  private final Set<Position> turbo = new HashSet<>();
  
  /** Mapa de pares de teleportación (entrada -> salida) */
  private final Map<Position, Position> teleports = new HashMap<>();

  /**
   * Enumeración que representa los posibles resultados de un movimiento.
   */
  public enum MoveResult {
    /** Movimiento normal sin evento especial */
    MOVED,
    /** La serpiente comió un ratón y creció */
    ATE_MOUSE,
    /** La serpiente chocó con un obstáculo */
    HIT_OBSTACLE,
    /** La serpiente recogió un power-up de turbo */
    ATE_TURBO,
    /** La serpiente fue teletransportada */
    TELEPORTED
  }

  /**
   * Constructor del tablero de juego.
   * 
   * Inicializa el tablero con dimensiones especificadas y genera
   * elementos aleatorios: 6 ratones, 4 obstáculos, 3 turbos y 2 pares de teleports.
   * 
   * @param width  Ancho del tablero (debe ser positivo)
   * @param height Alto del tablero (debe ser positivo)
   * @throws IllegalArgumentException si las dimensiones no son positivas
   */
  public Board(int width, int height) {
    if (width <= 0 || height <= 0) throw new IllegalArgumentException("Board dimensions must be positive");
    this.width = width;
    this.height = height;
    for (int i=0;i<6;i++) mice.add(randomEmpty());
    for (int i=0;i<4;i++) obstacles.add(randomEmpty());
    for (int i=0;i<3;i++) turbo.add(randomEmpty());
    createTeleportPairs(2);
  }

  /**
   * Obtiene el ancho del tablero.
   * @return Ancho en número de celdas
   */
  public int width() { return width; }

  /**
   * Obtiene el alto del tablero.
   * @return Alto en número de celdas
   */
  public int height() { return height; }

  /**
   * Obtiene una copia del conjunto de posiciones con ratones.
   * Método sincronizado que retorna copia defensiva.
   * @return Conjunto de posiciones de ratones
   */
  public synchronized Set<Position> mice() { return new HashSet<>(mice); }

  /**
   * Obtiene una copia del conjunto de posiciones con obstáculos.
   * Método sincronizado que retorna copia defensiva.
   * @return Conjunto de posiciones de obstáculos
   */
  public synchronized Set<Position> obstacles() { return new HashSet<>(obstacles); }

  /**
   * Obtiene una copia del conjunto de posiciones con turbo.
   * Método sincronizado que retorna copia defensiva.
   * @return Conjunto de posiciones de turbo
   */
  public synchronized Set<Position> turbo() { return new HashSet<>(turbo); }

  /**
   * Obtiene una copia del mapa de teleports.
   * Método sincronizado que retorna copia defensiva.
   * @return Mapa de pares de teleportación
   */
  public synchronized Map<Position, Position> teleports() { return new HashMap<>(teleports); }

  /**
   * Ejecuta un paso de movimiento para una serpiente.
   * 
   * Calcula la nueva posición, verifica colisiones y actualiza el estado
   * del tablero y la serpiente. La región crítica sincronizada se minimiza
   * para optimizar la concurrencia.
   * 
   * @param snake La serpiente a mover (no puede ser null)
   * @return El resultado del movimiento (MoveResult)
   * @throws NullPointerException si snake es null
   */
  public MoveResult step(Snake snake) {
    Objects.requireNonNull(snake, "snake");
    
    // Cálculos fuera de la región crítica (no acceden a recursos compartidos)
    var head = snake.head();
    var dir = snake.direction();
    Position next = new Position(head.x() + dir.dx, head.y() + dir.dy).wrap(width, height);

    // Región crítica: solo acceso a colecciones compartidas
    synchronized (this) {
      if (obstacles.contains(next)) return MoveResult.HIT_OBSTACLE;

      boolean teleported = false;
      if (teleports.containsKey(next)) {
        next = teleports.get(next);
        teleported = true;
      }

      boolean ateMouse = mice.remove(next);
      boolean ateTurbo = turbo.remove(next);

      snake.advance(next, ateMouse);

      if (ateMouse) {
        mice.add(randomEmpty());
        obstacles.add(randomEmpty());
        if (ThreadLocalRandom.current().nextDouble() < 0.2) turbo.add(randomEmpty());
      }

      if (ateTurbo) return MoveResult.ATE_TURBO;
      if (ateMouse) return MoveResult.ATE_MOUSE;
      if (teleported) return MoveResult.TELEPORTED;
      return MoveResult.MOVED;
    }
  }

  /**
   * Crea pares de teleports bidireccionales en posiciones aleatorias.
   * 
   * @param pairs Número de pares de teleport a crear
   */
  private void createTeleportPairs(int pairs) {
    for (int i=0;i<pairs;i++) {
      Position a = randomEmpty();
      Position b = randomEmpty();
      teleports.put(a, b);
      teleports.put(b, a);
    }
  }

  /**
   * Genera una posición aleatoria vacía (sin ratones, obstáculos, turbo ni teleports).
   * 
   * Intenta hasta width*height*2 veces antes de retornar cualquier posición
   * para evitar bucles infinitos en tableros muy llenos.
   * 
   * @return Una posición vacía aleatoria
   */
  private Position randomEmpty() {
    var rnd = ThreadLocalRandom.current();
    Position p;
    int guard = 0;
    do {
      p = new Position(rnd.nextInt(width), rnd.nextInt(height));
      guard++;
      if (guard > width*height*2) break;
    } while (mice.contains(p) || obstacles.contains(p) || turbo.contains(p) || teleports.containsKey(p));
    return p;
  }
}

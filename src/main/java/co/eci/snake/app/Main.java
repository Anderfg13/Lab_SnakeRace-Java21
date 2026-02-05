package co.eci.snake.app;

import co.eci.snake.ui.legacy.SnakeApp;

/**
 * Clase principal que sirve como punto de entrada de la aplicación Snake Race.
 * Esta clase contiene el método main que inicia la interfaz gráfica del juego
 * de serpientes concurrente. El juego utiliza hilos virtuales de Java 21 para
 * manejar múltiples serpientes simultáneamente.
 * 
 * @author Anderson Fabian Garcia Nieto
 * @author Juana Lozano Chaves
 * @version 1.0
 */
public final class Main {

  /**
   * Constructor privado para evitar instanciación de clase utilitaria.
   */
  private Main() {}

  /**
   * Método principal que inicia la aplicación Snake Race.
   * Delega al método {@link SnakeApp#launch()} para inicializar la interfaz
   * gráfica de usuario en el hilo de eventos de Swing (Event Dispatch Thread).
   * El número de serpientes se puede configurar mediante la propiedad del sistema
   * {@code -Dsnakes=N}.
   * 
   * @param args Argumentos de línea de comandos (no utilizados directamente)
   */
  public static void main(String[] args) {
    SnakeApp.launch();
  }
}

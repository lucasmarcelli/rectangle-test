package ca.marcelli;

import ca.marcelli.entities.DrawRectangle;
import java.util.HashMap;

public class Main {

  public static void main(String[] args) {
    System.out.println("Launching frame.");
    new RectangleFrame(createInitialRectangles());
  }

  private static HashMap<String, DrawRectangle> createInitialRectangles() {
    HashMap<String, DrawRectangle> rectangles = new HashMap<>();

    // Not intersecting
    DrawRectangle r = new DrawRectangle(10, 25, 150, 150);
    rectangles.put(r.getName(), r);
    r = new DrawRectangle(170, 25, 150, 150);
    rectangles.put(r.getName(), r);

    // Intersecting
    r = new DrawRectangle(10, 195, 150, 150);
    rectangles.put(r.getName(), r);
    r = new DrawRectangle(140, 225, 100, 100);
    rectangles.put(r.getName(), r);

    // Contained
    r = new DrawRectangle(400, 25, 300, 300);
    rectangles.put(r.getName(), r);
    r = new DrawRectangle(400, 25, 100, 100);
    rectangles.put(r.getName(), r);

    // Adjacent
    r = new DrawRectangle(300, 400, 50, 50);
    rectangles.put(r.getName(), r);

    r = new DrawRectangle(350, 400, 50, 50);
    rectangles.put(r.getName(), r);

    r = new DrawRectangle(150, 400, 50, 45);
    rectangles.put(r.getName(), r);

    r = new DrawRectangle(200, 400, 50, 50);
    rectangles.put(r.getName(), r);

    r = new DrawRectangle(700, 400, 100, 100);
    rectangles.put(r.getName(), r);

    r = new DrawRectangle(800, 425, 50, 50);
    rectangles.put(r.getName(), r);
    return rectangles;
  }
}

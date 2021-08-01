package ca.marcelli.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.marcelli.RectanglePanel;
import java.awt.Point;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class DrawRectangleTest {

  @Test
  void testIsIntersectingWith() {
    // Check if an intersection is found, and that it's commutative
    DrawRectangle r = new DrawRectangle(0, 0, 20, 30);
    DrawRectangle r1 = new DrawRectangle(10, 10, 30, 40);
    assertNotNull(r1.isIntersectingWith(r));
    assertNotNull(r.isIntersectingWith(r1));

    // Check that intersections are not found if they don't exist
    DrawRectangle r2 = new DrawRectangle(400, 400, 10, 10);
    assertNull(r2.isIntersectingWith(r1));
    assertNull(r1.isIntersectingWith(r2));
  }

  @Test
  void testIsAdjacentTo() {
    // Check if it finds the adjacent rectangle, and that it's commutative
    DrawRectangle r = new DrawRectangle(0, 0, 20, 30);
    DrawRectangle r1 = new DrawRectangle(20, 0, 20, 20);
    assertFalse(r.isAdjacentTo(r1).isEmpty());
    assertFalse(r1.isAdjacentTo(r).isEmpty());

    // Make sure the adjacent segment is correct, and that it got the right type
    DrawSegment segment = r.isAdjacentTo(r1).get(0);
    DrawSegment expected = new DrawSegment(List.of(new Point(20, 0), new Point(20, 20)), "");
    assertEquals(expected, segment);
    assertTrue(segment.getMessage().contains("Sub-Line"));

    // Check proper
    DrawRectangle r3 = new DrawRectangle(0, 30, 20, 30);
    segment = r.isAdjacentTo(r3).get(0);
    expected = new DrawSegment(List.of(new Point(0, 30), new Point(20, 30)), "");
    assertEquals(expected, segment);
    assertTrue(segment.getMessage().contains("Proper"));

    // Check partial
    DrawRectangle r4 = new DrawRectangle(20, 10, 20, 40);
    segment = r.isAdjacentTo(r4).get(0);
    expected = new DrawSegment(List.of(new Point(20, 10), new Point(20, 30)), "");
    assertEquals(expected, segment);
    assertTrue(segment.getMessage().contains("Partial"));
  }

  @Test
  void testGetVertices() {
    // Just make sure that the right points are returned for a rectangle
    DrawRectangle r = new DrawRectangle(0, 0, 20, 30);
    Set<Point> expected = Set.of(
        new Point(0, 0),
        new Point(0, 30),
        new Point(20, 0),
        new Point(20, 30)
    );
    assertEquals(expected, r.getVertices());
  }

  @Test
  void testHasContainmentWith() {
    // Test that containment works and that the inverse does not
    DrawRectangle r = new DrawRectangle(0, 0, 30, 30);
    DrawRectangle r1 = new DrawRectangle(5, 5, 5, 5);
    assertTrue(r.hasContainmentWith(r1));
    assertFalse(r1.hasContainmentWith(r));
  }


  @Test
  void testExtractIntersects() {
    // Testing this using RectanglePanel.getAllIntersectionsFor because the test would
    // look exactly the same as that. This also effectively tests getPointHashes.
    RectanglePanel rectanglePanel = new RectanglePanel();
    DrawRectangle r = new DrawRectangle(0, 0, 20, 30);
    DrawRectangle r1 = new DrawRectangle(10, 10, 30, 40);
    DrawRectangle intersection = r.isIntersectingWith(r1);
    // Normal intersection
    List<Drawable> expected = List.of(new DrawPoint(20, 10), new DrawPoint(10, 30));
    List<Drawable> result = rectanglePanel.getAllIntersectionsFor(intersection, r, r1);
    assertEquals(expected.size(), result.size());
    assertTrue(result.containsAll(expected));
    // No intersection
    DrawRectangle r2 = new DrawRectangle(400, 400, 10, 10);
    intersection = r.isIntersectingWith(r2);
    assertNull(rectanglePanel.getAllIntersectionsFor(intersection, r, r2));
    // Edge case
    DrawRectangle r3 = new DrawRectangle(10, 10, 14, 100);
    intersection = r1.isIntersectingWith(r3);
    DrawSegment expectedSegment = new DrawSegment(new DrawPoint(10, 50), new DrawPoint(10, 10));
    DrawSegment expectedSegment1 = new DrawSegment(new DrawPoint(24, 10), new DrawPoint(10, 10));
    expected = List.of(new DrawPoint(24, 50), expectedSegment, expectedSegment1);
    result = rectanglePanel.getAllIntersectionsFor(intersection, r1, r3);
    assertEquals(expected.size(), result.size());
    assertTrue(result.containsAll(expected));
  }
}
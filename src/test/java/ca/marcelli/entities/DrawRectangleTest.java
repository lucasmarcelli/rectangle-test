package ca.marcelli.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    var expected = Set.of(
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

}
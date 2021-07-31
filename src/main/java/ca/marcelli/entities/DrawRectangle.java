package ca.marcelli.entities;

import com.github.javafaker.Faker;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DrawRectangle extends Drawable {

  // I want a name to identify rectangles by, faker comes in handy for this
  private String name = Faker.instance().name().firstName();

  public DrawRectangle(int x, int y, int width, int length) {
    super(x, y, width, length);
  }

  public static List<Map<Integer, List<Point>>> getPointHashes(Set<Point> vertices) {
    Map<Integer, List<Point>> xPointsMap = new HashMap<>();
    Map<Integer, List<Point>> yPointsMap = new HashMap<>();
    vertices.forEach(point -> {
      List<Point> xPointList = new ArrayList<>();
      List<Point> yPointList = new ArrayList<>();
      xPointsMap.putIfAbsent(point.x, xPointList);
      yPointsMap.putIfAbsent(point.y, yPointList);
      xPointList.addAll(xPointsMap.get(point.x));
      yPointList.addAll(yPointsMap.get(point.y));
      yPointList.add(point);
      xPointList.add(point);
      xPointsMap.put(point.x, xPointList);
      yPointsMap.put(point.y, yPointList);
    });
    xPointsMap.values().removeIf(v -> v.size() < 3);
    yPointsMap.values().removeIf(v -> v.size() < 3);
    return List.of(xPointsMap, yPointsMap);
  }

  // Deep copy, mostly for adding the indicator rectangle to the hash
  public static DrawRectangle copyRectangle(DrawRectangle drawRectangle) {
    DrawRectangle dr =
        new DrawRectangle(drawRectangle.getX(), drawRectangle.getY(), drawRectangle.getWidth(),
            drawRectangle.getLength());
    dr.setName(drawRectangle.getName());
    return dr;
  }

  // Check for intersection using the points defined
  // return a rectangle that shows the intersection if it exists
  public DrawRectangle isIntersectingWith(DrawRectangle other) {
    int leftXPoint = Math.max(getX(), other.getX());
    int rightXPoint = Math.min(getX() + getWidth(), other.getX() + other.getWidth());
    int topYPoint = Math.max(getY(), other.getY());
    int bottomYPoint = Math.min(getY() + getLength(), other.getY() + other.getLength());
	  if (leftXPoint >= rightXPoint || topYPoint >= bottomYPoint) {
		  return null;
	  }
    return new DrawRectangle(leftXPoint, topYPoint, rightXPoint - leftXPoint,
        bottomYPoint - topYPoint);
  }

  public List<DrawSegment> isAdjacentTo(DrawRectangle other) {
    Set<Point> allPoints = getVertices();
    allPoints.addAll(other.getVertices());

    // 6 points means a proper adjacency, and the adjacent segment is the two shared
    // vertices
    if (allPoints.size() == 6) {
      List<DrawPoint> points = other.getVertices().stream().filter(p -> getVertices().contains(p))
          .map(DrawPoint::new).collect(Collectors.toList());
      return List.of(new DrawSegment(points.get(0), points.get(1),
          String.format("%s is Proper Adjacent to %s", getName(), other.getName())));
    }

    if (allPoints.size() == 7) {
      String message = String.format("%s is Sub-Line Adjacent to %s", getName(), other.getName());
      // Find the shared vertex
      var shared = other.getVertices().stream().filter(p -> getVertices().contains(p))
          .collect(Collectors.toList()).get(0);
      // The adjacent side will be the smaller of the two if it exists
      int length = Math.min(other.getLength(), getLength());
      int width = Math.min(other.getWidth(), getWidth());
      return allPoints.stream().filter(point -> (point.x == shared.x
          && (getX() == other.getX() + other.getWidth() || other.getX() == getX() + getWidth())
          && (shared.y + length == point.y || shared.y - length == point.y))
          || (point.y == shared.y
          && (getY() == other.getY() + other.getLength() || other.getY() == getY() + getLength())
          && (shared.x + width == point.x || shared.x - width == point.x)))
          .map(p -> new DrawSegment(List.of(shared, p), message)).collect(Collectors.toList());
    }

    List<Map<Integer, List<Point>>> hashes = getPointHashes(allPoints);
    return Stream
        .concat(getAdjacentPointsFromHash(hashes.get(0), false).entrySet().stream()
                .flatMap(entry -> getAdjacentType(entry.getValue(), other, false)),
            getAdjacentPointsFromHash(hashes.get(1), true).entrySet().stream()
                .flatMap(entry -> getAdjacentType(entry.getValue(), other, true)))
        .filter(Objects::nonNull).distinct().collect(Collectors.toList());
  }

  private Stream<DrawSegment> getAdjacentType(List<Point> points, DrawRectangle other,
                                              boolean isY) {
    String message;
    var maxPoint = points.stream().max(Comparator.comparingInt(p -> isY ? p.x : p.y)).get();
    var minPoint = points.stream().min(Comparator.comparingInt(p -> isY ? p.x : p.y)).get();
    var max = isY ? maxPoint.x : maxPoint.y;
    var min = isY ? minPoint.x : minPoint.y;
    if (isY && (max <= getX() && max <= getX() + getWidth()
        || max <= other.getX() && max <= other.getX() + other.getWidth())) {
      return null;
    } else if (!isY && (max <= getY() && max <= getY() + getLength()
        || max <= other.getY() && max <= other.getY() + other.getLength())) {
      return null;
    }
    int minSize =
        isY ? Math.min(other.getWidth(), getWidth()) : Math.min(other.getLength(), getLength());
    if (max - min != minSize) {
      message = "Partial Adjacent";
    } else {
      message = "Sub-line Adjacent";
    }
    return Stream.of(new DrawSegment(points,
        String.format("%s is %s to %s", getName(), message, other.getName())));
  }

  private Map<Integer, List<Point>> getAdjacentPointsFromHash(Map<Integer, List<Point>> hash,
                                                              boolean isY) {
    hash.replaceAll((k, points) -> {
      int count = points.size();
      return points.stream().sorted(Comparator.comparingInt(p -> isY ? p.x : p.y)).skip(1)
          .limit(count - 2)
          .collect(Collectors.toList());
    });
    return hash;
  }

  // Get all 4 vertices of the rectangle
  public Set<Point> getVertices() {
    Set<Point> vertices = new HashSet<>();
    vertices.add(new Point(getX(), getY()));
    vertices.add(new Point(getX() + getWidth(), getY()));
    vertices.add(new Point(getX(), getY() + getLength()));
    vertices.add(new Point(getX() + getWidth(), getY() + getLength()));
    return vertices;
  }

  @Override
  public void draw(Graphics2D g2d) {
    super.draw(g2d);
    // Draw the name in the centre of the rectangle
    int textWidth = g2d.getFontMetrics().stringWidth(getName());
    int textHeight = g2d.getFontMetrics().getHeight();
    g2d.drawString(getName(), getX() + getWidth() / 2 - textWidth / 2,
        getY() + getLength() / 2 + textHeight / 2);
  }

  // Containment is just intersection, but one of the rectangles directly matches
  // the
  // resulting intersection rectangle's dimensions.
  public boolean contains(DrawRectangle other) {
    DrawRectangle intersection = isIntersectingWith(other);
    return null != intersection && contains(other, intersection);
  }

  // Overload that assumes you already found the intersection because it's handy
  public boolean contains(DrawRectangle other, DrawRectangle intersection) {
    return other.equals(intersection);
  }
}

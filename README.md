# Rectangle Take-home

### Setup and Start

If using an IDE like IntelliJ, you can simply run `Main.java` in the runner after building. Otherwise:

1. I used maven for dependencies, so run `mvn install` in the project root.
2. `mvn compile exec:java -Dexec.mainClass=ca.marcelli.Main`

### Usage

A swing window will come up. Each rectangle has a friendly name to make it easier to follow. The default rectangles are there to demonstrate all 4 states clearly.

You can press the "Evaluate" button to see what rectangles have intersections, containment within, or are adjacent to the one pressed.
The results will appear as labels beneath the list. Intersections will highlight in pink, containment will highlight in blue.
Line intersection points are highlighted in magenta.

You can draw new rectangles and evaluate against those, simply click and drag in the right panel. Rectangles can be removed by pressing `delete`.

### Tests

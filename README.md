# Rectangle Take-home

### Setup and Start

If using an IDE like IntelliJ, you can simply run `Main.java` in the runner after building. Otherwise:

1. I used maven for dependencies, so run `mvn install` in the project root.
2. `mvn compile exec:java -Dexec.mainClass=ca.marcelli.Main`

The majority of the logic concerning the actual rectangles is in `DrawRectangle`, with `RectanglePanel` doing some of the work.
The other entities are mainly to help render things to the GUI, or to give information. There are extensive comments explaining the approach taken,
edge cases, and implementation details for the most important, relevant pieces, and smaller comments throughout.

### Usage

A swing window will come up. Each rectangle has a friendly name to make it easier to follow. The default rectangles can demonstrate some of the
scenarios easily.

You can press the "Evaluate" button to see what rectangles have intersections, containment within, or are adjacent to the one pressed.
The results will appear as labels on the GUI. Intersections will highlight in pink, containment will highlight in blue.
Line intersection points are highlighted. Adjacent lines will be highlighted as well. 

You can draw new rectangles and evaluate against those, simply click and drag in the right panel. Rectangles can be removed by pressing `delete`.

### Tests

`mvn test` will run unit tests.


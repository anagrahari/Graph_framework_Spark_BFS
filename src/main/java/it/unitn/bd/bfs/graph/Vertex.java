package it.unitn.bd.bfs.graph;

import com.google.common.base.Splitter;

import java.io.Serializable;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Vertex is used for MapReduce Graph processing and storing intermediate results in format:
 * <p/>
 * | ID |  Neighbours  |    Path   | Distance | Color |
 * -----------------------------------------------------
 * |  3 |    [2, 4, 5] | [0, 5, 3] |     2    | BLACK |
 * |  5 |       [0, 3] |    [0, 5] |     1    | BLACK |
 * |  1 |       [0, 2] |    [0, 1] |     1    | BLACK |
 * |  0 |    [1, 2, 5] |       [0] |     0    | BLACK |
 * |  2 | [0, 1, 3, 4] |    [0, 2] |     1    | BLACK |
 * |  4 |       [2, 3] | [0, 2, 4] |     2    | BLACK |
 *
 * @see Color
 */
public final class Vertex implements Serializable {

    private static final String BAR_SEPARATOR = "\\|";
    private static final Splitter BAR = Splitter.on(BAR_SEPARATOR);
    private static final Splitter COMMA = Splitter.on(",").trimResults().omitEmptyStrings();

    private final int id;

    private Set<Integer> neighbours;

    private List<Integer> path;

    private int distance;

    private Color color;

    public Vertex(int id, Set<Integer> neighbours, List<Integer> path, int distance, Color color) {
        this.id = id;
        this.neighbours = neighbours;
        this.path = path;
        this.distance = distance;
        this.color = color;
    }

    /**
     * Construct vertex from a source string (provided by the same class in toString() method)
     *
     * @param source vertex string
     */
    public Vertex(String source) {
    	// Iterator<String> token = BAR.splitToList(source).iterator();
    	source = source.replace("\\", "");
    	System.out.println("Source " + source);
    	
    	String[] tokens = source.split("\\|");
    	System.out.println("token size " + tokens.length);
    	for(int j =0;j<tokens.length;j++){
    		System.out.println("tokens " + tokens[j]);
    	}
    	//System.out.println("tokens " + tokens[1]);
      //  Iterator<String> tokens = BAR.splitToList(source).iterator();
        id = Integer.parseInt(tokens[0]/*tokens.next()*/);
        neighbours = new HashSet<>();
        String s = tokens[1].replace("[", "").replace("]", "");
        String[] str = s.split(",");
        System.out.println("str " + str[0]);
        for (int i =0 ;i<str.length;i++) {
            neighbours.add(Integer.parseInt(str[i].trim()));
        }
       /* for (String vertex : COMMA.splitToList(tokens[1].replace("[", "").replace("]", ""))) {
            neighbours.add(Integer.parseInt(vertex));
        }*/
        path = new LinkedList<>();
        s = tokens[2].replace("[", "").replace("]", "");
        str = s.split(",");
        for (int j=0;j<str.length;j++) {
        	path.add(Integer.parseInt(str[j].trim()));
        }
      /*  for (String vertex : COMMA.splitToList(tokens.next().replace("[", "").replace("]", ""))) {
            path.add(Integer.parseInt(vertex));
        }*/
        distance = Integer.parseInt(tokens[3]);
        color = Color.valueOf(tokens[4]);
        System.out.print("id " + id + "neighbours " + neighbours + " paths " + path + "distance " + distance +" color " + color);
    }

    public int getId() {
        return id;
    }

    public Set<Integer> getNeighbours() {
        return Collections.unmodifiableSet(neighbours);
    }

    public void addNeighbour(int vertex) {
        neighbours.add(vertex);
    }

    public List<Integer> getPath() {
        return Collections.unmodifiableList(path);
    }

    public int getDistance() {
        return distance;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        if (o instanceof Vertex) {
            Vertex object = (Vertex) o;

            return Objects.equals(id, object.id) &&
                    Objects.equals(neighbours, object.neighbours) &&
                    Objects.equals(path, object.path) &&
                    Objects.equals(distance, object.distance) &&
                    Objects.equals(color, object.color);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, neighbours, path, distance, color);
    }

    /**
     * Provides the source to regenerate a vertex from
     *
     * @return vertex string
     */
    @Override
    public String toString() {
        return id + BAR_SEPARATOR + neighbours + BAR_SEPARATOR + path + BAR_SEPARATOR + distance + BAR_SEPARATOR + color;
    }
}
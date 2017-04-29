package it.unitn.bd.bfs;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFlatMapFunction;

import com.google.common.base.Joiner;
import com.google.common.base.Stopwatch;

import it.unitn.bd.ServiceConfiguration;
import it.unitn.bd.bfs.graph.Color;
import it.unitn.bd.bfs.graph.Vertex;
import scala.Tuple2;

/**
 * Parallel BFS test using Spark
 * <p/>
 * NOTE: Remember to configure the environment in "service.properties" !
 * NOTE: If running from IDE, remember to remove intermediate results fro under the "test-sets/" folder
 *
 * @see ServiceConfiguration
 */
public final class BfsSpark {

  //  private static final Logger logger = LogManager.getLogger();

    private static final Joiner NEW_LINE = Joiner.on("\n");

    private static final String APP_NAME = ServiceConfiguration.getAppName();
  //  private static final String IP = ServiceConfiguration.getIp();
 //   private static final int PORT = ServiceConfiguration.getPort();
 //   private static final String JAR = ServiceConfiguration.getJar();
  //  private static final List<String> PROBLEM_FILES = ServiceConfiguration.getProblemFiles();

    public static void main(String[] args) throws Exception {
     //   String master = "spark://" + IP + ':' + PORT;
    //    logger.info("Application name: " + APP_NAME);
     //   logger.info("Problem files path: " + PROBLEM_FILES);
   //     logger.info("Using JAR file: " + JAR);
      //  logger.info("Connecting to: " + master);

        JavaSparkContext spark = new JavaSparkContext(new SparkConf().setAppName("BFS_SPARK"));
      // JavaRDD<String> PROBLEM_FILES = spark.textFile("small_graph");
      //  spark.addJar("target/" + JAR + ".jar");
String problemFile = "C:/Users/ANKIT/workspace_spark/Sample/test-sets/tinyCG.txt";
     //   for (String problemFile : PROBLEM_FILES) {
   //         logger.info("Problem file: " + problemFile);
            GraphFileUtil.convert(problemFile);

            int index = 0;
            boolean isGrayVertex = true;
        //    Stopwatch stopwatch = Stopwatch.createUnstarted();
            
            // Continue until there is at least one GRAY vertex
            while (isGrayVertex) {
                JavaRDD<String> lines = spark.textFile(problemFile + '_' + index++);
                System.err.println(lines);
           //     stopwatch.start();
                long startTime = System.currentTimeMillis();
                // Recover vertices from text file and emit all of them plus emit the GRAY neighbours as new modified vertices
                JavaPairRDD<Integer, Vertex> mapper = lines.flatMapToPair(new PairFlatMapFunction<String, Integer, Vertex>() {

					/*@Override
					public Iterator<Tuple2<Integer, Vertex>> call(String arg0) throws Exception {
						// TODO Auto-generated method stub
						return null;
					}*/
                    @Override
                    public Iterator<Tuple2<Integer, Vertex>> call(String source) throws Exception {
                        Vertex vertex = new Vertex(source);
                        Set<Tuple2<Integer, Vertex>> result = new HashSet<>();

                        // Emit neighbours of a GRAY vertex
                        if (vertex.getColor() == Color.GRAY) {
                            for (final int neighbour : vertex.getNeighbours()) {
                                List<Integer> path = new LinkedList<Integer>(vertex.getPath()) {{
                                    add(neighbour);
                                }};
                                result.add(new Tuple2<>(neighbour, new Vertex(neighbour, new HashSet<Integer>(), path, vertex.getDistance() + 1, Color.GRAY)));
                            }
                            vertex.setColor(Color.BLACK);
                        }

                        // Emit the current vertex
                        result.add(new Tuple2<>(vertex.getId(), vertex));
                        return result.iterator();
                    }
                });

                // Combine all vertices by id, while choosing the shortest path with minimal distance
                JavaPairRDD<Integer, Vertex> reducer = mapper.reduceByKey(new Function2<Vertex, Vertex, Vertex>() {
                    @Override
                    public Vertex call(Vertex vertex1, Vertex vertex2) {
                        // Chose the original vertex with full list of all the neighbours
                        Set<Integer> neighbours = !vertex1.getNeighbours().isEmpty() ? vertex1.getNeighbours() : vertex2.getNeighbours();

                        // Chose the shortest path from the source to current vertex
                        List<Integer> path = vertex1.getDistance() < vertex2.getDistance() ? vertex1.getPath() : vertex2.getPath();

                        // Chose the minimum distance
                        int distance = vertex1.getDistance() < vertex2.getDistance() ? vertex1.getDistance() : vertex2.getDistance();

                        // Chose the darkest color
                        Color color = vertex1.getColor().ordinal() > vertex2.getColor().ordinal() ? vertex1.getColor() : vertex2.getColor();

                        // Emit the best possible solution found so far for a given vertex id
                        return new Vertex(vertex1.getId(), neighbours, path, distance, color);
                    }
                });

                Collection<Vertex> vertices = reducer.collectAsMap().values();
            //    stopwatch.stop();
                System.out.println("Elapsed Time = " + (System.currentTimeMillis() - startTime) + " seconds");
           //     logger.info("Elapsed time [" + index + "] ==> " + stopwatch);

                String content = NEW_LINE.join(vertices);
                // Save intermediate results into a text file for the next iteration if GRAY vertex is still present
                Files.write(Paths.get(problemFile + '_' + index), content.getBytes(), StandardOpenOption.CREATE);
                isGrayVertex = content.contains(Color.GRAY.name());
                System.out.println("is gray is " + isGrayVertex);
            }
       // }
        spark.stop();
    }
}

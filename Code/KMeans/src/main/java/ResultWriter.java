import com.google.common.base.Stopwatch;
import kmeans.Vars;
import kmeans.model.Vector;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Kasper on 24-09-2014.
 */
public class ResultWriter {

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");



    public static void PrintResult(Stopwatch sw, int nrVectors, int maxIterationCount, int nrClusters,int nrMappers, String implementation, String outputFolderName){
        long elapsedSeconds = sw.elapsed(TimeUnit.MILLISECONDS);

        try {
            ResultWriter.WriteResult(elapsedSeconds,TimeUnit.MILLISECONDS, nrVectors, maxIterationCount,nrClusters,nrMappers,implementation, outputFolderName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Elapsed milliseconds:");
        System.out.println(elapsedSeconds);
    }

    public static void WriteResult(long time, TimeUnit unit, int nrVectors, int nrIterations, int nrClusters, int nrMappers, String implementation, String outputFolder) throws IOException {
        String path = Vars.getPath();
        Path p = Paths.get(path);
        if (Files.notExists(p)){
            Files.createDirectory(p);
        }

        p = Paths.get(path,implementation);
        if (Files.notExists(p)){
            Files.createDirectory(p);
        }

        if(outputFolder != null && !outputFolder.isEmpty()){
            p = Paths.get(p.toString(),outputFolder);
            if (Files.notExists(p)){
                Files.createDirectory(p);
            }
        }

        String fileName = "V"+nrVectors+"M"+nrMappers+"C"+nrClusters+"I"+nrIterations+".td";
        path = Paths.get(p.toString(),fileName).toString();
        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(path,true)))) {
            writer.println("Timestamp: "+sdf.format(new Date()));
            writer.println("Implementation: " + implementation);
            writer.println("Vectors: " + nrVectors);
            writer.println("Mappers: " + nrMappers);
            writer.println("Clusters: " + nrClusters);
            writer.println("Iterations: " + nrIterations);
            writer.println("Time: " + time + " " + unit.name());
            writer.println();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void printVectors(List<Vector> vectors){
        StringBuilder sb = buildOutput(vectors);
        System.out.println(sb.toString());
    }

    public static void writeVectorsToDisk(List<Vector> vectors, String impl) throws IOException {
        String path = Vars.getPath();
        Path p = Paths.get(path);
        if (Files.notExists(p)){
            Files.createDirectory(p);
        }

        String filename = "staticoutput_"+impl+".td";
        path = Paths.get(path, filename).toString();
        StringBuilder sb = buildOutput(vectors);
        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(path,true)))) {
            writer.println(sb);
            writer.println();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static StringBuilder buildOutput(List<Vector> vectors){
        StringBuilder sb = new StringBuilder();
        for (Vector v : vectors){

            for (int i = 0; i < v.size(); i++){
                sb.append(v.itemAt(i)+",");

            }
            sb.deleteCharAt(sb.length()-1);
            sb.append('\n');
        }

        return sb;
    }
}

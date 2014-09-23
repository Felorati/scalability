import kmeans.Mapper;
import kmeans.Reducer;
import kmeans.clustering.Cluster;
import kmeans.clustering.Clustering;
import kmeans.clustering.ClusteringService;
import kmeans.datageneration.DataGenerator;
import kmeans.model.Vector;
import kmeans.parsing.DataParser;
import kmeans.partitioning.Partition;
import kmeans.partitioning.Partitioner;
import kmeans.partitioning.Partitioning;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Kasper on 22-09-2014.
 */
public class Main {

    public static void main(String[] args) {
        try {

            List<Vector> vectors = DataGenerator.generateData();
            RunClustering(vectors,5,10);

            /*
            ArrayList<Integer> a1 = new ArrayList<Integer>();
            a1.add(87);
            a1.add(67);
            a1.add(17);
            a1.add(35);
            a1.add(42);
            ArrayList<Integer> a2 = new ArrayList<Integer>();
            a2.add(15);
            a2.add(44 );
            a2.add(57);
            a2.add(23);
            a2.add(76);
            Vector v1 = new Vector(a1);
            Vector v2 = new Vector(a2);
            double cosineSimilarity = v1.vectorSimilarityWith(v2);
            double manDistance = v1.manhattanDistanceTo(v2);
            double euDistance = v1.euclideanDistanceTo(v2);
            double covariance = v1.covarianceWith(v2);
            double sdv = v1.standardDeviation();
            double pearsons = v1.pearsonCorrelationWith(v2);
            */


            /*
            Partitioning<Vector> partitioning = new Partitioner<Vector>().partition(vectors, 5);
            List<Vector> randomMeans = DataGenerator.generateRandomVectors(100,5,100);
            Clustering clustering = ClusteringService.ClusterKMeans(partitioning.getPartitions().get(0).getData(),randomMeans);
            for(Cluster c : clustering.getClusters()){
                Vector mean = c.calcMean();
                String s = "";
            }
            */
            String breakString = "";

        }
        catch (Exception ex) {
            System.out.println("Error:");
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }


    }

    public static void RunClustering(List<Vector> vectors, int nrClusters, int maxIterationCount) {

        Partitioning<Vector> partitioning = new Partitioner<Vector>().partition(vectors, nrClusters);
        List<Vector> randomMeans = DataGenerator.generateRandomVectors(100,nrClusters,100);
        Semaphore sem = new Semaphore(0);
        ReentrantLock lock = new ReentrantLock();
        Queue<Clustering> queue = new LinkedList<>();
        for(Partition<Vector> p : partitioning.getPartitions()){
            Thread t = new Thread(new Mapper(sem,lock,p.getData(),randomMeans,queue));
            t.start();
        }

        Thread reducerThread = new Thread(new Reducer(sem,lock,queue,nrClusters));
        reducerThread.start();

    }
}

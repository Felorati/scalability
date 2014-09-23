package kmeans;

import kmeans.clustering.Cluster;
import kmeans.clustering.Clustering;
import java.util.Queue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Kasper on 23-09-2014.
 */
public class Reducer implements Runnable{

    private Semaphore semaphore;
    private ReentrantLock lock;
    private Queue<Clustering> queue;
    private int nrClusters;

    public Reducer(Semaphore semaphore, ReentrantLock lock, Queue<Clustering> queue, int nrClusters) {
        this.semaphore = semaphore;
        this.lock = lock;
        this.queue = queue;
        this.nrClusters = nrClusters;
    }


    @Override
    public void run() {
        System.out.println("Reducer started");

        int recieved = 0;
        Clustering clustering = null;
        try {
            while (recieved != nrClusters) {
                Clustering c;
                semaphore.acquire(1);
                lock.lock();
                c = queue.poll();
                lock.unlock();
                recieved++;
                System.out.println("Reducer recieved nr: "+recieved);
                if (clustering == null){
                    clustering = c;
                }else{
                    for (int i = 0; i < clustering.getClusters().size(); i++) {
                        Cluster c1 = clustering.getClusters().get(0);
                        Cluster c2 = c.getClusters().get(0);
                        c1.mergeWith(c2);
                    }
                }

            }
        }catch (InterruptedException ex){

        }

        System.out.println("Reducer finished");
    }
}

/**
 * Created by Kasper on 12/23/2014.
 */
public class Philosopher implements Runnable {


    private Fork leftFork;
    private Fork rightFork;
    private String name;
    private Plate plate;

    public Philosopher(Fork leftFork, Fork rightFork, String name, Plate plate) {
        this.leftFork = leftFork;
        this.rightFork = rightFork;
        this.name = name;
        this.plate = plate;
    }

    private void eat() throws InterruptedException {
        //System.out.println(name.concat("  Eating"));
        plate.takeFood();
        Thread.sleep(100);
    }

    private void think() throws InterruptedException {
        Thread.sleep(100);
    }

    @Override
    public void run() {
        try {
            while (plate.hasFood()) {
                if (leftFork.pickUp(100)) {
                    if (rightFork.pickUp(100)) {
                        eat();
                        rightFork.putDown();
                    }
                    leftFork.putDown();
                }
                think();
            }
        } catch (InterruptedException ex) {

        }
    }
}

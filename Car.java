public class Car {
    private double maxSpeed;
    private int numberOfWheels;

    public Car(double m, int num) {
        maxSpeed = m;
        numberOfWheels = num;

    }

    public double getCharisma() {
        return maxSpeed * numberOfWheels;

    }

    public double getMaxSpeed() {
        return maxSpeed;

    }

    public double getDistance(double time) {
        return maxSpeed * time;
    }

}

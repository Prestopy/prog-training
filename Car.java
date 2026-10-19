public class Car {
    private int maxSpeed;
    private int numberOfWheels;

    public Car(int s, int w) {
        maxSpeed = s;
        numberOfWheels = w;
    }

    public int getCharisma() {
        return maxSpeed * numberOfWheels;
    }

    public int getMaxSpeed() {
        return maxSpeed;
    }

    public int getDistance(int t) {
        return maxSpeed * t;
    }    

}

public class JustinLai {
    public static void main(String[] args){
        Car RizzyCar = new Car(10, 4);
        Car BizzyCar = new Car(6, 7);
        Car JizzyCar = new Car(5, 10);

        System.out.println(RizzyCar.getCharisma()-BizzyCar.getCharisma()-JizzyCar.getCharisma());
        System.out.println(RizzyCar.getMaxSpeed()/JizzyCar.getMaxSpeed());
    }
}

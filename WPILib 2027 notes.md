```table-of-contents
```
# Terminology
- **Mechanism**: an independent, physical mechanism on a robot.
- **Command**: a style of programming that encapsulates robot code in discrete, reusable actions (commands).
	- **Coroutine**: a pause-able function.
- **Teleoperated (teleop)**
- **Autonomous (auto/auton)**
# Prerequisites: Java Inheritance
- Java is an object oriented programming language (OOP)
	- This means it uses classes and objects (instances)
- Inheritance is a core part of OOP
	- Inheritance establishes relationships between a parent and a child class or interface
	- This allows us to create complex structured code
## Classes
- Classes are blueprints
- Classes can `extend` others
	- This means that the **subclass** acquires the properties and methods of its parent class
	- For example:

```java
// Parent Class (Superclass)
class Animal {
    protected String name = "Generic Animal";
    
    public void sleep() {
	    System.out.println("I SLEEP!!!!!");
    }

    public void eat() {
        System.out.println("This animal is eating.");
    }
}

// Child Class (Subclass) extending the parent class
class Dog extends Animal {
    private String breed = "Labrador";

    @Override
    public void eat() {
        System.out.println("The dog crunches on kibble.");
    }

    public void bark() {
        System.out.println("Woof! Woof!");
    }
}
```

- In the above example, the subclass `Dog` extends the superclass `Animal`
	- Notice the `@Override`  annotation
	- This is not required, but promotes readability
	- This means that the method within the subclass **overwrites** an existing method already  in the `Animal` class
	- Furthermore, notice how `Dog` does not define a `sleep()` method. However, because it extends the `Animal` class, which *does* have that method, you can call `new Dog().sleep()`.
## Interfaces
- Interfaces are contracts that specify behaviors that a class must implement
	- Blueprint for classes
	- Blueprint for blueprints??!!

```java
// Define the interface contract
public interface Vehicle {
    void accelerate(int speed); // Abstract method (no body)
    void brake();
}

// Implement the interface contract in a concrete class
public class Bicycle implements Vehicle {
    private int currentSpeed = 0;

    @Override
    public void accelerate(int speed) {
        this.currentSpeed += speed;
        System.out.println("Bicycle speed increased to " + currentSpeed);
    }

    @Override
    public void brake() {
        this.currentSpeed = 0;
        System.out.println("Bicycle stopped.");
    }
}
```

- The `interface` `Vehicle` is a contract with two methods: `void accelerate(int speed)` and `void brake()`, specifying that any class that implements this interface **must** also implement these methods
	- Notice how the methods that are implemented also have the `@Override` annotation since they are specified in the contract
	- Methods that are not specified do not need this annotation
##  Aside: programming conventions
- Always name classes or interfaces with CamelCase
- Always name variables with snakeCase
- Whenever you override something, annotate it with `@Override`
- Always provide Javadocs for methods
	- See below:
```java
/**
 * Runs the intake motors at the given speed
 *
 * @param speed The speed to run the intake motors at as an {@link AngularVelocity}
 */
void go(AngularVelocity speed);

/**
 * Gets the angular velocity of the intake motors
 *
 * @return The angular velocity of the intake motors as an {@link AngularVelocity}
 */
AngularVelocity getVelocity();
```
# Basic code structure
- `Main`
- `Robot`
	- Manages core lifecycle loops
		- `robotInit()`
		- `robotPeriodic()`
		- `autonomousInit()`
		- `teleopInit()`
- `RobotContainer`
	- Initiates mechanisms
- `Superstructure` (optional)
	- Choreographs the dance between mechanisms
- Mechanisms
	- IO interface
	- Real
	- Sim (optional)
	- Constants
# `Main`, `Robot`, `RobotContainer`
- Core parts of robot code
- Everything else is called from here
	- `Main` calls `Robot`
	- `Robot` calls `Robot/Container` and other periodic functions
	- `RobotContainer` initializes the mechanisms
# `Superstructure`
- Orchestrates multiple mechanisms together
- Handles input from drivers and operators
```java
public class Superstructure {
    public static CANBus canDoAttitude = new CANBus("canDoAttitude");

    private static final Shooter shooter = Shooter.system();
    private static final Turret turret = Turret.system();
    public  static final Hood hood = Hood.system();
    private static final Feeder feeder = Feeder.system();
    private static final Spindexer spindexer = Spindexer.system();
    private static final Extender extender = Extender.system();
    private static final Intake intake = Intake.system();
    private static final Swerve swerve = Swerve.system();
    private static final Limelight limelight = Limelight.system();
    
    // ...
}
```
# Mechanisms
- Each individual mechanism has the following:
	- An **I/O** interface
	- A **Real/Sim** file
	- A **constants** file
## LOW-LEVEL Mechanism Code Structure
- There are usually two copies of a mechanism's core code:
	- **Real** and **Sim**
	- These are ran depending on if we are using the robot in real life or simulating it digitally
	- E.g., the **Real** class may run real motors when the `goToPos()` method is ran, but the **Sim** class may simulate a fake motor when the `goToPos()` method is ran.
- To standardize these copies, we use IO interfaces
- Each mechanism has an IO interface
	- Every class that implements it will have all the necessary methods that make that specific mechanism run
- The IO interfaces typically have:
	- An inputs class that contains the sensor readings that the mechanism may need
	- A `stop()` method to stop the mechanism
	- Any other **low-level behaviors** the mechanism may need
		- This means no "move to level 5" methods
		- There should only be "move to position x" methods
			- (As general as possible; the mechanism's main class should handle all high-level logic.)

```java
public interface TurretIO {
  class TurretIOInputs {
    public boolean motorConnected = false;
    public double positionRad = 0.0;
    public double velocityRadPerSec = 0.0;
    public double appliedVolts = 0.0;
    public double supplyCurrentAmps = 0.0;
    public double statorCurrentAmps = 0.0;
    public double temperatureCelsius = 0.0;
  }

  /** Refresh {@code inputs} from the hardware. Call exactly once per loop. */
  public void updateInputs(TurretIOInputs inputs) {}

  /** Closed-loop (profiled) move to a turret angle. Goal is clamped to the soft limits. */
  public void setPosition(double angleRad) {}

  /** Open-loop voltage. Use for characterization / manual jog. */
  public void setVoltage(double volts) {}

  /** Declare the turret to currently be at {@code angleRad} (homing / zeroing). */
  public void resetPosition(double angleRad) {}

  /** Stop the motor. */
  public void stop() {
    setVoltage(0.0);
  }
}
```

```java
public class TurretReal implements TurretIO {
	// ... instance variables for motors, sensors, etc.
	
	public TurretReal() {
		// ... code to initialize the motors
	}
	
	@Override
	public void updateInputs(TurretIOInputs inputs) {
		inputs.motorConnected = motor.isOK();
		// ... code to update the rest of the inputs
	}
	
	@Override
	public void setPosition(double angleRad) {
		// ... code to move motor to angleRad	
	}
	
	@Override
	public void setVoltage(double volts) {
		// ... code to move motor at volts
	}
	
	@Override
	public void resetPosition(double angleRad) {
		// ... code to zero encoder at current position
	}
}
```

```java
public class TurretSim implements TurretIO {
	// ... instance variables for fake motors, sensors, etc.
	
	public TurretReal() {
		// ... code to initialize the fake motors
	}
	
	@Override
	public void updateInputs(TurretIOInputs inputs) {
		inputs.motorConnected = true;
		// ... code to simulate the rest of the inputs
	}
	
	@Override
	public void setPosition(double angleRad) {
		// ... code to move simulated motor to angleRad	
	}
	
	@Override
	public void setVoltage(double volts) {
		// ... code to move simulated motor at volts
	}
	
	@Override
	public void resetPosition(double angleRad) {
		// ... code to zero simulated encoder at current position
	}
}
```

> [!NOTE]
> Raid Zero has wrapper classes!
> We already have wrapper classes that make it easy to write concise code for controlling motors, etc.

Finally, we have a constants file with all of our constants for a single mechanism:

```java
public final class TurretConstants {
    public static final class Yaw {
        public static final int MOTOR_ID = 14;
        public static final CANBus CAN_BUS = new CANBus("canDoAttitude");
        public static final MotorArrangementValue MOTOR_ARRANGEMENT = MotorArrangementValue.Minion_JST;

        public static final double SENSOR_TO_MECHANISM_RATIO = 280.0 / 10.0; // old ratio: 250.0 / 10.0;
        public static final double TURRET_RING_TEETH = 280.0;
        public static final double JARED_TEETH_1 = 29.0;
        public static final double JARED_TEETH_2 = 22.0;

        public static final int JMM_1_ID = 6; // 29 tooth
        public static final int JMM_2_ID = 5; // 22 tooth

        public static final double JMM_OFFSET_1 = -0.574463; // 29 tooth
        public static final double JMM_OFFSET_2 = -0.622559; // 22 tooth

        public static final Current STATOR_CURRENT_LIMIT = Amps.of(30);
        public static final Current SUPPLY_CURRENT_LIMIT = Amps.of(50);

        public static final double POSITION_P = 50.0;
        public static final double POSITION_I = 0.0;
        public static final double POSITION_D = 0.0;

        public static final double POSITION_S = 0.25;
        public static final double POSITION_G = 0.0; // * NOTE: this should be 0
        public static final double POSITION_V = 2.6;
        public static final double POSITION_A = 0.0;

        // ... more code
}
```

This allows us to change a value in one place and have it update everywhere else automatically without having to painstakingly update constant values everywhere it is used.

## HIGH-LEVEL Mechanism Code Structure
The main class for each mechanism contains high-level logic, unlike the low-level IO classes that act merely as adapters to connect to our hardware.

The high-level main class **contains** either the **Real** or **Sim** low-level IO classes, depending on if we run the code on a real robot or in a simulation.

For instance, whereas the IO class may have `void goToPos(Distance x)`, our main mechanism's class may have `Command moveToL1()` that calls the IO's `goToPos` method under the hood.
# Commands v3
See also: https://daprestopy.notion.site/Commands-v3-3f54815bfabf80be8933d620b67b5f0a

Command-based programming is a style of programming that encapsulates robot code in discrete, reusable actions (commands).

Commands are used in the **high-level, main mechanism classes**, **NOT in the low-level IO**!!

Commands themselves do not automatically run. They **must be given to a `Scheduler` which will run them**. The WPILib `Scheduler` decides which `Command`s to run next.

> [!WARNING]
> Commands not being put into the `Scheduler` is one of the main sources for bugs!! Thankfully this year WPILib automatically warns you if a `Command` is not passed to the `Scheduler`.

```java
/**
 * Moves the yaw at the given duty cycle percent
 *
 * @param percent the percent [0,1]
 * @return A {@link Command} to be scheduled and run
 */
public Command movePercent(double percent) {
	return run(coroutine -> io.runYawPercent(percent))
		.named("Turret duty cycle");
}
```

The code above runs a turret at a certain duty cycle. It **returns a `Command`**. It **does not run the `Command`**. Whatever calls this method is responsible for then passing this `Command` onto the `Scheduler`.

Here's a more complicated method that returns a `Command`:

```java
/**
 * Runs the spindexer at the anti-jam speed for a longer duration to attempt to
 * clear more severe jams
 *
 * @return A {@link Command} that runs the spindexer at the anti-jam speed for a
 *         longer duration
 */
public Command antiJamBig() {
	return run(coroutine -> {
		while (true) {
			coroutine.await(
				runRepeatedly(() -> io.go(IntakeConstants.Spindexer.ANTIJAM_SPEED))
					.named("Spindexer antijam")
					.withTimeout(Seconds.of(0.2))
			);
			coroutine.await(
				runRepeatedly(() -> io.go(IntakeConstants.Spindexer.INDEX_SPEED))
					.named("Spindexer intake")
					.withTimeout(Seconds.of(0.3))
			);
			coroutine.yield();
		}
	})
		.named("Spindexer antijam big");
}
```
## Creating Commands: Command Groups vs. Coroutines
There are two ways to create a `Command`, depending on the complexity necessary.
1. **Command Groups**: These are created using builder objects (fluent framework):
	1. `Command.sequence(...)` and `commandA.andThen(commandB)`
	2. `Command.parallel(...)` and `commandA.alongWith(commandB)`
	3. `Command.race(...)` and `commandA.raceWith(commandB)`

```java
public Command scoreSequence() {
	return drive.driveToScoringLocation()
		.andThen(elevator.moveToScoringHeight())
		.andThen(gripper.release())
		.named("Score Sequence")
}

public Command prepareToScore() {
	return turret.aimAtGoal()
		.alongWith(shooter.spinUp())
		.named("Prepare To Score");
}

public Command intakeUntilPieceOrTimeout() {
	return Command.race(
		intake.intake(),
		Command.waitFor(Seconds.of(2)).named("Intake Timeout")
	).named("Intake Until Piece Or Timeout");
}
```

3. **Coroutines**: These are created using a single `run(courotuine -> {})`

```java
void commandBody(Coroutine coroutine) {
	initialize();
	while (!isFinished()) {
	    execute();
	    coroutine.yield();
	}
	end();
}
```

> [!WARNING]
> Commands must be named!
> This year, all commands must be named to help with debugging. You can name a command by using the `.named()` method on a `Command`.

# Aside: Units
Yeah. WPILib provides `Units` that make it easy to convert between units. It essentially stores all values as generic measurements. For example:

- `Seconds.of(0.3)` returns a generic `Time` class that represents 0.3 seconds, or 300 ms, or any equivalent unit of measurement. The point is, they all mean the same thing.

This makes it impossible to mix your units up.

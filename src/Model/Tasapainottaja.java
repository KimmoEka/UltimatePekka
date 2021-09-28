import lejos.robotics.Gyroscope;
import lejos.robotics.RegulatedMotor;
import lejos.utility.Delay;

public class Tasapainottaja extends Thread {
	
	private RegulatedMotor left_motor;
	private RegulatedMotor right_motor;
	private Gyroscope gyro;
	private double ratioWheel;
	private long tCalcStart; // hetki jolla robotti aloittaa tasapainottelun
	private double tInterval; // yksittäisen loopin aika sekunteina
	// Gyro globals
	private double gOffset;
	private double gAngleGlobal = 0;
	private static final double EMAOFFSET = 0.0005;
	// These are the main four balance constants, only the gyro
	// constants are relative to the wheel size.  KPOS and KSPEED
	// are self-relative to the wheel size.
	private static final double KGYROANGLE = 7.5;
	private static final double KGYROSPEED = 1.15;
	private static final double KPOS = 0.07;
	private static final double KSPEED = 0.1;
	private double gyroSpeed, gyroAngle;
	private double motorSpeed;
	// Motor globals
	private double motorPos = 0;
	private long mrcSum = 0, mrcSumPrev;
	private long motorDiff;
	private long mrcDeltaP3 = 0;
	private long mrcDeltaP2 = 0;
	private long mrcDeltaP1 = 0;
	/**
	 * motorControlDrive is the target speed for the sum of the two motors
	 * in degrees per second.
	 */
	private double motorControlDrive = 0.0;
	
	/**
	 * motorControlSteer is the target change in difference for two motors
	 * in degrees per second.
	 */
	private double motorControlSteer = 0.0;
	/**
	 * This constant aids in drive control. When the robot starts moving because of user control,
	 * this constant helps get the robot leaning in the right direction.  Similarly, it helps 
	 * bring robot to a stop when stopping.
	 */
	private static final double KDRIVE = -0.02;
	
	/**
	 * This global contains the target motor differential, essentially, which 
	 * way the robot should be pointing.  This value is updated every time through 
	 * the balance loop based on motorControlSteer.
	 */
	private double motorDiffTarget = 0.0;
	
	/**
	 * Power differential used for steering based on difference of target steering and actual motor difference.
	 */
	private static final double KSTEER = 0.25;
	

	/** 
	 * Global variables used to control the amount of power to apply to each wheel.
	 * Updated by the steerControl() method.
	 */
	private int powerLeft, powerRight;
	
	/** 
	 * If robot power is saturated (over +/- 100) for over this time limit then 
	 * robot must have fallen.  In milliseconds.
	 */
	private static final double TIME_FALL_LIMIT = 500;
	
	/** 
	 * Loop wait time.  WAIT_TIME is the time in ms passed to the Wait command.
	 * NOTE: Balance control loop only takes 1.128 MS in leJOS NXJ. 
	 */
	private static final int WAIT_TIME = 7;
	
	private static final double CONTROL_SPEED  = 600.0;

	public Tasapainottaja(RegulatedMotor left, RegulatedMotor right, Gyroscope gyro, double wheelDiameter) {
		this.left_motor = left;
		this.right_motor = right;
		// Optional code to accept BasicMotor: this.right_motor = (NXTMotor)right;
		this.gyro = gyro;
		this.ratioWheel = wheelDiameter/5.6; // Original algorithm was tuned for 5.6 cm NXT 1.0 wheels.
		
		// Took out 50 ms delay here.
		
		// Get the initial gyro offset
		getGyroOffset();
		
		Delay.msDelay(1000);

		// Play warning beep sequence before balance starts
		
		
			
	}
	
private void getGyroOffset() {
		
		System.out.println("UlTIMATE PEKKA");
		System.out.println();
		System.out.println("Laita Pekka makuulleen");
		System.out.println("kalibrointia varten");
		
		//left_motor.flt(); // TODO: This didn't seem to make a bit of difference with GyroSensor calibration.
		//right_motor.flt();
		
		gyro.recalibrateOffset();
	}

private void laskeInterval(long cLoop) { // laskee jokaisen loopin keston
	if (cLoop == 0) {
		// First time through, set an initial tInterval time and
		// record start time
		tInterval = 0.0055;
		tCalcStart = System.currentTimeMillis();
	} else {
		// Take average of number of times through the loop and
		// use for interval time.
		tInterval = (System.currentTimeMillis() - tCalcStart)/(cLoop*1000.0);
	}
}

private void updateGyroData() {
	// TODO: The GyroSensor class actually rebaselines for drift ever 5 seconds. This not needed? Or is this method better?
	// Some of this fine tuning may actually interfere with fine-tuning happening in the hardcoded dIMU and GyroScope code.
	float gyroRaw;

	gyroRaw = gyro.getAngularVelocity();
	//gOffset = EMAOFFSET * gyroRaw + (1-EMAOFFSET) * gOffset;
	gyroSpeed = gyroRaw;// - gOffset; // Angular velocity (degrees/sec)
	//gyroSpeed = gyroRaw - gOffset; // Angular velocity (degrees/sec)
	gAngleGlobal += gyroSpeed*tInterval;
	gyroAngle = gAngleGlobal; // Absolute angle (degrees)
	//System.out.println("speed" + gyroSpeed);
	//System.out.println("angle" + gyroAngle);
}

private void updateMotorData() {
	long mrcLeft, mrcRight, mrcDelta; // moottoreiden paikat

	// Keep track of motor position and speed
	mrcLeft = left_motor.getTachoCount();
	mrcRight = right_motor.getTachoCount();

	// Maintain previous mrcSum so that delta can be calculated and get
	// new mrcSum and Diff values
	mrcSumPrev = mrcSum;
	mrcSum = mrcLeft + mrcRight;
	motorDiff = mrcLeft - mrcRight;

	// mrcDetla is the change int sum of the motor encoders, update
	// motorPos based on this detla
	mrcDelta = mrcSum - mrcSumPrev;
	motorPos += mrcDelta;

	// motorSpeed is based on the average of the last four delta's.
	motorSpeed = (mrcDelta+mrcDeltaP1+mrcDeltaP2+mrcDeltaP3)/(4*tInterval);

	// Shift the latest mrcDelta into the previous three saved delta values
	mrcDeltaP3 = mrcDeltaP2;
	mrcDeltaP2 = mrcDeltaP1;
	mrcDeltaP1 = mrcDelta;
}

private void steerControl(int power) {
	int powerSteer;

	// Update the target motor difference based on the user steering
	// control value.
	motorDiffTarget += motorControlSteer * tInterval;

	// Determine the proportionate power differential to be used based
	// on the difference between the target motor difference and the
	// actual motor difference.
	powerSteer = (int)(KSTEER * (motorDiffTarget - motorDiff));

	// Apply the power steering value with the main power value to
	// get the left and right power values.
	powerLeft = power + powerSteer;
	powerRight = power - powerSteer;

	// Limit the power to motor power range -100 to 100
	if (powerLeft > 100)   powerLeft = 100;
	if (powerLeft < -100)  powerLeft = -100;

	// Limit the power to motor power range -100 to 100
	if (powerRight > 100)  powerRight = 100;
	if (powerRight < -100) powerRight = -100;
}

public void run() {

	int power;
	long tMotorPosOK;
	long cLoop = 0;
			
	System.out.println("Balancing");
	System.out.println();
	tMotorPosOK = System.currentTimeMillis();

	// Reset the motors to make sure we start at a zero position
	left_motor.resetTachoCount();
	right_motor.resetTachoCount();

	// NOTE: This balance control loop only takes 1.128 MS to execute each loop in leJOS NXJ.
	while(true) {
		laskeInterval(cLoop++);

		updateGyroData();

		updateMotorData();

		// Apply the drive control value to the motor position to get robot to move.
		motorPos -= motorControlDrive * tInterval;

		// This is the main balancing equation
		power = (int)((KGYROSPEED * gyroSpeed +               // Deg/Sec from Gyro sensor
				KGYROANGLE * gyroAngle) / ratioWheel + // Deg from integral of gyro
				KPOS       * motorPos +                 // From MotorRotaionCount of both motors
				KDRIVE     * motorControlDrive +        // To improve start/stop performance
				KSPEED     * motorSpeed);                // Motor speed in Deg/Sec
		
		System.out.println("c" + motorControlDrive);

		if (Math.abs(power) < 100)
			tMotorPosOK = System.currentTimeMillis();

		steerControl(power); // Movement control. Not used for balancing.

		// Apply the power values to the motors
		// NOTE: It would be easier/faster to use MotorPort.controlMotorById(), but it needs to be public.
		left_motor.setSpeed((int)((Math.abs(powerLeft)/100)*left_motor.getMaxSpeed()));
		right_motor.setSpeed((int)((Math.abs(powerRight)/100)*right_motor.getMaxSpeed()));

		if(powerLeft > 0) left_motor.forward(); 
		else left_motor.backward();

		if(powerRight > 0) right_motor.forward(); 
		else right_motor.backward();

		// Check if robot has fallen by detecting that motorPos is being limited
		// for an extended amount of time.
		//if ((System.currentTimeMillis() - tMotorPosOK) > TIME_FALL_LIMIT) break;
		
		//try {Thread.sleep(WAIT_TIME);} catch (InterruptedException e) {}
	} // end of while() loop
	
	//left_motor.flt();
	//right_motor.flt();

	
} // END OF BALANCING THREAD CODE

/**
 * This method allows the robot to move forward/backward and make in-spot rotations as
 * well as arcs by varying the power to each wheel. This method does not actually 
 * apply direct power to the wheels. Control is filtered through to each wheel, allowing the robot to 
 * drive forward/backward and make turns. Higher values are faster. Negative values cause the wheel
 * to rotate backwards. Values between -200 and 200 are good. If values are too high it can make the
 * robot balance unstable.
 * 
 * @param left_wheel The relative control power to the left wheel. -200 to 200 are good numbers.
 * @param right_wheel The relative control power to the right wheel. -200 to 200 are good numbers.
 */

public void wheelDriver(int left_wheel, int right_wheel) {
	// Set control Drive and Steer.  Both these values are in motor degree/second
	motorControlDrive = (left_wheel + right_wheel) * CONTROL_SPEED / 200.0;
	motorControlSteer = (left_wheel - right_wheel) * CONTROL_SPEED / 200.0;
}		
}


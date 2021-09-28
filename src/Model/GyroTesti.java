import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.UnregulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.Port;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.DexterIMUSensor;
import lejos.hardware.sensor.EV3IRSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.hardware.sensor.AnalogSensor;
import lejos.hardware.sensor.HiTechnicGyro;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.SampleProvider;
import lejos.utility.Delay;
import lejos.robotics.navigation.*;
import lejos.robotics.Gyroscope;
import lejos.robotics.GyroscopeAdapter;
import lejos.robotics.EncoderMotor;
import lejos.robotics.subsumption.*;
public class GyroTesti {

	
	private static Port port;
	private static SensorModes sensor;
	private static SampleProvider sample;
	private static Gyroscope gyro;
	private static HiTechnicGyro hi;
	final static double halkaisija = 5.5 ; //6.8
//	private static RegulatedMotor M1 = new EV3LargeRegulatedMotor(MotorPort.A);
	//private static RegulatedMotor M2 = new EV3LargeRegulatedMotor(MotorPort.D);
	private static UnregulatedMotor M1 = new UnregulatedMotor(MotorPort.D);
	private static UnregulatedMotor M2 = new UnregulatedMotor(MotorPort.A);
	private static Tasapainottaja tasap;
	private static Segoway seg;
	private static DataOutputStream out;
	private static DataInputStream in;


	public static void main(String[] args) {
		int suunta = 1;
		float freq = 300;
		port = LocalEV3.get().getPort("S4");
		sensor = new HiTechnicGyro(port);
		sample = ((HiTechnicGyro) sensor).getRateMode();
		gyro = new GyroscopeAdapter(sample, freq);
		//tasap = new Tasapainottaja(M1,M2,gyro,halkaisija);
		//tasap.start();
		seg = new Segoway(M1,M2,gyro,halkaisija);
		Behavior b1 = 	new Aja(suunta);
		//Behavior b2 = new VaroTeippi();
		Behavior [] bArray = {b1};
		Arbitrator arby = new Arbitrator(bArray);
		arby.go();
		
		
	}
	public void setSuunta (int suunta) {
		try {
			suunta = in.readInt();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

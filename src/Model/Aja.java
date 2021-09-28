import lejos.robotics.subsumption.Behavior;

public class Aja implements Behavior {

	private volatile boolean suppressed = false;
	private int suunta;
	private Tasapainottaja robotti;
	public Aja(int suunta) {
		this.suunta = suunta;
	}
	@Override
	public boolean takeControl() {
		if (suunta > -1 || suunta<4) {
			return true;
		}
		return false;
	}

	@Override
	public void action() {
		suppressed = false;
		if(suunta == 1) {
		robotti.wheelDriver(100,100);
		}
		else if(suunta == 0) {
			robotti.wheelDriver(-100, -100);
		}
		else if (suunta == 2) {
			robotti.wheelDriver(0, 100);
		}
		else if (suunta == 3) {
			robotti.wheelDriver(100, 0);
		}else {
			robotti.wheelDriver(0, 0);
		}
	}

	@Override
	public void suppress() {
		suppressed  = true;
	}

}

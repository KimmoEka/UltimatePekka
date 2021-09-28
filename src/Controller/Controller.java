package Controller;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import View.View_Interface;

public class Controller implements Controller_Interface {
	View_Interface view;
	Socket s;

	public Controller(View_Interface view) {
		
		this.view = view;
	}

	@Override
	public void Sammu() {
		
		
		view.setSammuViesti("Pekka sammutettiin");
		
		CloseCon();
		
		// TODO Auto-generated method stub
		
	}

	@Override
	public void Eteen() {
		
		view.setSammuViesti("Pekka menee eteenpäin");
		
		// TODO Auto-generated method stub
		
	}

	@Override
	public void Oikealle() {
		
		view.setSammuViesti("Pekka menee oikealle");
		
		// TODO Auto-generated method stub
		
	}

	@Override
	public void Vasemalle() {
		
		view.setSammuViesti("Pekka menee vasemalle");
		
		// TODO Auto-generated method stub
		
	}

	@Override
	public void Taakse() {
		
		view.setSammuViesti("Pekka menee taaksepäin");
		
		// TODO Auto-generated method stub
		
	}

	@Override
	public void Yhdistys() throws UnknownHostException, IOException {
		view.setSammuViesti("Pekkaan yhdistetään");
		//s = new Socket("10.0.1.1", 1111);
		// TODO Auto-generated method stub
		
	}

	@Override
	public void CloseCon() {
		view.setSammuViesti("Pekkasta poistetaan yhteys");
		try {
			s.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			
		}
		// TODO Auto-generated method stub
		
	}
}

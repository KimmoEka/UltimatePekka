package View;
	
import java.io.IOException;
import java.net.UnknownHostException;

import Controller.Controller;
import Controller.Controller_Interface;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;


public class Main extends Application implements View_Interface {
	Controller_Interface controller;
	Text sammuViesti;
	int kierros=1;
	
	public void setSammuViesti(String sammuViesti) {
		this.sammuViesti.setText(sammuViesti);
	}

	@Override
	public void init() {
		controller = new Controller(this);
	}
	
	@Override
	public void start(Stage primaryStage) {
		try {
			BorderPane root = new BorderPane();
			primaryStage.setTitle("Ultimate Pekka");
			
			sammuViesti = new Text("");
			
			
			
			
			
			Scene scene = new Scene(createGrid(),400,400);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private GridPane createGrid() {
		GridPane grid = new GridPane();
		
		Button sammu = new Button("Sammuta Pekka.");
		
		sammu.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent event) {
				
					controller.Sammu();
					
			}
		});
			
		Button eteen = new Button("Eteenpäin");
		
		eteen.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent event) {
				
					controller.Eteen();
				
				
				
			}
		});
		
		Button taakse = new Button("Taaksepäin");
		
		taakse.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent event) {
				
					controller.Taakse();
				
			}
		});
		
		Button vasemalle = new Button("Vasemalle");
		
		vasemalle.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent event) {
				
					controller.Vasemalle();
				}
		});
		
		Button Oikealle = new Button("Oikealle");
		
		Oikealle.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent event) {
				
				controller.Oikealle();
				
			}
		});
		
		Button onoff = new Button("Autonominen ohjaus");
		
		onoff.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent event) {
				
				
					
					if (kierros == 1) {
						try {
							controller.Yhdistys();
						} catch (UnknownHostException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						onoff.setText("Manuaalinen ohjaus");
						kierros = 0;
					}else {
						controller.Sammu();
						onoff.setText("Autonominen ohjaus");
						kierros = 1;
					}
				
				
				
			}
		});
		grid.add(sammu, 3, 3);
		grid.add(eteen, 1, 0);
		grid.add(taakse, 1, 2);
		grid.add(Oikealle, 2, 1);
		grid.add(vasemalle, 0, 1);
		grid.add(onoff, 3, 0);
		grid.add(sammuViesti, 3, 4);
		return grid;
		
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}

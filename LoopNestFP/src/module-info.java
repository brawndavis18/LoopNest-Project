module LoopNestFP {
	requires javafx.controls;
	requires java.sql;
	requires java.desktop;
	requires javafx.graphics;
	requires javafx.base;
	
	opens LoopNestFX to javafx.graphics, javafx.fxml, javafx.base;
}

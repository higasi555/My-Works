package application;

import javafx.scene.Camera;
import javafx.scene.Group;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
 
public abstract class GameViewFX extends Group{
	public void mouseClicked(MouseEvent e) {}
	public void mouseMoved(MouseEvent e) {}
	public void mouseDragged(MouseEvent e) {}
	
	public Camera initializeCamera() {
        return null;
    }
}
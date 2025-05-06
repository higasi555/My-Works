package application;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;



public class Appmanager {
	//オープニングBGM開始
	public static void startBGM_opening() {
		try {
			Sound.playSE("SE/opening_16bit.wav");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public static Sound sound;
	//ゲームBGM開始
	public static void startBGM_game1() {
	    try {
	        sound = new Sound("SE/SE/6035134_MotionElements_positive-acoustic-country_16bit.wav");
	        Thread seThread = new Thread(new Runnable() {
	            public void run() {
	                sound.play();
	            }
	        });
	        seThread.start(); // BGM再生スレッドの実行
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	public static void stopBGM_game1() {
		
	}
	
	public static void startBGM_over() {
	    try {
	        Sound sound = new Sound("SE/mixkit-orchestra-game-over-1950_16bit.wav");
	        Thread seThread = new Thread(new Runnable() {
	            public void run() {
	                sound.play();
	            }
	        });
	        seThread.start(); // BGM再生スレッドの実行
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	
	public static void startSE_hit() {
	    try {
	        // 音声ファイルをロード
	        Sound sound = new Sound("SE/21336287_MotionElements_sfx-3585-film-game-foley_16bit.wav");
	        
	        // Clipが終了した時のリスナーを追加
	        sound.getClip().addLineListener(event -> {
	            if (event.getType() == LineEvent.Type.STOP) {
	                sound.close();
	            }
	        });

	        // 音を再生
	        sound.play();

	    } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
	        e.printStackTrace();
	    }
	}
	
	
	
}


class ImageManager_Stage1 {
    // 画像変数の定義
	public static Image img_BG;
    public static Image img_tile;
    public static Image img_light;
    public static Image img_w;
    public static Image img_bd1;
    public static Image img_bd2;
    public static Image img_bd3;
    public static Image img_bd4;
    public static Image img_bd5;
    public static Image img_bd6;
    
    public static Image img_Castle;

    static {
        // 画像のインスタンス化
        img_BG = new Image(new File("img/BG_v2.png").toURI().toString(), 7680, 4320, false, true);
        img_tile = new Image(new File("img_game/tile_v3.png").toURI().toString(), 1900, 565, true, true);
        img_light = new Image(new File("img_game/light_v1.png").toURI().toString(), 1200, 1200, true, true);
        img_w = new Image(new File("img_game/w_v1.png").toURI().toString(), 500, 500, true, true);
        img_bd1 = new Image(new File("img_game/building/bd1.png").toURI().toString(), 2500, 2500, true, true);
        img_bd2 = new Image(new File("img_game/building/bd2.png").toURI().toString(), 1500, 1500, true, true);
        img_bd3 = new Image(new File("img_game/building/bd3.png").toURI().toString(), 1500, 1500, true, true);
        img_bd4 = new Image(new File("img_game/building/bd4.png").toURI().toString(), 1500, 1500, true, true);
        img_bd5 = new Image(new File("img_game/building/bd5.png").toURI().toString(), 1500, 1500, true, true);
        img_bd6 = new Image(new File("img_game/building/bd6.png").toURI().toString(), 1500, 1500, true, true);
        
        img_Castle = new Image(new File("enemy/castle_v1.png").toURI().toString(), 2000, 2000, true, true);
    }

    // 背景画像をセットするメソッド
    public static void setImage(Game game) {
        //奥から順に設置
    	ImageView imgv_BG = new ImageView(img_BG);
    	ImageView imgv_BG2 = new ImageView(img_BG);
        
        //遠いものから順に配置
        game.addBG(img_BG, imgv_BG, 0, 0, 1000, 0);
        game.addBG(img_BG, imgv_BG2, 7680, 0, 1000, 0);
        
        
        for (int i = -4; i < 10; i++) {
        	ImageView imgv_tile = new ImageView(img_tile);
        	game.addBG(img_tile, imgv_tile, i*1900, 1080, 100, 90);
        }
               
        
        for (int i = 0; i < 3; i++) {
        	double r = Math.random();
        	if(r>0.8) {
        		game.addBG_new(img_bd2, i*3000-1500, 780, 440);
        	}else if(r>0.6) {
        		game.addBG_new(img_bd3, i*3000-1500, 750, 440);
        	}else if(r>0.4) {
        		game.addBG_new(img_bd4, i*3000-1500, 780, 440);
        	}else if(r>0.2) {
        		game.addBG_new(img_bd5, i*3000-1500, 820, 440);
        	}else if(r>=0) {
        		game.addBG_new(img_bd6, i*3000-1500, 770, 440);
        	}      	
            
            
        }
        for (int i = 0; i < 3; i++) {
            game.addBG_new(img_bd1, i*3000, 690, 400);
        }
        
        
        for (int i = -50; i < 300; i++) {
        	game.addBG_new(img_w, i*110, 1030, 350);
        }
        
        for (int i = -4; i < 30; i++) {
        	game.addBG_new(img_light, i*1200, 880, 300);
        }
    }
}


class ImageManager_Stage2 {
    // 画像変数の定義
	public static Image img_BG;
    public static Image img_tile;
    public static Image img_light;
    public static Image img_w;
    public static Image img_bd1;
    public static Image img_bd2;
    public static Image img_bd3;
    public static Image img_bd4;
    public static Image img_bd5;
    public static Image img_bd6;
    public static Image img_Castle;

    static {
        // 画像のインスタンス化
        img_BG = new Image(new File("img/BG_v2.png").toURI().toString(), 7680, 4320, false, true);
        img_tile = new Image(new File("img_GameStage2/sand_v2.png").toURI().toString(), 3840, 1157, true, true);
        //img_light = new Image(new File("img_game/light_v1.png").toURI().toString(), 1200, 1200, true, true);
        img_w = new Image(new File("img_GameStage2/cactus.png").toURI().toString(), 500, 500, true, true);
        img_bd1 = new Image(new File("img_GameStage2/dunes.png").toURI().toString(), 2500, 2500, true, true);
        img_bd2 = new Image(new File("img_game/building/bd2.png").toURI().toString(), 1500, 1500, true, true);
        img_bd3 = new Image(new File("img_game/building/bd3.png").toURI().toString(), 1500, 1500, true, true);
        img_bd4 = new Image(new File("img_game/building/bd4.png").toURI().toString(), 1500, 1500, true, true);
        img_bd5 = new Image(new File("img_game/building/bd5.png").toURI().toString(), 1500, 1500, true, true);
        img_bd6 = new Image(new File("img_game/building/bd6.png").toURI().toString(), 1500, 1500, true, true);
        
        img_Castle = new Image(new File("enemy/castle_v1.png").toURI().toString(), 2000, 2000, true, true);
        
    }

    // 背景画像をセットするメソッド
    public static void setImage(Game game) {
        //奥から順に設置
    	ImageView imgv_BG = new ImageView(img_BG);
    	ImageView imgv_BG2 = new ImageView(img_BG);
        
        //遠いものから順に配置
        game.addBG(img_BG, imgv_BG, 0, 0, 1000, 0);
        game.addBG(img_BG, imgv_BG2, 7680, 0, 1000, 0);
        
        //背景建物
        for (int i = 0; i < 10; i++) {
        	double r = Math.random();
        	if(r>0.8) {
        		game.addBG_new(img_bd1, i*1400+700, 690, 500);
        	}else if(r>0.6) {
        		game.addBG_new(img_bd1, i*1400+400, 690, 500);
        	}else if(r>0.4) {
        		game.addBG_new(img_bd1, i*1400+200, 690, 500);
        	}else if(r>0.2) {
        		game.addBG_new(img_bd1, i*1400-100, 690, 500);
        	}else if(r>=0) {
        		game.addBG_new(img_bd1, i*1400, 690, 500);
        	}    
            
        }
        
        //地面
        for (int i = -2; i < 5; i++) {
        	ImageView imgv_tile = new ImageView(img_tile);
        	game.addBG(img_tile, imgv_tile, i*3840, 1080, 100, 90);
        }
                        
        
        //サボテン
        for (int i = -50; i < 300; i++) {
        	double r = Math.random();
        	if(r>0.8) {
        		game.addBG_new(img_w, i*200, 1000, 350);
        	}else if(r>0.6) {
        		game.addBG_new(img_w, i*200, 1000, 350+20);
        	}else if(r>0.4) {
        		game.addBG_new(img_w, i*200, 1000, 350+50);
        	}else if(r>0.2) {
        		game.addBG_new(img_w, i*200, 1000, 350+80);
        	}else if(r>=0) {
        		game.addBG_new(img_w, i*200, 1000, 350+100);
        	}
        }

    }
}


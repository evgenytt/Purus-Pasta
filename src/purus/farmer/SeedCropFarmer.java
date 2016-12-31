package purus.farmer;

import static haven.OCache.posres;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;

import haven.Button;
import haven.Coord;
import haven.FlowerMenu;
import haven.GItem;
import haven.GameUI;
import haven.Gob;
import haven.HavenPanel;
import haven.IMeter;
import haven.Inventory;
import haven.Label;
import haven.Widget;
import haven.Window;
import purus.BotUtils;

public class SeedCropFarmer extends Window implements Runnable {
	
	private Coord rc1;
	private Coord rc2;
	
	private BotUtils BotUtils;
	
	private ArrayList<Gob> crops = new ArrayList<Gob>();
	
	private boolean stopThread = false;
	
	private Label lblProg;
	
	private int stage;
	private String cropName;
	private String seedName;
	
	private long startTime;
	
	public SeedCropFarmer(Coord rc1, Coord rc2, BotUtils BotUtils, String cropName, String seedName, int stage) {
        super(new Coord(120, 65), cropName.substring(cropName.lastIndexOf("/")+1).substring(0, 1).toUpperCase()+
        		cropName.substring(cropName.lastIndexOf("/")+1).substring(1)+" Farmer");
		this.BotUtils = BotUtils;
		this.rc1 = rc1;
		this.rc2 = rc2;
		this.cropName = cropName;
		this.stage = stage;
		this.seedName = seedName;
		
        Label lblstxt = new Label("Progress:");
        add(lblstxt, new Coord(15, 35));
        lblProg = new Label("Initializing...");
        add(lblProg, new Coord(65, 35));
		
        Button stopBtn = new Button(120, "Stop") {
            @Override
            public void click() {
                stop();
            }
        };
        add(stopBtn, new Coord(0, 0));
		
	}
	
	public void run() {
		// Initialize crop list
		crops = Crops();
		int totalCrops = crops.size();
		int cropsHarvested = 0;
		lblProg.settext(cropsHarvested+"/"+totalCrops);
		startTime = System.currentTimeMillis();
		for(Gob g : crops) {
			if(stopThread)
				return;
			// Check if stamina is under 30%, drink if so
			GameUI gui = HavenPanel.lui.root.findchild(GameUI.class);
			 IMeter.Meter stam = gui.getmeter("stam", 0);
			 if (stam.a <= 30) {
				 BotUtils.drink();
			 }
			 
				if(stopThread)
					return;
				
			 // Right click the crop
				BotUtils.doClick(g, 1, 0);
				BotUtils.gui().map.wdgmsg("click", Coord.z, g.rc.floor(posres), 1, 0);
				while(BotUtils.player().rc.x!=g.rc.x||BotUtils.player().rc.y!=g.rc.y) {
					BotUtils.sleep(10);
					System.out.println(g.rc.y + ", " + BotUtils.player().rc.y);
				}
				System.out.println("didtheclick");
				BotUtils.pfRightClick(g, 0);
			
			 // Wait for harvest menu to appear and harvest the crop
			
			while(ui.root.findchild(FlowerMenu.class)==null) {
				BotUtils.sleep(10);
			}
			
			if(stopThread)
				return;
			
			@SuppressWarnings("deprecation")
			FlowerMenu menu = ui.root.findchild(FlowerMenu.class);
	            if (menu != null) {
	                for (FlowerMenu.Petal opt : menu.opts) {
	                    if (opt.name.equals("Harvest")) {
	                        menu.choose(opt);
	                        menu.destroy();
	                    }
	                }
	            }
	            while(BotUtils.findObjectById(g.id)!=null) {
	            	BotUtils.sleep(10);
	            }
			
			if(stopThread)
				return;
			
            GItem item = null;
            while (BotUtils.getItemAtHand()==null) {
            	 Inventory inv = BotUtils.playerInventory();
                 for (Widget w = inv.child; w != null; w = w.next) {
                     if (w instanceof GItem && ((GItem) w).resname().equals(seedName)) {
                         item = (GItem)w;
                         break;
                     	}
                 }
                 if(item!=null)
	                BotUtils.takeItem(item);
            }
            
            while(BotUtils.getItemAtHand()==null)
            	BotUtils.sleep(10);
			
			// Plant the seed from hand
            BotUtils.mapInteractClick(0);
			while(BotUtils.findNearestStageCrop(5, 0, cropName)==null) {
				BotUtils.sleep(10);
			}
			BotUtils.drop_item(0);
            for (Widget w = BotUtils.playerInventory().child; w != null; w = w.next) {
                if (w instanceof GItem && ((GItem) w).resname().equals(seedName)) {
                    item = (GItem)w;
	                    try {
	                    	item.wdgmsg("drop", Coord.z);
	                    } catch(Exception e) {
	                    	//Shouldnt matter
	                    }
                	}
            }
			
			cropsHarvested ++;
			lblProg.settext(cropsHarvested+"/"+totalCrops);
		}
		BotUtils.sysMsg(cropName.substring(cropName.lastIndexOf("/")+1).substring(0, 1).toUpperCase()+
        		cropName.substring(cropName.lastIndexOf("/")+1).substring(1)+" Farmer finished!", Color.white);
		this.destroy();
	}
	
	public ArrayList<Gob> Crops() {
		// Initializes list of crops to harvest between selected coords
		ArrayList<Gob> gobs = new ArrayList<Gob>();
		double bigX = rc1.x>rc2.x?rc1.x:rc2.x;
		double smallX = rc1.x<rc2.x?rc1.x:rc2.x;
		double bigY = rc1.y>rc2.y?rc1.y:rc2.y;
		double smallY = rc1.y<rc2.y?rc1.y:rc2.y;
        synchronized (ui.sess.glob.oc) {
            for (Gob gob : ui.sess.glob.oc) {
            	if(gob.rc.x<=bigX && gob.rc.x>=smallX &&
            			gob.getres()!=null &&
            			gob.rc.y<=bigY && gob.rc.y>=smallY && 
            			gob.getres().name.contains(cropName) && 
            			gob.getStage() == stage) {
            		gobs.add(gob);
            	}
            }
        }
		gobs.sort(new CoordSort());
        return gobs;
	}
	
    @Override
    public void wdgmsg(Widget sender, String msg, Object... args) {
        if (sender == cbtn) {
            stop();
            reqdestroy();
        }
        else
            super.wdgmsg(sender, msg, args);
    }
    
    // Sorts coordinate array to efficient sequence
    class CoordSort implements Comparator<Gob> {
        public int compare(Gob a, Gob b) {
        	
        	if(a.rc.x==b.rc.x) {
        		if(a.rc.x%2==0)
        			return (a.rc.y < b.rc.y) ? 1 : (a.rc.y > b.rc.y) ? -1 : 0;
        		else
                    return (a.rc.y < b.rc.y) ? -1 : (a.rc.y > b.rc.y) ? 1 : 0;
        	} else
        		return (a.rc.x < b.rc.x) ? -1 : (a.rc.x > b.rc.x) ? 1 : 0;
        }
    }
	
	public void stop() {
		// Stops thread
		BotUtils.sysMsg(cropName.substring(cropName.lastIndexOf("/")+1).substring(0, 1).toUpperCase()+
        		cropName.substring(cropName.lastIndexOf("/")+1).substring(1)+" Farmer stopped!", Color.white);
                gameui().map.wdgmsg("click", Coord.z, gameui().map.player().rc.floor(posres), 1, 0);
        if (gameui().map.pfthread != null) {
            gameui().map.pfthread.interrupt();
        }
		stopThread = true;
		this.destroy();
	}
}
package purus.farmer;

import java.awt.Color;
import java.util.ArrayList;

import haven.Button;
import haven.Coord;
import haven.FlowerMenu;
import haven.GItem;
import haven.GameUI;
import haven.Gob;
import haven.HavenPanel;
import haven.IMeter;
import haven.Inventory;
import haven.UI;
import haven.Widget;
import haven.Window;
import purus.BotUtils;

public class CarrotFarmer {
	/* This script harvests and replants carrots.
	 *  Doesn't pathfind around objects.
	 *  Does this for all stage 4 carrots in sight.
	 *  Designed for round fields, lift beehives at middle temporarily off before harvest.
	 */
	public static boolean CarrotsNearby;

	private final UI ui;
    private haven.Widget w;
    private Inventory i;
    private Widget window;
    private boolean running = true;
    
    private ArrayList<Gob> gobs;
    private String Seed = "gfx/invobjs/carrot";
    private String Plant = "gfx/terobjs/plants/carrot";
    private int Stage = 4;
    
	BotUtils BotUtils;

	public CarrotFarmer (UI ui, Widget w, Inventory i, ArrayList<Gob> gobs) {
		this.ui = ui;
		this.w = w;
		this.i = i;
		this.gobs = gobs;
		BotUtils = new BotUtils(ui, w, i);
	}
	
	public void Run () {
		t.start();
		}
		Thread t = new Thread(new Runnable() {
		public void run()  {
			stop:
			while(running) {
			BotUtils.sysMsg("Carrot Farmer Started", Color.WHITE);
			window = BotUtils.gui().add(new StatusWindow(), 300, 200);
			for (Gob gob : gobs) {
				GameUI gui = HavenPanel.lui.root.findchild(GameUI.class);
				 IMeter.Meter stam = gui.getmeter("stam", 0);
				 if (stam.a <= 30) {
					 BotUtils.drink();
				 }
			BotUtils.pfRightClick(gob, 0);
			while(ui.root.findchild(FlowerMenu.class)==null) {
				sleep(100);
			}
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
	            while(BotUtils.findObjectById(gob.id)!=null) {
	            	sleep(100);
	            }
	            // Some better method should be implemented, but now it just waits a bit for items to appear on inventory and stuff
	            sleep(100);
	            GItem item = BotUtils.getItemAtHand();
	            if (item == null) {
	            	 Inventory inv = BotUtils.playerInventory();
	                 for (Widget w = inv.child; w != null; w = w.next) {
	                     if (w instanceof GItem && isCarrot((GItem) w)) {
	                         item = (GItem)w;
	                         break;
	                     	}
	                 }
		                BotUtils.takeItem(item);
}
	            // Planttaa, siemen käteen tähän vaiheeseen mennessä
			BotUtils.mapInteractClick(1);
			while(BotUtils.findNearestStageCrop(5, 0, "gfx/terobjs/plants/carrot")==null) {
				sleep(100);
			}
			//  TODO Droppaa kaikki siemenet tms. invistä + kädestä = saa toimimaan kaikkiin siemeniin joku hieno
			//	juttu jolla saa valittua mitä kasvia farmaa jne. 
		}
            running = false;
			}
            window.destroy();
		}
		
    	final String[] carrot = {Seed};
        protected boolean isCarrot(final GItem item) {
	        String resName = item.resname();
	        if (resName != null && !resName.isEmpty()) {
	            for (String food : carrot)
	                if (resName.contains(food))
	                    return true;
	       }
	        return false;
	    }
		});
		
		private void sleep(int t){
			try {
				Thread.sleep(t);
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			}
		}
		
		// This thingy makes that stupid window with cancel button, TODO: make it better
		private class StatusWindow extends Window {
	        public StatusWindow() {
	            super(Coord.z, "Carrot Farmer");
	            setLocal(true);
	            add(new Button(120, "Cancel") {
	                public void click() {
	                    window.destroy();
	                    if(t != null) {
	                    	gameui().msg("Carrot Farmer Cancelled", Color.WHITE);
	                    	t.stop();
	                    }
	                }
	            });
	            pack();
	        }
	        public void wdgmsg(Widget sender, String msg, Object... args) {
	            if (sender == this && msg.equals("close")) {
                	t.stop();
	            }
	            super.wdgmsg(sender, msg, args);
	        }
	        
		}
}

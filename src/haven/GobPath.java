package haven;

import java.awt.Color;

public class GobPath extends Sprite {
    private static final States.ColState clrst = new States.ColState(new Color(233, 185, 110));

    public GobPath(Gob gob) {
        super(gob, null);
    }

    public boolean setup(RenderList rl) {
        Location.goback(rl.state(), "gobx");
        rl.prepo(States.xray);
        rl.prepo(clrst);
        return true;
    }

    public void draw(GOut g) {
        if (MapView.pllastcc == null)
            return;

        Gob gob = (Gob) owner;
        Coord3f pc = gob.getc();
        double lcx = MapView.pllastcc.x;
        double lcy = MapView.pllastcc.y;
        double x = lcx - pc.x;
        double y = -lcy + pc.y;
        double z = Math.sqrt(x * x + y * y) >= 44 * 11 ? 0 : gob.glob.map.getcz(lcx, lcy) - pc.z;

        g.apply();
        BGL gl = g.gl;
        gl.glLineWidth(2.0F);
        gl.glEnable(GL2.GL_BLEND);
        gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
        gl.glEnable(GL2.GL_LINE_SMOOTH);
        gl.glHint(GL2.GL_LINE_SMOOTH_HINT, GL2.GL_NICEST);
        gl.glBegin(GL2.GL_LINES);
        gl.glVertex3f(0, 0, 0);
        gl.glVertex3f((float) x, (float) y, (float) z);
        gl.glEnd();
        gl.glDisable(GL2.GL_LINE_SMOOTH);
    }
}

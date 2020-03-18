package socotra.util;

import javafx.scene.paint.Color;

public enum Colors {
    BLUE(Color.BLUE), GREEN(Color.GREEN), PINK(Color.PINK), ORANGE(Color.ORANGE), YELLOW(Color.YELLOW),
    MAGENTA(Color.CRIMSON), LIGHT_BLUE(Color.LIGHTBLUE), LIME(Color.LIME), PURPLE(Color.PURPLE), PEACH(Color.PEACHPUFF),
    GRAY(Color.GRAY), LIGHT_GREEN(Color.LIGHTGREEN), CORAL(Color.CORAL), RED(Color.RED), TEAL(Color.TEAL),
    BROWN(Color.CHOCOLATE), VIOLET(Color.VIOLET);

    private javafx.scene.paint.Paint color;

    Colors(javafx.scene.paint.Paint color){
        this.color = color;
    }

    public javafx.scene.paint.Paint getColor() {
        return color;
    }
}

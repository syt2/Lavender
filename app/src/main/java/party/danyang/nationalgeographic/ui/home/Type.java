package party.danyang.nationalgeographic.ui.home;

/**
 * Created by dream on 16-10-11.
 */
public enum  Type {
    TW, US;

    public static Type valueOf(int ordinal) {
        if (ordinal < 0 || ordinal >= values().length) {
            throw new IndexOutOfBoundsException("Invalid ordinal");
        }
        return values()[ordinal];
    }
}

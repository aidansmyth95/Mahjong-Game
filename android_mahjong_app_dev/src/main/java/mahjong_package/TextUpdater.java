package mahjong_package;


// https://stackoverflow.com/questions/10996479/how-to-update-a-textview-of-an-activity-from-another-class/27939196#27939196

public interface TextUpdater {

    void updateTextView(String s);

    void updateTextView(String s, Object... args);

}

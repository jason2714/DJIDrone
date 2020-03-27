package ntou.project.djidrone.fragment;

public class Setting {
    private String name;
    private int imageSrc;

    public Setting(String name,int imageSrc){
        this.name=name;
        this.imageSrc=imageSrc;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setImageSrc(int imageSrc) {
        this.imageSrc = imageSrc;
    }

    public int getImageSrc() {
        return imageSrc;
    }

    public String getName() {
        return name;
    }
}

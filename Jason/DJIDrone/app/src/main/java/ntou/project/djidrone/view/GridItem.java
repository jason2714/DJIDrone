package ntou.project.djidrone.view;

public class GridItem {
    private String name;
    private int imageSrc;

    public GridItem(String name, int imageSrc){
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

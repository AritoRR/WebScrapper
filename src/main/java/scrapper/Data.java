package scrapper;

import java.util.List;

public class Data {
    private String apiName;
    private List<String> data;

    public Data() {}

    public Data(String apiName, List<String> data) {
        this.apiName = apiName;
        this.data = data;
    }

    public String getApiName() {
        return apiName;
    }
    public void setApiName(String apiName) {
        this.apiName = apiName;
    }
    public List<String> getData() {
        return data;
    }
    public void setData(List<String> data) {
        this.data = data;
    }
}

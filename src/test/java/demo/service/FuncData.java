package demo.service;

import lombok.Data;

/**
 * Created by boying592 on 2019/2/12.
 */
@Data
public class FuncData {
    private String strResult;
    private Long longResult;

    public FuncData() {
    }

    public FuncData(String strResult, Long longResult) {
        this.strResult = strResult;
        this.longResult = longResult;
    }
}

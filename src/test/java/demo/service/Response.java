package demo.service;

import lombok.Data;

/**
 * Created by boying592 on 2019/2/12.
 */
@Data
public class Response<T> {
    private String code = "000";
    private String msg = "success";
    private T data;
}
